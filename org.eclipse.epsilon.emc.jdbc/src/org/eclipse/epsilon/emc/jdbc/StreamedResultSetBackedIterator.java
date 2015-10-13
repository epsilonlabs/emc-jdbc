package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class StreamedResultSetBackedIterator<T> extends ResultSetBackedIterator<T> {

	protected T next = null;
	protected boolean nextConsumed = true;
	protected boolean hasNext;
	
	public StreamedResultSetBackedIterator(ResultSet rs, JdbcModel model, Table table) {
		super(rs, model, table);
	}

	@Override
	public boolean hasNext() {
		// When in streaming mode rs.hasNext() doesn't work
		// so we need to peek ahead
		if (nextConsumed) {
			try {
				hasNext = rs.next();
			} catch (SQLException e) {
				return false;
			}

			if (hasNext) { 
				next = getValueAtCurrentIndex();
				nextConsumed = false;
			}
			else {
				model.getConnectionPool().finishedStreaming(rs);
			}
		}
		return hasNext;
	}
	
	@Override
	public T next() {
		nextConsumed = true;
		return (T) next;
	}

}
