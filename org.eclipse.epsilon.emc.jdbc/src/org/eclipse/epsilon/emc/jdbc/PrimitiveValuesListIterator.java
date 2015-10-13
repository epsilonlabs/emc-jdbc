package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PrimitiveValuesListIterator extends ResultSetBackedIterator<Object> {

	public PrimitiveValuesListIterator(ResultSet rs, JdbcModel model, Table table) {
		super(rs, model, table);
	}

	@Override
	protected Object getValueAtCurrentIndex() {
		try {
			return rs.getObject(1);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

}
