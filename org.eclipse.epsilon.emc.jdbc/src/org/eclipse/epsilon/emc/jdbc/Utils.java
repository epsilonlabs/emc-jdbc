/*******************************************************************************
 * Copyright (c) 2011-2014 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Konstantinos Barmpis - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.emc.jdbc;

public class Utils {

	// wrap all feature names in quotes to ensure escaping of special characters
	// like spaces (permitted by some technologies, which would result in
	// incorrect SQL)
	public static String wrap(String s, String identifierQuoteString) {
		return identifierQuoteString + s + identifierQuoteString;
	}

}
