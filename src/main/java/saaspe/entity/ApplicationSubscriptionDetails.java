package saaspe.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "saaspe_application_subscription_details")
public class ApplicationSubscriptionDetails {

	@NonNull
	@Column(name = "SUBSCRIPTION_NAME")
	private String subscriptionName;

	@Id
	@Column(name = "SUBSCRIPTION_ID")
	private String subscriptionId;

	@Column(name = "SUBSCRIPTION_NUMBER")
	private String subscriptionNumber;

	@NonNull
	@Column(name = "SUBSCRIPTION_PROVIDER")
	private String subscriptionProvider;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPLICATION_ID", foreignKey = @ForeignKey(name = "FK_APPLICATOIN_ID"))
	private ApplicationDetails applicationId;

	@OneToMany(mappedBy = "subscriptionDetails")
	// @JoinColumn(name = "CONTRACT_ID", foreignKey = @ForeignKey(name =
	// "FK_CONTRACT_ID"))
	private List<ApplicationContractDetails> contractId;

	@NonNull
	@Column(name = "SUBSCRIPTION_STATUS")
	private String subscriptionStatus;

	@NonNull
	@Column(name = "SUBSCRIPTION_OWNER")
	private String subscriptionOwner;

	@Column(name = "SUBSCRIPTION_OWNER_EMAIL")
	private String subscriptionOwnerEmail;

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

	@NonNull
	@Column(name = "SUBSCRIPTION_START_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date subscriptionStartDate;

	@NonNull
	@Column(name = "SUBSCRIPTION_RENEWAL_TERM")
	private String subscriptionRenewalTerm;

	@NonNull
	@Column(name = "SUBSCRTIPTION_NEXT_RENEWAL")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date subscriptionNextRenewal;

	@JsonIgnore
	@OneToOne(mappedBy = "subscriptionId")
	private ApplicationLicenseDetails licenseId;

	@NonNull
	@Column(name = "PAYMENT_METHOD")
	private String paymentMethod;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;
}
