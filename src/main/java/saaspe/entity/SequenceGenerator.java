package saaspe.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "SEQUENCE_GENERATOR")
public class SequenceGenerator {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "TENANT_SEQUENCE")
	private Integer tenantSequence;

	@Column(name = "DEPARTMENT_SEQUENCE")
	private Integer departmentSequence;

	@Column(name = "USER_ONBOARDING")
	private Integer userOnboarding;

	@Column(name = "CATEGORY")
	private Integer applicationCategory;

	@Column(name = "CONTRACTS")
	private Integer applicationContacts;

	@Column(name = "APP_DETAILS")
	private Integer applicationDetails;

	@Column(name = "LICENSE")
	private Integer applicatiionLicense;

	@Column(name = "PROVIDERS")
	private Integer applicationProvider;

	@Column(name = "SUBSCRIPTION")
	private Integer applicationSubscription;

	@Column(name = "APPLICATION_REQUEST_ID")
	private Integer applicationRequestId;

	@Column(name = "REQUEST")
	private Integer requestId;

	@Column(name = "DEPT_REQUESTID")
	private Integer deptRequestId;

	@Column(name = "USER_REQUESTID")
	private Integer userRequestId;

	@Column(name = "PAYMENT_REQUESTID")
	private Integer paymentSequenceId;

	@Column(name = "PROJECT_SEQUENCEID")
	private Integer projectSequenceId;

	@Column(name = "CONTRACT_REQUEST_SEQUENCEID")
	private Integer contractRequestSequenceId;

	@Column(name = "CLOUD_SEQUENCEID")
	private Integer cloudSequenceId;

	@Column(name = "INVOICE_SEQUENCEID")
	private Integer invoiceSequenceId;

	//@Column(name = "CONVERSATION_SEQUENCEID")
	private Integer conversationsequenceId;
	
	@Column(name = "ENQUIRY_SEQUENCEID")
	private Integer enquirySequenceId;
}
