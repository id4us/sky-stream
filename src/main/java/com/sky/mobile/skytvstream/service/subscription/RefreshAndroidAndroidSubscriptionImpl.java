package com.sky.mobile.skytvstream.service.subscription;

import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.ssmtv.oauth.annotations.OauthAuthenticated;
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;

@Service
public class RefreshAndroidAndroidSubscriptionImpl implements RefreshAndroidSubscription {

    private static final Logger LOG = LoggerFactory
            .getLogger(RefreshAndroidAndroidSubscriptionImpl.class);

    @Resource
    AuthenticatedPerson authenticatedPerson;

    @Autowired
    private StreamConfig streamConfig;

    private final static String DISCOVERY_KEY = "com.sky.sstv.streaming.discovery";


    @OauthAuthenticated
    @Override
    public void updateAndroidSubscription(Collection<SubscriptionVo> activeProducts, String clientId) {

        String encryptedOauthToken = authenticatedPerson.getEncryptedOauthToken();
        for (SubscriptionVo productId : activeProducts) {
            ExpiryUpdateResponse expiryUpdateResponse = invokeRefreshSubscription(encryptedOauthToken, productId.getProductId(), clientId);

        }
    }

    private ExpiryUpdateResponse invokeRefreshSubscription(String token, String productId, String clientId) {
        ExpiryUpdateResponse expiryUpdateResponse = null;
        String activationUrl = null;
        HttpClient client = HttpClientBuilder.create().build();

        try {
            String discoveryString = streamConfig.getConfiguration(DISCOVERY_KEY);
            activationUrl = retrieveDiscoveryLinkForGoogleActivation(discoveryString);
        } catch (IOException exception) {
            exception.printStackTrace();
        }


        HttpPost httpPost = new HttpPost(activationUrl + "google/subscription/refresh");

        httpPost.addHeader("Authorization", "Bearer " + token);
        httpPost.addHeader("x-product", productId);
        httpPost.addHeader("x-subscription-provider", "GOOGLE");
        httpPost.addHeader("x-client-id", clientId);

        HttpResponse response = null;
        int status = 0;

        try {
            response = client.execute(httpPost);
            status = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            LOG.error("google/subscription/refresh status code :"+status);
        }

        expiryUpdateResponse = new ExpiryUpdateResponse(status, response.getStatusLine().getReasonPhrase());

        return expiryUpdateResponse;
    }

    public String retrieveDiscoveryLinkForGoogleActivation(String content) {
        JSONObject jsonObject = JSONObject.fromObject(content);
        JSONArray jsonArray = jsonObject.getJSONArray("serviceHosts");
        for (Object js : jsonArray) {
            JSONObject json = (JSONObject) js;
            if (json.get("name").equals("sstv.goo.activation"))
                return json.get("host").toString();
        }
        return null;
    }


    public class ExpiryUpdateResponse {
        private int status;
        private String content;

        public ExpiryUpdateResponse(int status, String content) {
            this.status = status;
            this.content = content;
        }

        public int getStatus() {
            return status;
        }

        public String getContent() {
            return content;
        }
    }


}
