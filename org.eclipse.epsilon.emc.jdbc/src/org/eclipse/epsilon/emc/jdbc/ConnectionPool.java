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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Properties;
import java.util.Queue;
import java.util.WeakHashMap;

/**
 * The connection pool creates and manages the connections to the DB. There is a shared connection and a pool of
 * streaming connections. Is the responsibility of the requester to return the streaming connection once it has
 * finished using it.
 * 
 * @author Dimitris Kolovos
 *
 */
public class ConnectionPool {
	
	/** DB Connection information */
	protected String username;
	protected String password;
	protected String jdbcUrl;
	
	/** The particular DB driver */
	protected Driver driver;
	
	/** The pool of streaming connections */
	protected Queue<Connection> streamingConnections = new ArrayDeque<>();
	
	/** The shated connection */
	protected Connection sharedConnection = null;
	
	/** A Map to keep relations between ResultSets and Connections  */
	protected WeakHashMap<ResultSet, Connection> resultSetMap = new WeakHashMap<>();
	
	/**
	 * Create a new pool with the supplied parameters
	 * 
	 * @param driver
	 * @param jdbcUrl
	 * @param username
	 * @param password
	 * @throws SQLException If the shared connection can not be created.
	 */
	public ConnectionPool(Driver driver, String jdbcUrl, String username, String password) throws SQLException {
		super();
		this.driver = driver;
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
		this.sharedConnection = createConnection();
	}
	
	/**
	 * Get the shared connection (i.e. non-streaming)
	 * 
	 * @return the connection
	 */
	protected Connection getSharedConnection() {
		return getConnection(false);
	}
	
	/**
	 * Get a streaming connection
	 *  
	 * @return The connection
	 */
	protected Connection getStreamingConnection() {
		return getConnection(true);
	}
	
	/**
	 * Get a connection
	 * 
	 * @param streaming If true, a connection from the streaming connection pool is used.
	 * @return The connection
	 */
	public Connection getConnection(boolean streaming) {
		Connection connection = null;
		
		if (streaming) {
			if (streamingConnections.isEmpty()) {
				try {
					connection = createConnection();
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			else {
				connection = streamingConnections.remove();
			}
		}
		else {
			connection = sharedConnection;
		}
		
		return connection;
	}
	
	/**
	 * Return a streaming connection to the pool
	 * 
	 * @param connection
	 */
	public void returnConnection(Connection connection) {
		if (connection != sharedConnection) {
			streamingConnections.add(connection);
		}
	}
	
	/**
	 * Create a connection
	 * 
	 * @return the connection
	 * @throws SQLException
	 */
	protected Connection createConnection() throws SQLException {
		Properties properties = new Properties();
		properties.put("user", username);
		properties.put("password", password);
		return driver.connect(jdbcUrl, properties);
	}
	
	/**
	 * Close the connections.
	 * @throws SQLException
	 */
	public void dispose() throws SQLException {
		sharedConnection.close();
		for (Connection connection : streamingConnections) {
			connection.close();
		}
		streamingConnections.clear();
	}
	
	/**
	 * Register a ResultSet as owner of a connection
	 * 
	 * @param resultSet
	 * @param connection
	 */
	public void register(ResultSet resultSet, Connection connection) {
		resultSetMap.put(resultSet, connection);
	}
	
	/**
	 * Signal the pool that the ResultSet has finished streaming. If still exists, the connection owned by the
	 * result set will be returned to the pool.
	 * 
	 * @see #register(ResultSet, Connection)
	 * @param resultSet
	 */
	public void finishedStreaming(ResultSet resultSet) {
		Connection connection = resultSetMap.get(resultSet);
		if (connection != null) {
			returnConnection(connection);
		}
	}
	
}
