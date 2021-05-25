package ru.nsu.fit.dsync;


import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.Future;

public class TestClient {
	public static void main(String args[]) throws Exception{
		run();
	}


	public static void run() throws Exception{
		URI uri = URI.create("ws://localhost:8090/LOGGED/events/");

		WebSocketClient client = new WebSocketClient();
		client.start();
				// The socket that receives events
		ClientSocket socket = new ClientSocket();
		// Attempt Connect
		Future<Session> fut = client.connect(socket, uri);
		// Wait for Connect
		Session session = fut.get();

		//Thread.sleep(1000);
		socket.sendMessage("{\"op\" : \"login\", \"login\" : \"1\", \"password\" : \"12345\"}");

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
