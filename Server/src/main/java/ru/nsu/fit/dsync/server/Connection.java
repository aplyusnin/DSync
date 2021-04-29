package ru.nsu.fit.dsync.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection implements Runnable {

	private Socket socket;
	private InputStream input;
	private OutputStream output;

	public Connection(Socket socket) throws Exception{
		this.socket = socket;
		this.input = socket.getInputStream();
		this.output = socket.getOutputStream();
	}

	@Override
	public void run()
	{
		try
		{

			socket.close();
		}
		catch (Exception e){
			System.err.println(e.getStackTrace());
		}
	}

}
