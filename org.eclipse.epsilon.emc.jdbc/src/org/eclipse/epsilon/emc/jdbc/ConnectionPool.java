package org.eclipse.epsilon.emc.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.WeakHashMap;

public class ConnectionPool {
	
	protected String username;
	protected String password;
	protected Driver driver;
	protected String jdbcUrl;
	
	protected ArrayList<Connection> streamingConnections = new ArrayList<Connection>();
	protected Connection sharedConnection = null;
	protected WeakHashMap<ResultSet, Connection> resultSetMap = new WeakHashMap<ResultSet, Connection>();
	
	public ConnectionPool(Driver driver, String jdbcUrl, String username, String password) throws SQLException {
		super();
		this.driver = driver;
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
		this.sharedConnection = createConnection();
	}

	protected Connection getSharedConnection() {
		return getConnection(false);
	}
	
	protected Connection getStreamingConnection() {
		return getConnection(true);
	}
	
	public Connection getConnection(boolean streaming) {
		Connection connection = null;
		
		if (streaming) {
			if (streamingConnections.size() == 0) {
				try {
					connection = createConnection();
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			else {
				connection = streamingConnections.remove(0);
			}
		}
		else {
			connection = sharedConnection;
		}
		
		return connection;
	}
	
	public void returnConnection(Connection connection) {
		if (connection != sharedConnection) {
			streamingConnections.add(connection);
		}
	}
	
	protected Connection createConnection() throws SQLException {
		Properties properties = new Properties();
		properties.put("user", username);
		properties.put("password", password);
		return driver.connect(jdbcUrl, properties);
	}
	
	public void dispose() throws SQLException {
		sharedConnection.close();
		for (Connection connection : streamingConnections) {
			connection.close();
		}
		streamingConnections.clear();
	}
	
	public void register(ResultSet resultSet, Connection connection) {
		resultSetMap.put(resultSet, connection);
	}
	
	public void finishedStreaming(ResultSet resultSet) {
		Connection connection = resultSetMap.get(resultSet);
		if (connection != null) {
			returnConnection(connection);
		}
	}
	
}
