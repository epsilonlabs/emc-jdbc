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
		return "`"+name+"`";
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
