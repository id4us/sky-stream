package com.sky.mobile.skytvstream.controller

import com.sky.mobile.skytvstream.domain.ChannelVo
import com.sky.mobile.skytvstream.domain.ProductVo
import com.sky.mobile.skytvstream.domain.SubscriptionVo
import com.sky.mobile.skytvstream.domain.UserPurchasableProductsVo
import com.sky.mobile.skytvstream.service.products.ProductService
import com.sky.mobile.skytvstream.service.subscription.SubscriptionsService
import com.sky.mobile.skytvstream.utils.StreamingHeaders
import com.sky.mobile.skytvstream.utils.SubscriptionProvider
import com.sky.mobile.ssmtv.oauth.vo.AuthenticatedPerson
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

class TestUserProductController extends Specification {

    def userProductController = new UserProductController()
    def subscriptionsService = Mock(SubscriptionsService.class)
    def productService = Mock(ProductService.class)
    def person = new AuthenticatedPerson()
    def currentRequest = new HashMap()
    def country = ['GB']

    SubscriptionProvider testSubscriptionProvider = SubscriptionProvider.APPLE
    def mySubscriptions = new ArrayList<SubscriptionVo>()

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userProductController).build()

    def setup() {
        person.profileId = "12324"
        userProductController.productService = productService
        userProductController.subService = subscriptionsService
        subscriptionsService.getAllSubscriptionsForUserByProfileId(person.profileId, testSubscriptionProvider) >> mySubscriptions
        currentRequest.put(StreamingHeaders.SUB_PROVIDER, testSubscriptionProvider.toString())
        currentRequest.put(StreamingHeaders.COUNTRY_ID,country[0])
        currentRequest.put(StreamingHeaders.VERSION,"v1_0")
        currentRequest.put(StreamingHeaders.CLIENT_ID,"435")
        userProductController.currentRequest = currentRequest
        userProductController.user = person
    }

    def "Happy test of the controller"() {

        given: "a valid subscriber with existing subscription for pack 1 and no pack 2"

        ProductVo productVoPack1 = ProductVo.newInstance([name                : "pack1",
                                                          description         : "pack 1 description",
                                                          displayText         : "pack 1 display text",
                                                          displayCost         : "pack 1 display cost",
                                                          purchaseId          : "pack 1 purchaseId",
                                                          subscriptionProvider: "subscription provider 1",
                                                          available           : "true"]
        )

        ChannelVo channelVoPack1 = ChannelVo.newInstance([channelid: "3333", channelName: "channel 1"])

        productVoPack1.getChannels().add(channelVoPack1)

        ProductVo productVoPack2 = ProductVo.newInstance([name                : "pack2",
                                                          description         : "pack 2 description",
                                                          displayText         : "pack 2 display text",
                                                          displayCost         : "pack 2 display cost",
                                                          purchaseId          : "pack 2 purchaseId",
                                                          subscriptionProvider: "subscription provider 2",
                                                          available           : "true"]
        )

        ChannelVo channelVoPack2 = ChannelVo.newInstance([channelid: "4353", channelName: "channel 2"])

        productVoPack2.getChannels().add(channelVoPack2)

        def userPurchases = new UserPurchasableProductsVo()
        userPurchases.addToSubscribed(productVoPack1)
        userPurchases.addToNotSubscribed(productVoPack2)

        productService.getUserPurchasableProductsFor(testSubscriptionProvider, mySubscriptions) >> userPurchases

        when: "a call is made to the controller, expecting a json response"

        def response = mockMvc.perform(get('/user/products')
                .contentType(APPLICATION_JSON)).andReturn().response

        then: "Expect an OK response and a valid product in a json response"
        response.status == OK.value()
        response.contentAsString == """{"subscribed":[{"available":true,"description":"pack 1 description","displayCost":"pack 1 display cost","displayText":"pack 1 display text","name":"pack1","purchaseId":"pack 1 purchaseId"}],"notSubscribed":[{"available":true,"description":"pack 2 description","displayCost":"pack 2 display cost","displayText":"pack 2 display text","name":"pack2","purchaseId":"pack 2 purchaseId"}]}"""
    }

    def "Happy test of the controller no subscriptions"() {

        given: "a valid subscriber with existing subscription for pack 1 and no pack 2"
        def userPurchases = new UserPurchasableProductsVo()

        productService.getUserPurchasableProductsFor(testSubscriptionProvider, mySubscriptions) >> userPurchases

        when: "a call is made to the controller, expecting a json response"

        def response = mockMvc.perform(get('/user/products')
                .contentType(APPLICATION_JSON)).andReturn().response

        then: "Expect an OK response and a valid product in a json response"
        response.status == OK.value()
        println response.contentAsString
        response.contentAsString == """{"subscribed":[],"notSubscribed":[]}"""
    }
}