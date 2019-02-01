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
import org.eclipse.epsilon.eol.execute.operations.declarative.CollectOperation;

public class ResultSetListCollectOperation extends CollectOperation {

	@Override
	public Collection<?> execute(Object target, NameExpression operationNameExpression, List<Parameter> iterators,
			List<Expression> expressions, IEolContext context) throws EolRuntimeException {

		ResultSetList resultSetList = (ResultSetList) target;
		return new PrimitiveValuesList(resultSetList.getModel(), resultSetList.getTable(), 
				resultSetList.getModel().ast2sql(
					resultSetList.getTable(), createIteratorVariable(null, iterators.get(0), context),
					expressions.get(0), context, new ArrayList<>()
				), 
				resultSetList.getCondition(), resultSetList.getParameters(), false, resultSetList.isStreamed(),
				resultSetList.isOne());
	}

}
