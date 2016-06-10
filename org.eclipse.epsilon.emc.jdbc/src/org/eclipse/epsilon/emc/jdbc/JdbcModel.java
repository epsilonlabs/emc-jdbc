package org.eclipse.epsilon.emc.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.epsilon.common.parse.AST;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.eol.dom.CollectionLiteralExpression;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FeatureCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.NotOperatorExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolEnumerationValueNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.exceptions.models.EolNotInstantiableModelElementTypeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;
import org.eclipse.epsilon.eol.execute.operations.contributors.IOperationContributorProvider;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.models.Model;
import org.eclipse.epsilon.eol.types.EolMap;

public abstract class JdbcModel extends Model implements IOperationContributorProvider {

	public static final String PROPERTY_SERVER = "server";
	public static final String PROPERTY_PORT = "port";
	public static final String PROPERTY_DATABASE = "database";
	public static final String PROPERTY_USERNAME = "username";
	public static final String PROPERTY_PASSWORD = "password";
	public static final String PROPERTY_READONLY = "readonly";
	public static final String PROPERTY_STREAMRESULTS = "streamresults";

	protected String server;
	protected int port;
	protected String databaseName;
	protected String username;
	protected String password;
	protected Database database;
	protected ResultPropertyGetter propertyGetter = new ResultPropertyGetter(this);
	protected ResultPropertySetter propertySetter = new ResultPropertySetter(this);
	protected boolean readOnly = true;
	protected boolean streamResults = true;
	protected ConnectionPool connectionPool = null;
	protected StreamedPrimitiveValuesListOperationContributor operationContributor = new StreamedPrimitiveValuesListOperationContributor();

	protected abstract Driver createDriver() throws SQLException;

	protected abstract String getJdbcUrl();

	protected String identifierQuoteString = "unknown";

