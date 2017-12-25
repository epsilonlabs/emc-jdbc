/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.emc.jdbc;

import java.sql.ResultSet;
import java.util.List;

public abstract class ResultSetBackedList<T> extends ImmutableList<T> {
	
	protected JdbcModel model = null;
	protected Table table = null;
	protected String condition;
	protected List<Object> parameters;
	protected ResultSet resultSet = null;
	protected boolean streamed = false;
	protected boolean one = false;
	
	public ResultSetBackedList(JdbcModel model, Table table, String condition, List<Object> parameters, boolean streamed, boolean one) {
		super();
		this.model = model;
		this.table = table;
		this.condition = condition;
		this.parameters = parameters;
		this.streamed = streamed;
		this.one = one;
	}
	
	public abstract String getSelection();
	
	protected ResultSet getResultSet() {
		if (resultSet == null) {
			resultSet = model.getResultSet(getSelection(), condition, parameters, table, streamed, isOne());
		}
		return resultSet;
	}
	
	public JdbcModel getModel() {
		return model;
	}
	
	public Table getTable() {
		return table;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public List<Object> getParameters() {
		return parameters;
	}
	
	public boolean isStreamed() {
		return streamed;
	}
	
	public void setStreamed(boolean streamed) {
		this.streamed = streamed;
	}
	
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean isOne() {
		return one;
	}
	
	public void setOne(boolean one) {
		this.one = one;
	}
}
