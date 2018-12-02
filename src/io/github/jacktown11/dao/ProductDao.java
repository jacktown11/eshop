package io.github.jacktown11.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import io.github.jacktown11.domain.Category;
import io.github.jacktown11.domain.Order;
import io.github.jacktown11.domain.OrderItem;
import io.github.jacktown11.domain.Product;
import io.github.jacktown11.utils.C3P0Utils;

public class ProductDao {
	private QueryRunner qr = new QueryRunner(C3P0Utils.getDataSource());

	public List<Product> getHotProductList(int i) throws SQLException {
		String sql = "SELECT * FROM product WHERE is_hot=? LIMIT 0,?";
		return qr.query(sql, new BeanListHandler<Product>(Product.class), 1, i);
	}

	public List<Product> getNewProductList(int i) throws SQLException {
		String sql = "SELECT * FROM product ORDER BY pdate DESC LIMIT 0, ?";
		return qr.query(sql, new BeanListHandler<Product>(Product.class), i);
	}

	public List<Category> getCategoriyList() throws SQLException {
		String sql = "SELECT * FROM category";
		return qr.query(sql, new BeanListHandler<Category>(Category.class));
		
	}

	public int getTotalCountByCid(String cid) throws SQLException {
		String sql = "SELECT COUNT(*) FROM product WHERE cid=?";
		Long count = (Long) qr.query(sql, new ScalarHandler(), cid);
		return count.intValue();
	}

	public List<Product> getProductListByCid(String cid, int startIndex, int count) throws SQLException {
		String sql = "SELECT * FROM product WHERE cid=? LIMIT ?,?";
		List<Product> productList = qr.query(sql, new BeanListHandler<Product>(Product.class), cid, startIndex, count);
		return productList;
	}

	public Product getProductByPid(String pid) throws SQLException {
		String sql = "SELECT * FROM product WHERE pid=?";
		return qr.query(sql, new BeanHandler<Product>(Product.class), pid);
	}

	public List<Product> getProductByPids(List<String> pidList) throws SQLException {
		int size = pidList.size();
		if(size == 0) return null;
		
		StringBuffer sqlBuf = new StringBuffer("SELECT * FROM product WHERE pid IN (?");
		for(int i = 1; i < size; i++) {
			sqlBuf.append(",?");
		}
		sqlBuf.append(")");
		String sql = sqlBuf.toString();
		
		return qr.query(sql, new BeanListHandler<Product>(Product.class), pidList.toArray());
	}

	public int addOrder(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner();
		Connection conn = C3P0Utils.getConnection();
		String sql = "INSERT INTO orders VALUES(?,?,?,?, ?,?,?,?)";
		return runner.update(conn, sql, order.getOid(), new Date(order.getOrdertime().getTime()), 
				order.getTotal(), order.getState(),
				order.getAddress(), order.getName(), 
				order.getTelephone(), order.getUser().getUid());
	}

	public int addOrderItem(OrderItem orderItem) throws SQLException {
		QueryRunner runner = new QueryRunner();
		Connection conn = C3P0Utils.getConnection();
		String sql = "INSERT INTO orderitem VALUES(?,?,?, ?,?)";
		return runner.update(conn,sql, orderItem.getItemid(), orderItem.getCount(), 
				orderItem.getSubtotal(), orderItem.getProduct().getPid(), 
				orderItem.getOrder().getOid());
	}

	public int updateOrderReceiver(Order order) throws SQLException {
		String sql = "UPDATE orders SET name=?, telephone=?, address=? WHERE oid=?";
		return qr.update(sql, order.getName(), order.getTelephone(), 
				order.getAddress(), order.getOid());
	}

	public int changeStateToPayed(String oid) throws SQLException {
		String sql = "UPDATE orders SET state=? WHERE oid=?";
		int row = qr.update(sql, 1, oid);
		return row;
	}

	public List<Order> getOrderListByUid(String uid) throws SQLException {
		String sql = "SELECT * FROM orders WHERE uid=?";
		return qr.query(sql, new BeanListHandler<Order>(Order.class), uid);
	}

	public List<OrderItem> getOrderItemsByOid(String oid) throws SQLException, IllegalAccessException, InvocationTargetException {
		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
		String sql = "SELECT * FROM orderitem AS i, product AS p WHERE oid=? AND i.pid=p.pid";
		List<Map<String, Object>> mapList = qr.query(sql, new MapListHandler(), oid);
		for(Map<String, Object> map : mapList) {
			OrderItem orderItem = new OrderItem();
			Product prod = new Product();
			BeanUtils.populate(orderItem, map);
			BeanUtils.populate(prod, map);
			orderItem.setProduct(prod);
			orderItemList.add(orderItem);
		}
		return orderItemList;
	}
}
