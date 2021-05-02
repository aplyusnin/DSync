package ru.nsu.fit.dsync.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import ru.nsu.fit.dsync.server.servlets.VersionControlServlet;

public class Main {

	public static void main(String[] args) throws Exception {
		int port = 8090;//Integer.parseInt(args[0]);
		Server server = new Server(port);

		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		handler.addServlet(VersionControlServlet.class, "/INFO");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { handler });
		server.setHandler(handlers);

		try {
			server.start();
			System.out.println("Listening port: " + port);
			server.join();
		}
		catch (Exception e){
			System.out.println("Error.");
			e.printStackTrace();
		}
	}
}
