package io.github.jacktown11.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import io.github.jacktown11.domain.Category;
import io.github.jacktown11.domain.Order;
import io.github.jacktown11.domain.OrderItem;
import io.github.jacktown11.domain.Product;
import io.github.jacktown11.utils.C3P0Utils;

public class AdminDao {
	QueryRunner qr = new QueryRunner(C3P0Utils.getDataSource());
	
	public List<Category> getCategoryList() throws SQLException {
		String sql = "SELECT * FROM category";
		return qr.query(sql, new BeanListHandler<Category>(Category.class));
	}

	public Category getCategoryByCid(String cid) throws SQLException {
		String sql = "SELECT * FROM category WHERE cid=?";
		return qr.query(sql, new BeanHandler<Category>(Category.class), cid);
	}

	public int updateCategory(Category category) throws SQLException {
		String sql = "UPDATE category SET cname=? WHERE cid=?";
		return qr.update(sql, category.getCname(), category.getCid());
	}

	public int deleteCategoryByCid(String cid) throws SQLException {
		String sql = "DELETE FROM category WHERE cid=?";
		return qr.update(sql, cid);
	}

	public int addCategory(Category category) throws SQLException {
		String sql = "INSERT INTO category VALUES (?, ?)";
		return qr.update(sql, category.getCid(), category.getCname());
	}

	public int addProduct(Product product) throws SQLException {
		String sql = "INSERT INTO product VALUES (?,?, ?,?, ?,?, ?,?, ?,?)";
		System.out.println(product.getPdate());
		return qr.update(sql, product.getPid(), product.getPname(), 
				product.getMarket_price(), product.getShop_price(), 
				product.getPimage(), new SimpleDateFormat("yyyy-MM-dd").format(product.getPdate()), 
				product.getIs_hot(), product.getPdesc(), 
				product.getPflag(), product.getCategory().getCid());
	}

	public List<Product> getProductList() throws SQLException {
		String sql = "SELECT * FROM product as p, category as c WHERE p.cid=c.cid";
		List<Map<String, Object>> mapList = qr.query(sql, new MapListHandler());
		List<Product> productList = null;
		if(mapList != null) {
			productList = new ArrayList<Product>();
			for(Map<String,Object> row : mapList) {
				Product product = new Product();
				Category category = new Category();
				try {
					BeanUtils.populate(product, row);
					BeanUtils.populate(category, row);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
				product.setCategory(category);
				productList.add(product);
			}
		}
		return productList;
	}

	public int deleteProductByPid(String pid) throws SQLException {
		String sql = "DELETE FROM product WHERE pid=?";
		return qr.update(sql, pid);
	}
	
	public Product getProductByPid(String pid) throws SQLException {
		String sql = "SELECT * FROM product as p, category as c WHERE p.cid=c.cid AND pid=?";
		Map<String, Object> map= qr.query(sql, new MapHandler(), pid);
		Product product = null;
		if(map != null) {
			try {
				product = new Product();
				BeanUtils.populate(product, map);
				Category category = new Category();
				BeanUtils.populate(category, map);
				product.setCategory(category);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return product;
	}

	public int updateProduct(Product product) throws SQLException{
		String sql = "UPDATE product SET pname=?, is_hot=?, market_price=?, shop_price=?, pimage=?, cid=?, pdesc=? WHERE pid=?";
		return qr.update(sql, product.getPname(), product.getIs_hot(), 
				product.getMarket_price(), product.getShop_price(), 
				product.getPimage(), product.getCategory().getCid(), 
				product.getPdesc(), product.getPid());
	}

	public List<Order> getOrderList() throws SQLException {
		String sql = "SELECT * FROM orders";
		return qr.query(sql, new BeanListHandler<Order>(Order.class));
	}

	public List<OrderItem> getOrderItemListByOid(String oid) throws SQLException {
		String sql = "SELECT * FROM orderitem AS o, product as p WHERE p.pid=o.pid AND oid=?";
		List<Map<String, Object>> mapList = qr.query(sql, new MapListHandler(), oid);
		
		List<OrderItem> orderItemList = null;
		if(mapList != null) {
			orderItemList = new ArrayList<OrderItem>();
			for(Map<String, Object> map : mapList) {
				OrderItem orderItem = new OrderItem();
				Product product = new Product();
				try {
					BeanUtils.populate(orderItem, map);
					BeanUtils.populate(product, map);
					orderItem.setProduct(product);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
				orderItemList.add(orderItem);
			}
		}
		return orderItemList;
	}
	
}
