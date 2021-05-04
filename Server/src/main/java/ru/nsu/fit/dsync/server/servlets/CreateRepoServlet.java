package ru.nsu.fit.dsync.server.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.nsu.fit.dsync.server.storage.DirHandler;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.UserMetaData;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;

import java.io.IOException;

@WebServlet("/RepoCreation")
public class CreateRepoServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String login = req.getParameter("login");
		String password = req.getParameter("password");
		try {
			UserMetaData.getInstance().validateUserData(login, password);
		}
		catch (InvalidRequestDataException e)
		{
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

		String repository = req.getParameter("repo");

		if (!UserMetaData.getInstance().isUserExists(login)){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"User doesn't exist\"}");
			return;
		}

		try {
			UserMetaData.getInstance().createRepo(login, repository);
		}
		catch (InvalidRequestDataException e)
		{
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
		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println("{ \"status\": \"success\"}");
	}
}
