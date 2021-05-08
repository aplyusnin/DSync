package ru.nsu.fit.dsync.server.sockets;

import ru.nsu.fit.dsync.server.storage.RepoHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ConnectionManager {

	private static ConnectionManager instance = null;

	private HashSet<UserConnection> activeConnections;
	private HashMap<RepoHandler, HashSet<UserConnection>> subscribitions;

	private ConnectionManager(){
		activeConnections = new HashSet<>();
		subscribitions = new HashMap<>();
	}

	public static ConnectionManager getInstance() {
		if (instance == null) instance = new ConnectionManager();
		return instance;
	}

	public UserConnection createConnection(NotifyWebSocket socket){
		UserConnection connection = new UserConnection(socket);
		activeConnections.add(connection);
		return connection;
	}

	public void closeConnection(UserConnection connection){

	}
}
