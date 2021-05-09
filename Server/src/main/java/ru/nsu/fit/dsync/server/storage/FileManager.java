package ru.nsu.fit.dsync.server.storage;

import java.util.HashMap;

/**
 * Class that perform all work on storage
 */
public class FileManager {
	private static FileManager instance = null;
	private HashMap<String, RepoHandler> handlers;

	private String fileRoot = "src/test/resources/Users/";

	private FileManager(){
		handlers = new HashMap<>();
	}

	public static FileManager getInstance(){
		if (instance == null) instance = new FileManager();
		return instance;
	}

	private RepoHandler restoreHandler(String user, String repo) throws Exception{
		String key = user + "/" + repo;
		if (!handlers.containsKey(key)) {
			handlers.put(key, new RepoHandler(user, repo));
		}
		return handlers.get(key);
	}

	/**
	 * Get directory handler by name
	 * @param user - name of user
	 * @param repo - name of repo
	 * @return handler of directory
	 * @throws Exception - directory doesn't exist
	 */
	public RepoHandler getHandler(String user, String repo) throws Exception {
		return restoreHandler(user, repo);
	}




}
