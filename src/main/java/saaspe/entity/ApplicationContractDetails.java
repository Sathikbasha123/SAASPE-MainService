package saaspe.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "saaspe_application_contract_details")
public class ApplicationContractDetails {

	@NonNull
	@Column(name = "CONTRACT_NAME")
	private String contractName;

	@Id
	@Column(name = "CONTRACT_ID")
	private String contractId;

	@Column(name = "CONTRACT_PROVIDER")
	private String contractProvider;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TENANT_ID", foreignKey = @ForeignKey(name = "FK_TENANT_ID"))
	private TenantDetails tenantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPLICATION_ID", foreignKey = @ForeignKey(name = "FK_APPLICATOIN_ID"))
	private ApplicationDetails applicationId;

	@OneToMany(mappedBy = "contractId")
	private List<ApplicationLicenseDetails> licenseDetails;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SUBSCRIPTION_ID", foreignKey = @ForeignKey(name = "FK_SUBSCRIPTION_ID"))
	private ApplicationSubscriptionDetails subscriptionDetails;

	@Column(name = "CONTRACT_STATUS")
	private String contractStatus;

	@Column(name = "CONTRACT_OWNER")
	private String contractOwner;

	@Column(name = "CONTRACT_DESCRIPTION", length = 500)
	private String contractDescription;

	@Column(name = "CONTRACT_START_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractStartDate;

	@Column(name = "CONTRACT_END_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractEndDate;

	@Column(name = "CONTRACT_PAYMENT_TERM")
	private String contractPaymentTerm;

	@Column(name = "CONTRACT_PAYMENT_METHOD")
	private String contractPaymentMethod;

	@Column(name = "CONTRACT_NOTICE_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractNoticeDate;

	@Column(name = "CONTRACT_CURRENCY")
	private String contractCurrency;

	@Column(name = "AUTO_RENEW")
	private Boolean autoRenew;

	@NonNull
	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

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

	@Column(name = "RENEWAL_TERM")
	private String renewalTerm;

	// @NonNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "RENEWAL_DATE")
	private Date renewalDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "REMINDER_DATE")
	private Date reminderDate;

	@Column(name = "CONTRACT_OWNER_EMAIL")
	private String contractOwnerEmail;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@Column(name = "BILLING_FREQUENCY")
	private String billingFrequency;

	@Column(name = "AUTO_RENEWAL_CANCELLATION")
	private Integer autoRenewalCancellation;

	@Column(name = "CONTRACT_TYPE")
	private String contractType;

	@Column(name = "CONTRACT_TENURE")
	private Integer contractTenure;

}
