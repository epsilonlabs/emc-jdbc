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
