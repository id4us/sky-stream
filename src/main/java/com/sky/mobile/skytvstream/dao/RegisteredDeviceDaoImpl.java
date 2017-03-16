package com.sky.mobile.skytvstream.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.sky.mobile.skytvstream.domain.RegisteredDevice;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;

@Repository
public class RegisteredDeviceDaoImpl implements RegisteredDeviceDao {

	private static final String SELECT_SQL = "select * from registered_devices where profile_id = ? and provider = ?";

	private static final String INSERT_SQL = "insert into registered_devices (profile_id, provider, data, device_count) values (? , ? , ?, ?)";

	private static final String UPDATE_SQL = "update registered_devices SET data = ? , device_count = ? where  profile_id = ? and provider = ?";

	private final JdbcTemplate template;

	@Autowired
	public RegisteredDeviceDaoImpl(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}

	@Override
	public RegisteredDevice findByProfileIdForProvider(String profileId,
			SubscriptionProvider subscriptionProvider) {
		try {
		return template.queryForObject(SELECT_SQL, new Object[] { profileId,
				subscriptionProvider.getProviderName() }, new DeviceRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public void insertRegisteredDevice(RegisteredDevice registeredDevice) {
		template.update(INSERT_SQL, new Object[] {
				registeredDevice.getProfileId(),
				registeredDevice.getProviderString(),
				registeredDevice.getDeviceIdsAsJson(),
				registeredDevice.getDeviceCount()
		});
	}

	@Override
	public void updateRegisteredDevice(RegisteredDevice registeredDevice) {
		template.update(UPDATE_SQL, new Object[] {
				registeredDevice.getDeviceIdsAsJson(),
				registeredDevice.getDeviceCount(),
				registeredDevice.getProfileId(),
				registeredDevice.getProviderString()
		});
	}

	private class DeviceRowMapper implements RowMapper<RegisteredDevice> {

		@Override
		public RegisteredDevice mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			RegisteredDevice registeredDevice = new RegisteredDevice();
			registeredDevice.setProfileId(rs.getString("profile_id"));
			registeredDevice.setProvider(rs.getString("provider"));
			registeredDevice.setDeviceId(rs.getString("data"));
			return registeredDevice;
		}

	}

}
