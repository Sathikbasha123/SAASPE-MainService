package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class MarketPlaceEmailRequest {

    private String productId;

    private String productName;

    private String enquiryMessage;

}
