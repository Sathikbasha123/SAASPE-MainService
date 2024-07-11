package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saaspe.configuration.ValidPassword;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "saaspe_user_onboarding")
public class UserOnboarding {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "NUMBER")
	private Integer number;
	
	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;

	@Column(name = "USER_EMAIL")
	private String userEmail;

	@Column(name = "ONBOARD_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date onboardDate;

	@Column(name = "JOINING_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date joiningDate;

	@Column(name = "USER_DESIGNATION")
	private String userDesignation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEPARTMENT_ID", foreignKey = @ForeignKey(name = "FK_DEPARTMENT_ID"))
	private DepartmentDetails departmentId;

	@Column(name = "USER_REPORTING_MANAGER")
	private String userReportingManager;

	@Column(name = "USER_TYPE")
	private String userType;

	@Column(name = "GENDER")
	private String gender;

	@Column(name = "REQUEST_NUMBER")
	private String requestNumber;

	@Column(name = "CHILD_REQUEST_NUMBER")
	private String childRequestNumber;

	@Column(name = "MOBILE_NUMBER")
	private String mobileNumber;

	@ValidPassword
	@Column(name = "PASSWORD")
	private String passWord;

	@Column(name = "SECURITY_QUESTION")
	private String securityQuestion;

	@Column(name = "SECURITY_ANSEWER")
	private String securityAnsewer;

	@Column(name = "OPID")
	private String opID = "SAASPE";

	@Column(name = "BUID")
	private String buID;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@Column(name = "WORK_GROUP")
	private String workGroup;

	@Column(name = "APPROVED_REJECTED")
	private String approvedRejected;

	@Column(name = "WORK_GROUP_USEREMAIL")
	private String workGroupUserEmail;

	@Column(name = "COMMENTS")
	private String comments;

	@Column(name = "END_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;

	@Column(name = "LOGO_URL")
	private String logoUrl;

	@Column(name = "ONBOARDED_BY_USEREMAIL")
	private String onboardedByUserEmail;
	
	@Column(name = "ONBOARDING_STATUS")
	private String onboardingStatus;
	
	@Column(name = "SIGN_UP")
	private boolean signUp;
	
	@Column(name = "VERIFICATION_URL")
	private String verificationUrl;
		
}
