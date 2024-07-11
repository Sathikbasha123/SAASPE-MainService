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
@Table(name = "Invoice_Details")
public class InvoiceDetails {

	@Id
	@Column(name = "INVOICE_NUMBER")
	private String invoiceNumber;
	
	@Column(name = "SUBSCRIPTION_ID")
	private String subscriptionId;

	@Column(name = "INVOICE_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date invoiceDate;

	@Column(name = "BILL_PERIOD")
	private String billPeriod;

	@Column(name = "APPLICATION_ID")
	private String applicatoinId;

	@Column(name = "INVOICE_AMOUNT")
	private BigDecimal invoiceAmount;

	@Column(name = "AMOUNT_DUE")
	private BigDecimal amountDue;

	@Column(name = "CURRENCY")
	private String currency;

	@Column(name = "DUE_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date dueDate;

	@NonNull
	@Column(name = "LOGO_URL")
	private String logoUrl;

	@Column(name = "SUBSCRIPTION_EMAIL")
	private String subscriptionEmail;

	@Column(name = "SUBSCRIPTION_PLAN")
	private String subscriptionPlan;

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

	@NonNull
	@Column(name = "BUID")
	private String buID;

	@NonNull
	@Column(name = "OPID")
	private String opID = "SAASPE";

}
