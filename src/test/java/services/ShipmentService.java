package services;

import config.ConfigReader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import utils.TestUtils;

import static io.restassured.RestAssured.given;

public class ShipmentService extends BaseService {

    private static final String ENDPOINT = "/v6/misl/epcis/capture";

    private static final String USERNAME = ConfigReader.getProperty("shipment.username");
    private static final String PASSWORD = ConfigReader.getProperty("shipment.password");

    public Response sendShipmentEvent(String eventTime, String sscc, String locationId,
                                      String messageId, String transactionId, String deliveryNoteId) {

        String xmlBody = TestUtils.readFileAsString("shipment_template.xml");

        xmlBody = xmlBody
                .replace("{{shipment.username}}", USERNAME)
                .replace("{{shipment.password}}", PASSWORD)
                .replace("{{EVENT_TIME}}", eventTime)
                .replace("{{SSCC}}", sscc)
                .replace("{{LOCATION_ID}}", locationId)
                .replace("{{MESSAGE_ID}}", messageId)
                .replace("{{TRANSACTION_ID}}", transactionId)
                .replace("{{DELIVERY_NOTE_ID}}", deliveryNoteId);

        return given()
                .spec(defaultRequestSpec())
                .auth().preemptive().basic(USERNAME, PASSWORD)
                .contentType(ContentType.XML)
                .body(xmlBody)
                .log().ifValidationFails()
                .when()
                .post(ENDPOINT)
                .then()
                .log().ifError()
                .extract().response();
    }
}
