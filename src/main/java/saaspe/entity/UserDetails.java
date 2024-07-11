package saaspe.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "saaspe_user_details")
public class UserDetails {

	@Id
	@NonNull
	@Column(name = "USER_EMAIL")
	private String userEmail;

	@NonNull
	@Column(name = "USER_NAME")
	private String userName;

	@NonNull
	@Column(name = "USER_TYPE")
	private String userType;

	@NonNull
	@Column(name = "USER_DESIGNATION")
	private String userDesigination;

	@NonNull
	@Column(name = "USER_REPORTING_MANAGER")
	private String userReportingManager;

	@NonNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "JOINING_DATE")
	private Date joiningDate;

	@NonNull
	@Column(name = "TEAM")
	private String team;

	@NonNull
	@Column(name = "USER_STATUS")
	private String userStatus;

	@NonNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "CREATED_ON")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "UPDATED_ON")
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
	@Column(name = "JOB_LEVEL")
	private String jobLevel;

	@NonNull
	@Column(name = "LOGO_URL")
	private String logoUrl;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "LAST_LOGIN_TIME")
	private Date lastLoginTime;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "USERS_APPLICATION", joinColumns = { @JoinColumn(name = "USER_EMAIL") }, inverseJoinColumns = {
			@JoinColumn(name = "APPLICATION_ID") })
	private List<ApplicationDetails> applicationId = new ArrayList<>();

	@Column(name = "USER_ID")
	private String userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEPARTMENT_ID", foreignKey = @ForeignKey(name = "DEPARTMENT_ID"))
	private DepartmentDetails departmentId;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "userId")
	private List<ApplicationLicenseDetails> licenseId = new ArrayList<>();

	@Column(name = "USER_ROLE")
	private String userRole;

	@Column(name = "USER_ACCESS")
	private String userAccess;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@Column(name = "IDENTITY_ID")
	private String identityId;

	@Column(name = "CURRENCY")
	private String currency;

	@Column(name = "MOBILE_NUMBER")
	private String mobileNumber;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "SAASPE_LASTLOGIN")
	private Date saaspeLastLogin;
}
