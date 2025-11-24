package services;

import config.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class BaseService {

    protected static final String BASE_URI = ConfigReader.getProperty("base.url");
    protected static final String DEFAULT_USER = ConfigReader.getProperty("app.username");
    protected static final String DEFAULT_PASS = ConfigReader.getProperty("app.password");


    protected RequestSpecification defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setAuth(io.restassured.RestAssured.preemptive().basic(DEFAULT_USER, DEFAULT_PASS))
                .setContentType(ContentType.JSON)
                .build();
    }
}
