package ru.nsu.fit.dsync;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Future;

public class TestClient {
	public static void main(String args[]) throws Exception{
		run();
	}


	public static void run() throws Exception{

		URL log = new URL("http://localhost:8090/LOGIN?login=1&password=12345");
		HttpURLConnection logconnection = (HttpURLConnection)log.openConnection();

		BufferedReader in = new BufferedReader(new InputStreamReader(logconnection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode root = objectMapper.readValue(response.toString(), ObjectNode.class);

		String token = root.get("token").asText();

		URI uri = URI.create("ws://localhost:8090/events/");

		WebSocketClient client = new WebSocketClient();
		client.start();
				// The socket that receives events
		ClientSocket socket = new ClientSocket();
		// Attempt Connect
		Future<Session> fut = client.connect(socket, uri);
		// Wait for Connect
		Session session = fut.get();

		//Thread.sleep(1000);
		socket.sendMessage("{\"op\" : \"login\", \"token\" : \"" + token + "\"}");

		//Thread.sleep(1000);
		socket.sendMessage("{\"op\" : \"latest\", \"owner\" : \"1\", \"repo\" : \"repo1\", \"file\" : \"File1\"}");

		//Thread.sleep(1000);
		socket.sendMessage("{\"op\" : \"subscribe\", \"owner\" : \"1\", \"repo\" : \"repo1\"}");

		//Thread.sleep(1000);
				// Close session
		session.close();

		client.stop();
	}
}
