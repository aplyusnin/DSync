package ru.nsu.fit.dsync.server;

import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import ru.nsu.fit.dsync.server.servlets.*;
import ru.nsu.fit.dsync.server.sockets.NotifyWebSocket;

public class DSyncServer {
	private Server server;
	private int port;
	public DSyncServer(int port){
		this.port = port;
		server = new Server(port);
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		//handler.addServlet(VersionControlServlet.class, "/INFO");
		handler.addServlet(VersionDownloadServlet.class, "/DOWNLOAD");
		handler.addServlet(VersionUploadServlet.class, "/UPLOAD").getRegistration()
				.setMultipartConfig(new MultipartConfigElement("./Temp.dat", 1024 * 1024 * 5, 1024 * 1024 * 5 * 5, 1024 * 1024));
		handler.addServlet(CreateRepoServlet.class, "/NEWREPO");
		handler.addServlet(CreateUserServlet.class, "/NEWUSER");
		handler.addServlet(GetRepoInfoServlet.class, "/REPOINFO");
		handler.addServlet(RepoSharingServlet.class, "/SHARE");

		/*HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { handler });*/
		server.setHandler(handler);
		JettyWebSocketServletContainerInitializer.configure(handler, (servletContext, wsContainer) ->
		{
			// Configure default max size
			wsContainer.setMaxTextMessageSize(65535);

			// Add websockets
			wsContainer.addMapping("/events/*", NotifyWebSocket.class);
		});
	}

	public void run() throws Exception {
		server.start();
		server.join();
	}

	public void shutdown() throws Exception {
		server.stop();
	}
}