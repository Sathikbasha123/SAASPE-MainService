package saaspe.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@NoArgsConstructor
@Data
@Table(name = "payment_details")
public class PaymentDetails {

	@Column(name = "APPLICATION_ID")
	private String applicationId;

	@Column(name = "DESCRIPTION", length = 500)
	private String description;

	@Column(name = "TRANSACTION_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date transactionDate;

	@Column(name = "AMMOUNT")
	private BigDecimal amount;

	@Column(name = "PAYMENT_METHOD")
	private String paymentMethod;

	@Id
	@Column(name = "INVOICE_NO")
	private String invoiceNo;

	@NonNull
	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@NonNull
	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "CARDHOLDER_NAME")
	private String cardholderName;

	@Column(name = "CARD_NUMBER")
	private String cardNumber;

	@Column(name = "VALID_THROUGH")
	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-YYYY")
	private String validThrough;

	@Column(name = "WALLET_NAME")
	private String walletName;

	@NonNull
	@Column(name = "BUID")
	private String buID;

	@NonNull
	@Column(name = "OPID")
	private String opID = "SAASPE";

	@NonNull
	@Column(name = "LOGO_URL")
	private String logoUrl;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;
	
	@Column(name = "SECRET_KEY")
	private String secretKey;

	@Column(name = "CONTRACT_ID")
	private String contractId;

}
