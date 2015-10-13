package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.IAbstractOperationContributor;

public class ResultSetList extends ResultSetBackedList<Result> implements IAbstractOperationContributor {

	protected int size = -1;
	protected ResultSet resultSet = null;
	protected static ResultSetListSelectOperation resultSetListSelectOperation = new ResultSetListSelectOperation();
	protected static ResultSetListCollectOperation resultSetBackedListCollectOperation = new ResultSetListCollectOperation();
	
	public ResultSetList(JdbcModel model, Table table, String condition, List<Object> parameters, boolean streamed, boolean one) {
		super(model, table, condition, parameters, streamed, one);
	}
	
	@Override
	public String getSelection() {
		return "*";
	}
	
	@Override
	public boolean contains(Object o) {
		return (o instanceof Result && ((Result) o).getResultSet() == getResultSet());
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) return false;
		}
		return c.size() > 0;
	}

	@Override
	public int size() {
		
		if (size == -1) {
			if (streamed) {
				size = (int) (long) new StreamedPrimitiveValuesListSqlOperation<Long>("count", getSelection(), condition, parameters, model, table, one).getValue();
			}
			else {
				try {
					ResultSet rs = getResultSet();
					int row = rs.getRow();
					rs.last();
					size = rs.getRow();
					if (row != 0) rs.absolute(row);
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
			
		return size;
	}

	@Override
	public Result get(int index) {
		return new Result(getResultSet(), index+1, model, table, streamed);
	}

	@Override
	public int indexOf(Object o) {
		return ((Result) o).getRow();
	}

	@Override
	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	@Override
	public ListIterator<Result> listIterator() {
		if (streamed) {
			return new StreamedResultSetListIterator(getResultSet(), model, table);
		}
		else {
			return new ResultSetListIterator(getResultSet(), model, table);
		}
	}
	
	public ResultSetList fetch() {
		ResultSetList fetched = new ResultSetList(model, table, condition, parameters, false, one);
		return fetched;
	}
	
	public ResultSetList stream() {
		ResultSetList streamed = new ResultSetList(model, table, condition, parameters, true, one);
		return streamed;		
	}
	
	@Override
	public AbstractOperation getAbstractOperation(String name) {
		if ("select".equals(name)) {
			return resultSetListSelectOperation;
		}
		else if ("collect".equals(name)) {
			return resultSetBackedListCollectOperation;
		}
		else return null;
	}
	

}
