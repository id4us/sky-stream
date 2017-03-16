package com.sky.mobile.skytvstream.service.subscription;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.sky.mobile.skytvstream.dao.ProductsDao;
import com.sky.mobile.skytvstream.domain.ChannelVo;
import com.sky.mobile.skytvstream.domain.ProductVo;
import com.sky.mobile.skytvstream.domain.SubscriptionVo;
import com.sky.web.utils.Country;
import com.sky.web.utils.CountryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestSubscriptionServiceChannels.TestConfig.class})
public class TestSubscriptionServiceChannels {

    @Autowired
    SubscriptionsService service;

    @Autowired
    ProductsDao mockProductsDao;


    @Resource(name="countryCode")
    private Country countryCode;

    @Test
    public void testgetChannelsForProducts() {
        Optional<ProductVo> product1 = Optional.of(createProduct1());
        String productName = product1.get().getName();
        when(mockProductsDao.getProductsByName(productName)).thenReturn(product1);

        Collection<String> result = service.getChannelsForProducts(Sets.newHashSet(getSubscription1()));
        assertEquals(2, result.size());
        assertTrue(result.contains("1234"));
        assertTrue(result.contains("0342"));
    }

    @Test
    public void testgetChannelsForProducts_multiple() {
        Optional<ProductVo> product1 = Optional.of(createProduct1());
        Optional<ProductVo> product2 = Optional.of(createProduct2());
        String product1Name = product1.get().getName();
        String product2Name = product2.get().getName();

        when(mockProductsDao.getProductsByName(product1Name)).thenReturn(product1);
        when(mockProductsDao.getProductsByName(product2Name)).thenReturn(product2);
        when(countryCode.getCountry()).thenReturn("GB");

        Collection<String> result = service.getChannelsForProducts(Sets.newHashSet(getSubscription1(), getSubscription2()));
        assertEquals(3, result.size());
        assertTrue(result.contains("1234"));
        assertTrue(result.contains("0342"));
        assertTrue(result.contains("0562"));
    }


    @Test
    public void testgetChannelsForProductsRoi() {
        Optional<ProductVo> product1 = Optional.of(createProductRoi());
        String product1Name = product1.get().getName();

        when(mockProductsDao.getProductsByName(product1Name)).thenReturn(product1);
        when(countryCode.getCountry()).thenReturn("ROI");

        Collection<String> result = service.getChannelsForProducts(Sets.newHashSet(getSubscriptionRoi()));
        assertEquals(2, result.size());
        assertTrue(result.contains("1234"));
        assertTrue(result.contains("0342"));
    }

    @Test
    public void testgetChannelsForProducts_notfound() {
        String product1Name = "Bogus";

        Optional<ProductVo> product = Optional.absent();

        when(mockProductsDao.getProductsByName(product1Name)).thenReturn(product);
        when(countryCode.getCountry()).thenReturn("GB");

        Collection<String> result = service.getChannelsForProducts(Sets.newHashSet(getSubscriptionBogus()));
        assertEquals(0, result.size());
    }

    @Test
    public void testgetChannelsForProducts_notfound2() {
        String product1Name = "Bogus";

        Optional<ProductVo> product2 = Optional.of(createProduct2());
        String product2Name = product2.get().getName();
        when(countryCode.getCountry()).thenReturn("GB");


        Optional<ProductVo> product1 = Optional.absent();

        when(mockProductsDao.getProductsByName(product1Name)).thenReturn(product1);
        when(mockProductsDao.getProductsByName(product2Name)).thenReturn(product2);

        Collection<String> result = service.getChannelsForProducts(Sets.newHashSet(getSubscriptionBogus(), getSubscription2()));
        assertEquals(2, result.size());
        assertTrue(result.contains("1234"));
        assertTrue(result.contains("0562"));
    }

    private ProductVo createProduct2() {
        ProductVo product = new ProductVo();
        product.setName("test.product2");
        product.getChannels().add(new ChannelVo("1234", "test"));
        product.getChannels().add(new ChannelVo("0562", "test3"));
        return product;
    }

    private ProductVo createProduct1() {
        ProductVo product = new ProductVo();
        product.setName("test.product1");
        product.getChannels().add(new ChannelVo("1234", "test"));
        product.getChannels().add(new ChannelVo("0342", "test2"));
        return product;
    }

    private ProductVo createProductRoi() {
        ProductVo product = new ProductVo();
        product.setName("test.product.roi");
        product.getChannels().add(new ChannelVo("1234", "testroi"));
        product.getChannels().add(new ChannelVo("0342", "test2roi"));
        return product;
    }

    private SubscriptionVo getSubscription1() {
        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setProductId("test.product1");
        return subscription;
    }

    private SubscriptionVo getSubscription2() {
        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setProductId("test.product2");
        return subscription;
    }

    private SubscriptionVo getSubscriptionRoi() {
        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setProductId("test.product.roi");
        return subscription;
    }

    private SubscriptionVo getSubscriptionBogus() {
        SubscriptionVo subscription = new SubscriptionVo();
        subscription.setProductId("Bogus");
        return subscription;
    }

    @Configurable
    @EnableWebMvc
    public static class TestConfig extends WebMvcConfigurationSupport {

        @Bean
        public ProductsDao getProductsDao() {
            return mock(ProductsDao.class);
        }

        @Bean
        public SubscriptionsService getSubscriptionsService(ProductsDao prodDao) {
            return new SubscriptionsServiceImpl(null, prodDao, null);
        }

        @Bean(name = "countryCode")
        public Country getCountryCode() {
            return mock(Country.class);
        }
    }
}
