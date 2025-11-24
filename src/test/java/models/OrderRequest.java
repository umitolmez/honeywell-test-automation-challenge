package models;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {
    private String sourceId;
    private String date;

    @JsonProperty("isCancelled")
    private boolean isCancelled;
    private String sourceSystem;
    private boolean isActive;

    private Map<String, OrderItem> orderItems;

    private String number;
    private String eventTime;
    private String buyerOrderNumber;
    private Customer customer;

    @JsonProperty("isCompleted")
    private boolean isCompleted;

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
    public static class Customer {
        private String sourceId;
        private String sourceSystem;
    }
}