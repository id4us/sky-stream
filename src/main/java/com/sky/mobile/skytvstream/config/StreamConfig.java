package com.sky.mobile.skytvstream.config;

import java.io.IOException;

public interface StreamConfig {

    String DISCOVERY_KEY = "com.sky.sstv.streaming.discovery";
    String ALL_CHANNELS_KEY = "com.sky.sstv.streaming.allchannels";
    String ALL_CHANNELS_KEY_ROI = "com.sky.sstv.streaming.allchannels.roi";
    String PACKAGE_LIST = "com.sky.sstv.streaming.products";
    String PACKAGE_LIST_ROI = "com.sky.sstv.streaming.products.roi";
    String UNSUPPORTED_DEVICES = "com.sky.sstv.unsupported.device";
    String CHANNEL_STREAMS = "com.sky.sstv.channel.streams";
    String ALLOWED_VERSIONS_KEY = "com.sky.sstv.supported.versions";
    String SLATED_VERSIONS_KEY = "com.sky.sstv.supported.slatedversions";
    String SLATED_CHANNEL_ID = "SLATED";
    String LEGACY_ANDROID_PACK = "com.sky.sstv.legacy.android.pack";
    String LEGACY_APPLE_PACK = "com.sky.sstv.legacy.apple.pack";

    String getConfiguration(String key) throws IOException;
}
