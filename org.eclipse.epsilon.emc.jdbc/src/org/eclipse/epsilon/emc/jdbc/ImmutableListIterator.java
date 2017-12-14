package org.eclipse.epsilon.emc.jdbc;

import java.util.ListIterator;

/**
 * A base ListIterator<T> implementation that is immutable.
 * 
 * @author Dimitris Kolovos
 *
 * @param <T>
 */
public abstract class ImmutableListIterator<T> implements ListIterator<T>{

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
