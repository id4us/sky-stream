package com.sky.mobile.skytvstream.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.domain.ChannelStreamVo;
import com.sky.mobile.skytvstream.event.RefreshDataEvent;


@Component
public class ChannelStreamDaoImpl implements ChannelStreamDao, 
		ApplicationListener<RefreshDataEvent>, InitializingBean {
	private static final Logger LOG = LoggerFactory.getLogger(ChannelStreamDaoImpl.class);

	
	private Map<String, ChannelStreamVo> streams;

	private StreamConfig config;

	@Autowired
	public ChannelStreamDaoImpl(StreamConfig streamConfig) {
		this.config = streamConfig;
	}
	
	
	@Override
	public Optional<ChannelStreamVo> getChanelStreamByChannelId(String channelId) {
		Preconditions.checkNotNull(streams, "Streams cannot be null");
		return Optional.fromNullable(streams.get(channelId));
	}

	@Override
	public void onApplicationEvent(RefreshDataEvent arg0) {
		loadData();
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		loadData();
		
	}
		
	private void loadData() {
		LOG.info("Loading Streams list");
		try {
			String jsonString = config.getConfiguration(StreamConfig.CHANNEL_STREAMS);
			Preconditions.checkNotNull(jsonString,
					"PackageList unable to be loaded");
			Map<String, ChannelStreamVo> items = Maps.newHashMap();
			for (ChannelStreamVo stream : parseJson(jsonString)) {
				items.put(stream.getChannelId().toLowerCase(), stream);
			}

			streams = items;
		} catch (Exception e) {
			String msg = "FATAL ERROR, building Products List. Please check the Configuration table config_stream: key:"
					+ StreamConfig.PACKAGE_LIST;
			if ((streams != null) && (!streams.isEmpty())) {
				msg += ". Using previous loaded list";
			}
			LOG.error(msg, e);
			throw new IllegalArgumentException(msg, e);
		}
		
	}

	private List<ChannelStreamVo> parseJson(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<ChannelStreamVo> list = mapper.readValue(jsonString,
				new TypeReference<List<ChannelStreamVo>>() {
				});

		return list;

	}

}
