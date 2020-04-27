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

import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;

public class StreamedPrimitiveValuesListOperationContributor extends OperationContributor {

	@Override
	public boolean contributesTo(Object target) {
		return target instanceof PrimitiveValuesList && 
			((PrimitiveValuesList) target).isStreamed();
	}
	
	public Object min() {
		PrimitiveValuesList l = (PrimitiveValuesList) getTarget();
		System.err.println("Min...");
		return new StreamedPrimitiveValuesListSqlOperation<>("min", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
	public Object max() {
		PrimitiveValuesList l = (PrimitiveValuesList) getTarget();
		return new StreamedPrimitiveValuesListSqlOperation<>("max", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
	public Object sum() {
		PrimitiveValuesList l = (PrimitiveValuesList) getTarget();
		return new StreamedPrimitiveValuesListSqlOperation<>("sum", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
	public Object avg() {
		PrimitiveValuesList l = (PrimitiveValuesList) getTarget();
		return new StreamedPrimitiveValuesListSqlOperation<>("avg", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
}
