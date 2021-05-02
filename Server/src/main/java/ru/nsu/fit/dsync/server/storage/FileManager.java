package ru.nsu.fit.dsync.server.storage;

import ru.nsu.fit.dsync.utils.Misc;

import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;

/**
 * Class that perform all work on storage
 */
public class FileManager {
	private static FileManager instance = null;
	private HashMap<String, DirHandler> handlers;

	private String fileRoot = "src/test/resources/Users/";

	private FileManager(){
		handlers = new HashMap<>();
	}

	public static FileManager getInstance(){
		if (instance == null) instance = new FileManager();
		return instance;
	}

	/**
	 * Get directory handler by name
	 * @param user - name of user
	 * @param repo - name of repo
	 * @return handler of directory
	 * @throws Exception - directory doesn't exist
	 */
	public synchronized DirHandler getHandler(String user, String repo) throws Exception {
		String path = "Users/" + user + "/Files/" + repo + "/";
		if (!handlers.containsKey(path)) {
			handlers.put(path, new DirHandler(path));
		}
		return handlers.get(path);
	}

	/**
	 * Creates new file version and fills it with pre-stored data from temp
	 * @param temp  pre-stored file
	 * @param info information about file
	 * @param name name of initial file
	 * @return version name
	 * @throws Exception - couldn't create copy
	 */
	public String createVersion(File temp, FileInfo info, String name) throws Exception {
		String hash = Misc.bytesToHex(Misc.getSHA256Hash(new FileInputStream(temp)));
		String path = fileRoot + info.getOwner() + "/Files/" + info.getCommit() + "/data/" + info.getLocalpath() + "/" + info.getName() + "/" + hash;
		File newDir = new File(path);
		if (!newDir.mkdir()) throw new Exception("Can't create new version");
		File file = new File(newDir.getPath() + "/" + name);
		if (!file.createNewFile()) throw new Exception("Can't create new version");

		int size = 8196;
		byte[] buffer = new byte[size];
		int count = 0;
		int off = 0;

		InputStream inputStream = new FileInputStream(temp);
		OutputStream outputStream = new FileOutputStream(file);

		while ((count = inputStream.read(buffer, off, size)) != 0){
			outputStream.write(buffer, off, count);
			off += count;
			if (count != size) break;
		}
		return hash;
	}

	/**
	 * Sets new version of file into versions info
	 * @param handler - handler of directory
	 * @param info - info about file
	 * @param version - latest file version
	 * @throws Exception - unable to update
	 */
	public void updateLatestVersion(DirHandler handler, FileInfo info, String version) throws Exception
	{
		String path = "";
		if (!info.getLocalpath().equals("")) path = info.getLocalpath();
		path += info.getName();
		handler.rewriteVersionEntry(path, version);
	}

	/**
	 * Writes latest file versions into zip-stream
	 * @param out - zip-stream
	 * @param handler directory handler
	 */
	public void getLatestVersions(ZipOutputStream out, DirHandler handler){

	}
}
