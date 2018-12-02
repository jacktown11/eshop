package io.github.jacktown11.web;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings(value = { "all" })
public class BaseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		
		Class cls = this.getClass();
		String methodName = req.getParameter("method");
		System.out.println(methodName);
		try {
			Method m = cls.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
			m.invoke(this, req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
