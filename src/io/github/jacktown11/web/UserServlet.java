package io.github.jacktown11.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import io.github.jacktown11.domain.User;
import io.github.jacktown11.service.UserService;
import io.github.jacktown11.utils.CommonUtils;
import io.github.jacktown11.utils.MailUtils;

public class UserServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	private UserService userService = new UserService();

	public void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// get form data
		request.setCharacterEncoding("UTF-8");

		Map<String, String[]> params = request.getParameterMap();
		User u = new User();
		ConvertUtils.register(new Converter() {
			@Override
			public Object convert(Class clazz, Object dateStr) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				try {
					date = format.parse((String) dateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return date;
			}
		}, Date.class);

		try {
			BeanUtils.populate(u, params);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		// fill user data not in the form
		u.setUid(CommonUtils.getUUID());
		u.setTelephone(null);
		u.setState(0);
		u.setCode(CommonUtils.getUUID());

		// check validate code
		String validateCode = request.getParameter("validate-code");
		HttpSession session = request.getSession();
		boolean isCodeValid = validateCode != null && validateCode.equals(session.getAttribute("checkcode_session"));

		boolean isSuccess = isCodeValid;
		if (isSuccess) {
			// if the validate code is right
			// try to register into database
			try {
				isSuccess = userService.registerUser(u);
			} catch (SQLException e) {
				isSuccess = false;
				e.printStackTrace();
			}
		}

		if (isSuccess) {
			// send activation email
			String url = "http://localhost:8080" + request.getContextPath() + "/activation?code=" + u.getCode();
			String emailMsg = u.getName() + "恭喜您注册成功，请点击一下链接激活：<a href='" + url + "'>" + url + "</a>";
			try {
				MailUtils.sendMail("lucy@jacktown.com", emailMsg);
			} catch (MessagingException e) {
				e.printStackTrace();
			}

			// redirect to log in succeed page
			request.setAttribute("user", u);
			response.sendRedirect(request.getContextPath() + "/registerSucceed.jsp");
			System.out.println("注册成功");
		} else {
			response.sendRedirect(request.getContextPath() + "/registerFail.jsp");
			System.out.println("注册失败");
		}
	}

	public void activation(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String code = request.getParameter("code");
		boolean isSuccess = false;
		try {
			isSuccess = new UserService().activateByCode(code);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (isSuccess) {
			// succeed to activation
			// redirect to home page
			response.sendRedirect(request.getContextPath() + "/index.jsp");
		} else {
			// activation fail
			// redirect to register page
			response.sendRedirect(request.getContextPath() + "/register.jsp");
		}
	}

	public void checkUsername(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String username = request.getParameter("username");
		boolean isOccupied = false;
		try {
			isOccupied = new UserService().checkUsername(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String json = "{\"isOccupied\": " + isOccupied + "}";
		response.getWriter().write(json);
	}
	
	 public void checkValidateCode(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String validateCode = request.getParameter("validateCode");
		HttpSession session = request.getSession();
		boolean isRight = validateCode != null && 
			validateCode.equals(session.getAttribute("checkcode_session"));
		String json =  "{\"isRight\": "+isRight+"}";
		response.getWriter().write(json);
	}
	 
	public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get params
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String checkCode = request.getParameter("checkCode");
		String autoLogin = request.getParameter("autoLogin");
		
		// check checkCode	
		HttpSession session = request.getSession();
		boolean isRightCheckCode = checkCode != null && checkCode.equals(session.getAttribute("checkcode_session"));
		if(!isRightCheckCode) {
			// wrong checkCode
			request.setAttribute("error", "验证码错误");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return ;
		}
		
		// validate login params
		User u = userService.getUserByUsernameAndPassword(username, password);
		if(u == null) {
			// wrong username or password
			request.setAttribute("error", "用户名或密码错误");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return ;			
		}
		
		// whether autoLogin
		if(autoLogin != null && autoLogin.equals("autoLogin")) {
			// auto login 
			Cookie usernameCk = new Cookie("username", username);
			usernameCk.setPath("/");
			usernameCk.setMaxAge(60*60*24 * 7); // auto login in 7 days
			Cookie passwordCk = new Cookie("password", password);
			passwordCk.setPath("/");
			passwordCk.setMaxAge(60*60*24 * 7); // auto login in 7 days
			response.addCookie(usernameCk);
			response.addCookie(passwordCk);
		}else {
			// not auto login
			// delete cookie by setting maxAge to 0
			Cookie usernameCk = new Cookie("username", "");
			usernameCk.setPath("/");
			usernameCk.setMaxAge(0); 
			Cookie passwordCk = new Cookie("password", "");
			passwordCk.setPath("/");
			passwordCk.setMaxAge(0); 
			response.addCookie(usernameCk);
			response.addCookie(passwordCk);
		}
		
		// login succeed
		session.setAttribute("user", u);
		response.sendRedirect(request.getContextPath() + "/product?method=home");
	}
	
	public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().removeAttribute("user");
		Cookie[] cks = request.getCookies();
		if(cks != null) {
			for(Cookie ck : cks) {
				String name = ck.getName();
				if("username".equals(name) || "password".equals(name)) {
					ck.setPath("/");
					ck.setMaxAge(0);
					response.addCookie(ck);
				}
			}
		}
		response.sendRedirect(request.getContextPath() + "/product?method=home");
	}
	
}