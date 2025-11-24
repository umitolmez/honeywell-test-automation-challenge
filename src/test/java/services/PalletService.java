package services;

import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;

public class PalletService extends BaseService {

    private static final String ENDPOINT = "/misl/test-pallet-management/all";

    private static final String[] KNOWN_PRODUCTS = {
            "9990001", "9990002", "9990003",
            "99990001", "99990002", "99990003",
            "00000000009990001", "000000000099990001",
            "00000000009990002", "000000000099990002",
            "00000000009990003", "000000000099990003"
    };

    public SSCCResult getAnyAvailableSSCC(String locationId) {
        System.out.println("--- Checking on (" + locationId + ")---");

        for (String productCode : KNOWN_PRODUCTS) {
            try {
                Response response = given()
                        .spec(defaultRequestSpec())
                        .queryParam("rsku", productCode)
                        .queryParam("location", locationId)
                        .queryParam("amount", 1)
                        .get(ENDPOINT);

                if (response.statusCode() == 200) {
                    List<String> ssccList = response.jsonPath().getList("$");
                    if (ssccList != null && !ssccList.isEmpty()) {
                        String foundSSCC = ssccList.get(0);
                        System.out.println(">>> FOUNDED! Product: " + productCode + " | SSCC: " + foundSSCC);

                        return new SSCCResult(foundSSCC, productCode);
                    }
                }
            } catch (Exception e) {
            }
        }

        System.out.println("!!! THERE ARE NO PRODUCTS LEFT IN THIS WAREHOUSE !!!");
        throw new RuntimeException("CRITICAL: Warehouse (" + locationId + ") empty. The test data has been exhausted.");
    }

    public static class SSCCResult {
        public String sscc;
        public String productCode;
        public SSCCResult(String sscc, String productCode) {
            this.sscc = sscc;
            this.productCode = productCode;
        }
    }
}