/*******************************************************************************
 * Copyright (c) 2012 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.emc.mysql.dt;

import org.eclipse.epsilon.common.dt.launching.dialogs.AbstractModelConfigurationDialog;
import org.eclipse.epsilon.emc.mysql.MySqlModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MySqlModelConfigurationDialog extends AbstractModelConfigurationDialog {

	protected String getModelName() {
		return "MySQL Database";
	}
	
	protected String getModelType() {
		return "MySQL";
	}
	
	protected Text databaseText;	
	protected Text serverText;	
	protected Text portText;	
	protected Text usernameText;	
	protected Text passwordText;
	protected Button readonlyButton;
	protected Button streamedResultsButton;
	
	protected void createGroups(Composite control) {
		super.createGroups(control);
		createFilesGroup(control);
	}
	
	protected Composite createFilesGroup(Composite parent) {
		final Composite groupContent = createGroupContainer(parent, "Database", 2);
		
		serverText = createLabeledText(groupContent, "Server");
		portText = createLabeledText(groupContent, "Port");
		databaseText = createLabeledText(groupContent, "Database");
		usernameText = createLabeledText(groupContent, "Username");
		passwordText = createLabeledText(groupContent, "Password", SWT.BORDER | SWT.PASSWORD);
		readonlyButton = createLabeledButton(groupContent, "Read-only", SWT.CHECK);
		//only allow read-only connections for now
		readonlyButton.setEnabled(false);
		readonlyButton.setSelection(true);
		streamedResultsButton = createLabeledButton(groupContent, "Stream results", SWT.CHECK);
		//only allow streamed connections for now
		streamedResultsButton.setEnabled(false);
		streamedResultsButton.setSelection(true);
		
		
		
		groupContent.layout();
		groupContent.pack();
		return groupContent;
	}
	
	protected Button createLabeledButton(Composite parent, String label, int style) {
		Label controlLabel = new Label(parent, SWT.NONE);
		controlLabel.setText(label + ": ");
		Button button = new Button(parent, style);
		return button;
	}
	
	protected Text createLabeledText(Composite parent, String label, int style) {
		Label controlLabel = new Label(parent, SWT.NONE);
		controlLabel.setText(label + ": ");
		Text text = new Text(parent, style);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}
	
	protected Text createLabeledText(Composite parent, String label) {
		return createLabeledText(parent, label, SWT.BORDER);
	}
	
	protected void loadProperties(){
		super.loadProperties();
		
		if (properties == null) { 
			serverText.setText("localhost");
			portText.setText("3306");
			readonlyButton.setSelection(true);
			return;
		}
		
		databaseText.setText(properties.getProperty(MySqlModel.PROPERTY_DATABASE));
		serverText.setText(properties.getProperty(MySqlModel.PROPERTY_SERVER));
		portText.setText(properties.getProperty(MySqlModel.PROPERTY_PORT));
		usernameText.setText(properties.getProperty(MySqlModel.PROPERTY_USERNAME));
		passwordText.setText(properties.getProperty(MySqlModel.PROPERTY_PASSWORD));
		readonlyButton.setSelection(properties.getBooleanProperty(MySqlModel.PROPERTY_READONLY, true));
		streamedResultsButton.setSelection(properties.getBooleanProperty(MySqlModel.PROPERTY_STREAMRESULTS, true));
	}
	
	protected void storeProperties(){
		super.storeProperties();
		properties.put(MySqlModel.PROPERTY_DATABASE, databaseText.getText());
		properties.put(MySqlModel.PROPERTY_SERVER, serverText.getText());
		properties.put(MySqlModel.PROPERTY_PORT, portText.getText());
		properties.put(MySqlModel.PROPERTY_USERNAME, usernameText.getText());
		properties.put(MySqlModel.PROPERTY_PASSWORD, passwordText.getText());
		properties.put(MySqlModel.PROPERTY_READONLY, readonlyButton.getSelection() + "");
		properties.put(MySqlModel.PROPERTY_STREAMRESULTS, streamedResultsButton.getSelection() + "");
	}
}
