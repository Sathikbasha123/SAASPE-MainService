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
@Table(name = "user_last_login_details")
public class UserLastLoginDetails {

	@Id
	@Column(name = "Id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "USER_EMAIL")
	private String userEmail;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "USER_STATUS")
	private String userStatus;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "CREATED_ON")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "UPDATED_ON")
	private Date updatedOn;

	@Column(name = "BUID")
	private String buID;

	@Column(name = "OPID")
	private String opID = "SAASPE";

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "LAST_LOGIN_TIME")
	private Date lastLoginTime;

	@Column(name = "USER_ID")
	private String userId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@Column(name = "USER_IDENTITY_ID")
	private String userIdentityId;

	@Column(name = "USER_APPLICATION_ID")
	private String userApplicationId;

	@Column(name = "APPLICATION_ID")
	private String applicationId;

}
