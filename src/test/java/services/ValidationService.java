package services;

import models.ValidationRequest;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ValidationService extends BaseService{

    private static final String ENDPOINT = "/misl/v6/workflow-tracker-service/validation-result/3pl-filter";

    public Response getValidationResult(ValidationRequest request) {
        return given()
                .spec(defaultRequestSpec())
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(ENDPOINT)
                .then()
                .log().ifError()
                .extract().response();
    }
}