	public void print(ResultSet rs) throws Exception {
		System.err.println("---");
		while (rs.next()) {
			for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
				System.err.print(rs.getMetaData().getColumnName(i) + "=" + rs.getString(i) + " - ");
			}
			System.err.println();
		}
		System.err.println("---");
	}

	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver) throws EolModelLoadingException {
		super.load(properties, resolver);
		this.databaseName = properties.getProperty(PROPERTY_DATABASE);
		this.server = properties.getProperty(PROPERTY_SERVER, this.server);
		this.port = properties.getIntegerProperty(PROPERTY_PORT, this.port);
		this.username = properties.getProperty(PROPERTY_USERNAME);
		this.password = properties.getProperty(PROPERTY_PASSWORD);
		this.readOnly = properties.getBooleanProperty(PROPERTY_READONLY, this.readOnly);
		this.streamResults = properties.getBooleanProperty(PROPERTY_STREAMRESULTS, this.streamResults);
		load();
	}

	/*
	 * public ResultSetList query(String sql) throws SQLException { return new
	 * ResultSetList( connection.createStatement(), this, null, null, null); }
	 */

	@Override
	public Object createInstance(String type)
			throws EolModelElementTypeNotFoundException, EolNotInstantiableModelElementTypeException {
		return createInstance(type, Collections.emptyList());
	}

	protected int getResultSetType() {
		if (!isReadOnly()) {
			return ResultSet.CONCUR_UPDATABLE;
		} else {
			return ResultSet.CONCUR_READ_ONLY;
		}
	}

	// Create separate connections for each streamed list
	protected HashMap<String, PreparedStatement> preparedStatementCache = new HashMap<String, PreparedStatement>();

	protected PreparedStatement prepareStatement(String sql, int options, int resultSetType, boolean streamed)
			throws SQLException {

		PreparedStatement preparedStatement = null;

		if (!streamed) {
			preparedStatement = preparedStatementCache.get(sql + options + "" + resultSetType);
			if (preparedStatement == null) {
				preparedStatement = connectionPool.getConnection(streamed).prepareStatement(sql, options,
						resultSetType);
				preparedStatementCache.put(sql + options + "" + resultSetType, preparedStatement);
			}
		} else {
			preparedStatement = connectionPool.getConnection(streamed).prepareStatement(sql, options, resultSetType);
		}

		return preparedStatement;
	}

	public ResultSet getResultSet(String selection, String condition, List<Object> parameters, Table table,
			boolean streamed, boolean one) {
		try {
			String sql = "select " + selection + " from " + table.getName();
			if (condition != null && condition.trim().length() > 0) {
				sql += " where " + condition;
			}
			if (one) {
				sql += " limit 1";
			}

			// System.err.println(sql);

			int options = ResultSet.TYPE_SCROLL_INSENSITIVE;
			int resultSetType = this.getResultSetType();

			if (streamed) {
				options = ResultSet.TYPE_FORWARD_ONLY;
				resultSetType = ResultSet.CONCUR_READ_ONLY;
			}

			System.out.println(sql);
			System.out.println(parameters);
			PreparedStatement preparedStatement = this.prepareStatement(sql, options, resultSetType, streamed);

			if (streamed) {
				preparedStatement.setFetchSize(Integer.MIN_VALUE);
			} else {
				preparedStatement.setFetchSize(Integer.MAX_VALUE);
			}

			if (parameters != null) {
				this.setParameters(preparedStatement, parameters);
			}

			ResultSet resultSet = preparedStatement.executeQuery();
			connectionPool.register(resultSet, preparedStatement.getConnection());
			return resultSet;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Object createInstance(String type, Collection<Object> parameters)
			throws EolModelElementTypeNotFoundException, EolNotInstantiableModelElementTypeException {

		try {

			// Create a Statement for scrollable ResultSet
			PreparedStatement sta = prepareStatement("SELECT * FROM " + type + " WHERE 1=2 limit 1",
					ResultSet.TYPE_SCROLL_INSENSITIVE, getResultSetType(), false);

			// Catch the ResultSet object
			ResultSet res = sta.executeQuery();

			// Move the cursor to the insert row
			res.moveToInsertRow();

			if (parameters.iterator().hasNext()) {
				EolMap values = (EolMap) parameters.iterator().next();
				for (Object key : values.keySet()) {
					res.updateObject(key + "", values.get(key));
				}
			}

			// Store the insert into database
			res.insertRow();
			res.next();
			return new Result(res, res.getRow(), this, database.getTable(type), false);

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	@Override
	public void load() throws EolModelLoadingException {
		try {
			connectionPool = new ConnectionPool(createDriver(), getJdbcUrl(), username, password);
			database = new Database();

			DatabaseMetaData md = connectionPool.getSharedConnection().getMetaData();

			identifierQuoteString = md.getIdentifierQuoteString();

			// Cache table names
			ResultSet rs = md.getTables(null, null, null, new String[] {});
			while (rs.next()) {
				Table table = new Table(rs.getString(3), database);
				database.getTables().add(table);
			}

			/*
			 * for (Table table : database.getTables()) { ResultSet
			 * foreignKeysRs = connection.getMetaData().getImportedKeys(null,
			 * null, table.getName()); while (foreignKeysRs.next()) { ForeignKey
			 * foreignKey = new ForeignKey();
			 * foreignKey.setColumn(foreignKeysRs.getString("FKCOLUMN_NAME"));
			 * Table foreignTable =
			 * database.getTable(foreignKeysRs.getString("PKTABLE_NAME"));
			 * foreignKey.setForeignTable(foreignTable);
			 * foreignKey.setForeignColumn("PKCOLUMN_NAME");
			 * foreignKey.setName(foreignKeysRs.getString("FK_NAME"));
			 * table.getOutgoing().add(foreignKey);
			 * foreignTable.getIncoming().add(foreignKey); } }
			 */

		} catch (Exception ex) {
			throw new EolModelLoadingException(ex, this);
		}
	}

	@Override
	public boolean hasType(String type) {
		return database.getTable(type) != null
				|| database.getTable(identifierQuoteString + type + identifierQuoteString) != null;
	}

	@Override
	public Collection<?> getAllOfType(String type) throws EolModelElementTypeNotFoundException {
		Collection<?> ret = null;
		try {
			ret = new ResultSetList(this, database.getTable(type), "", null, streamResults, false);
		} catch (Exception e) {
			// suppress non-wrapped table names exception
		}
		type = type.replace("'", identifierQuoteString);
		ret = new ResultSetList(this, database.getTable(identifierQuoteString + type + identifierQuoteString), "", null,
				streamResults, false);
		return ret;
	}

	@Override
	public Object getEnumerationValue(String enumeration, String label) throws EolEnumerationValueNotFoundException {
		// FIXME handle enums
		return enumeration + "#" + label;
		// throw new UnsupportedOperationException();
	}

	@Override
	public Collection<?> allContents() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOfType(Object instance, String metaClass) throws EolModelElementTypeNotFoundException {
		return metaClass.equals(getTypeNameOf(instance));
	}

	@Override
	public boolean isOfKind(Object instance, String metaClass) throws EolModelElementTypeNotFoundException {
		return isOfType(instance, metaClass);
	}

	@Override
	public String getTypeNameOf(Object instance) {
		return ((Result) instance).getTable().getName();
	}

	protected void setParameters(PreparedStatement preparedStatement, List<Object> parameters) throws SQLException {
		preparedStatement.clearParameters();
		int i = 1;
		for (Object parameter : parameters) {
			preparedStatement.setObject(i, parameter);
			i++;
		}
	}

	public String ast2sql(Table t, Variable iterator, AST ast, IEolContext context, ArrayList<Object> variables)
			throws EolRuntimeException {

		// not operations
		if (ast instanceof NotOperatorExpression) {
			final NotOperatorExpression nexp = (NotOperatorExpression) ast;
			return "not (" + ast2sql(t, iterator, nexp.getFirstOperand(), context, variables) + ")";
		}
		// binary operations
		else if (ast instanceof OperatorExpression && ((OperatorExpression) ast).getFirstOperand() != null
				&& ((OperatorExpression) ast).getSecondChild() != null) {

			String originalOperation = ast.getText();
			String operation = originalOperation;

			final OperatorExpression oexp = (OperatorExpression) ast;

			// special cases of eol2sql incompatible terminologies or
			// CNL-defined functions
			if (originalOperation.equals("=="))
				// == needs to be =
				operation = "=";
			if (originalOperation.equals("implies")) {
				// implies needs to be normalised (not a or b)
				return "((not " + ast2sql(t, iterator, oexp.getFirstOperand(), context, variables) + ") or "
						+ ast2sql(t, iterator, oexp.getSecondOperand(), context, variables) + ")";

				//
			} else
				return "(" + ast2sql(t, iterator, oexp.getFirstOperand(), context, variables) + operation
						+ ast2sql(t, iterator, oexp.getSecondOperand(), context, variables) + ")";

		}
		// property/operation calls of current iterator
		else if (ast instanceof PropertyCallExpression
				&& ((NameExpression) ((PropertyCallExpression) ast).getTargetExpression()).getName()
						.equals(iterator.getName())) {
			PropertyCallExpression pexp = (PropertyCallExpression) ast;
			return Utils.wrap(pexp.getPropertyNameExpression().getText(), identifierQuoteString);
		} else if (ast instanceof OperationCallExpression
				// operation
				&& ((OperationCallExpression) ast).getTargetExpression() instanceof FeatureCallExpression
				// to a feature
				&& ((FeatureCallExpression) ((OperationCallExpression) ast).getTargetExpression())
						.getTargetExpression() instanceof NameExpression
						// with name
				&& ((NameExpression) ((FeatureCallExpression) ((OperationCallExpression) ast).getTargetExpression())
						.getTargetExpression()).getName().equals(iterator.getName())
		// equal to iterator
		) {
			OperationCallExpression ocexp = (OperationCallExpression) ast;
			String operationname = Utils.wrap(ocexp.getOperationName(), identifierQuoteString);
			// currently the hasType function supported fully
			if (operationname.equals(identifierQuoteString + "hasType" + identifierQuoteString))
				try {
					String datatype = getTypeMetaData(t, ((PropertyCallExpression) ocexp.getTargetExpression())
							.getPropertyNameExpression().getName());
					// System.err.println(">"+datatype);
					String requiredtype = ((StringLiteral) ocexp.getParameterExpressions().get(0)).getValue();
					// System.err.println(">>"+requiredtype);
					boolean match = datatype.equals(requiredtype)
							|| datatype.equals("String") && stringToEnum(datatype);
					String ret = "" + match;
					// System.err.println(ret);
					return ret;
				} catch (SQLException e) {
					throw new EolRuntimeException("SQLException in ast2sql(...): " + e.getLocalizedMessage());
				}
			else {
				System.err.println("warning unsupported function found: " + operationname);
				return operationname;
			}
		} else if (isOperationIsPropertySet(ast, iterator)) {
			// operation call of current iterator, using it as a parameter --
			// currently supporting isPropertySet
			OperationCallExpression currentoperation = (OperationCallExpression) ast;

			List<Expression> parameters = currentoperation.getParameterExpressions();
			if (parameters.size() == 2 && parameters.get(0).getText().equals(iterator.getName()))
				return "(" + Utils.wrap(((StringLiteral) parameters.get(1)).getText(), identifierQuoteString)
						+ " is not null)";
			else
				throw new UnsupportedOperationException("cannot translate calls to operation: "
						+ currentoperation.getOperationName() + " with parameters: " + parameters);
		} else if (isOperationIncludes(ast, iterator)) {
			// currently supporting includes with a feature of iterator as
			// parameter
			OperationCallExpression currentoperation = (OperationCallExpression) ast;
			List<Expression> parameters = currentoperation.getParameterExpressions();
			System.err.println(parameters);
			String ret = "(";
			for (String s : ((Collection<String>) currentoperation.getTargetExpression().execute(context))) {
				ret = ret + Utils.wrap(
						((NameExpression) ((PropertyCallExpression) parameters.get(0)).getPropertyNameExpression())
								.getName(),
						identifierQuoteString) + "=? or ";
				variables.add(s);
			}
			ret = ret.substring(0, ret.length() - 4);
			ret += ")";
			return ret;
		} else
		// other
		{
			Object result = context.getExecutorFactory().executeAST(ast, context);
			variables.add(result);
			return "?";
		}

		// TODO add other SQL operations useful to CNL
	}

	// private String flatten(AST ast, String ret) {
	// if (ast != null) {
	// ret += ast + " [" + ast.getClass().getName() + "]";
	// if (ast.hasChildren()) {
	// ret += "\n";
	// AST c1 = ast.getFirstChild();
	// AST c2 = ast.getSecondChild();
	// ret += flatten(c1, ret);
	// ret += " ::: ";
	// ret += flatten(c2, ret);
	// ret += "\n";
	// }
	// } else
	// ret += "{empty}";
	// return ret;
	// }

	private boolean isOperationIncludes(AST ast, Variable iterator) {
		boolean ret = ast instanceof OperationCallExpression;
		ret = ret && ((OperationCallExpression) ast).getTargetExpression() instanceof CollectionLiteralExpression;
		ret = ret && ((OperationCallExpression) ast).getOperationName().equals("includes");
		// the operation is specifically 'isPropertySet'
		ret = ret && ((NameExpression) ((PropertyCallExpression) ((OperationCallExpression) ast)
				.getParameterExpressions().get(0)).getTargetExpression()).getName().equals(iterator.getName());
		return ret;
	}

	private boolean stringToEnum(String datatype) {
		// TODO FIXME enums
		return true;
	}

	private String getTypeMetaData(Table table, String column) throws SQLException {

		DatabaseMetaData dmd = connectionPool.getSharedConnection().getMetaData();

		ResultSet rs = dmd.getColumns(null, null, table.getName(), column);

		String type = "unknown";

		if (rs.next())
			type = rs.getString(6);

		return asPrimitive(type);
	}

	// returns the name of the Java primitive 'self' represents, if any --
	// similar to operation Any asPrimitive() in utils.eol
	private String asPrimitive(String self) {
		String id = "unknown";
		if (self != null)
			id = self;
		if (id.contains("CHAR"))
			return "String";
		if (id.equals("BOOLEAN"))
			return "Boolean";
		if (id.equals("INTEGER"))
			return "Integer";
		if (id.equals("DECIMAL") || id.equals("DOUBLE") || id.equals("FLOAT"))
			return "Decimal";
		if (id.equals("DATE"))
			return "Date";
		if (id.equals("org.eclipse.emf.ecore.impl.EEnumLiteralImpl"))
			// FIXME enums?
			return self;
		// FIXME non-emf enums
		if (id.equals("java.util.Enumeration"))
			return self;
		return "unknown";
	}

	private boolean isOperationAvg(AST ast) {
		boolean ret = ast instanceof OperationCallExpression;
		ret = ret && ((OperationCallExpression) ast).getTargetExpression() instanceof NameExpression;
		ret = ret
				&& ((NameExpression) ((OperationCallExpression) ast).getTargetExpression()).getName().equals(getName());
		// the current sub-expression is made up of: `Model`.x
		ret = ret && ((OperationCallExpression) ast).getOperationName().equals("avg");
		return ret;
	}

	private boolean isOperationIsPropertySet(AST ast, Variable iterator) {
		boolean ret = ast instanceof OperationCallExpression;
		ret = ret && ((OperationCallExpression) ast).getTargetExpression() instanceof NameExpression;
		ret = ret
				&& ((NameExpression) ((OperationCallExpression) ast).getTargetExpression()).getName().equals(getName());
		// the current sub-expression is made up of: `Model`.x
		ret = ret && ((OperationCallExpression) ast).getOperationName().equals("isPropertySet");
		// the operation is specifically 'isPropertySet'
		ret = ret && ((NameExpression) ((OperationCallExpression) ast).getParameterExpressions().get(0)).getName()
				.equals(iterator.getName());
		return ret;
	}

	@Override
	public Object getElementById(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getElementId(Object instance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPropertyGetter getPropertyGetter() {
		return propertyGetter;
	}

	@Override
	public IPropertySetter getPropertySetter() {
		return propertySetter;
	}

	@Override
	public Collection<?> getAllOfKind(String type) throws EolModelElementTypeNotFoundException {
		return getAllOfType(type);
	}

	@Override
	public boolean owns(Object instance) {
		return (instance instanceof Result && ((Result) instance)
				.getOwningModel() == this)/*
											 * || ((instance instanceof
											 * ResultSetList) &&
											 * ((ResultSetList)
											 * instance).getModel() == this)
											 */;
	}

	/*
	 * @Override public IModelTransactionSupport getTransactionSupport() {
	 * return new IModelTransactionSupport() {
	 * 
	 * @Override public void startTransaction() { try {
	 * connection.setAutoCommit(false); } catch (SQLException ex) { throw new
	 * RuntimeException(ex); } }
	 * 
	 * @Override public void commitTransaction() { try { connection.commit();
	 * connection.setAutoCommit(true); } catch (SQLException ex) { throw new
	 * RuntimeException(ex); } }
	 * 
	 * @Override public void rollbackTransaction() { try {
	 * connection.rollback(); connection.setAutoCommit(true); } catch
	 * (SQLException ex) { throw new RuntimeException(ex); } }
	 * 
	 * }; }
	 */

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDatabase() {
		return databaseName;
	}

	public void setDatabase(String database) {
		this.databaseName = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void dispose() {
		super.dispose();
		try {
			connectionPool.dispose();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setElementId(Object instance, String newId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteElement(Object instance) throws EolRuntimeException {

	}

	@Override
	public boolean isInstantiable(String type) {
		return !readOnly;
	}

	@Override
	public boolean store(String location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean store() {
		throw new UnsupportedOperationException();
	}

	public boolean isStreamResults() {
		return streamResults;
	}

	public void setStreamResults(boolean streamResults) {
		this.streamResults = streamResults;
	}

	@Override
	public OperationContributor getOperationContributor() {
		return operationContributor;
	}

	public ConnectionPool getConnectionPool() {
		return connectionPool;
	}

}
