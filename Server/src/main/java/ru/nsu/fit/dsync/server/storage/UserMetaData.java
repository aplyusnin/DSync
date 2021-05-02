package ru.nsu.fit.dsync.server.storage;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.nsu.fit.dsync.utils.Misc;

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
		return new File("Users/" + user);
 	}

 	public String getUserPasswordHash(String user) throws Exception {
		File f = findUser(user);

		File passwordHashFile = new File(f.getPath() + "/password.txt");


		//byte[] hash = passwordHashFile.read
		Scanner sc = new Scanner(new FileInputStream(passwordHashFile));

		return sc.next();
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
	    String hashServer = getUserPasswordHash(user);
		String hashClient = Misc.bytesToHex(Misc.getSHA256Hash(password));

		if (!hashServer.equals(hashClient)){}
			//throw  new Exception("Invalid password");
    }


}
