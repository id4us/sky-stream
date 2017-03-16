package com.sky.mobile.skytvstream.service.stream;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.dao.ChannelStreamDao;
import com.sky.mobile.skytvstream.domain.*;
import com.sky.mobile.skytvstream.service.secure.*;
import com.sky.web.utils.HTTPUtils;
import com.sky.web.utils.TimeProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class StreamServiceImpl implements StreamService {

    private static final Logger LOG = LoggerFactory
            .getLogger(StreamServiceImpl.class);

    private final StreamKeyService streamKeyService;
    private final ChallengeValidator challengeValidator;
    private final ChannelStreamDao channelStreamDao;
    private TimeProvider timeProvider;
    private CryptoFactory cryptoFactory;


    @Value("${stream.cookie.duration}")
    long cookieDuration;

    @Value("${Xsalt.key.cookie}")
    String keyCookieSalt;

    @Value("${stream.cookie.domain}")
    private String configuredCookieDomainName;

    @Value("${Xsalt.playlist.cookie}")
    private String playlistCookieSalt;

    @Value("${stream.redirect.degradecheck.url}")
    private String redirectUrl;

    @Autowired       // @Qualifier("cacheStreamKeyService") StreamKeyService cacheStreamKeyService,
    public StreamServiceImpl(@Qualifier("algorithmicStreamKeyService") StreamKeyService algorithmicStreamKeyService,
                             ChallengeValidator challengeValidator,
                             ChannelStreamDao channelStreamDao,
                             TimeProvider timeProvider,
                             CryptoFactory cryptoFactory ) {
        this.streamKeyService = algorithmicStreamKeyService;
        this.challengeValidator = challengeValidator;
        this.channelStreamDao = channelStreamDao;
        this.timeProvider = timeProvider;
        this.cryptoFactory = cryptoFactory;
    }

    @Override
    public StreamResponseObjectVo createStream(String channelId, String profileId,
                                               String deviceId, String model) {

        StreamKeyVo streamKeyVo = new StreamKeyVo(channelId, profileId, deviceId, model);

        Optional<String> streamKeyOption = streamKeyService.generateKey(streamKeyVo);

        StreamResponseObjectVo streamObject = new StreamResponseObjectVo();
        if(streamKeyOption.isPresent()) {
            streamObject.setNonce(streamKeyVo.getNonce());
            streamObject.setNextToken(new String(cryptoFactory.getNonce()));
            streamObject.setStreamId(streamKeyOption.get());
        }

        return streamObject;
    }

    @Override
    public boolean isValidStreamObject(StreamVerificationVo streamVerificationVo) {

        if (!hasRequiredData(streamVerificationVo)) {
            return false;
        }

        String streamId = streamVerificationVo.getStreamId();
        String challenge = streamVerificationVo.getClientChallengeResponse();
        String proxyOAuth = streamVerificationVo.getProxyOAuth();

        LOG.debug("challenge:{}", challenge);

        Optional<StreamKeyVo> streamKeyOption = streamKeyService.getStreamKeyVo(streamId);
        if(!streamKeyOption.isPresent()) {
            LOG.error("failed to retrieve stream key from cache");
            return false;
        }

        StreamKeyVo streamKeyVo = streamKeyOption.get();

        if(!matches(streamVerificationVo, streamKeyVo)){
            LOG.error("failed to valid in coming data did not match the data stored");
            return false;
        }

        boolean validChallenge = challengeValidator.validate(
                streamId, proxyOAuth, streamKeyVo.getChannelId(), streamKeyVo.getNonce(),
                streamKeyVo.getDeviceId(), challenge, streamKeyVo.getModel());

        if(!validChallenge) {
            LOG.warn("following challenge has failed : {}", challenge);
        }

        return validChallenge;
    }

    @Override
    public Optional<ChannelStreamVo> getChannelStreamingUrls(String channelId) {
        return channelStreamDao.getChanelStreamByChannelId(channelId);
    }

    @Override
    public void buildStreamAndCookies(StreamResponseObjectVo streamObject,
                                      ChannelStreamVo channelStreamVO, HttpServletResponse response,
                                      String deviceId, DeviceVo deviceVo) {

        streamObject.setStreamId(cryptoFactory.getRandomNumber()+"");

        String platformPlaylistUrl = (deviceVo.getOs().equalsIgnoreCase("IOS")) ? getRedirectContentURL(channelStreamVO
                .getPlaylist()) : channelStreamVO.getPlaylist();

        streamObject.setUri(platformPlaylistUrl);

        streamObject.setClientChallengeResponse(null);
        long currentTime = timeProvider.getTimeMillis() / 1000L;
        final String clientIp = "";
        String serviceEntropy = new StringBuilder()
                .append(streamObject.getStreamId()).append(',')
                .append(deviceId).append(',')
                .append(channelStreamVO.getChannelId()).toString();

        AkamaiCookie keyCookie = new AkamaiCookie(clientIp, currentTime,
                cookieDuration, channelStreamVO.getKeyPath()
                + AkamaiToken.URL_SEPARATOR + serviceEntropy,
                keyCookieSalt, false, true);
        String keyCookieToken = keyCookie.getToken();
        boolean isKeyCookieSecure = true;
        String domainName = configuredCookieDomainName;

        HTTPUtils.addCookieHeader(response, AkamaiCookie.KEY_COOKIE_NAME,
                keyCookieToken, null, domainName,
                HTTPUtils.COOKIE_CONST_IGNORE_MAX_AGE, "/", isKeyCookieSecure,
                1);

        AkamaiCookie playlistCookie = new AkamaiCookie(clientIp, currentTime,
                cookieDuration, channelStreamVO.getPlaylistPath()
                + AkamaiToken.URL_SEPARATOR + serviceEntropy,
                playlistCookieSalt, false, true);

        String playlistCookieToken = playlistCookie.getToken();
        HTTPUtils.addCookieHeader(response, AkamaiCookie.PLAYLIST_COOKIE_NAME,
                playlistCookieToken, null, domainName,
                HTTPUtils.COOKIE_CONST_IGNORE_MAX_AGE, "/", false, 1);

    }

    private String getRedirectContentURL(String platformPlayListUri) {
        String redirectPlatformPlayListUri = null;
        try {
            redirectPlatformPlayListUri = redirectUrl + "?content="
                    + URLEncoder.encode(platformPlayListUri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Unable to encode redirect URL: " + platformPlayListUri, e);
            return platformPlayListUri;
        }
        return redirectPlatformPlayListUri;
    }

    private boolean hasRequiredData(StreamVerificationVo streamVerificationVo) {

        boolean isValid = StringUtils.isNotBlank(streamVerificationVo.getStreamId())
                && StringUtils.isNotBlank(streamVerificationVo.getUri())
                && StringUtils.isNotBlank(streamVerificationVo.getNextToken())
                && StringUtils.isNotBlank(streamVerificationVo.getClientChallengeResponse())
                && StringUtils.isNotBlank(streamVerificationVo.getChannelId())
                && StringUtils.isNotBlank(streamVerificationVo.getProfileId())
                && StringUtils.isNotBlank(streamVerificationVo.getDeviceId())
                && StringUtils.isNotBlank(streamVerificationVo.getModel())
                && StringUtils.isNotBlank(streamVerificationVo.getProxyOAuth());

        if(!isValid) {
            LOG.warn("the following stream request is missing required data : {}", streamVerificationVo);
        }

        return isValid;
    }

    private boolean matches(StreamVerificationVo streamVerificationVo, StreamKeyVo streamKeyVo) {

        boolean payloadMatches = streamKeyVo.getChannelId().equals(streamVerificationVo.getChannelId())
                && streamKeyVo.getProfileId().equals(streamVerificationVo.getProfileId())
                && streamKeyVo.getModel().equals(streamVerificationVo.getModel())
                && streamKeyVo.getDeviceId().equals(streamVerificationVo.getDeviceId())
                && streamKeyVo.getExpiryTime() > timeProvider.getTimeMillis();

        if(!payloadMatches) {
            LOG.warn("payload do not match :{} \n {}", streamVerificationVo, streamKeyVo);
        }

        return payloadMatches;
    }
}
