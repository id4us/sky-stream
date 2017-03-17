package com.sky.mobile.skytvstream.service.products;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.dao.ProductsDao;
import com.sky.mobile.skytvstream.domain.ProductVo;
import com.sky.mobile.skytvstream.domain.SimpleProductVo;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.mobile.skytvstream.domain.UserPurchasableProductsVo;
import com.sky.mobile.skytvstream.event.RefreshDataEvent;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.web.utils.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

@Service
public class ProductServiceImpl implements ProductService, ApplicationListener<RefreshDataEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    public String legacyAndroidPack;
    public String legacyApplePack;

    private ProductsDao productsDao;
    private StreamConfig streamConfig;
    private TimeProvider timeProvider;


    @Autowired
    public ProductServiceImpl(ProductsDao productsDao, StreamConfig streamConfig, TimeProvider timeProvider) throws IOException {
        this.productsDao = productsDao;
        this.streamConfig = streamConfig;
        this.timeProvider = timeProvider;
        loadData();
    }

    private void loadData() throws IOException {
        legacyAndroidPack = streamConfig.getConfiguration(
                StreamConfig.LEGACY_ANDROID_PACK);
        legacyApplePack = streamConfig.getConfiguration(
                StreamConfig.LEGACY_APPLE_PACK);
    }

    @Override
    public UserPurchasableProductsVo getUserPurchasableProductsFor(
            SubscriptionProvider subProvider,
            Collection<SubscriptionVo> subscriptionVoList) {
        final UserPurchasableProductsVo userPurchasableProductsVo = new UserPurchasableProductsVo();

        if (SubscriptionProvider.VODAFONE != subProvider) {

            Optional<ProductDetails> legacyPack = getLegacyPack(subscriptionVoList, subProvider);
            Optional<ProductVo> productOption;

            if (legacyPack.isPresent() && (productOption = productsDao.getProductsByName(legacyPack.get().getProductName())).isPresent()) {
                if(legacyPack.get().getExpiryDate().after(timeProvider.getDate()))
                    userPurchasableProductsVo.addToSubscribed(productOption.get());
                else
                    userPurchasableProductsVo.addToNotSubscribed(productOption.get());

            } else {
                setSubscriptionData(userPurchasableProductsVo, subscriptionVoList, subProvider);
            }
        }

        return userPurchasableProductsVo;
    }

    private void setSubscriptionData(
            UserPurchasableProductsVo userPurchasableProductsVo,
            Collection<SubscriptionVo> subscriptionVoList,
            SubscriptionProvider subscriptionProvider) {

        subscriptionVoList.parallelStream()
                .filter(subscriptionVo1 -> subscriptionVo1.getExpiry().after(timeProvider.getDate()))
                .map(subscriptionVo -> subscriptionVo.getProductId())
                .map(p -> productsDao.getProductsByName(p))
                .map(productVoOptional -> productVoOptional.get())
                .map(productVoOptional -> { userPurchasableProductsVo.addToSubscribed(productVoOptional);return 1;}).count();

        Collection<ProductVo> productVoList = productsDao.getAllProducts();

          productVoList.stream().filter(p->p.isAvailable())
                .filter(p -> !userPurchasableProductsVo.getSubscribed().contains(new SimpleProductVo(p)))
                .filter(productVo -> productVo.getSubscriptionProvider().equalsIgnoreCase(subscriptionProvider.toString()))
                .map(productVoOptional -> {
                    userPurchasableProductsVo.addToNotSubscribed(productVoOptional);
                    return 1;
                })
                .count();
    }

    private Optional<ProductDetails> getLegacyPack(
            Collection<SubscriptionVo> subscriptionVoList,
            SubscriptionProvider subscriptionProvider) {
        for (SubscriptionVo subsVo : subscriptionVoList) {
            boolean sameProvider = subsVo.getProvider().equalsIgnoreCase(
                    subscriptionProvider.toString());
            boolean isLegacyProduct = subsVo.getProductId().equalsIgnoreCase(
                    legacyAndroidPack)
                    || subsVo.getProductId().equalsIgnoreCase(
                    legacyApplePack);

            if (sameProvider && isLegacyProduct) {
                return Optional.of(new ProductDetails(subsVo.getProductId(), subsVo.getExpiry()));
            }
        }

        return Optional.absent();
    }

    @Override
    public void onApplicationEvent(RefreshDataEvent event) {
        try {
            loadData();
        } catch (IOException e) {
            LOG.error("Ioexception trying to Load/refresh Device List", e);
        }
    }


    class ProductDetails {

        private String productName;
        private Date expiryDate;

        public ProductDetails(String productName, Date expiryDate) {
            this.productName = productName;
            this.expiryDate = expiryDate;
        }

        public Date getExpiryDate() {
            return expiryDate;
        }

        public String getProductName() {
            return productName;
        }


    }

}
