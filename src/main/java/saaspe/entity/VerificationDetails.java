package saaspe.entity;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "verification_details")
public class VerificationDetails {

	@Id
	@Column(name = "USER_EMAIL")
	private String userEmail;

	@Column(name = "SALT")
	private String salt;

	@Column(name = "EMAIL_VERIFIED")
	private Boolean emailVerified;

	@Column(name = "EMAIL_VERIFICATION_CODE")
	private String emailVerificationCode;

	@Column(name = "RESET_VERIFICATION_CODE")
	private String resetVerificationCode;

	@Column(name = "RESET_VERIFICATION_CODE_EXPIRY_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date resetVerificationCodeExpiryDate;

	@Column(name = "EMAIL_VERIFICATION_CODE_SEND_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date emailVerificationCodeSendDate;

	@Column(name = "EMAIL_VERIFICATION_CODE_EXPIRY_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date emailVerificationCodeExpiryDate;

	@Column(name = "EMAIL_VERIFIED_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date emailVerifiedDate;

	@Column(name = "BUID")
	private String buID;

	@Column(name = "OPID")
	private String opID;

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

	@Column(name = "REFRESH_TOKEN")
	private String refreshToken;

	@Column(name = "REFRESH_TOKEN_EXPIRY")
	private Date refreshTokenExpiry;

	@Column(name = "LOGIN_OTP")
	private String loginOTP;

	@Column(name = "LOGIN_OTP_GENERATEDTIME")
	private Timestamp loginOtpGeneratedTime;

	@Column(name = "LOGIN_OTP_EXPIRYTIME")
	private Timestamp loginOtpExpiryTime;

	@Column(name = "MFA_ENABLED")
	private Boolean isMfaEnabled;

	@Column(name = "FAILED_COUNT")
	private Integer failedCount;

}
