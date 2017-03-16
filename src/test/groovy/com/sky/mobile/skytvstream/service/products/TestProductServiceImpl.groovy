package com.sky.mobile.skytvstream.service.products

import com.sky.mobile.skytvstream.config.StreamConfig
import com.sky.mobile.skytvstream.dao.ProductsDao
import com.sky.mobile.skytvstream.dao.ProductsDaoImpl
import com.sky.mobile.skytvstream.domain.UserPurchasableProductsVo
import com.sky.mobile.skytvstream.utils.SubscriptionProvider
import com.sky.web.utils.TimeProvider
import spock.lang.Specification

import javax.annotation.Resource

class TestProductServiceImpl extends Specification {

    private ProductService productServiceImpl
    private ProductsDao productsDao;


    def setup() {
        StreamConfig streamConfig = Mock(StreamConfig)
        TimeProvider timeProvider = Mock(TimeProvider)

        streamConfig.getConfiguration(StreamConfig.PACKAGE_LIST) >> new File('src/test/resources/testdiscovery/productlist.json').text
        streamConfig.getConfiguration(StreamConfig.PACKAGE_LIST_ROI) >> new File('src/test/resources/testdiscovery/productlistroi.json').text
        streamConfig.getConfiguration(StreamConfig.LEGACY_ANDROID_PACK) >> "android.legacy.pack"
        streamConfig.getConfiguration(StreamConfig.LEGACY_APPLE_PACK) >> "com.bskyb.skysportstv.iap.monthly"
        timeProvider.getDate() >> new Date().minus(5)

        productsDao = new ProductsDaoImpl(streamConfig)
        productServiceImpl = new ProductServiceImpl(productsDao, streamConfig, timeProvider);
    }


    def "test purchasable products for vodafone subscriber"() {
        given:
        def subscriptionVoList = SamplePurchaseData.vodafoneCustomerSubList

        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.VODAFONE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.notSubscribed.isEmpty()
    }

    def "test purchasable products for legacy Android subscriber, when provider is GOOGLE"() {
        given:
        def subscriptionVoList = SamplePurchaseData.androidCustomerWithLegacySubList
        when:
                UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.GOOGLE,
                        subscriptionVoList
                )
        then:
        !userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.subscribed.getAt(0).name == SamplePurchaseData.legacyAndroidSub.productId
        userPurchasableProductsVo.notSubscribed.isEmpty()
    }


    def "test purchasable products for legacy Android subscriber with other subs"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.androidPack1, SamplePurchaseData.legacyAndroidSub]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.GOOGLE,
                        subscriptionVoList
                )
        then:
        !userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.subscribed.getAt(0).name == SamplePurchaseData.legacyAndroidSub.productId
        userPurchasableProductsVo.notSubscribed.isEmpty()
    }


    def "test purchasable products for new Android subscriber"() {
        given:
        def subscriptionVoList = []
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.GOOGLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.notSubscribed.getAt(0).name == 'subscription.test.monthly.pack1'
        userPurchasableProductsVo.notSubscribed.getAt(1).name == 'subscription.test.monthly.pack2'
    }


    def "test purchasable products for current Android subscriber with pack 1 only"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.androidPack1]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.GOOGLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.getAt(0).name == 'subscription.test.monthly.pack1'
        userPurchasableProductsVo.notSubscribed.getAt(0).name == 'subscription.test.monthly.pack2'
    }

    def "test purchasable products for current Android subscriber with pack 2 only"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.androidPack2]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.GOOGLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.getAt(0).name == 'subscription.test.monthly.pack2'
        userPurchasableProductsVo.notSubscribed.getAt(0).name == 'subscription.test.monthly.pack1'
    }


    def "test purchasable products for current Android subscriber with pack 1 and 2"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.androidPack1, SamplePurchaseData.androidPack2]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.GOOGLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.getAt(0).name == 'subscription.test.monthly.pack1'
        userPurchasableProductsVo.subscribed.getAt(1).name == 'subscription.test.monthly.pack2'
        userPurchasableProductsVo.notSubscribed.isEmpty()
    }

    def "test purchasable products for legacy ios subscriber, when provider is IOS"() {
        given:
        def subscriptionVoList = SamplePurchaseData.iosCustomerWithLegacySubList
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        !userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.subscribed.getAt(0).name == SamplePurchaseData.legacyIphoneSub.productId
        userPurchasableProductsVo.notSubscribed.isEmpty()
    }


    def "test purchasable products for legacy ios subscriber with other subs"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.iosPack1, SamplePurchaseData.legacyIphoneSub]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        !userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.subscribed.getAt(0).name == SamplePurchaseData.legacyIphoneSub.productId
        userPurchasableProductsVo.notSubscribed.isEmpty()
    }


    def "test purchasable products for new ios subscriber"() {
        given:
        def subscriptionVoList = []
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.notSubscribed.getAt(0).name == 'com.bskyb.skysportstv.iap.monthly.pack1'
        userPurchasableProductsVo.notSubscribed.getAt(1).name == 'com.bskyb.skysportstv.iap.monthly.pack2'
    }


    def "test purchasable products for current ios subscriber with pack 1 only"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.iosPack1]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.getAt(0).name == 'com.bskyb.skysportstv.iap.monthly.pack1'
        userPurchasableProductsVo.notSubscribed.getAt(0).name == 'com.bskyb.skysportstv.iap.monthly.pack2'
    }

    def "test purchasable products for current ios subscriber with pack 2 only"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.iosPack2]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.getAt(0).name == 'com.bskyb.skysportstv.iap.monthly.pack2'
        userPurchasableProductsVo.notSubscribed.getAt(0).name == 'com.bskyb.skysportstv.iap.monthly.pack1'
    }


    def "test purchasable products for current ios subscriber with pack 1 and 2"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.iosPack1, SamplePurchaseData.iosPack2]
        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.getAt(0).name == 'com.bskyb.skysportstv.iap.monthly.pack1'
        userPurchasableProductsVo.subscribed.getAt(1).name == 'com.bskyb.skysportstv.iap.monthly.pack2'
        userPurchasableProductsVo.notSubscribed.isEmpty()
    }


    def "test purchasable products for current ios subscriber with pack 1 expired and 2 never purchased"() {
        given:
        def subscriptionVoList = [SamplePurchaseData.iosPack1Expired ]

        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.notSubscribed.getAt(0).name == 'com.bskyb.skysportstv.iap.monthly.pack1'
        userPurchasableProductsVo.notSubscribed.getAt(1).name == 'com.bskyb.skysportstv.iap.monthly.pack2'
    }

    def "test purchasable products for legacy ios subscriber when expired"() {
        given:

        def subscriptionVoList = [SamplePurchaseData.legacyIphoneSubExpired]

        when:
        UserPurchasableProductsVo userPurchasableProductsVo =
                productServiceImpl.getUserPurchasableProductsFor(
                        SubscriptionProvider.APPLE,
                        subscriptionVoList
                )
        then:
        userPurchasableProductsVo.subscribed.isEmpty()
        userPurchasableProductsVo.notSubscribed.getAt(0).name == SamplePurchaseData.legacyIphoneSubExpired.productId
    }

}


 

