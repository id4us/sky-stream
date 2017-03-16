package com.sky.mobile.skytvstream.service.secure

import com.sky.mobile.ssmtv.oauth.secure.OauthTokenTranslator
import com.sky.mobile.ssmtv.oauth.secure.OauthTokenTranslatorImpl
import spock.lang.Specification

class TestOauthTokenTranslator extends Specification {

    OauthTokenTranslator oauthTokenTranslator

    def setup(){
        oauthTokenTranslator = new OauthTokenTranslatorImpl();
        oauthTokenTranslator.streamTokenSalt ="somesalt"
    }

    def "test can encryptOauth"() {
        given:
        def oauthToken = "oauth_token_1"
        def deviceId = "device_id_1"
        when:
        def encryptedOauthToken = oauthTokenTranslator.oauthEncrypt(oauthToken, deviceId)
        then:
        encryptedOauthToken == "mkkOdZkpZDc7bggzZPqL5sJsdC6Ht2XRmdIEtZy5ekY="
    }

    def "test can encryptOauth with real values"() {
        given:
        def oauthToken = "91b2a06ae9a75deb5f3d43f7e6b0b40057342fcf6042c24883b5aa8b410de648"
        def deviceId = "1615CF02-7CBE-4B7B-96D0-C2B4C01054BA"
        when:
        def encryptedOauthToken = oauthTokenTranslator.oauthEncrypt(oauthToken, deviceId)
        then:
        encryptedOauthToken == "aPrC9z8m6WIYhjDbbnUkqEOFcvhdmX4AT6P4O7pPRO45DM/2lG6wVSIvsvhl6UvVdTNDLtZQRvLV7vcmI6M0hAqNlVn3OyWs0/Flzoa5anJrP3CEZo0rKgbf4HOL71PfFi3b63ZlCB0+4oDT11v2bg=="
    }

    def "test can encryptOauth with real values and odd UDID"() {
        given:
        def oauthToken = "91b2a06ae9a75deb5f3d43f7e6b0b40057342fcf6042c24883b5aa8b410de648"
        def deviceId = "1615CF02-7CBE-4B7B-96D0-C2B4C01054BAAAA"
        when:
        def encryptedOauthToken = oauthTokenTranslator.oauthEncrypt(oauthToken, deviceId)
        then:
        encryptedOauthToken == "aPrC9z8m6WIYhjDbbnUkqEOFcvhdmX4AT6P4O7pPRO45DM/2lG6wVSIvsvhl6UvVyQJmLQx6PDvn3g/U1aDTMf/9iE8k8b4NXvkywAxtkZsFZjdoaZW/4x5UYdfbwVMbNqQl2iyAvyB2ONBRvVIQ5w=="
    }

    def "test can decryptOauth"() {
        given:
        def oauthToken = "oauth_token_1"
        def encryptedOauthToken = "mkkOdZkpZDc7bggzZPqL5sJsdC6Ht2XRmdIEtZy5ekY="
        def deviceId = "device_id_1"

        when:
        def oAuthToken = oauthTokenTranslator.oauthDecrypt(encryptedOauthToken)
        then:
        oAuthToken.oauth == oauthToken
        oAuthToken.deviceId == deviceId
    }

    def "test can decryptOauth with real values"() {
        given:
        def oauthToken = "91b2a06ae9a75deb5f3d43f7e6b0b40057342fcf6042c24883b5aa8b410de648"
        def encryptedOauthToken = "aPrC9z8m6WIYhjDbbnUkqEOFcvhdmX4AT6P4O7pPRO45DM/2lG6wVSIvsvhl6UvVdTNDLtZQRvLV7vcmI6M0hAqNlVn3OyWs0/Flzoa5anJrP3CEZo0rKgbf4HOL71PfFi3b63ZlCB0+4oDT11v2bg=="
        def deviceId = "1615CF02-7CBE-4B7B-96D0-C2B4C01054BA"

        when:
        def oAuthToken = oauthTokenTranslator.oauthDecrypt(encryptedOauthToken)
        then:
        oAuthToken.oauth == oauthToken
        oAuthToken.deviceId == deviceId
    }

    def "test can decryptOauth with real values with odd number UDID"() {
        given:
        def oauthToken = "91b2a06ae9a75deb5f3d43f7e6b0b40057342fcf6042c24883b5aa8b410de648"
        def encryptedOauthToken = "aPrC9z8m6WIYhjDbbnUkqEOFcvhdmX4AT6P4O7pPRO45DM/2lG6wVSIvsvhl6UvVyQJmLQx6PDvn3g/U1aDTMf/9iE8k8b4NXvkywAxtkZsFZjdoaZW/4x5UYdfbwVMbNqQl2iyAvyB2ONBRvVIQ5w=="
        def deviceId = "1615CF02-7CBE-4B7B-96D0-C2B4C01054BAAAA"

        when:
        def oAuthToken = oauthTokenTranslator.oauthDecrypt(encryptedOauthToken)
        then:
        oAuthToken.oauth == oauthToken
        oAuthToken.deviceId == deviceId
    }

}


 

