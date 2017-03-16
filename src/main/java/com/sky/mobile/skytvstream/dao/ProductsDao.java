package com.sky.mobile.skytvstream.dao;

import java.util.Collection;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.ProductVo;

public interface ProductsDao {

	Collection<ProductVo> getAllProducts();
	
	Optional<ProductVo> getProductsByName(String productName);
	
}
