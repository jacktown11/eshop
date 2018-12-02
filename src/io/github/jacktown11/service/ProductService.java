package io.github.jacktown11.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.github.jacktown11.dao.ProductDao;
import io.github.jacktown11.domain.Category;
import io.github.jacktown11.domain.Order;
import io.github.jacktown11.domain.OrderItem;
import io.github.jacktown11.domain.PageBean;
import io.github.jacktown11.domain.Product;
import io.github.jacktown11.domain.User;
import io.github.jacktown11.utils.C3P0Utils;

public class ProductService {
	ProductDao dao = new ProductDao();
	
	public List<Product> getHotProductList(int i) {
		List<Product> hotProducts = null;
		try {
			hotProducts = dao.getHotProductList(i);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hotProducts;
	}

	public List<Product> getNewProductList(int i) {
		List<Product> newProducts = null;
		try {
			newProducts = dao.getNewProductList(i);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newProducts;
	}

	public List<Category> getCategoryList() {
		List<Category> categories = null;
		try {
			categories = dao.getCategoriyList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	public PageBean<Product> getProductListByCid(String cid, int currentSlide) {
		PageBean<Product> bean = new PageBean<>();
		// set currentCount and currentSlide
		int currentCount = 12;
		bean.setCurrentCount(currentCount);
		bean.setCurrentSlide(currentSlide);
		
		// get total count and cal total slide
		int totalCount = 0;
		try {
			totalCount = dao.getTotalCountByCid(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		bean.setTotalCount(totalCount);
		int totalSlide = (int) Math.ceil(1.0 * totalCount/currentCount);
		bean.setTotalSlide(totalSlide);
		
		// get product list according to cid and current page info
		int startIndex = (currentSlide-1)*currentCount; // not included
		List<Product> items = null;
		try {
			items = dao.getProductListByCid(cid, startIndex, currentCount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		bean.setItemList(items);
		
		return bean;
	}

	public Product getProductById(String pid) {
		Product prod = null;
		try {
			prod = dao.getProductByPid(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prod;
	}

	public List<Product> getProductListByPids(List<String> pidList) {
		List<Product> productList = null;
		try {
			productList = dao.getProductByPids(pidList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return productList;
	}

	public boolean addOrder(Order order) {
		// if order is empty
		if(order == null || order.getOrderItems().size() == 0) return false;
		
		try {
			C3P0Utils.startTransaction();
			// insert order into 'orders' table
			int row = dao.addOrder(order);
			if(row == 0) {
				C3P0Utils.rollback();
				return false;
			}
			
			// insert all order items into 'orderitem' table
			for(OrderItem orderItem : order.getOrderItems()) {
				row = dao.addOrderItem(orderItem);			
				if(row == 0) {
					C3P0Utils.rollback();
					return false;
				}
			}
		} catch (SQLException e) {
			try {
				C3P0Utils.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return false;
		} finally{
			try {
				C3P0Utils.commitAndRelease();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;		
	}

	public boolean updateOrderReceiver(Order order) {
		int row = 0;
		try {
			row = dao.updateOrderReceiver(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return row > 0;
	}

	public int changeStateToPayed(String oid) {
		int row = 0;
		try {
			row = dao.changeStateToPayed(oid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return row;
	}

	public List<Order> getOrderListByUser(User user) {
		List<Order> orderList = null;
		List<OrderItem> orderItems = null;
		try {
			orderList = dao.getOrderListByUid(user.getUid());
			if(orderList != null) {
				for(Order order : orderList) {
					orderItems = dao.getOrderItemsByOid(order.getOid());
					order.setOrderItems(orderItems);
				}
			}
		} catch (SQLException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return orderList;
	}

}
