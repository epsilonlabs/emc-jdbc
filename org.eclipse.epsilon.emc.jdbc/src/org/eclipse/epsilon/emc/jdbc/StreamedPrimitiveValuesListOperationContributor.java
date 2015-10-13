package org.eclipse.epsilon.emc.jdbc;

import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;

public class StreamedPrimitiveValuesListOperationContributor extends OperationContributor {

	@Override
	public boolean contributesTo(Object target) {
		return target instanceof PrimitiveValuesList && 
			((PrimitiveValuesList) target).isStreamed();
	}
	
	public Object min() {
		PrimitiveValuesList l = (PrimitiveValuesList) target;
		System.err.println("Min...");
		return new StreamedPrimitiveValuesListSqlOperation<Object>("min", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
	public Object max() {
		PrimitiveValuesList l = (PrimitiveValuesList) target;
		return new StreamedPrimitiveValuesListSqlOperation<Object>("max", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
	public Object sum() {
		PrimitiveValuesList l = (PrimitiveValuesList) target;
		return new StreamedPrimitiveValuesListSqlOperation<Object>("sum", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
	public Object avg() {
		PrimitiveValuesList l = (PrimitiveValuesList) target;
		return new StreamedPrimitiveValuesListSqlOperation<Object>("avg", l.getSelection(), l.getCondition(), l.getParameters(), l.getModel(), l.getTable(), l.isOne()).getValue();
	}
	
}
