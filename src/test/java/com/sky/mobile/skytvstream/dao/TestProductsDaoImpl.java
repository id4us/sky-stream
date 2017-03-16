package com.sky.mobile.skytvstream.dao;

import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.config.StreamConfig;
import com.sky.mobile.skytvstream.domain.ProductVo;
import com.sky.mobile.skytvstream.event.Events;
import com.sky.mobile.skytvstream.testutils.SimpleTestEventPublisher;
import com.sky.mobile.skytvstream.utils.SubscriptionProvider;
import com.sky.web.utils.Country;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestProductsDaoImpl.TestConfig.class})
public class TestProductsDaoImpl {
    private static final String TEST_PRODUCTSLIST = "src/test/resources/testdiscovery/productlist.json";
    private static final String TEST_PRODUCTSLIST_ROI = "src/test/resources/testdiscovery/productlistroi.json";
    private static final String TEST_PRODUCTSLISTALT = "src/test/resources/testdiscovery/productlistalternative.json";

    @Autowired
    private ProductsDao dao;

    @Autowired
    private StreamConfig streamConfig;

    @Autowired
    private SimpleTestEventPublisher simpleTestEventPublisher;

    @Resource(name="countryCode")
    private Country countryCode;


    @Test
    @DirtiesContext
    public void testGetAllProducts() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        Collection<ProductVo> products = dao.getAllProducts();
        assertEquals(9, products.size());
    }

    @Test
    @DirtiesContext
    public void testGetProductByName() throws IOException {
        String productName = "com.bskyb.skysportstv.iap.monthly.pack1";
        when(countryCode.getCountry()).thenReturn("GB");
        Optional<ProductVo> productOptional = dao.getProductsByName(productName);
        assertTrue(productOptional.isPresent());
        ProductVo product = productOptional.get();
        assertEquals(productName, product.getName());
        assertEquals("apple store product", product.getDescription());
       // assertTrue(product.getDisplayCost().contains("£4.99"));
        assertTrue(product.isAvailable());


        assertEquals(2, product.getChannels().size());
        assertEquals("apple", product.getSubscriptionProvider());
        assertEquals(SubscriptionProvider.APPLE, product.getSubscriptionProviderEnum());
        assertEquals("1301", product.getChannels().get(0).getChannelid());
        assertEquals("Sky Sports 1", product.getChannels().get(0).getChannelName());
        assertEquals("1302", product.getChannels().get(1).getChannelid());
        assertEquals("Sky Sports 2", product.getChannels().get(1).getChannelName());
    }

    @Test
    @DirtiesContext
    public void testGetProductByNameRoi() throws IOException {
        String productName = "com.bskyb.skysportstv.iap.monthly.pack1";
        when(countryCode.getCountry()).thenReturn("IE");
        Optional<ProductVo> productOptional = dao.getProductsByName(productName);
        assertTrue(productOptional.isPresent());
        ProductVo product = productOptional.get();
        assertEquals(productName, product.getName());
        assertEquals("apple store product", product.getDescription());
        // assertTrue(product.getDisplayCost().contains("£4.99"));
        assertTrue(product.isAvailable());


        assertEquals(2, product.getChannels().size());
        assertEquals("apple", product.getSubscriptionProvider());
        assertEquals(SubscriptionProvider.APPLE, product.getSubscriptionProviderEnum());
        assertEquals("1301", product.getChannels().get(0).getChannelid());
        assertEquals("Sky Sports ROI", product.getChannels().get(0).getChannelName());
        assertEquals("1302", product.getChannels().get(1).getChannelid());
        assertEquals("Sky Sports ROI", product.getChannels().get(1).getChannelName());
    }

    @Test
    @DirtiesContext
    public void testGetLegacy() throws IOException {
        String productName = "android.legacy.pack";
        when(countryCode.getCountry()).thenReturn("GB");

        Optional<ProductVo> productOptional = dao.getProductsByName(productName);
        assertTrue(productOptional.isPresent());
        ProductVo product = productOptional.get();
        assertEquals(productName, product.getName());
        assertTrue(SubscriptionProvider.GOOGLE.toString().equalsIgnoreCase(product.getSubscriptionProvider()));
        assertEquals("google play legacy product", product.getDescription());
       // assertTrue(product.getDisplayCost().contains("£9.99"));
        assertFalse(product.isAvailable());


        assertEquals(8, product.getChannels().size());
    }


    @Test
    @DirtiesContext
    public void testGetProductByName_notfound() {
        String productName = "boguspack";
        when(countryCode.getCountry()).thenReturn("ROI");
        Optional<ProductVo> productOptional = dao.getProductsByName(productName);
        assertFalse(productOptional.isPresent());

    }

    @Test
    @DirtiesContext
    public void testReloadProperties() throws IOException {
        String productName = "alt.pack.1";

        when(countryCode.getCountry()).thenReturn("UK");
        Optional<ProductVo> productOptional = dao.getProductsByName(productName);
        assertFalse(productOptional.isPresent());


        when(streamConfig.getConfiguration(StreamConfig.PACKAGE_LIST))
                .thenReturn(fileFromString(TEST_PRODUCTSLISTALT));

        simpleTestEventPublisher.publishEvent(Events.newRefreshDataEvent(this));

        assertEquals(2, dao.getAllProducts().size());
        productOptional = dao.getProductsByName(productName);
        assertTrue(productOptional.isPresent());
    }


    private static String fileFromString(String fname) throws IOException {
        File f = new File(fname);
        return FileUtils.readFileToString(f);
    }

    @Configurable
    public static class TestConfig {

        @Autowired
        ApplicationContext ctx;

        @Bean
        public SimpleTestEventPublisher getPublisher() {
            SimpleTestEventPublisher publisher = new SimpleTestEventPublisher();
            publisher.setApplicationContext(ctx);
            return publisher;
        }

        @Bean
        public StreamConfig getStreamConfig() throws IOException {
            StreamConfig streamConfig = mock(StreamConfig.class);
            when(streamConfig.getConfiguration(StreamConfig.PACKAGE_LIST))
                    .thenReturn(fileFromString(TEST_PRODUCTSLIST));
            when(streamConfig.getConfiguration(StreamConfig.PACKAGE_LIST_ROI))
                    .thenReturn(fileFromString(TEST_PRODUCTSLIST_ROI));

            return streamConfig;
        }

//        @Bean
//        public StreamConfig getStreamConfigRoi() throws IOException {
//            StreamConfig streamConfig = mock(StreamConfig.class);
//            when(streamConfig.getConfiguration(StreamConfig.PACKAGE_LIST_ROI))
//                    .thenReturn(fileFromString(TEST_PRODUCTSLIST_ROI));
//            return streamConfig;
//        }
        @Bean
        public ProductsDao getProductsDao(StreamConfig config) {
            return new ProductsDaoImpl(config);
        }

        @Bean(name = "countryCode")
        public Country getCountryCode() {
            return mock(Country.class);
        }


    }

}
