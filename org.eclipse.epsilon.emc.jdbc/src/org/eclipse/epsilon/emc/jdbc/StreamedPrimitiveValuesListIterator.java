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
import java.sql.SQLException;

public class StreamedPrimitiveValuesListIterator extends StreamedResultSetBackedIterator<Object>{

	public StreamedPrimitiveValuesListIterator(ResultSet rs, JdbcModel model, Table table) {
		super(rs, model, table);
	}

	@Override
	protected Object getValueAtCurrentIndex() {
		try {
			return rs.getObject(1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
