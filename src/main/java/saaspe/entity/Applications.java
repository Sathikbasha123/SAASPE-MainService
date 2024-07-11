package saaspe.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="saaspe_applications")
public class Applications {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="APPLICATION_ID")
	private String applicationId;
	
	@Column(name="ACTIVE_CONTRACTS")
	private Integer activeContracts;
	
	@Column(name="APPLICATION_DESCRIPTION")
	private String applicationDescription;
	
	@Column(name="APPLICATION_NAME")
	private String applicationName;
	
	@Column(name="APPLICATION_STATUS")
	private String applicationStatus;
	
	@Column(name="BUID")
	private String buId;
	
	@Column(name="CATEGORY_ID")
	private String categoryId;
	
	@Column(name="CATEGORY_NAME")
	private String categoryName;
	
	@Column(name="CREATED_BY")
	private String createdBy;
	
	@Column(name="CREATED_ON")
	private Date createdOn;
	
	@Column(name="END_DATE")
	private Date endDate;
	
	@Column(name="LOGO_URL")
	private String logoUrl;
	
	@Column(name="OPID")
	private String opId;
	
	@Column(name="OWNER")
	private String owner;
	
	@Column(name="OWNER_DEPARTMENT")
	private String ownerDepartment;
	
	@Column(name="OWNER_EMAIL")
	private String ownerEmail;
	
	@Column(name="RENEWAL_DATE")
	private Date renewalDate;
	
	@Column(name="START_DATE")
	private Date startDate;
	
	@Column(name="TAGS")
	private String tags;
	
	@Column(name="UPDATED_BY")
	private String updatedBy;
	
	@Column(name="UPDATED_ON")
	private Date updatedOn;
	
	@Column(name="PROJECT_NAME")
	private String projectName;
	
	@Column(name="GRAPH_APPLICATION_ID")
	private String graphApplicationId;
	
	@Column(name="SSO_ENABLED")
	private Boolean ssoEnabled;
	
	@Column(name="IDENTITY_PROVIDER")
	private String identityProvider;
	
	@Column(name="CONTRACT_ID")
	private String contractId;
	
	@Column(name="AUTO_RENEW")
	private Boolean autoRenew;
	
	@Column(name="CONTRACT_CURRENCY")
	private String contractCurrency;
	
	@Column(name="CONTRACT_DESCRIPTION")
	private String contractDescription;
	
	@Column(name="CONTRACT_END_DATE")
	private Date contractEndDate;
	
	@Column(name="CONTRACT_NAME")
	private String contractName;
	
	@Column(name="CONTRACT_NOTICE_DATE")
	private Date contractNoticeDate;
	
	@Column(name="CONTRACT_OWNER")
	private String contractOwner;
	
	@Column(name="CONTRACT_OWNER_EMAIL")
	private String contractOwnerEmail;
	
	@Column(name="CONTRACT_PAYMENT_METHOD")
	private String paymentMethod;
	
	@Column(name="CONTRACT_PAYMENT_TERM")
	private String paymentTerm;
	
	@Column(name="CONTRACT_PROVIDER")
	private String contractProvider;
	
	@Column(name="CONTRACT_START_DATE")
	private Date contractStartDate;
	
	@Column(name="CONTRACT_STATUS")
	private String contractStatus;
	
	@Column(name="REMINDER_DATE")
	private Date reminderDate;
	
	@Column(name="RENEWAL_TERM")
	private String renewalTerm;
	
	@Column(name="SUBSCRIPTION_ID")
	private String subscriptionId;
	
	@Column(name="SUBSCRIPTION_NAME")
	private String subscriptionName;
	
	@Column(name="SUBSCRIPTION_START_DATE")
	private Date subscriptionStartDate;
	
	@Column(name="SUBSCRIPTION_END_DATE")
	private Date subscriptionEndDate;
	
	@Column(name="SUBSCRIPTION_PROVIDER")
	private String subscriptionProvider;
	
	@Column(name="SUBSCRIPTION_OWNER")
	private String subscriptionOwner;
	
	@Column(name="TENANT_ID")
	private String tenantId;
	
	@Column(name="CONTRACT_TYPE")
	private String contractType;
	
	@Column(name="BILLING_FREQUENCY")
	private String billingFrequency;
	
	@Column(name="AUTO_RENEWAL_CANCELLATION")
	private Integer autoRenewalCancellation;
	
	@Column(name="CONTRACT_TENURE")
	private Integer contractTenure;
	
	@Column(name="LICENSE_ID")
	private String licenseId;
	
	@Column(name="CURRENCY")
	private String currency;
	
	@Column(name="PRODUCT_CATEGORY")
	private String productCategory;
	
	@Column(name="UNIT_PRICE")
	private BigDecimal unitPrice;
	
	@Column(name="PRODUCT_NAME")
	private String productName;
	
	@Column(name="QUANTITY")
	private Integer quantity;
	
	@Column(name="LICENSE_MAPPED")
	private Integer licenseMapped;
	
	@Column(name="LICENSE_UNMAPPED")
	private Integer licenseUnmapped;
	
	@Column(name="LICENSE_START_DATE")
	private Date licenseStartDate;
	
	@Column(name="LICENSE_END_DATE")
	private Date licenseEndDate;
	
	@Column(name="UNIT_PRICE_TYPE")
	private String unitPriceType;
	
	@Column(name="TOTAL_COST")
	private BigDecimal totalCost;
	
	@Column(name="CONVERTED_COST")
	private BigDecimal convertedCost;
	
	@Column(name="APPLICATION_DEPARTMENT_ID")
	private String departmentId;
	
	@Column(name="APPLICATION_DEPARTMENT_NAME")
	private String departmentName;
	
	@Column(name="PAYMENT_DESCRIPTION")
	private String paymentDescription;
	
	@Column(name="PAYMENT_TRANSACTION_DATE")
	private Date paymentTransactionDate;
	
	@Column(name="PAYMENT_AMOUNT")
	private BigDecimal paymentAmount;
	
	@Column(name="CARDHOLDER_NAME")
	private String cardholderName;
	
	@Column(name="CARD_NUMBER")
	private String cardNumber;
	
	@Column(name="VALID_THROUGH")
	private String validThrough;
	
	@Column(name="WALLET_NAME")
	private String walletName;
	
	@Column(name="PAYMENT_START_DATE")
	private Date paymentStartDate;
	
	@Column(name="PAYMENT_END_DATE")
	private Date paymentEndDate;
	
	@Column(name="PAYMENT_SECRET_KEY")
	private String paymentSecretKey;
	

}
