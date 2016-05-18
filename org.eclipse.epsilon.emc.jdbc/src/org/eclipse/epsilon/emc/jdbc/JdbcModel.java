package org.eclipse.epsilon.emc.jdbc;

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
import org.eclipse.epsilon.eol.parse.EolParser;
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
	
	public void print(ResultSet rs) throws Exception {
		System.err.println("---");
		while (rs.next()) {
			for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
				System.err.print( rs.getMetaData().getColumnName(i) + "=" + rs.getString(i) + " - ");
			}
			System.err.println();
		}
		System.err.println("---");
	}
	
	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver)
			throws EolModelLoadingException {
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
	public ResultSetList query(String sql) throws SQLException {
		return new ResultSetList(
				connection.createStatement(),
				this, null, null, null);
	}*/
	
	@Override
	public Object createInstance(String type)
			throws EolModelElementTypeNotFoundException,
			EolNotInstantiableModelElementTypeException {
		return createInstance(type, Collections.emptyList());
	}
	
	protected int getResultSetType() {
		if (!isReadOnly()) {
			return ResultSet.CONCUR_UPDATABLE;
		}
		else {
			return ResultSet.CONCUR_READ_ONLY;
		}
	}
	
	// Create separate connections for each streamed list
	protected HashMap<String, PreparedStatement> preparedStatementCache = new HashMap<String, PreparedStatement>();
	protected PreparedStatement prepareStatement(String sql, int options, int resultSetType, boolean streamed) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		if (!streamed) {
			preparedStatement = preparedStatementCache.get(sql + options + "" + resultSetType);
			if (preparedStatement == null) {
				preparedStatement = connectionPool.getConnection(streamed).prepareStatement(sql, options, resultSetType);
				preparedStatementCache.put(sql + options + "" + resultSetType, preparedStatement);
			}
		}
		else {
			preparedStatement = connectionPool.getConnection(streamed).prepareStatement(sql, options, resultSetType);
		}
		
		return preparedStatement;
	}
	
	public ResultSet getResultSet(String selection, String condition, List<Object> parameters, Table table, boolean streamed, boolean one) {
			try {
				String sql = "select " + selection + " from " + table.getName();
				if (condition != null && condition.trim().length() > 0) {
					sql += " where " + condition;
				}
				if (one) { sql += " limit 1"; }
				
				// System.err.println(sql);
				
				int options = ResultSet.TYPE_SCROLL_INSENSITIVE;
				int resultSetType = this.getResultSetType();
				
				if (streamed) {
					options = ResultSet.TYPE_FORWARD_ONLY;
					resultSetType = ResultSet.CONCUR_READ_ONLY;
				}
				
				PreparedStatement preparedStatement = this.prepareStatement(sql, options, resultSetType, streamed);
				
				if (streamed) {
					preparedStatement.setFetchSize(Integer.MIN_VALUE);
				}
				else {
					preparedStatement.setFetchSize(Integer.MAX_VALUE);
				}
				
				if (parameters != null) {
					this.setParameters(preparedStatement, parameters);
				}
				
				ResultSet resultSet = preparedStatement.executeQuery();
				connectionPool.register(resultSet, preparedStatement.getConnection());
				return resultSet;
			}
			catch (Exception ex) { throw new RuntimeException(ex); } 
	}
	
	@Override
	public Object createInstance(String type, Collection<Object> parameters)
			throws EolModelElementTypeNotFoundException,
			EolNotInstantiableModelElementTypeException {

		try {
			
			// Create a Statement for scrollable ResultSet
			PreparedStatement sta = prepareStatement("SELECT * FROM " + type
					+ " WHERE 1=2 limit 1",
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
			
	        // Cache table names
	        ResultSet rs = connectionPool.getSharedConnection().getMetaData().getTables(null, null, null, new String[]{});
			while (rs.next()) {
				Table table = new Table(rs.getString(3), database);
				database.getTables().add(table);
			}
			
			/*
			for (Table table : database.getTables()) {
				ResultSet foreignKeysRs = connection.getMetaData().getImportedKeys(null, null, table.getName());
				while (foreignKeysRs.next()) {
					ForeignKey foreignKey = new ForeignKey();
					foreignKey.setColumn(foreignKeysRs.getString("FKCOLUMN_NAME"));
					Table foreignTable = database.getTable(foreignKeysRs.getString("PKTABLE_NAME"));
					foreignKey.setForeignTable(foreignTable);
					foreignKey.setForeignColumn("PKCOLUMN_NAME");
					foreignKey.setName(foreignKeysRs.getString("FK_NAME"));
					table.getOutgoing().add(foreignKey);
					foreignTable.getIncoming().add(foreignKey);
				}
			}*/
			
		}
		catch (Exception ex) {
			throw new EolModelLoadingException(ex, this);
		}
	}
	
	@Override
	public boolean hasType(String type) {
		return database.getTable(type) != null;
	}
	
	@Override
	public Collection<?> getAllOfType(String type)
			throws EolModelElementTypeNotFoundException {
		
		return new ResultSetList(this, database.getTable(type), "", null, streamResults, false);
	}
	
	@Override
	public Object getEnumerationValue(String enumeration, String label)
			throws EolEnumerationValueNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<?> allContents() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOfType(Object instance, String metaClass)
			throws EolModelElementTypeNotFoundException {
		return metaClass.equals(getTypeNameOf(instance));
	}
	
	@Override
	public boolean isOfKind(Object instance, String metaClass)
			throws EolModelElementTypeNotFoundException {
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
	
	public String ast2sql(Variable iterator, AST ast, IEolContext context, ArrayList<Object> variables) throws EolRuntimeException {
		if (ast.getType() == EolParser.OPERATOR && ast.getText().equals("not")) {
			return "not (" + ast2sql(iterator, ast.getFirstChild(), context, variables) + ")";
		} else if (ast.getType() == EolParser.OPERATOR && ast.getChildren().size() == 2) {
			return "(" + ast2sql(iterator, ast.getFirstChild(), context, variables)
					+ ast.getText() + 
					ast2sql(iterator, ast.getFirstChild().getNextSibling(), context, variables) + ")";
		}
		else if (ast.getType() == EolParser.POINT && ast.getFirstChild().getText().equals(iterator.getName())) {
			return ast.getFirstChild().getNextSibling().getText();
		}
		else {
			Object result = context.getExecutorFactory().executeAST(ast, context);
			variables.add(result);
			return "?";
		}
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
	public Collection<?> getAllOfKind(String type)
			throws EolModelElementTypeNotFoundException {
		return getAllOfType(type);
	}
	
	@Override
	public boolean owns(Object instance) {
		return (instance instanceof Result && 
			((Result) instance).getOwningModel() == this)/*
			|| ((instance instanceof ResultSetList) && 
			((ResultSetList) instance).getModel() == this)*/;
	}
	
	/*
	@Override
	public IModelTransactionSupport getTransactionSupport() {
		return new IModelTransactionSupport() {
			
			@Override
			public void startTransaction() {
				try {
					connection.setAutoCommit(false);
				} catch (SQLException ex) {
					throw new RuntimeException(ex);
				}
			}
			
			@Override
			public void commitTransaction() {
				try {
					connection.commit();
					connection.setAutoCommit(true);
				}
				catch (SQLException ex) {
					throw new RuntimeException(ex);
				}
			}
			
			@Override
			public void rollbackTransaction() {
				try {
					connection.rollback();
					connection.setAutoCommit(true);
				}
				catch (SQLException ex) {
					throw new RuntimeException(ex);
				}
			}
			
		};
	}*/
	
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
