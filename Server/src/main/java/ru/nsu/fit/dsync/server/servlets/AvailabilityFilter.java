package ru.nsu.fit.dsync.server.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.RepoHandler;
import ru.nsu.fit.dsync.server.storage.UserMetaData;

import java.io.IOException;

public class AvailabilityFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		String user = (String) req.getAttribute("user");

		String repo = req.getParameter("repo");
		String owner = req.getParameter("owner");

		if (repo == null || owner == null) {

			((HttpServletResponse)response).setStatus(400);
		}

		RepoHandler handler = null;
		try {
			handler = FileManager.getInstance().getHandler(owner, repo);
		}
		catch (Exception e) {
			((HttpServletResponse)response).setStatus(400);
		}
		if (!UserMetaData.getInstance().hasAccess(user, handler)) {
			((HttpServletResponse)response).setStatus(401);
		}
		chain.doFilter(request, response);
	}

}
