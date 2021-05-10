package ru.nsu.fit.dsync.server.sockets;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.Part;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.RepoHandler;
import ru.nsu.fit.dsync.server.storage.UserMetaData;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;
import ru.nsu.fit.dsync.utils.Pair;

import java.util.HashSet;

public class UserConnection {

	public enum State{
		ANONYMOUS,
		IDLE
	}
	private NotifyWebSocket socket;
	private String user = "";
	private HashSet<Pair<String, String>> subscribes;
	private State state;
	private ObjectMapper mapper = new ObjectMapper();

	public UserConnection(NotifyWebSocket socket){
		this.socket = socket;
		subscribes = new HashSet<>();
		state = State.ANONYMOUS;
	}

	public void receiveMessage(String text){
		switch (state){
			case IDLE -> receiveIdle(text);
			case ANONYMOUS -> receiveAnonymous(text);
		}
	}

	private void serverError(){
		try {
			socket.sendMessage("{ \"error\" : \"server error\"}");
		}
		catch (Exception e){}
	}

	private void inputError(String message){
		try {
			socket.sendMessage("{ \"error\" : \"" + message + "\"}");
		}
		catch (Exception e){}
	}

	private void success(){
		try {
			socket.sendMessage("{\"status\" : \"success\"}");
		}
		catch (Exception e){}
	}

	private void receiveAnonymous(String text){
		try {
			ObjectNode node = mapper.readValue(text, ObjectNode.class);
			String op = node.get("op").asText();
			if (!"login".equals(op)) throw new InvalidRequestDataException("Expected login operation");
			String login = node.get("login").asText();
			String password = node.get("password").asText();
			UserMetaData.getInstance().validateUserData(login, password);
			this.user = login;
			state = State.IDLE;
			success();
		}
		catch (InvalidRequestDataException e){
			inputError(e.getMessage());
		}
		catch (Exception e){
			serverError();
		}
	}

	private void receiveIdle(String text) {
		try {
			ObjectNode node = mapper.readValue(text, ObjectNode.class);
			String op = node.get("op").asText();

			switch (op){
				case "subscribe"-> {
					String owner = node.get("owner").asText();
					String repo = node.get("repo").asText();
					RepoHandler handler = FileManager.getInstance().getHandler(owner, repo);
					if (!UserMetaData.getInstance().hasAccess(user, handler)){
						throw new InvalidRequestDataException("Access denied");
					}
					try{
						ConnectionManager.getInstance().subscribe(owner, repo, this);
					}
					catch (InvalidRequestDataException e) {
						inputError(e.getMessage());
					}
					finally {
						subscribes.add(new Pair<>(owner, repo));
					}

				}
				case "latest" -> {
					String owner = node.get("owner").asText();
					String repo = node.get("repo").asText();
					String file = node.get("file").asText();
					RepoHandler handler = FileManager.getInstance().getHandler(owner, repo);
					if (!UserMetaData.getInstance().hasAccess(user, handler)){
						throw new InvalidRequestDataException("Access denied");
					}
					String version =  handler.getLastVersion(file);
					sendMessage("{ \"version\" : \"" + version + "\"}");
				}
				default -> throw new InvalidRequestDataException("Invalid operation");
			}
		}
		catch (InvalidRequestDataException e){
			inputError(e.getMessage());
		}
		catch (Exception e){
			serverError();
		}
	}


	private void sendMessage(String message){
		try{
			System.out.println("Sending: " + message);
			socket.sendMessage(message);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
	}

	public void sendNotification(String owner, String repo, String file, String version){
		try {
			socket.sendMessage("{ \"event\" : \"fileUpdate\",\n" +
					                   "\"owner\" : \"" + owner + "\",\n" +
					                   "\"repo\" : \"" + repo + "\",\n" +
					                   "\"file\" : \"" + file + "\",\n" +
			                           "\"version\" : \"" + version + "\"}");
		}
		catch (Exception e){}
	}


	public HashSet<Pair<String, String>> getSubscribes() {
		return subscribes;
	}

	public String getUser() {
		return user;
	}

}
