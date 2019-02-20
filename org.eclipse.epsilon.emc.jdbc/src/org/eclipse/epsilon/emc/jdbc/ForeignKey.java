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

/**
 * A representation of a ForeignKey in a DB
 * 
 * @author Dimitris Kolovos
 *
 */
public class ForeignKey {
	
	protected String name;
	protected String column;
	protected String foreignColumn;
	protected Table foreignTable;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getColumn() {
		return column;
	}
	
	public void setColumn(String column) {
		this.column = column;
	}
	
	public String getForeignColumn() {
		return foreignColumn;
	}
	
	public void setForeignColumn(String foreignColumn) {
		this.foreignColumn = foreignColumn;
	}
	
	public Table getForeignTable() {
		return foreignTable;
	}
	
	public void setForeignTable(Table foreignTable) {
		this.foreignTable = foreignTable;
	}
	
}
