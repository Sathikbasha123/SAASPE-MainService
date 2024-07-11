package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "saaspe_application_onboarding")
public class ApplicationOnboarding {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "NUMBER")
	private Integer number;

	@Column(name = "APPLCATION_ONBOARD_REQUEST", length = 50000)
	private String applcationOnboardRequest;

	@Column(name = "REQUEST_NUMBER")
	private String requestNumber;

	@Column(name = "CHILD_REQUEST_NUMBER")
	private String childRequestNumber;

	@Column(name = "ONBOARDED_BY_USEREMAIL")
	private String onboardedByUserEmail;

	@Column(name = "APPLICATION_NAME")
	private String applicationName;

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

	@Column(name = "WORK_GROUP_USEREMAIL")
	private String workGroupUserEmail;

	@Column(name = "COMMENTS")
	private String comments;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "ONBOARD_DATE")
	private Date onBoardDate;

	@Column(name = "ONBOARDING_STATUS")
	private String onboardingStatus;

	@Column(name = "OWNER_DEPARTMENT")
	private String ownerDepartment;

	@Column(name = "PROJECT_NAME")
	private String projectName;
}
