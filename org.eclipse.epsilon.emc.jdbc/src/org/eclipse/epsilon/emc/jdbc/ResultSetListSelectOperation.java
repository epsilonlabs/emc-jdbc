package org.eclipse.epsilon.emc.jdbc;

import java.util.ArrayList;

import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.execute.operations.declarative.SelectOperation;

public class ResultSetListSelectOperation extends SelectOperation {
	
	@Override
	public Object execute(Object target, Variable iterator, Expression expressionAst, IEolContext context, boolean returnOnFirstMatch) throws EolRuntimeException {

		ResultSetList list = (ResultSetList) target;
		
		ArrayList<Object> selectParameters = new ArrayList<Object>();
		String selectCondition = list.getModel().ast2sql(list.getTable(),iterator, expressionAst, context, selectParameters);
		
		if (list.getCondition() != null && list.getCondition().trim().length() > 0) {
			selectParameters.addAll(list.getParameters());
			selectCondition = "(" + selectCondition + ") and (" + list.getCondition() + ")";
		}
		
		return new ResultSetList(list.getModel(), list.getTable(), selectCondition, selectParameters, list.isStreamed(), returnOnFirstMatch);
		
	}

}
