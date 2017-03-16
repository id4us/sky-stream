package com.sky.mobile.skytvstream.dao;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

@Repository
public class SubscriptionDaoImpl implements SubscriptionDao {

	private static final String SELECT_BY_PROFILE_AND_PROVIDER = "select * from subscriptions where profile_id = ? and provider = ? order by expiry desc";
	private static final String SELECT_BY_PROFILE = "select * from subscriptions where profile_id = ? order by expiry desc";
	private static final String SELECT_UPDATED_DATE_BY_PROFILE = "select updated from subscriptions where profile_id = ? and product_id= ? and provider=? order by expiry desc LIMIT 1";

	private final JdbcTemplate template;

	@Autowired
	public SubscriptionDaoImpl(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<SubscriptionVo> getSubscriptionsByProfileIdAndProvider(String profileId, SubscriptionProvider subscriptionProvider) {
		return template.query(SELECT_BY_PROFILE_AND_PROVIDER, new Object [] {profileId, subscriptionProvider.getProviderName() } ,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
	}

	@Override
	public Date getLastUpdated(SubscriptionVo sub) {
		SimpleDateFormat dateParser = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		return template.queryForObject(SELECT_UPDATED_DATE_BY_PROFILE, new Object [] {sub.getProfileId(), sub.getProductId(), sub.getProvider()} , Date.class);

	}

	@Override
	public Collection<SubscriptionVo> getSubscriptionsByProfileId(String profileId) {
		return template.query(SELECT_BY_PROFILE, new Object [] {profileId} ,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
	}

}
