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

import java.util.ListIterator;

/**
 * A base ListIterator<T> implementation that is immutable.
 * 
 * @author Dimitris Kolovos
 *
 * @param <T>
 */
public abstract class ImmutableListIterator<T> implements ListIterator<T> {

	@Override
	public void add(T arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(T arg0) {
		throw new UnsupportedOperationException();
	}

}
