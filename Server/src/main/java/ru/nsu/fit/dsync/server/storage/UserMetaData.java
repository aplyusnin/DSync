package ru.nsu.fit.dsync.server.storage;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;
import ru.nsu.fit.dsync.utils.Misc;

/**
 * Class for storing information about users
 */
public class UserMetaData {
	private static UserMetaData instance = null;
	private HashMap<String, String> tokenToUser;


	private String root = "Users";

	private UserMetaData(){
		tokenToUser = new HashMap<>();
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

/*
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
    */

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
		File pass = new File(root + "/" + username + "/password.bin");
		pass.createNewFile();
		OutputStream out = new FileOutputStream(pass);
		out.write(hash);
		out.close();
		File access = new File(root + "/" + username + "/access.json");
		access.createNewFile();
		PrintWriter writer = new PrintWriter(new FileOutputStream(access));
		writer.println("{}");
		writer.close();
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


	public boolean hasAccess(String user, RepoHandler handler) {
		if (handler.getOwner().equals(user)) return true;
		File file = new File("Users/" + user + "/access.json");
		if (!file.exists()) return false;
		ObjectMapper objectMapper = new ObjectMapper();
		try
		{
			ObjectNode root = objectMapper.readValue(file, ObjectNode.class);
			ArrayNode node = (ArrayNode) root.get(handler.getOwner());
			for (int i = 0; i < node.size(); i++){
				if (node.get(i).asText().equals(handler.getRepoName())) return true;
			}
			return false;
		}
		catch (Exception e) {
			return false;
		}
	}

	public void addAccess(String user, RepoHandler handler){
		if (hasAccess(user, handler)) return;
		try
		{
			File file = new File("Users/" + user + "/access.json");
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode root = objectMapper.readValue(file, ObjectNode.class);
			ArrayNode node = (ArrayNode) root.get(handler.getOwner());
			if (node == null){
				node = objectMapper.createArrayNode();
			}
			node.add(handler.getRepoName());
			root.set(handler.getOwner(), node);
			FileWriter writer1 = new FileWriter("Users/" + user + "/access.json");
			PrintWriter writer = new PrintWriter(writer1);
			writer.println(root.toString());
			writer.flush();
			writer1.flush();
			writer1.close();
		}
		catch (Exception e) {
			return;
		}
	}

	public String createToken(String user)	{
    	try {
		    String time = Long.toString(System.currentTimeMillis());
		    String token = Base64.getEncoder().encodeToString(
				    (user + ":" + Misc.bytesToHex(Misc.getSHA256Hash(user + " " + Misc.bytesToHex(Misc.getSHA256Hash(time)) + Double.toString(Math.random() * 1e9)))).getBytes());

		    tokenToUser.put(token, user);
		    return token;
	    }
    	catch (Exception e){return null; }
	}

	public boolean isTokenExist(String token){
		return tokenToUser.containsKey(token);
	}

	public String getUserByToken(String token){
		return tokenToUser.get(token);
	}

}
