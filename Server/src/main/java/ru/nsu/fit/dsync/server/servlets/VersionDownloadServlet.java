package ru.nsu.fit.dsync.server.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.RepoHandler;
import ru.nsu.fit.dsync.server.storage.UserMetaData;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;

import java.io.*;

@WebServlet("/FileDownloader")
public class VersionDownloadServlet extends HttpServlet {

	private final int SIZE = 1024;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String login = req.getParameter("login");
		String password = req.getParameter("password");
		try {
			UserMetaData.getInstance().validateUserData(login, password);
		}
		catch (InvalidRequestDataException e) {
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\"}");
			return;
		}
		catch(Exception e){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"server error\"}");
			return;
		}
		String owner = req.getParameter("owner");
		String repository = req.getParameter("repo");
		String filename =  req.getParameter("filename");
		String version = req.getParameter("version");
		RepoHandler handler;
		try {
			handler = FileManager.getInstance().getHandler(owner, repository);
		}
		catch (InvalidRequestDataException e) {
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\"}");
			return;
		}
		catch(Exception e){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"server error\"}");
			return;
		}
		if (!UserMetaData.getInstance().hasAccess(login, handler)){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"access denied\"}");
			return;
		}
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_OK);
		File response;
		try {
			response = handler.findFile(filename, version);
		}
		catch (InvalidRequestDataException e) {
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\"}");
			return;
		}
		catch(Exception e){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"server error\"}");
			return;
		}

		InputStream input = new FileInputStream(response);
		OutputStream out = resp.getOutputStream();

		int len = 0;
		byte[] buf = new byte[SIZE];
		while ((len = input.read(buf)) > 0) {
			out.write(buf, 0, len);
		}


	}

}
