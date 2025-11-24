package services;

import io.restassured.response.Response;
import models.DeliveryNoteRequest;

import static io.restassured.RestAssured.given;

public class DeliveryNoteService extends BaseService{

    private static final String ENDPOINT = "/v6/misl/masterdata/delivery-note/{sourceSystem}/{sourceId}";

    public Response createDeliveryNote(DeliveryNoteRequest request) {

        return given()
                .spec(defaultRequestSpec())
                .pathParam("sourceSystem", request.getSourceSystem())
                .pathParam("sourceId", request.getSourceId())
                .body(request)
                .log().ifValidationFails()
                .when()
                .put(ENDPOINT)
                .then()
                .log().ifError()
                .extract().response();
    }
}
