package tests;

import config.ConfigReader;
import models.ValidationRequest;
import services.*;
import io.restassured.response.Response;
import models.DeliveryNoteRequest;
import models.OrderRequest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.TestContext;
import utils.TestUtils;

import java.util.Collections;
import java.util.Map;

public class EndToEndTestWithFreshSSCC {

    private final OrderService orderService = new OrderService();
    private final DeliveryNoteService deliveryNoteService = new DeliveryNoteService();
    private final ShipmentService shipmentService = new ShipmentService();
    private final ValidationService validationService = new ValidationService();
    private final PalletService palletService = new PalletService();

    private final long MAX_POLLING_DURATION = Long.parseLong(ConfigReader.getProperty("max.polling.duration"));

    @DataProvider(name = "excelData")
    public Object[][] getExcelData() {
        return new Object[][] {
                //Warehouse, Warehouse Location
                { "QA-NL-TEST-3PL1", "urn:itg:id:tpd:fid:LEWL1FhY5k6k"}
                //{ "QA-NL-TEST-3PL2", "urn:itg:id:tpd:fid:LEWL1FRtsSRE"},
                //{ "QA-NL-TEST-3PL3", "urn:itg:id:tpd:fid:LEWL1F8IMPtO"}
        };
    }

    @Test(dataProvider = "excelData")
    public void testOrderAndDeliveryFlow(String warehouseCustomer, String location) throws InterruptedException {
        String freshSSCC;
        String validProductCode;

        // --- STEP 0: Smart Stock Check
        System.out.println("--- Step 0: Searching for ANY Available Stock ---");

        try {
            PalletService.SSCCResult result = palletService.getAnyAvailableSSCC(location);
            freshSSCC = result.sscc;
            validProductCode = result.productCode;

        } catch (RuntimeException e) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("WARNING: Actual stock not found in warehouse (Environment Data Exhausted)");
            System.err.println("Using RANDOM SSCC to demonstrate the test flow for Product Code:99990001");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            freshSSCC = TestUtils.generateSSCC();
            validProductCode = "99990001";
        }

        String formattedProduct = TestUtils.formatProductCode(validProductCode);

        // --- STEP 1: ORDER ---
        System.out.println("--- Step 1: Creating Order ---");

        String orderId = TestUtils.generateUUID("ORDER");

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
                        .sourceId(warehouseCustomer)
                        .sourceSystem("HONEYWELLQA").build())
                .isCompleted(false)
                .orderItems(Collections.singletonMap("000010", OrderRequest.OrderItem.builder()
                        .itemNumber("000010")
                        .unitOfMeasure("TSD")
                        .value("15.000")
                        .rsku(formattedProduct)
                        .build()))
                .build();

        Response orderResponse = orderService.createOrder(orderReq);
        Assert.assertEquals(orderResponse.getStatusCode(), 200, "Order Creation Failed!");
        System.out.println("Order Created: " + orderId);


        // --- STEP 2: DELIVERY NOTE ---
        System.out.println("--- Step 2: Creating Delivery Note ---");

        String deliveryNoteId = TestUtils.generateUUID("DN");

        // Sender
        DeliveryNoteRequest.Party senderParty = DeliveryNoteRequest.Party.builder()
                .sourceId(warehouseCustomer)
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
                freshSSCC,
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

                    TestContext.SHARED_SSCC = freshSSCC;
                    System.out.println("SSCC is saved for Web Testing: " + TestContext.SHARED_SSCC);

                    break;
                } else {
                    String typeStr = (replyObj != null) ? replyObj.getClass().getSimpleName() : "null";
                    System.out.println("Validation Pending. Status: " + status + ", Reply Type: " + typeStr);
                }
            } else {
                System.out.println("API Error: " + valResponse.getStatusCode());
            }

            System.out.println("Waiting... (Retrying in 10s)");
            Thread.sleep(10000);
        }

        Assert.assertTrue(isValidationSuccessful, "Validation timeout! Shipment could not be verified within "
                + MAX_POLLING_DURATION +" minutes.");
    }
}
