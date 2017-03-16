package com.sky.mobile.skytvstream.domain;

import java.util.Set;
import java.util.TreeSet;

public class UserPurchasableProductsVo {

	private final Set<SimpleProductVo> subscribed = new TreeSet<>();
	private final Set<SimpleProductVo> notSubscribed = new TreeSet<>();

	public void addToSubscribed(ProductVo product) {
		this.addToSubscribed(new SimpleProductVo(product));
	}

	public void addToSubscribed(SimpleProductVo product) {
		subscribed.add(product);
	}
	
	public void addToNotSubscribed(ProductVo product) {
		addToNotSubscribed(new SimpleProductVo(product));
	}

	public void addToNotSubscribed(SimpleProductVo product) {
		notSubscribed.add(product);
	}
	
	public Set<SimpleProductVo> getSubscribed() {
		return subscribed;
	}

	public Set<SimpleProductVo> getNotSubscribed() {
		return notSubscribed;
	}

}
