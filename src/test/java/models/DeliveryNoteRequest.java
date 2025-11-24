package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryNoteRequest {

    private String sourceId;

    @JsonProperty("isCancelled")
    private String isCancelled;

    private Party soldToParty;
    private String sourceSystem;
    private String type;

    @JsonProperty("isActive")
    private String isActive;

    @JsonProperty("customsDataRelevant")
    private String customsDataRelevant;

    private Map<String, DeliveryItem> deliveryItems;

    @JsonProperty("consignmentDone")
    private String consignmentDone;

    private Party sender;
    private Party location;

    // --- Inner Classes ---

    @Data
    @Builder
    public static class Party {
        private String sourceId;
        private String sourceSystem;
    }

    @Data
    @Builder
    public static class DeliveryItem {
        private String itemNumber;
        private OrderItem orderItem;
        private OrderReference order;
    }

    @Data
    @Builder
    public static class OrderItem {
        private String itemNumber;
        private String unitOfMeasure;
        private String value;
        private String rsku;
    }

    @Data
    @Builder
    public static class OrderReference {
        private String sourceId;
        private String sourceSystem;
    }
}