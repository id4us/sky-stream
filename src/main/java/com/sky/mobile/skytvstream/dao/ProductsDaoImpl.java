package com.sky.mobile.skytvstream.dao;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.domain.ProductVo;
import com.sky.mobile.skytvstream.event.RefreshDataEvent;
import com.sky.web.utils.Country;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public class ProductsDaoImpl implements ProductsDao, ApplicationListener<RefreshDataEvent> {

	public final static Logger LOG = LoggerFactory
			.getLogger(ProductsDaoImpl.class);

	private Map<String, ProductVo> products;
	private Map<String, ProductVo> productsRoi;

	private StreamConfig config;

	@Resource(name="countryCode")
	private Country countryCode;

	@Autowired
	public ProductsDaoImpl(StreamConfig config) {
		this.config = config;
		loadData(config);
		loadDataRoi(config);
	}

	private void loadData(StreamConfig config) {
		try {
			String jsonString = config.getConfiguration(StreamConfig.PACKAGE_LIST);
			Preconditions.checkNotNull(jsonString,
					"PackageList unable to be loaded");
			Map<String, ProductVo> items = Maps.newHashMap();
			for (ProductVo product : parseJson(jsonString)) {
				items.put(product.getName().toLowerCase(), product);
			}

			products = items;
		} catch (Exception e) {
			String msg = "FATAL ERROR, building Products List. Please check the Configuration table config_streaming: key:"
					+ StreamConfig.PACKAGE_LIST;
			if ((products != null) && (!products.isEmpty())) {
				msg += ". Using previous loaded list";
			}
			LOG.error(msg, e);
			throw new IllegalArgumentException(msg, e);
		}
	}

	private void loadDataRoi(StreamConfig config) {
		try {
			String jsonString = config.getConfiguration(StreamConfig.PACKAGE_LIST_ROI);
			Preconditions.checkNotNull(jsonString,
					"PackageList unable to be loaded");
			Map<String, ProductVo> items = Maps.newHashMap();
			for (ProductVo product : parseJson(jsonString)) {
				items.put(product.getName().toLowerCase(), product);
			}

			productsRoi = items;
		} catch (Exception e) {
			String msg = "FATAL ERROR, building Products List. Please check the Configuration table config_streaming: key:"
					+ StreamConfig.PACKAGE_LIST_ROI;
			if ((products != null) && (!products.isEmpty())) {
				msg += ". Using previous loaded list";
			}
			LOG.error(msg, e);
			throw new IllegalArgumentException(msg, e);
		}
	}

	private List<ProductVo> parseJson(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<ProductVo> list = mapper.readValue(jsonString,
				new TypeReference<List<ProductVo>>() {
				});

		return list;

	}

	@Override
	public Collection<ProductVo> getAllProducts() {
		Preconditions.checkNotNull(products);
		return products.values();
	}

	@Override
	public Optional<ProductVo> getProductsByName(String productName) {
		Preconditions.checkNotNull(productName, "productName cannot be null");

		try {
			LOG.debug("country code products" + countryCode.getCountry());
			if (countryCode.getCountry().equals("IE")) {
				Preconditions.checkNotNull(productsRoi);
				return Optional.fromNullable(productsRoi.get(productName.toLowerCase()));
			} else {
				Preconditions.checkNotNull(products);
				return Optional.fromNullable(products.get(productName.toLowerCase()));
			}

		} catch (NullPointerException e){
			Preconditions.checkNotNull(products);
			return Optional.fromNullable(products.get(productName.toLowerCase()));
		}

	}
	
	@Override
	public void onApplicationEvent(RefreshDataEvent event) {
		LOG.info("Refreshing Products");
		loadData(config);
		loadDataRoi(config);
	}

}
