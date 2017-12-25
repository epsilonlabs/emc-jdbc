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

import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.execute.operations.declarative.CollectOperation;

public class ResultSetListCollectOperation extends CollectOperation {

	@Override
	public Object execute(Object target, Variable iterator, Expression expressionAst,
			IEolContext context) throws EolRuntimeException {
		
		ResultSetList resultSetList = (ResultSetList) target;
		return new PrimitiveValuesList(resultSetList.getModel(), resultSetList.getTable(), 
				resultSetList.getModel().ast2sql(resultSetList.getTable(),iterator, expressionAst, context, new ArrayList<Object>()), 
				resultSetList.getCondition(), resultSetList.getParameters(), false, resultSetList.isStreamed(),
				resultSetList.isOne());

	}

}
