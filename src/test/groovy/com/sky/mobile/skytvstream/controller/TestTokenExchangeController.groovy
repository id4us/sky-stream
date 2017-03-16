package com.sky.mobile.skytvstream.controller

import com.sky.mobile.skytvstream.domain.ExchangeTokenResponse
import com.sky.mobile.skytvstream.service.secure.TokenExchangeService
import com.sky.mobile.skytvstream.utils.StreamingHeaders
import com.sky.mobile.skytvstream.utils.SubscriptionProvider
import com.sky.mobile.ssmtv.oauth.exceptions.TokenExchangeException
import groovy.json.JsonSlurper
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

class TestTokenExchangeController extends Specification {

    def tokenController = new TokenExchangeController()
    def tokenServiceMock = Mock(TokenExchangeService)
    def currentRequest = new HashMap()

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(tokenController).build()

    def setup() {
        tokenController.tokenExchangeService = tokenServiceMock;
        currentRequest.put(StreamingHeaders.CLIENT_ID, "a device")
        tokenController.currentRequest = currentRequest
    }

    def "successful controller test"() {

        given: "a valid json message containing a valid access token"
        def testToken = "mySkyIdAccessCode"

        def expectedExchangeTokenResponse = new ExchangeTokenResponse()
        expectedExchangeTokenResponse.email = "abce@dd.com"
        expectedExchangeTokenResponse.oauthToken = "mySSTVToken"

        when: "a call is made to the controller, expecting a json response"

        def response = mockMvc.perform(post('/token')
                .contentType(APPLICATION_JSON)
                .content('{"token":"' + testToken + '"}')
        ).andReturn().response

        def content = new JsonSlurper().parseText(response.contentAsString)

        then: "Expect an OK response and a valid oauthToken in a json response"

        1 * tokenServiceMock.exchangeCode(testToken, "a device", _) >> expectedExchangeTokenResponse

        response.status == OK.value()
        content.oauthToken == expectedExchangeTokenResponse.oauthToken
        content.email == expectedExchangeTokenResponse.email
        content.APPLE == "INACTIVE"
        content.VODAFONE == "INACTIVE"
        content.GOOGLE == "INACTIVE"
    }

    def "unsuccessful controller test"() {

        given: "a valid json message containing an invalid access token"
        def testToken = "mySkyIdAccessCode"

        when: "a call is made to the controller, expecting a json response"

        def response = mockMvc.perform(post('/token')
                .contentType(APPLICATION_JSON)
                .content('{"token":"' + testToken + '"}')
        ).andReturn().response

        def content = response.getContentAsString()

        then: "Expect a 403 forbidden response, and the error message set"
        1 * tokenServiceMock.exchangeCode(testToken, "a device", _) >> {
            throw new TokenExchangeException("OOGWAY ERROR PROVIDING TOKEN")
        }

        response.status == FORBIDDEN.value;
        content == "{\"code\":\"9300\",\"message\":\"Invalid access code or Oogway is in error\"}"
    }

    def "check default subscription status"() {
        when:
        def expectedExchangeTokenResponse = new ExchangeTokenResponse()
        then:
        expectedExchangeTokenResponse.appleSubscriptionStatus == "INACTIVE"
        expectedExchangeTokenResponse.vodafoneSubscriptionStatus == "INACTIVE"
        expectedExchangeTokenResponse.googleSubscriptionStatus == "INACTIVE"
    }

    def "check subscription status no subs"() {
        when:
        def expectedExchangeTokenResponse = new ExchangeTokenResponse()
        expectedExchangeTokenResponse.setSubscriptionStatus([])
        then:
        expectedExchangeTokenResponse.appleSubscriptionStatus == "INACTIVE"
        expectedExchangeTokenResponse.vodafoneSubscriptionStatus == "INACTIVE"
        expectedExchangeTokenResponse.googleSubscriptionStatus == "INACTIVE"
    }

    def "check subscription status an active subs"() {
        when:
        def expectedExchangeTokenResponse = new ExchangeTokenResponse()
        expectedExchangeTokenResponse.setSubscriptionStatus([SubscriptionProvider.VODAFONE])
        then:
        expectedExchangeTokenResponse.appleSubscriptionStatus == "INACTIVE"
        expectedExchangeTokenResponse.vodafoneSubscriptionStatus == "ACTIVE"
        expectedExchangeTokenResponse.googleSubscriptionStatus == "INACTIVE"
    }

    def "check subscription status all active subs"() {
        when:
        def expectedExchangeTokenResponse = new ExchangeTokenResponse()
        expectedExchangeTokenResponse.setSubscriptionStatus([SubscriptionProvider.VODAFONE, SubscriptionProvider.APPLE, SubscriptionProvider.GOOGLE])
        then:
        expectedExchangeTokenResponse.appleSubscriptionStatus == "ACTIVE"
        expectedExchangeTokenResponse.vodafoneSubscriptionStatus == "ACTIVE"
        expectedExchangeTokenResponse.googleSubscriptionStatus == "ACTIVE"
    }
}
