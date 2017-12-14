package org.eclipse.epsilon.emc.jdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A place holder for the table information in the DB.
 * 
 * @author Dimitris Kolovos
 *
 */
public class Database {
	
	protected Map<String, Table> tables = new HashMap<String, Table>();
	
	public Collection<Table> getTables() {
		return Collections.unmodifiableCollection(tables.values());
	}
	
	public void addTable(Table table) {
		tables.put(table.getName(), table);
	}
	
	/**
	 * Get a table.
	 * @param name
	 * @return The table, or null if a table with the given name is not found.
	 */
	public Table getTable(String name) {
		return tables.get(name);
	}
	
}
