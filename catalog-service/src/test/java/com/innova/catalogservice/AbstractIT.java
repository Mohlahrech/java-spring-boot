package com.innova.catalogservice;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class AbstractIT {
    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.reset();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
        RestAssured.authentication = RestAssured.DEFAULT_AUTH;
    }
}
