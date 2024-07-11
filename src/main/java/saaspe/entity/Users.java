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
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "saaspe_users")
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@NonNull
	@Column (name = "USER_EMAIL")
	private String userEmail;
	
	@NonNull
	@Column(name = "OPID")
	private String opID = "SAASPE";

	@NonNull
	@Column(name = "BUID")
	private String buID;
	
	@NonNull
	@Column (name = "USER_CREATED_BY")
	private String userCreatedBy;
	
	@NonNull
	@Column (name = "USER_CREATED_ON")
	private Date userCreatedOn;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column (name = "USER_END_DATE")
	private Date userEndDate;
	
	@NonNull
	@Column (name = "JOB_LEVEL")
	private String jobLevel;
	
	@NonNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column (name = "JOINING_DATE")
	private Date joiningDate;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column (name = "LAST_LOGIN_TIME")
	private Date lastLoginTime;
	
	@NonNull
	@Column (name = "LOGO_URL")
	private String logoUrl;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column (name = "START_DATE")
	private Date startDate;
	
	@NonNull
	@Column (name = "TEAM")
	private String team;
	
	
	@Column (name = "UPDATED_BY")
	private String updatedBy;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column (name = "UPDATED_ON")
	private Date updatedOn;
	
	@NonNull
	@Column (name = "USER_DESIGNATION")
	private String userDesignation ;

	@Column (name = "USER_ID")
	private String userId;
	
	@NonNull
	@Column (name = "USER_NAME")
	private String userName;
	
	@NonNull
	@Column (name = "USER_REPORTING_MANAGER")
	private String userReportingManager;
	
	
	@Column (name = "USER_ROLE")
	private String userRole;
	
	@NonNull
	@Column (name = "USER_TYPE")
	private String userType;
	
	@Column (name = "DEPARTMENT_ID")
	private String departmentId;
	
	@Column (name = "IDENTITY_ID")
	private String identityId;
	
	@Column (name = "CURRENCY")
	private String currency;
	
	@Column (name = "MOBILE_NUMBER")
	private String mobileNumber;
	
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column (name = "SAASPE_LASTLOGIN")
	private Date saaspeLastLogin;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column (name = "LAST_LOGIN_TIME_APPLICATION")
	private Date lastLoginTimeApplication;
	
	
	@Column (name = "USER_APPLICATION_ID")
	private String userApplicationId;
	
	@Column (name = "USER_LICENSE_ID")
	private String userLicenseId;
	
	@NonNull
	@Column (name = "APPLICATION_NAME")
	private String applicationName;
	
	@NonNull
	@Column (name = "DEPARTMENT_NAME")
	private String departmentName;
	
	@Column (name = "APP_USER")
	private Boolean appUser;


}
