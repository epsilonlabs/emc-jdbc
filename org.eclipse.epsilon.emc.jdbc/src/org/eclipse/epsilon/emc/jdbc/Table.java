package org.eclipse.epsilon.emc.jdbc;

import java.util.ArrayList;
import java.util.List;

public class Table {
	
	protected Database database;
	protected String name;
	protected List<ForeignKey> incoming = new ArrayList<ForeignKey>();
	protected List<ForeignKey> outgoing = new ArrayList<ForeignKey>();
	
	public Table(String name, Database database) {
		this.name = name;
		this.database = database;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<ForeignKey> getIncoming() {
		return incoming;
	}
	
	public List<ForeignKey> getOutgoing() {
		return outgoing;
	}
}
