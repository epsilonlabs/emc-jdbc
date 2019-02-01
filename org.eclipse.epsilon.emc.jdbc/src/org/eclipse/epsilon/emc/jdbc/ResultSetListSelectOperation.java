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
import java.util.Collection;
import java.util.List;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.operations.declarative.SelectOperation;

public class ResultSetListSelectOperation extends SelectOperation {
	
	@Override
	public Collection<?> execute(boolean returnOnMatch, Object target, NameExpression operationNameExpression,
			List<Parameter> iterators, Expression expression, IEolContext context) throws EolRuntimeException {

		ResultSetList list = (ResultSetList) target;
		
		ArrayList<Object> selectParameters = new ArrayList<>();
		String selectCondition = list.getModel().ast2sql(
			list.getTable(),
			createIteratorVariable(null, iterators.get(0), context),
			expression, context, selectParameters
		);
		
		if (list.getCondition() != null && list.getCondition().trim().length() > 0) {
			selectParameters.addAll(list.getParameters());
			selectCondition = "(" + selectCondition + ") and (" + list.getCondition() + ")";
		}
		
		return new ResultSetList(list.getModel(), list.getTable(), selectCondition, selectParameters, list.isStreamed(), returnOnMatch);
	}

}
