package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "SAASPE_CONTRACT_ONBOARDING")
public class ContractOnboardingDetails {

	@Id
	@Column(name = "NUMBER")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer number;

	@Column(name = "CONTRACT_ONBOARDING_REQUEST")
	private String contractOnboardingRequest;

	@Column(name = "REQUEST_NUMBER")
	private String requestNumber;

	@Column(name = "CHILD_REQUEST_NUMBER")
	private String childRequestNumber;

	@Column(name = "ONBOARDED_BY_USEREMAIL")
	private String onboardedByUserEmail;

	@Column(name = "OPID")
	private String opID;

	@Column(name = "BUID")
	private String buID;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "CREATED_ON")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "UPDATED_ON")
	private Date updatedOn;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "WORK_GROUP")
	private String workGroup;

	@Column(name = "APPROVED_REJECTED")
	private String approvedRejected;

	@Column(name = "WORKGROUP_USEREMAIL")
	private String workGroupUserEmail;

	@Column(name = "COMMENTS")
	private String comments;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "ONBOARD_DATE")
	private Date onboardDate;

	@Column(name = "ONBOARDING_STATUS")
	private String onboardingStatus;

	@Column(name = "CONTRACT_NAME")
	private String contractName;

	@Column(name = "application_name")
	private String applicationName;
	
	@Column(name = "application_id")
    private String applicationId;

}
