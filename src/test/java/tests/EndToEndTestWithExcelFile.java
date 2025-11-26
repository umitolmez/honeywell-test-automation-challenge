package tests;

import config.ConfigReader;
import models.ValidationRequest;
import services.DeliveryNoteService;
import services.OrderService;
import services.ShipmentService;
import io.restassured.response.Response;
import models.DeliveryNoteRequest;
import models.OrderRequest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.ValidationService;
import utils.ExcelUtils;
import utils.TestUtils;

import java.util.Collections;
import java.util.Map;

public class EndToEndTestWithExcelFile {

    private final OrderService orderService = new OrderService();
    private final DeliveryNoteService deliveryNoteService = new DeliveryNoteService();
    private final ShipmentService shipmentService = new ShipmentService();
    private final ValidationService validationService = new ValidationService();

    private final long MAX_POLLING_DURATION = Long.parseLong(ConfigReader.getProperty("max.polling.duration"));

    // Excel File Reader
    @DataProvider(name = "excelData")
    public Object[][] getExcelData() {
        return ExcelUtils.getExcelData("ShipmentData.xlsx", "Pallet SSCCs available");
    }

    @Test(dataProvider = "excelData")
    public void testOrderAndDeliveryFlow(String product, String warehouseNameFromExcel, String location, String sscc) throws InterruptedException {

        String systemCustomerId = getSystemCustomerId(warehouseNameFromExcel);
        System.out.println("Mapping: '" + warehouseNameFromExcel + "' -> '" + systemCustomerId + "'");

        // --- STEP 1: ORDER ---
        System.out.println("--- Step 1: Creating Order ---");

        String orderId = TestUtils.generateUUID("ORDER");
        String formattedProduct = TestUtils.formatProductCode(product);

        OrderRequest orderReq = OrderRequest.builder()
                .sourceId(orderId)
                .sourceSystem("ITQCLNT500")
                .date(TestUtils.getCurrentDate())
                .isCancelled(false)
                .isActive(true)
                .number(orderId)
                .eventTime(TestUtils.getIsoDateTime())
                .buyerOrderNumber("BUYER-" + orderId)
                .customer(OrderRequest.Customer.builder()
                        .sourceId(systemCustomerId)
                        .sourceSystem("HONEYWELLQA").build())
                .isCompleted(false)
                .orderItems(Collections.singletonMap("000010", OrderRequest.OrderItem.builder()
                        .itemNumber("000010")
                        .unitOfMeasure("TSD")
                        .value("15.000")
                        .rsku(formattedProduct).build()))
                .build();

        Response orderResponse = orderService.createOrder(orderReq);
        Assert.assertEquals(orderResponse.getStatusCode(), 200, "Order Creation Failed!");
        System.out.println("Order Created: " + orderId);


        // --- STEP 2: DELIVERY NOTE ---
        System.out.println("--- Step 2: Creating Delivery Note ---");

        String deliveryNoteId = TestUtils.generateUUID("DN");

        // Sender
        DeliveryNoteRequest.Party senderParty = DeliveryNoteRequest.Party.builder()
                .sourceId(systemCustomerId)
                .sourceSystem("HONEYWELLQA")
                .build();

        // SoldTo & Location
        DeliveryNoteRequest.Party destinationParty = DeliveryNoteRequest.Party.builder()
                .sourceId("QA-NL-TEST-3PL2")
                .sourceSystem("HONEYWELLQA")
                .build();

        // Order Item
        DeliveryNoteRequest.OrderItem dnItem = DeliveryNoteRequest.OrderItem.builder()
                .itemNumber("000010")
                .unitOfMeasure("TSD")
                .value("15.000")
                .rsku(formattedProduct)
                .build();

        // Order Reference
        DeliveryNoteRequest.OrderReference orderRef = DeliveryNoteRequest.OrderReference.builder()
                .sourceId(orderId)
                .sourceSystem("ITQCLNT500")
                .build();

        // Creating Delivery Item
        DeliveryNoteRequest.DeliveryItem deliveryItemObj = DeliveryNoteRequest.DeliveryItem.builder()
                .itemNumber("000010")
                .orderItem(dnItem)
                .order(orderRef)
                .build();

        // Delivery Note Object
        DeliveryNoteRequest dnReq = DeliveryNoteRequest.builder()
                .sourceId(deliveryNoteId)
                .sourceSystem("ITQCLNT500")
                .isCancelled("false")
                .isActive("true")
                .type("1")
                .customsDataRelevant("false")
                .consignmentDone("false")
                .sender(senderParty)
                .soldToParty(destinationParty)
                .location(destinationParty)
                .deliveryItems(Collections.singletonMap("000010", deliveryItemObj))
                .build();

        Response dnResponse = deliveryNoteService.createDeliveryNote(dnReq);

        // For Debugging
        // dnResponse.prettyPrint();

        Assert.assertEquals(dnResponse.getStatusCode(), 200, "Delivery Note Creation Failed!");
        System.out.println("Delivery Note Created: " + deliveryNoteId);

        // --- STEP 3: SHIPMENT EVENT (XML) ---
        System.out.println("--- Step 3: Sending Shipment Event ---");

        String messageId = TestUtils.generateUUID("MSG");
        String transactionId = TestUtils.generateUUID("TRX");
        String eventTime = TestUtils.getIsoDateTime();

        // location attribute should be -> "urn:itg:id:tpd:fid:..."
        Response shipmentResponse = shipmentService.sendShipmentEvent(
                eventTime,
                sscc,
                location,
                messageId,
                transactionId,
                deliveryNoteId
        );

        Assert.assertEquals(shipmentResponse.getStatusCode(), 200, "Shipment Event Failed!");
        System.out.println("Shipment Event Sent Successfully. MessageID: " + messageId);


        // --- STEP 4: VALIDATION (POLLING) ---
        System.out.println("--- Step 4: Polling for Validation Result ---");

        ValidationRequest validationReq = ValidationRequest.builder()
                .locationId(location)
                .messageId(messageId)
                .build();

        boolean isValidationSuccessful = false;
        long startTime = System.currentTimeMillis();
        long maxDuration = MAX_POLLING_DURATION * 60 * 1000;

        while (System.currentTimeMillis() - startTime < maxDuration) {
            Response valResponse = validationService.getValidationResult(validationReq);

            if (valResponse.getStatusCode() == 200) {
                String status = valResponse.jsonPath().getString("status");

                Object replyObj = valResponse.jsonPath().get("reply");

                boolean isMatch = false;

                if (replyObj instanceof Map) {
                    Map<?, ?> replyMap = (Map<?, ?>) replyObj;
                    if (replyMap != null && !replyMap.isEmpty()) {
                        if (replyMap.containsKey(transactionId)) {
                            isMatch = true;
                        }
                    }
                }

                if ("ok".equalsIgnoreCase(status) && isMatch) {
                    System.out.println("Validation Successful!");
                    System.out.println("Final Response: " + valResponse.asString());
                    isValidationSuccessful = true;
                    break;
                } else {
//                    String typeStr = (replyObj != null) ? replyObj.getClass().getSimpleName() : "null";
//                    System.out.println("Validation Pending. Status: " + status + ", Reply Type: " + typeStr);
                }
            } else {
                System.out.println("API Error: " + valResponse.getStatusCode());
            }

            System.out.println("Waiting... (Retrying in 1s)");
            Thread.sleep(1000);
        }

        Assert.assertTrue(isValidationSuccessful, "Validation timeout! Shipment could not be verified within "
                + MAX_POLLING_DURATION +" minutes.");
    }

    /**
     * Converts the human-readable storage name from Excel to the API ID.
     */
    private String getSystemCustomerId(String excelWarehouseName) {
        if (excelWarehouseName == null) return "UNKNOWN";

        if (excelWarehouseName.contains("3PL1")) {
            return "QA-NL-TEST-3PL1";
        } else if (excelWarehouseName.contains("3PL2")) {
            return "QA-NL-TEST-3PL2";
        } else if (excelWarehouseName.contains("3PL3")) {
            return "QA-NL-TEST-3PL3";
        } else {
            return excelWarehouseName;
        }
    }
}
