package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UpcomingRenewalsResponse {

    private String contractID;
    private String applicationID;
    private String applicationName;
    private String applicationLogo;
    private BigDecimal renewalAmount;
	private BigDecimal adminCost;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date renewalDate;
    private String currency;

}
