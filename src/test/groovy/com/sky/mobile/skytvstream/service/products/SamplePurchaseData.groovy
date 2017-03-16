package com.sky.mobile.skytvstream.service.products

import com.sky.mobile.skytvstream.domain.SubscriptionVo
import com.sky.mobile.skytvstream.utils.SubscriptionProvider

class SamplePurchaseData {

    static def legacyIphoneSub = SubscriptionVo.newInstance([productId: "com.bskyb.skysportstv.iap.monthly",
                                                             provider : SubscriptionProvider.APPLE.toString(), expiry: new Date()])

    static def legacyIphoneSubExpired = SubscriptionVo.newInstance([productId: "com.bskyb.skysportstv.iap.monthly",
                                                             provider : SubscriptionProvider.APPLE.toString(), expiry: new Date().minus(74)])

    static def legacyAndroidSub = SubscriptionVo.newInstance([productId: "android.legacy.pack",
                                                              provider : SubscriptionProvider.GOOGLE.toString(), expiry: new Date()])

    static def androidPack1 = SubscriptionVo.newInstance([productId: 'subscription.test.monthly.pack1',
                                                          provider : SubscriptionProvider.GOOGLE.toString(), expiry: new Date()])

    static def androidPack2 = SubscriptionVo.newInstance([productId: 'subscription.test.monthly.pack2',
                                                          provider : SubscriptionProvider.GOOGLE.toString(), expiry: new Date()])

    static def iosPack1 = SubscriptionVo.newInstance([productId: 'com.bskyb.skysportstv.iap.monthly.pack1',
                                                      provider : SubscriptionProvider.APPLE.toString(), expiry: new Date()])

    static def iosPack1Expired = SubscriptionVo.newInstance([productId: 'com.bskyb.skysportstv.iap.monthly.pack1',
                                                              provider : SubscriptionProvider.APPLE.toString(), expiry: new Date().minus(80)])

    static def iosPack2 = SubscriptionVo.newInstance([productId: 'com.bskyb.skysportstv.iap.monthly.pack2',
                                                      provider : SubscriptionProvider.APPLE.toString(), expiry: new Date()])

    static def vodafoneSub = SubscriptionVo.newInstance([productId: "Doesn't matter what pack we have",
                                                         provider : SubscriptionProvider.VODAFONE.toString(), expiry: new Date()])

    static def vodafoneCustomerSubList = [vodafoneSub]

    static def iosCustomerWithLegacySubList = [legacyIphoneSub]

    static def androidCustomerWithLegacySubList = [legacyAndroidSub]

}
