package org.eclipse.epsilon.emc.jdbc;

import java.sql.SQLException;

import org.eclipse.epsilon.eol.exceptions.EolIllegalPropertyAssignmentException;
import org.eclipse.epsilon.eol.exceptions.EolInternalException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.introspection.AbstractPropertySetter;

public class ResultPropertySetter extends AbstractPropertySetter {

	protected JdbcModel model = null;
	
	public ResultPropertySetter(JdbcModel model) {
		this.model = model;
	}
	
	@Override
	public void invoke(Object value) throws EolRuntimeException {
		
		if (model.isReadOnly()) throw new EolIllegalPropertyAssignmentException(property, ast);
		
		try {
			((Result) object).setValue(property, value);
		} catch (SQLException e) {
			throw new EolInternalException(e);
		}
	}

}
