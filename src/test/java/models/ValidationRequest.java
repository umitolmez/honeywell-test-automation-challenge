package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationRequest {
    private String locationId;
    private String messageId;
}
