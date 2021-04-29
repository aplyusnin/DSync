package ru.nsu.fit.dsync.server;

import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

	public static void Main(String[] args) throws Exception {
		int port = Integer.parseInt(args[0]);
		ServerSocket serverSocket = new ServerSocket(port);

		while (true){
			Socket socket = serverSocket.accept();

			try {
				new Thread(new Connection(socket)).start();
			}
			catch (Exception e){
				System.err.println(e.getStackTrace());
			}


		}
	}
}
