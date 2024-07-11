package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ContractsListResponse {

	private String contractId;
	private String contractName;
	private String applicationId;
	private String applicationName;
	private String applicationLogo;
	private String providerName;
	private String providerLogo;
	private String departmentName;

	private String contractStatus;
	private String contractType;
	private BigDecimal totalCost;
	private String currencyCode;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractStartDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractEndDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date upcomingRenewalDate;
	private String billingFrequency;
	private Boolean autoRenewal;
	private String paymentMethod;
	private String cardHolderName;
	private String cardNumber;
	private String walletName;
	private String validThrough;
	private String autoRenewalCancellation;
	private List<Products> products;
	private Integer productQuantity;

	private BigDecimal adminCost;

}
