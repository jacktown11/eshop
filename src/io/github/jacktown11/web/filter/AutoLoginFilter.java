package io.github.jacktown11.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import io.github.jacktown11.domain.User;
import io.github.jacktown11.service.UserService;

public class AutoLoginFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// set utf-8 encoding by the way
		request.setCharacterEncoding("UTF-8");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		
		// if not loged in test if auto login
		if(session.getAttribute("user") == null) {
			
			// get username and password cookie
			Cookie[] cks = req.getCookies();
			String username = null;
			String password = null;
			if(cks != null) {			
				for(Cookie ck : cks) {
					if("username".equals(ck.getName())) {
						username = ck.getValue();
					}
					if("password".equals(ck.getName())) {
						password = ck.getValue();
					}
				}
			}
			
			// check username and password in cookie 
			if(username != null && password != null) {			
				User user = new UserService().getUserByUsernameAndPassword(username, password);
				if(user != null) {
					// pass check, auto login
					session.setAttribute("user", user);
				}
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}


}
