package io.github.jacktown11.domain;

import java.util.HashMap;
import java.util.Map;

import io.github.jacktown11.service.ProductService;

public class Cart {
	private Map<String, CartItem> contents = new HashMap<String, CartItem>();
	private double total = 0;
	
	public Map<String, CartItem> getContents() {
		return contents;
	}
	public void setContents(Map<String, CartItem> contents) {
		this.contents = contents;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public void setTotal() {
		double total = 0;
		if(contents != null) {
			for(String pid : contents.keySet()) {
				total += contents.get(pid).getSubTotal();
			}
		}
		this.total = total;
	}
	public void clear() {
		this.contents = new HashMap<String, CartItem>();
		this.total = 0;
	}
	public void update(String pid, int quantity) {
		CartItem item = null;
		if(contents.containsKey(pid)) {
			// exist in the contents
			item = contents.get(pid);
		}else {
			// create new CartItem using data from database
			item = new CartItem();
			item.setProduct(new ProductService().getProductById(pid));
		}
		
		// set new quantity and re-calculate
		item.setQuantity(quantity);
		item.setSubTotal();
		contents.put(pid, item);
		this.setTotal();
	}
	public void add(String pid, int quantity) {
		if(contents.containsKey(pid)) {
			quantity += contents.get(pid).getQuantity();
		}
		this.update(pid, quantity);
	}
	public void delete(String pid) {
		contents.remove(pid);
		this.setTotal();
	}
}
