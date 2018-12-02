package io.github.jacktown11.domain;

import java.util.List;

public class PageBean<T> {
	private int currentCount;
	private int totalCount;
	private int currentSlide;
	private int totalSlide;
	private List<T> itemList;
	public int getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public int getCurrentSlide() {
		return currentSlide;
	}
	public void setCurrentSlide(int currentSlide) {
		this.currentSlide = currentSlide;
	}
	public int getTotalSlide() {
		return totalSlide;
	}
	public void setTotalSlide(int totalSlide) {
		this.totalSlide = totalSlide;
	}
	public List<T> getItemList() {
		return itemList;
	}
	public void setItemList(List<T> itemList) {
		this.itemList = itemList;
	} 
	
}	
