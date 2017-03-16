package com.sky.mobile.skytvstream.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


@Configurable
public class TestDbConfig {

	private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/sstv_stream_test";
	private static final String DATABASE_USERNAME = "sstv";
	private static final String DATABASE_PASSWORD = "sstv";

	@Bean(name = "dataSource")
	public DataSource getDataSource() {

		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(DATABASE_DRIVER);
		dataSource.setUrl(DATABASE_URL);
		dataSource.setUsername(DATABASE_USERNAME);
		dataSource.setPassword(DATABASE_PASSWORD);
		new JdbcTemplate(dataSource);

		return dataSource;
	}

	@Bean(name = "testtemplate")
	public JdbcTemplate getTemplate() {
		return new JdbcTemplate(getDataSource());
	}
	
	
}
