package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class RenewalsListResponse {

	private String subscriptionName;

	private String contractName;
	
	private String contractId;

	private String applicationName;

	private String applicationLogo;

	private BigDecimal totalCost;
	
	private BigDecimal adminCost;

	private String currency;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date upcomingRenewalDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date reminderDate;

	private Boolean paymentEnable;

}
