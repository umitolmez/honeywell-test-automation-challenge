package services;

import io.restassured.response.Response;
import models.OrderRequest;

import static io.restassured.RestAssured.given;

public class OrderService extends BaseService {

    private static final String ENDPOINT = "/v6/misl/masterdata/order/{sourceSystem}/{sourceId}";

    public Response createOrder(OrderRequest orderRequest) {

        return given()
                .spec(defaultRequestSpec())
                .pathParam("sourceSystem", orderRequest.getSourceSystem())
                .pathParam("sourceId", orderRequest.getSourceId())
                .body(orderRequest)
                .log().ifValidationFails()
                .when()
                .put(ENDPOINT)
                .then()
                .log().ifError()
                .extract().response();
    }
}
