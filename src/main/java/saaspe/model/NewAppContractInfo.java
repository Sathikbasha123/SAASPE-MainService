package saaspe.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class NewAppContractInfo implements Serializable {

	private static final long serialVersionUID = -299482035708790407L;

	private String contractType;

	private String billingFrequency;

	private Integer autoRenewalCancellation;

	private String contractName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractStartDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractEndDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date upcomingRenewalDate;

	private Boolean autoRenewal;

	private String paymentMethod;

	private String cardHolderName;

	// @NotEmpty(message = "The Password field is required.")
	@Size(min = 16, max = 16)
	private Long cardNumber;

	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private String validThrough;

	private String cvvNumber;

	private String walletName;

	private String contractTenure;

	private String currencyCode;
}
