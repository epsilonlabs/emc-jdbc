package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class StreamedPrimitiveValuesListSqlOperation<T> {
	
	protected String selection;
	protected String condition;
	protected List<Object> parameters;
	protected JdbcModel model;
	protected Table table;
	protected String operation;
	protected boolean one = false;
	
	public StreamedPrimitiveValuesListSqlOperation(String operation, String selection, String condition, List<Object> parameters, JdbcModel model, Table table, boolean one) {
		super();
		this.operation = operation;
		this.selection = selection;
		this.condition = condition;
		this.parameters = parameters;
		this.model = model;
		this.table = table;
		this.one = one;
	}
	
	public String getSelection() {
		return operation + "(" + selection + ")";
	}
	
	public T getValue() {
		ResultSet resultSet = model.getResultSet(getSelection(), condition, parameters, table, false, one);
		try {
			if (resultSet.next()) {
				return (T) resultSet.getObject(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
}
