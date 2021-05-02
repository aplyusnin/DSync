package ru.nsu.fit.dsync.server.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.nsu.fit.dsync.server.storage.DirHandler;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.UserMetaData;

import java.io.IOException;


@WebServlet("/HashGetter")
public class VersionControlServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String login = req.getParameter("login");
			String password = req.getParameter("password");;
			UserMetaData.getInstance().validateUserData(login, password);
			String repository = req.getParameter("repo");
			String filename =  req.getParameter("filename");
			DirHandler handler = FileManager.getInstance().getHandler(login, repository);

			String version = handler.getLastVersion(filename);
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"version\" : \"" + version + "\"}");
		}
		catch (Exception e){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println(e.getMessage());
		}
	}

}
