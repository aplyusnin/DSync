package ru.nsu.fit.dsync.server.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import ru.nsu.fit.dsync.server.storage.UserMetaData;

import java.io.IOException;
import java.net.http.HttpRequest;

public class AuthenticationFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest)request;
		//String token = req.getParameter("X-Access-Token");
		String token = req.getHeader("X-Access-Token");
		if (token == null){
			((HttpServletResponse)response).setStatus(401);
		}
		else {

			if (!UserMetaData.getInstance().isTokenExist(token))
			{
				((HttpServletResponse)response).setStatus(401);
			}

			String user = UserMetaData.getInstance().getUserByToken(token);
			req.setAttribute("user", user);
			chain.doFilter(request, response);
		}
	}

}
