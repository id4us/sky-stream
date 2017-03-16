package com.sky.mobile.skytvstream.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic=true)
public class SimpleProductVo implements Comparable<SimpleProductVo> {

	private String name;

	private String description;

	private String displayText;

	private String displayCost;

	private String purchaseId;

	private boolean available;


	public SimpleProductVo() {
		
	}
	

	public SimpleProductVo(ProductVo that) {
		this.name = that.getName();
		this.description = that.getDescription();
		this.displayText = that.getDisplayText();
		this.displayCost = that.getDisplayCost();
		this.purchaseId = that.getPurchaseId();
		this.available = that.isAvailable();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public String getDisplayCost() {
		return displayCost;
	}

	public void setDisplayCost(String displayCost) {
		this.displayCost = displayCost;
	}

	public String getPurchaseId() {
		return purchaseId;
	}

	public void setPurchaseId(String purchaseId) {
		this.purchaseId = purchaseId;
	}

	@Override
	public int compareTo(SimpleProductVo that) {
		return this.name.compareToIgnoreCase(that.name);
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleProductVo other = (SimpleProductVo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
