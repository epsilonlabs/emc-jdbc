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
				// When we've consumed all the items, make sure the result set is closed
				try {
					rs.close();
				} catch (SQLException e) {
					System.err.println("Error closing used up result set");
					e.printStackTrace();
				}
				model.getConnectionPool().finishedStreaming(rs);
			}
		}
		return hasNext;
	}
	
	@Override
	public T next() {
		nextConsumed = true;
		return next;
	}

}
