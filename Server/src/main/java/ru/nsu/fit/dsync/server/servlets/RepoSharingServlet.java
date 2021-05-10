package ru.nsu.fit.dsync.server.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import ru.nsu.fit.dsync.server.sockets.ConnectionManager;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.RepoHandler;
import ru.nsu.fit.dsync.server.storage.UserMetaData;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;

import java.io.*;

@WebServlet("/FileUploader")
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
		maxFileSize = 1024 * 1024 * 5,
		maxRequestSize = 1024 * 1024 * 5 * 5)
public class RepoSharingServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\n"  + e.getStackTrace().toString() + "\n\"}");
			return;
		}

		String repository = req.getParameter("repo");
		String user = req.getParameter("user");

		if (!UserMetaData.getInstance().isUserExists(user)){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"user doesn't exist\"}");
			return;
		}

		RepoHandler handler;

		try{
			handler =  FileManager.getInstance().getHandler(login, repository);
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
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\n"  + e.getStackTrace().toString() + "\n\"}");
			return;
		}

		UserMetaData.getInstance().addAccess(user, handler);

		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println("{ \"status\": \"success\"}");
	}

}
