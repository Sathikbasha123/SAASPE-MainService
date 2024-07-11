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

@Entity
@Data
@NoArgsConstructor
@Table(name = "Authentication_Entity")
public class AuthenticationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "EMAIL_ADDRESS")
	private String emailAddress;
	
	@Column(name = "SSO_IDENTITY_PROVIDER")
	private String ssoIdentityProvider;

	@Column(name = "CLIENT_ID")
	private String clinetId;

	@Column(name = "CLIENT_SECRET")
	private String clientSecret;

	@Column(name = "CLIENT_SECRET_EXPIRY")
	private String clientSecretExpiry;

	@Column(name = "ADMIN_CONSENT")
	private Boolean adminConsent;
	
	@Column(name = "IS_CONNECTED")
	private Boolean isConnected;

	@Column(name = "AZURE_ACCOUNT_ID", length = 50000)
	private String azureAccountId;
	
	@Column(name = "TENANT_ID")
	private String tenantId;

	@Column(name = "REDIRECT_URI")
	private String redirectUri;

	@Column(name = "AUTH_CODE", length = 50000)
	private String authCode;

	@Column(name = "ACCESS_TOKEN", length = 50000)
	private String accessToken;

	@Column(name = "ACCESS_TOKEN_EXPIRY")
	private String accessTokenExpiry;

	@Column(name = "REFRESH_TOKEN", length = 50000)
	private String refreshToken;

	@Column(name = "ID_TOKEN", length = 50000)
	private String idToken;

	@Column(name = "OPID")
	private String opID;

	@Column(name = "BUID")
	private String buID;

	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

}