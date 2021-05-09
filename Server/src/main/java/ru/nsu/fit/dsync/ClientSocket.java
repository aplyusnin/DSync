package ru.nsu.fit.dsync;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class ClientSocket extends WebSocketAdapter {

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
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		/* do nothing */
	}

	@Override
	public void onWebSocketText(String message) {
		System.out.println(message);
	}

	public void sendMessage(String message) throws Exception {
		getRemote().sendString(message);
	}
}