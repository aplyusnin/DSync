package ru.nsu.fit.dsync.server.sockets;

import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.RepoHandler;
import ru.nsu.fit.dsync.server.storage.UserMetaData;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ConnectionManager {

	private static ConnectionManager instance = null;

	private HashSet<UserConnection> activeConnections;
	private HashMap<RepoHandler, HashSet<UserConnection>> subscribitions;

	private ConnectionManager(){
		activeConnections = new HashSet<>();
		subscribitions = new HashMap<>();
	}

	public static synchronized ConnectionManager getInstance() {
		if (instance == null) instance = new ConnectionManager();
		return instance;
	}

	public UserConnection createConnection(NotifyWebSocket socket){
		UserConnection connection = new UserConnection(socket);
		activeConnections.add(connection);
		return connection;
	}

	public void closeConnection(UserConnection connection){
		for (var x : connection.getSubscribes()){
			try {
				RepoHandler handler = FileManager.getInstance().getHandler(x.first, x.second);
				subscribitions.get(handler).remove(connection);
			}
			catch (Exception e){}
		}
	}

	public void subscribe(String owner, String repo, UserConnection connection) throws Exception{
		synchronized (this) {
			RepoHandler handler = FileManager.getInstance().getHandler(owner, repo);
			if (!connection.getUser().equals(owner)
					&& !UserMetaData.getInstance().hasAccess(connection.getUser(), handler))
				throw new InvalidRequestDataException("User hasn't access to the given repo");
			if (!subscribitions.containsKey(handler))
			{
				subscribitions.put(handler, new HashSet<>());
			}
			if (subscribitions.get(handler).contains(connection)) throw new InvalidRequestDataException("Already subscribed");
			subscribitions.get(handler).add(connection);
			//notifyOnUpdate(handler, "File1", "3c35bf778a7a6c61a150da8705559da4a4ec58aa4c50fd5ee68586c816deaa8d");
		}
	}

	public void notifyOnUpdate(RepoHandler handler, String filename, String version){
		for (var x : subscribitions.get(handler)){
			x.sendNotification(handler.getOwner(), handler.getRepoName(), filename, version);
		}
	}
}
