package com.netpace.expressit.model;

public class Meta {
	
	private int width;
	private int height;

	public Meta() {
		super();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "Meta [width=" + width + ", height=" + height + "]";
	}
	
}
