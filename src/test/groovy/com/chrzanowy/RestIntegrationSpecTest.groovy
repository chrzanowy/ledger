package com.chrzanowy

import groovy.transform.CompileStatic
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import org.springframework.boot.test.web.server.LocalServerPort

class RestIntegrationSpecTest extends BaseIntegrationSpec {

    @LocalServerPort
    private int serverPort;

    @CompileStatic
    def given() {
        RestAssured.given(new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(serverPort)
                .build())
                .header("Content-Type", "application/json")
    }
}
