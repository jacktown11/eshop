package io.github.jacktown11.domain;

public class CartItem {
	private Product product;
	private int quantity;
	private double subTotal;
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getSubTotal() {
		return subTotal;
	}
	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}
	public void setSubTotal() {
		if(product != null) {
			this.subTotal = quantity * product.getShop_price();			
		}
	}
}
