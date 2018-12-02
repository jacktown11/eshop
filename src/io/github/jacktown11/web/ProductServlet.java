package io.github.jacktown11.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.Gson;

import io.github.jacktown11.domain.Cart;
import io.github.jacktown11.domain.CartItem;
import io.github.jacktown11.domain.Category;
import io.github.jacktown11.domain.Order;
import io.github.jacktown11.domain.OrderItem;
import io.github.jacktown11.domain.PageBean;
import io.github.jacktown11.domain.Product;
import io.github.jacktown11.domain.User;
import io.github.jacktown11.service.ProductService;
import io.github.jacktown11.utils.CommonUtils;
import io.github.jacktown11.utils.JedisPoolUtils;
import io.github.jacktown11.utils.PaymentUtil;
import redis.clients.jedis.Jedis;

public class ProductServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	private ProductService productService = new ProductService();

	public void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get hot and newest products
		List<Product> hotProducts = productService.getHotProductList(9);
		List<Product> newProducts = productService.getNewProductList(9);
		request.setAttribute("hotProducts", hotProducts);
		request.setAttribute("newProducts", newProducts);

		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

	public void categoryList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// get category with redis
		Jedis jedis = JedisPoolUtils.getJedis();
//		String categoryListJson = jedis.get("categoryListJson");
		String categoryListJson = null;
		if (categoryListJson == null) {
			// no data in redis
			System.out.println("no categoryListJson in redis, get from db");
			List<Category> categoryList = productService.getCategoryList();
			Gson gson = new Gson();
			categoryListJson = gson.toJson(categoryList);
			jedis.set("categoryListJson", categoryListJson);
		}

		// send categoryList with json dataType
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().write(categoryListJson);
	}

	public void productList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// get parameters
		String cid = request.getParameter("cid");
		String slideStr = request.getParameter("slide");
		// default currentSlide is 1 if no slide parameter is passed
		int currentSlide = slideStr != null ? Integer.parseInt(slideStr) : 1;

		// get all visited products according to cookie
		LinkedList<String> pidList = new LinkedList<>();
		Cookie[] cks = request.getCookies();
		if (cks != null) {
			for (Cookie ck : cks) {
				if ("pids".equals(ck.getName())) {
					pidList = new LinkedList<>(Arrays.asList(ck.getValue().split(",")));
				}
			}
		}
		int showCount = pidList.size() > 7 ? 7 : pidList.size();
		List<Product> visitedList = productService.getProductListByPids(pidList.subList(0, showCount));
		request.setAttribute("visitedList", visitedList);

		// get all products according to cid
		PageBean<Product> pageBean = productService.getProductListByCid(cid, currentSlide);
		request.setAttribute("pageBean", pageBean);
		request.setAttribute("cid", cid);
		request.getRequestDispatcher("/product_list.jsp").forward(request, response);
	}

	public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cid = request.getParameter("cid");
		String currentSlide = request.getParameter("currentSlide");
		request.setAttribute("cid", cid);
		request.setAttribute("currentSlide", currentSlide);

		String pid = request.getParameter("pid");
		Product product = productService.getProductById(pid);
		request.setAttribute("product", product);

		// insert pid into visited products' cookie
		LinkedList<String> pidList = new LinkedList<>();
		Cookie[] cks = request.getCookies();
		if (cks != null) {
			for (Cookie ck : cks) {
				if ("pids".equals(ck.getName())) {
					String[] pidArr = ck.getValue().split(",");
					pidList = new LinkedList<>(Arrays.asList(pidArr));
					if (pidList.contains(pid))
						pidList.remove(pid);
				}
			}
		}
		pidList.addFirst(pid);
		StringBuffer pidStrBuf = new StringBuffer();
		for (String pidItem : pidList) {
			pidStrBuf.append(",");
			pidStrBuf.append(pidItem);
		}
		String pidStr = pidStrBuf.substring(1);
		Cookie pidCk = new Cookie("pids", pidStr);
		response.addCookie(pidCk);

		// dispatch request
		request.getRequestDispatcher("/product_info.jsp").forward(request, response);
	}
	
	public void addToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get params
		String pid = request.getParameter("pid");
		int quantity = Integer.parseInt(request.getParameter("quantity"));
		
		// get cart from session
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart == null) cart = new Cart();
		
		// add to cart
		cart.add(pid, quantity);
		session.setAttribute("cart", cart);
		
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	}	
	
	public void updateCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get params and validate simply
		String pid= request.getParameter("pid");
		String quantityStr = request.getParameter("quantity");
		if(pid == null || quantityStr == null) {			
			response.sendRedirect(request.getContextPath()+"/cart.jsp");
			return ;
		}
		int quantity = Integer.parseInt(quantityStr);
		
		// update cart
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart == null) cart = new Cart();
		cart.update(pid, quantity);
		session.setAttribute("cart", cart);
		
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	}
	
	public void deleteFromCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pid = request.getParameter("pid");
		if(pid != null) {
			HttpSession session = request.getSession();
			Cart cart = (Cart) session.getAttribute("cart");
			if(cart == null) cart = new Cart();
			
			cart.delete(pid);
			session.setAttribute("cart", cart);
		}
		
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	}
	public void clearCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.setAttribute("cart", new Cart());
		
		response.sendRedirect(request.getContextPath()+"/cart.jsp");
	}
	public void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get cart bean in session
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		
		if(cart != null && cart.getContents().size()>0) {
			// do have some orderItem in the cart
			
			// check for login status
			User user = (User) session.getAttribute("user");
			if(user == null) {
				// not login
				response.sendRedirect(request.getContextPath() + "/login.jsp");
				return ;
			}
			
			// prepare order bean from cart bean
			Order order = new Order();
			order.setOid(CommonUtils.getUUID());
			order.setOrdertime(new Date());
			order.setState(0);// not payed			
			order.setTotal(cart.getTotal());
			order.setUser(user);
			order.setTelephone(null);
			order.setName(null);
			order.setAddress(null);
			
			// prepare order's orderItems field
			Map<String, CartItem> contents = cart.getContents();
			List<OrderItem> orderItems = new ArrayList<>(); 
			for(Map.Entry<String, CartItem> entry : contents.entrySet()) {
				// package all cartItem bean into orderItem bean
				// and save in orderItems List
				OrderItem orderItem = new OrderItem();
				CartItem cartItem = entry.getValue();
				orderItem.setItemid(CommonUtils.getUUID());
				orderItem.setCount(cartItem.getQuantity());
				orderItem.setProduct(cartItem.getProduct());
				orderItem.setSubtotal(cartItem.getSubTotal());
				orderItem.setOrder(order);
				
				orderItems.add(orderItem);
			}
			order.setOrderItems(orderItems);
			
			// insert the order bean into database table order and orderitem
			boolean isSucceed = productService.addOrder(order);
			if(isSucceed) {
				// clear cart
				System.out.println("添加订单成功");
				session.removeAttribute("cart");
				session.setAttribute("order", order);
				response.sendRedirect(request.getContextPath() + "/order_info.jsp");
			}else {
				System.out.println("添加订单失败");
				response.sendRedirect(request.getContextPath() + "/cart.jsp");
			}
		}else {
			// no order item in the cart, just turn back to cart.jsp
			response.sendRedirect(request.getContextPath() + "/cart.jsp");
		}		
	}
	
	public void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get receiver's info
		Map<String, String[]> params = request.getParameterMap();
		Order order = new Order();
		try {
			BeanUtils.populate(order, params);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		// update the receiver's info into db
		boolean isSucceed = productService.updateOrderReceiver(order);
		
		// redirect to pay page
		
		// 获得 支付必须基本数据
		String orderid = request.getParameter("oid");
		String money = request.getParameter("total");
		// String money = "0.01";
		// 银行
		String pd_FrpId = request.getParameter("pd_FrpId");

		// 发给支付公司需要哪些数据
		String p0_Cmd = "Buy";
		String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
		String p2_Order = orderid;
		String p3_Amt = money;
		String p4_Cur = "CNY";
		String p5_Pid = "";
		String p6_Pcat = "";
		String p7_Pdesc = "";
		// 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
		// 第三方支付可以访问网址
		String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
		String p9_SAF = "";
		String pa_MP = "";
		String pr_NeedResponse = "1";
		// 加密hmac 需要密钥
		String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
				"keyValue");
		String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
				p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
				pd_FrpId, pr_NeedResponse, keyValue);
		
		
		String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId="+pd_FrpId+
						"&p0_Cmd="+p0_Cmd+
						"&p1_MerId="+p1_MerId+
						"&p2_Order="+p2_Order+
						"&p3_Amt="+p3_Amt+
						"&p4_Cur="+p4_Cur+
						"&p5_Pid="+p5_Pid+
						"&p6_Pcat="+p6_Pcat+
						"&p7_Pdesc="+p7_Pdesc+
						"&p8_Url="+p8_Url+
						"&p9_SAF="+p9_SAF+
						"&pa_MP="+pa_MP+
						"&pr_NeedResponse="+pr_NeedResponse+
						"&hmac="+hmac;

		//重定向到第三方支付平台
		response.sendRedirect(url);
		
	}

	public void payCallback(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 获得回调所有数据
		String p1_MerId = request.getParameter("p1_MerId");
		String r0_Cmd = request.getParameter("r0_Cmd");
		String r1_Code = request.getParameter("r1_Code");
		String r2_TrxId = request.getParameter("r2_TrxId");
		String r3_Amt = request.getParameter("r3_Amt");
		String r4_Cur = request.getParameter("r4_Cur");
		String r5_Pid = request.getParameter("r5_Pid");
		String r6_Order = request.getParameter("r6_Order");
		String r7_Uid = request.getParameter("r7_Uid");
		String r8_MP = request.getParameter("r8_MP");
		String r9_BType = request.getParameter("r9_BType");
		String rb_BankId = request.getParameter("rb_BankId");
		String ro_BankOrderId = request.getParameter("ro_BankOrderId");
		String rp_PayDate = request.getParameter("rp_PayDate");
		String rq_CardNo = request.getParameter("rq_CardNo");
		String ru_Trxtime = request.getParameter("ru_Trxtime");
		// 身份校验 --- 判断是不是支付公司通知你
		String hmac = request.getParameter("hmac");
		String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
				"keyValue");

		// 自己对上面数据进行加密 --- 比较支付公司发过来hamc
		boolean isValid = PaymentUtil.verifyCallback(hmac, p1_MerId, r0_Cmd,
				r1_Code, r2_TrxId, r3_Amt, r4_Cur, r5_Pid, r6_Order, r7_Uid,
				r8_MP, r9_BType, keyValue);
				
				
		if (isValid) {
			// 响应数据有效
			if (r9_BType.equals("1")) {
				// 浏览器重定向
				
				// change order's state
				productService.changeStateToPayed(r6_Order);
				
				// show pay success status to user
				response.setContentType("text/html;charset=utf-8");
				response.getWriter().println("<h1>付款成功！等待商城进一步操作！等待收货...</h1>");
			} else if (r9_BType.equals("2")) {
				// 服务器点对点 --- 支付公司通知你
				System.out.println("付款成功！");
				// 修改订单状态 为已付款
				// 回复支付公司
				response.getWriter().print("success");
			}
		} else {
			// 数据无效
			System.out.println("数据被篡改！");
		}
	}
	
	public void getOrderListByUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// check login status
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		if(user == null) {
			// not login
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return ;
		}
		
		// have log in, get all orders of the user
		List<Order> orderList = productService.getOrderListByUser(user);
		request.setAttribute("orderList", orderList);
		request.getRequestDispatcher("/order_list.jsp").forward(request, response);
	}
	
}
