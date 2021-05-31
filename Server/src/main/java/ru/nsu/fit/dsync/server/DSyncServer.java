package ru.nsu.fit.dsync.server;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.ini4j.Wini;
import ru.nsu.fit.dsync.server.servlets.*;
import ru.nsu.fit.dsync.server.sockets.NotifyWebSocket;

import java.io.File;
import java.time.Duration;
import java.util.EnumSet;

public class DSyncServer {
	private Server server;
	private int port;

	/*public DSyncServer()
	{
		Server server1 = new Server();
		ServerConnector connector = new ServerConnector(server1);
		connector.setHost("127.0.0.1");
	}*/

	public void configure()
	{

		String config = "./config.ini";
		String address  = "localhost";
		int port = 8090;
		try
		{
			Wini ini = new Wini(new File(config));
			address = ini.get("serverInfo", "address", String.class);
			port = ini.get("serverInfo", "port", int.class);
		}
		catch (Exception e)
		{
			address = "localhost";
			port = 8090;
		}
		ServerConnector connector = new ServerConnector(server);
		connector.setHost(address);
		connector.setPort(port);
		server.addConnector(connector);
	}


	public DSyncServer(){
		server = new Server();

	}

	public void setServices()
	{
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);


		handler.addFilter(AuthenticationFilter.class, "/DATA/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
		handler.addFilter(AvailabilityFilter.class, "/DATA/ACCESS/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
		//handler.addServlet(VersionControlServlet.class, "/INFO");
		handler.addServlet(VersionDownloadServlet.class, "/DATA/ACCESS/DOWNLOAD");
		handler.addServlet(VersionUploadServlet.class, "/DATA/ACCESS/UPLOAD").getRegistration()
				.setMultipartConfig(new MultipartConfigElement("./Temp.dat", 1024 * 1024 * 5, 1024 * 1024 * 5 * 5, 1024 * 1024));
		handler.addServlet(CreateRepoServlet.class, "/DATA/NEWREPO");
		handler.addServlet(CreateUserServlet.class, "/NEWUSER");
		handler.addServlet(LoginServlet.class, "/LOGIN");
		handler.addServlet(GetRepoInfoServlet.class, "/DATA/ACCESS/REPOINFO");
		handler.addServlet(RepoSharingServlet.class, "/DATA/ACCESS/SHARE");
		/*HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { handler });*/
		server.setHandler(handler);
		JettyWebSocketServletContainerInitializer.configure(handler, (servletContext, wsContainer) ->
		{
			// Configure default max size
			wsContainer.setMaxTextMessageSize(65535);

			wsContainer.setIdleTimeout(Duration.ofDays(10));

			// Add websockets
			wsContainer.addMapping("/events/*", NotifyWebSocket.class);
		});
	}


	public static DSyncServer create()
	{
		DSyncServer server = new DSyncServer();
		server.configure();
		server.setServices();
		return server;
	}


	public void run() throws Exception {
		server.start();
		server.join();
	}

	public void shutdown() throws Exception {
		server.stop();
	}
}
