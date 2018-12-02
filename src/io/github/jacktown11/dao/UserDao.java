package io.github.jacktown11.dao;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import io.github.jacktown11.domain.User;
import io.github.jacktown11.utils.C3P0Utils;

public class UserDao {
	private QueryRunner qr = new QueryRunner(C3P0Utils.getDataSource());

	public int registerUser(User u) throws SQLException {
		String sql = "INSERT INTO user VALUES (?,?,?, ?,?,?, ?,?,?, ?)";
		int rows = qr.update(sql, u.getUid(), u.getUsername(), u.getPassword(), 
				u.getName(), u.getEmail(), u.getTelephone(), 
				u.getBirthday(), u.getSex(), u.getState(), 
				u.getCode());
		return rows;
	}

	public int activateByCode(String code) throws SQLException {
		String sql = "UPDATE user SET state=? WHERE code=?";
		return qr.update(sql, 1, code);
	}

	public int checkUsername(String username) throws SQLException {
		String sql = "SELECT COUNT(*) FROM user WHERE username=?";
		Long count = (Long) qr.query(sql, new ScalarHandler(), username);
		return count.intValue();
	}

	public User getUserByUsernameAndPassword(String username, String password) throws SQLException {
		String sql = "SELECT * FROM user WHERE username=? AND password=?";
		return qr.query(sql, new BeanHandler<User>(User.class), username, password);
	}

}
