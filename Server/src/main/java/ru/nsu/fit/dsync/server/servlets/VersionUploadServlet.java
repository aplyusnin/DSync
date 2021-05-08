package ru.nsu.fit.dsync.server.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import ru.nsu.fit.dsync.server.storage.RepoHandler;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.UserMetaData;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;

import java.io.*;

@WebServlet("/FileUploader")
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
		maxFileSize = 1024 * 1024 * 5,
		maxRequestSize = 1024 * 1024 * 5 * 5)
public class VersionUploadServlet extends HttpServlet {

	private String getFileName(Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename"))
				return content.substring(content.indexOf("=") + 2, content.length() - 1);
		}
		return "tempName.dat";
	}


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
		String filename = req.getParameter("filename");

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

		File temp;
		Part p = req.getParts().iterator().next();
		try
		{
			temp = handler.getTemp();

			InputStream input = p.getInputStream();
			OutputStream output = new FileOutputStream(temp);

			byte[] buffer = new byte[1024];
			int read = 0;

			while ((read = input.read(buffer)) > 0)
			{
				output.write(buffer, 0, read);
			}

			output.close();
		}
		catch (InvalidRequestDataException e) {
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\"}");
			handler.releaseHandler();
			return;
		}
		catch(Exception e){
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\n"  + e.getStackTrace().toString() + "\n\"}");
			handler.releaseHandler();
			return;
		}

		try {
			String hash = FileManager.getInstance().createVersion(temp, login, repository, filename, getFileName(p));
			handler.rewriteVersionEntry(filename, hash);
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"version\": \"" + hash + "\"}");
		}
		catch (Exception e) {
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("{ \"error\": \"" + e.getMessage() + "\n"  + e.getStackTrace().toString() + "\n\"}");
		}
		handler.releaseHandler();
	}

}
