package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class ContractDetailsOverviewResponse extends Response {

	private String contractName;

	private String providerName;

	private BigDecimal totalCost;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date upcomingRenewalDate;

	private Integer totalLicenses;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractStartDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractEndDate;

	private String renewalTerm;

	private Boolean autoRenewal;

	private String paymentMethod;

	private String cardHolderName;

	private String cardNumber;

	private String validThrough;

	private String walletName;

	private String currencyCode;

	private List<ContractLicenseDetailResponse> products;

	private String applicaitonName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date reminderDate;

	private Boolean contractStatus;

	private String contractType;

	private Integer autoRenewalCancellation;

	private String billingFrequency;

	private Integer licenseMapped;

	private Integer licenseUnMapped;
	
	private BigDecimal adminCost;
	
	private String subscriptionId;
}
