package io.github.jacktown11.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import io.github.jacktown11.domain.Category;
import io.github.jacktown11.domain.Order;
import io.github.jacktown11.domain.OrderItem;
import io.github.jacktown11.domain.Product;
import io.github.jacktown11.service.AdminService;
import io.github.jacktown11.utils.CommonUtils;

public class AdminServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	private AdminService service = new AdminService();
	
	public void getCategoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<Category> categoryList = service.getCategoryList();
		Gson gson = new Gson();
		String categoryListJson = gson.toJson(categoryList);
		response.setCharacterEncoding("utf-8");
		response.getWriter().write(categoryListJson);
	}
	
	public void showCategoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<Category> categoryList = service.getCategoryList();
		request.setAttribute("categoryList", categoryList);
		request.getRequestDispatcher("/admin/category/list.jsp").forward(request, response);
	}
	public void prepareEditCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cid = request.getParameter("cid");
		Category category = new Category();
		if(cid != null) {
			category = service.getCategoryByCid(cid);
		}
		request.setAttribute("category", category);
		request.getRequestDispatcher("/admin/category/edit.jsp").forward(request, response);
	}
	public void editCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cname = request.getParameter("cname");
		String cid = request.getParameter("cid");
		Category category = new Category();
		category.setCid(cid);
		category.setCname(cname);
		if(cid != null && cname != null) {
			int rows = service.updateCategory(category);
		}
		response.sendRedirect(request.getContextPath() + "/admin?method=showCategoryList");
	}
	public void deleteCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cid = request.getParameter("cid");
		if(cid != null) {
			int rows = service.deleteCategoryByCid(cid);
		}
		response.sendRedirect(request.getContextPath() + "/admin?method=showCategoryList");
	}
	public void addCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cname = request.getParameter("cname");
		if(cname != null) {
			String cid = CommonUtils.getUUID();
			Category category = new Category();
			category.setCid(cid);
			category.setCname(cname);
			int rows = service.addCategory(category);
		}
		response.sendRedirect(request.getContextPath() + "/admin?method=showCategoryList");
	}
	
	public void showProductList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<Product> productList = service.getProductList();
		request.setAttribute("productList", productList);
		request.getRequestDispatcher("/admin/product/list.jsp").forward(request, response);
	}
	public void addProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload=  new ServletFileUpload(factory);
		Map<String, String> formMap = new HashMap<String, String>();
		try {
			List<FileItem> fileItemList = upload.parseRequest(request);
			for(FileItem fileItem : fileItemList) {
				if(fileItem.isFormField()) {
					// normal form field
					formMap.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
				}else {
					// file upload
					
					// save the file to disk
					InputStream is = fileItem.getInputStream();
					String imageServerPath = "products/jacktown/" + fileItem.getName();
					String imageRealPath= this.getServletContext().getRealPath(imageServerPath);
					System.out.println(imageRealPath);
					FileOutputStream fos = new FileOutputStream(imageRealPath);
					IOUtils.copy(is, fos);
					is.close();
					fos.close();
					fileItem.delete(); // delete temporary file
					// put the file's path into formMap
					formMap.put("pimage", imageServerPath);
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
		// load all form data into product bean
		Product product = new Product();
		try {
			BeanUtils.populate(product, formMap);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		// add default value for field not in the form
		product.setPdate(new Date());
		product.setPid(CommonUtils.getUUID());
		product.setPflag(0);
		product.setCategory(service.getCategoryByCid(formMap.get("cid")));
		
		// save into db and turn to product list page
		int rows = service.addProduct(product);
		System.out.println(rows);
		response.sendRedirect(request.getContextPath() + "/admin?method=showProductList");
	}
	
	public void deleteProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pid = request.getParameter("pid");
		if(pid != null) {
			int rows = service.deleteProductByPid(pid);
		}
		response.sendRedirect(request.getContextPath()+"/admin?method=showProductList");
	}
	public void prepareEditProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pid = request.getParameter("pid");
		if(pid != null) {
			Product product = service.getProductByPid(pid);
			HttpSession session = request.getSession();
			request.setAttribute("product", product);
			request.setAttribute("categoryList", service.getCategoryList());
		}
		request.getRequestDispatcher("/admin/product/edit.jsp").forward(request, response);
	}
	
	public void editProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload=  new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8"); // 解决上传文件名编码问题
		Map<String, String> formMap = new HashMap<String, String>();
		try {
			List<FileItem> fileItemList = upload.parseRequest(request);
			for(FileItem fileItem : fileItemList) {
				if(fileItem.isFormField()) {
					// normal form field
					formMap.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
				}else {
					// file upload
					String filename = fileItem.getName();
					String imageServerPath = null;
					System.out.println("filename: " + filename);
					if(filename != null && !filename.trim().equals("")) {
						// new picture is uploaded
						// save the file to disk
						InputStream is = fileItem.getInputStream();
						imageServerPath = "products/jacktown/" + fileItem.getName();
						String imageRealPath= this.getServletContext().getRealPath(imageServerPath);
						System.out.println("imageRealPath: "+imageRealPath);
						FileOutputStream fos = new FileOutputStream(imageRealPath);
						IOUtils.copy(is, fos);
						is.close();
						fos.close();
						fileItem.delete(); // delete temporary file
					}
					
					// put the file's path into formMap
					formMap.put("pimage", imageServerPath);
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
		// load all form data into product bean
		Product product = new Product();
		try {
			BeanUtils.populate(product, formMap);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		if(product.getPimage() == null) {
			// no new picture from client
			// use the old one
			product.setPimage(service.getProductByPid(product.getPid()).getPimage());
		}
		
		// add default value for field not in the form
		product.setCategory(service.getCategoryByCid(formMap.get("cid")));
		
		// save into db and turn to product list page
		int rows = service.updateProduct(product);
		System.out.println(rows);
		response.sendRedirect(request.getContextPath() + "/admin?method=showProductList");
	}

	public void showOrderList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<Order> orderList = service.getOrderList();
		System.out.println(orderList.size());
		request.setAttribute("orderList", orderList);
		request.getRequestDispatcher("/admin/order/list.jsp").forward(request, response);
	}
	
	public void getOrderItemListByOid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String oid = request.getParameter("oid");
		List<OrderItem> orderItemList = null;
		System.out.println(oid);
		if(oid != null && !oid.trim().equals("")) {
			orderItemList = service.getOrderItemListByOid(oid);
		}
		Gson gson = new Gson();
		String json = gson.toJson(orderItemList);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}
	
	
}
