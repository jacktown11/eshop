package io.github.jacktown11.service;

import java.sql.SQLException;

import io.github.jacktown11.dao.UserDao;
import io.github.jacktown11.domain.User;

public class UserService {
	UserDao dao = new UserDao();
	
	public boolean registerUser(User u) throws SQLException {
		return dao.registerUser(u) > 0;
	}

	public boolean activateByCode(String code) throws SQLException {
		int rows = new UserDao().activateByCode(code);
		return rows > 0;
	}

	public boolean checkUsername(String username) throws SQLException {
		int count = dao.checkUsername(username);
		return count > 0;
	}

	public User getUserByUsernameAndPassword(String username, String password) {
		User u = null;
		try {
			u = dao.getUserByUsernameAndPassword(username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return u;
	}

}
