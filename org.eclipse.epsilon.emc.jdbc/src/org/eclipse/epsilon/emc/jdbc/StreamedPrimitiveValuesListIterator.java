package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StreamedPrimitiveValuesListIterator extends StreamedResultSetBackedIterator<Object>{

	public StreamedPrimitiveValuesListIterator(ResultSet rs, JdbcModel model, Table table) {
		super(rs, model, table);
	}

	@Override
	protected Object getValueAtCurrentIndex() {
		try {
			return rs.getObject(1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
