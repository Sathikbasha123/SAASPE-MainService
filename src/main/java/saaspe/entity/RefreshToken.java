package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "refresh_token")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "REFRESH_TOKEN")
	private String refreshToken;

	@Column(name = "ACCESS_TOKEN")
	private String accessToken;

	@Column(name = "REFRESH_TOKEN_EXPIRY")
	private Date refreshTokenExpiry;

	@Column(name = "ACCESS_TOKEN_EXPIRY")
	private Date accessTokenExpiry;

	@Column(name = "CREATED_ON")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	private Date updatedOn;

	@Column(name = "USER_EMAIL")
	private String userEmail;

}
