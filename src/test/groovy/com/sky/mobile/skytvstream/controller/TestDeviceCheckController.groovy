package com.sky.mobile.skytvstream.controller

import com.sky.mobile.skytvstream.domain.DeviceVo
import com.sky.mobile.skytvstream.service.device.DeviceService
import com.sky.mobile.skytvstream.utils.StreamingHeaders
import groovy.json.JsonSlurper
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

class TestDeviceCheckController extends Specification {

    def deviceCheckController = new DeviceCheckController()
    def deviceServiceMock = Mock(DeviceService)
    def currentRequest = new HashMap()

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(deviceCheckController).build()

    def setup() {
        deviceCheckController.deviceService = deviceServiceMock;
        deviceCheckController.currentRequest = currentRequest
    }

    def "test supported device"() {

        given: "we use a supported device"

            def model ="supportedModel"

            def deviceVo = new DeviceVo()

            currentRequest.put(StreamingHeaders.MODEL_ID, model)

        when: "a call is made to the controller, expecting a json response"

            def response = mockMvc.perform(get('/supported/device').contentType(APPLICATION_JSON)).andReturn().response

            def content = new JsonSlurper().parseText(response.contentAsString)

        then: "Expect an OK response and successful json response"

            1 * deviceServiceMock.getDeviceVo(model) >> deviceVo;

            1 * deviceServiceMock.isSupportDevice(deviceVo) >> true

            response.status == OK.value()
            content.supported == "true"
    }

    def "test unsupported device"() {

        given: "we use a unsupported device"

            def model ="unSupportedModel"

            def deviceVo = new DeviceVo()

            currentRequest.put(StreamingHeaders.MODEL_ID, model)

        when: "a call is made to the controller, expecting a forbidden response"

            def response = mockMvc.perform(get('/supported/device').contentType(APPLICATION_JSON)).andReturn().response

        then: "Expect an forbidden response"

            1 * deviceServiceMock.getDeviceVo(model) >> deviceVo;

            1 * deviceServiceMock.isSupportDevice(deviceVo) >> false

            response.status == FORBIDDEN.value()

    }
}
