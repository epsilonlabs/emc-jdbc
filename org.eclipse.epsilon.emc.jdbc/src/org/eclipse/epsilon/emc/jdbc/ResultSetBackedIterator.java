package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResultSetBackedIterator<T> extends ImmutableListIterator<T> {
	
	protected ResultSet rs = null;
	protected JdbcModel model = null;
	protected Table table = null;
	
	public ResultSetBackedIterator(ResultSet rs, JdbcModel model, Table table) {
		this.rs = rs;
		this.model = model;
		this.table = table;
	}
	
	@Override
	public boolean hasNext() {
		try {
			return !rs.isLast();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T next() {
		try {
			rs.next();
			return getValueAtCurrentIndex();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected abstract T getValueAtCurrentIndex();
	
	@Override
	public boolean hasPrevious() {
		try {
			return !rs.isFirst();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int nextIndex() {
		try {
			return rs.getRow() + 1;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public T previous() {
		try {
			rs.previous();
			return getValueAtCurrentIndex();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int previousIndex() {
		try {
			return rs.getRow() - 1;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
