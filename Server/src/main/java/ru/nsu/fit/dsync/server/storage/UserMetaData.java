package ru.nsu.fit.dsync.server.storage;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class for storing information about users
 */
public class UserMetaData {
	private static UserMetaData instance = null;
	private HashMap<String, HashSet<String>> availableFiles;

	private String root = "/Users";

	private UserMetaData(){
		availableFiles = new HashMap<>();
	}

	public static UserMetaData getInstance() {
		if (instance == null) instance = new UserMetaData();
		return instance;
	}


	public File findUser(String user) throws Exception {
		File rootf = new File(UserMetaData.class.getResource(root).toURI());
		return new File(rootf.getPath() + "/" + user);
 	}

 	public byte[] getUserPasswordHash(String user) throws Exception {
		File f = findUser(user);

		File passwordHashFile = new File(f.getPath() + "/password.bin");

		byte[] hash = new byte[32];

		if (new FileInputStream(passwordHashFile).read(hash, 0, 32) != 32){
			throw new Exception("User's password hash is broken");
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

}
