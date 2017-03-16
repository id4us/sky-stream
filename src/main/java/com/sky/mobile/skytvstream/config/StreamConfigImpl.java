package com.sky.mobile.skytvstream.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StreamConfigImpl implements StreamConfig {

    private static final Logger LOG = LoggerFactory.getLogger(StreamConfigImpl.class);

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_SQL = "select data from config_stream where id = ?";

    @Autowired
    public StreamConfigImpl(DataSource datasource) {
        jdbcTemplate = new JdbcTemplate(datasource);
    }

    @Override
    public String getConfiguration(final String key) throws IOException {
        try {
        	
        	String value = jdbcTemplate.queryForObject(SELECT_SQL, new Object[]{key}, String.class); 
        	
            return cleanup(value);

        } catch (DataAccessException e) {
            LOG.error("Unable to access Streaming Configuration from database", e);
            return null;
        }
    }

	private String cleanup(String value) {
		String returns = StringUtils.replace(value, "\\n", "\n");
		returns = StringUtils.replace(returns, "\\r", "\r");
		returns = StringUtils.replace(returns, "\\t", "\t");
		return returns;
	}

}
