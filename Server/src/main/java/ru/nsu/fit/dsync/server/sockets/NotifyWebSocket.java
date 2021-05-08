package ru.nsu.fit.dsync.server.sockets;

import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;


public class NotifyWebSocket extends WebSocketAdapter {

	private UserConnection owner;

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		/* do nothing */
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		/* do nothing */
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		owner = ConnectionManager.getInstance().createConnection(this);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		/* do nothing */
	}

	@Override
	public void onWebSocketText(String message) {
		System.out.println("Received message: " + message);
		owner.receiveMessage(message);
	}

	public void sendMessage(String message) throws Exception {
		getRemote().sendString(message);
	}



}
