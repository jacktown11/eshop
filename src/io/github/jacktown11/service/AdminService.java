package io.github.jacktown11.service;

import java.sql.SQLException;
import java.util.List;

import io.github.jacktown11.dao.AdminDao;
import io.github.jacktown11.domain.Category;
import io.github.jacktown11.domain.Order;
import io.github.jacktown11.domain.OrderItem;
import io.github.jacktown11.domain.Product;

public class AdminService {
	private AdminDao dao = new AdminDao();
	public List<Category> getCategoryList() {
		List<Category> categoryList = null;
		try {
			categoryList = dao.getCategoryList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categoryList;
	}
	public Category getCategoryByCid(String cid) {
		Category category = null;
		try {
			category = dao.getCategoryByCid(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return category;
	}
	public int updateCategory(Category category) {
		int rows = 0;
		try {
			rows = dao.updateCategory(category);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}
	public int deleteCategoryByCid(String cid) {
		int rows = 0;
		try {
			rows = dao.deleteCategoryByCid(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}
	public int addCategory(Category category) {
		int rows = 0;
		try {
			rows = dao.addCategory(category);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}
	public int addProduct(Product product){
		int rows = 0;
		try {
			rows = dao.addProduct(product);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}
	public List<Product> getProductList() {
		List<Product> productList = null;
		try {
			productList = dao.getProductList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return productList;
	}
	public int deleteProductByPid(String pid) {
		int rows = 0;
		try {
			rows = dao.deleteProductByPid(pid);			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	public Product getProductByPid(String pid) {
		Product product = null;
		try {			
			product = dao.getProductByPid(pid);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return product;
	}
	public int updateProduct(Product product) {
		int rows = 0;
		try {
			rows = dao.updateProduct(product);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}
	public List<Order> getOrderList() {
		List<Order> orderList = null;
		try {
			orderList = dao.getOrderList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orderList;
	}
	public List<OrderItem> getOrderItemListByOid(String oid) {
		List<OrderItem> orderItemList = null;
		try {
			orderItemList = dao.getOrderItemListByOid(oid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orderItemList;
	}

}
