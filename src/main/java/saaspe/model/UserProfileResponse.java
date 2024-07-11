package saaspe.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class UserProfileResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean status;
	private String token;
	private UserProfileForm userProfile;
	private String refreshToken;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss ", timezone = "IST")
	private Date accessTokenExpiry;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss ", timezone = "IST")
	private Date refreshTokenExpiry;

	private boolean mfaEnabled;
}
