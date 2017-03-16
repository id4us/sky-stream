package com.sky.mobile.skytvstream.service.secure;


import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.StreamKeyVo;
import com.sky.mobile.skytvstream.service.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("cacheStreamKeyService")
public class CacheStreamKeyServiceImpl implements StreamKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(CacheStreamKeyServiceImpl.class);

    private static final long TIME_INTERVAL = 60000L;

    private final PersistStream persistStream;
    private final CryptoFactory cryptoFactory;

    @Autowired
    public CacheStreamKeyServiceImpl(PersistStream persistStream,
                                      CryptoFactory cryptoFactory){
        this.cryptoFactory = cryptoFactory;
        this.persistStream = persistStream;
    }

    public Optional<String> generateKey(StreamKeyVo streamKeyVo) {
        String nonce = new String(cryptoFactory.getNonce());
        streamKeyVo.setNonce(nonce);

        PersistPayload payload = new PersistPayload();
        payload.setNonce(streamKeyVo.getNonce());
        payload.setChannelId(streamKeyVo.getChannelId());
        payload.setUniqueDeviceId(streamKeyVo.getDeviceId());
        payload.setUniqueUserId(streamKeyVo.getProfileId());
        payload.setModel(streamKeyVo.getModel());

        String key;
        try {
            key = new String(cryptoFactory.getNonce());
            persistStream.persistStreamInfo(key, payload);
        } catch (Exception e) { // TODO Should trigger a floodgate scenario, as this is a sign we
            key = null;
            LOG.error("ALERT: Unable to persist Stream, as persistance mechanism is full", e);
        }

        return Optional.fromNullable(key);
    }


    public Optional<StreamKeyVo> getStreamKeyVo(final String streamKey) {
        StreamKeyVo streamKeyVo = null;
        try {
            PersistPayload payload = persistStream.getStreamInfo(streamKey);

            streamKeyVo = new StreamKeyVo(payload.getChannelId(),payload.getUniqueUserId(), payload.getUniqueDeviceId(), payload.getModel());
            streamKeyVo.setExpiryTime(System.currentTimeMillis() + TIME_INTERVAL);   //As we found the record it is always active
            streamKeyVo.setNonce(payload.getNonce());
        } catch (Exception e) {
            LOG.error("unable to find stream key from cache", e);
        } finally {
            persistStream.removeStreamInfo(streamKey);
        }
        return Optional.fromNullable(streamKeyVo);
    }
}
