package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetListIterator extends ResultSetBackedIterator<Result> {

	public ResultSetListIterator(ResultSet rs, JdbcModel model, Table table) {
		super(rs, model, table);
	}

	@Override
	protected Result getValueAtCurrentIndex() {
		try {
			return new Result(rs, rs.getRow(), model, table, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
