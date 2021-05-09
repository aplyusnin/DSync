package ru.nsu.fit.dsync.server.storage;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;
import ru.nsu.fit.dsync.utils.Misc;

/**
 * Class for storing information about users
 */
public class UserMetaData {
	private static UserMetaData instance = null;
	private HashMap<String, HashSet<String>> availableFiles;

	private String root = "Users";

	private UserMetaData(){
		availableFiles = new HashMap<>();
	}

	public static UserMetaData getInstance() {
		if (instance == null) instance = new UserMetaData();
		return instance;
	}


	public File findUser(String user) throws Exception {
		File f =  new File(root + "/" + user);

		if (!f.exists() || !f.isDirectory()) throw new InvalidRequestDataException("User doesn't exists");

		return f;
 	}

 	public byte[] getUserPasswordHash(String user) throws Exception {
	    File f = findUser(user);
		File passwordHashFile = new File(f.getPath() + "/password.bin");

		byte[] hash = new byte[1024];
		int cnt = 0;
		if ((cnt = new FileInputStream(passwordHashFile).read(hash, 0, 1024)) != 32)
		{
			throw new Exception("Invalid hash length");
		}
		return hash;
    }


    public HashSet<String> getSharedFiles(String user) throws Exception {
    	if (availableFiles.containsKey(user)) return availableFiles.get(user);
    	var f = findUser(user);

    	File fson = new File(f.getPath() + "/sharedFiles.json");

		HashSet<String> shared = new HashSet<>();
	    ObjectMapper objectMapper = new ObjectMapper();
	    ObjectNode node = objectMapper.readValue(fson, ObjectNode.class);

	    for (var x : node.get("Shared"))
	    {
	        shared.add(x.asText());
	    }
        availableFiles.put(user, shared);
	    return shared;
    }

    public void validateUserData(String user, String password) throws Exception {
	    byte[] hashServer = getUserPasswordHash(user);
	    byte[] hashClient = Misc.getSHA256Hash(password);

	    for (int i = 0; i < 32; i++) {
			if (hashServer[i] != hashClient[i])
		        throw new InvalidRequestDataException("Incorrect password");
	    }
    }

    public boolean isUserExists(String username){
		File f = new File(root + "/" + username);
		return f.exists();
	}

	public void createUser(String username, String password) throws Exception {
		byte[] hash = Misc.getSHA256Hash(password);
		File file = new File(root + "/" + username);
		System.out.println(file.mkdirs());
		File pass = new File(root + "/" + username + "/password.bin");
		System.out.println(pass.createNewFile());
		System.out.flush();
		OutputStream out = new FileOutputStream(pass);
		out.write(hash);
		out.close();
	}

	public void createRepo(String username, String repo) throws Exception {
		File file = new File(root + "/" + username + "/Files/" + repo);
		if (file.exists()) throw new InvalidRequestDataException("repo already exists");
		file.mkdirs();
		File versions = new File(root + "/" +username + "/Files/" + repo + "/versions.json");
		PrintWriter writer = new PrintWriter(new FileOutputStream(versions));
		writer.println("{}");
		writer.close();
	}


	public boolean hasAccess(String owner, RepoHandler handler) {
		return true;
	}


}
