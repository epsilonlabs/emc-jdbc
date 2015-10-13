package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;

public class StreamedResultSetListIterator extends StreamedResultSetBackedIterator<Result> {
	
	public StreamedResultSetListIterator(ResultSet rs, JdbcModel model, Table table) {
		super(rs, model, table);
	}
	
	@Override
	protected Result getValueAtCurrentIndex() {
		return new Result(rs, model, table, true);
	}
	
}
