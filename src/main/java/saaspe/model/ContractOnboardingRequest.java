package saaspe.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class ContractOnboardingRequest {

	private String contractName;
	private String contractType;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractEndDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractStartDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date upcomingRenewalDate;
	private Boolean autoRenewal;
	@JsonIgnore
	private String renewalTerm;
	private String contractTenure;
	private String paymentMethod;
	private String cardHolderName;
	private String cardNumber;
	private String validThrough;
	private String walletName;
	private String billingFrequency;
	private Integer autoRenewalCancellation;
	private String currencyCode;
	private List<Products> products;

}
