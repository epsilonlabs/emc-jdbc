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

import java.sql.SQLException;

import org.eclipse.epsilon.eol.exceptions.EolInternalException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.introspection.AbstractPropertyGetter;

public class ResultPropertyGetter extends AbstractPropertyGetter {
	
	protected JdbcModel model = null;
	
	public ResultPropertyGetter(JdbcModel model) {
		this.model = model;
	}
	
	@Override
	public Object invoke(Object object, String property)
			throws EolRuntimeException {
		
		try {
			Object ret = ((Result) object).getValue(property);
			//System.out.println(ret);
			return ret;
		} catch (SQLException e) {
			throw new EolInternalException(e);
		}
		/*
		if (object instanceof ResultSetList) {
			ResultSetList resultSetList = (ResultSetList) object;
			return new PrimitiveValuesList(resultSetList.getModel(), resultSetList.getTable(), 
					property, resultSetList.getCondition(), resultSetList.getParameters(), false, model.isStreamResults());
		}
		else {
			
		}*/
	}

}
