package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserLastLoginResponse {

	private String userName;
	private String userEmail;
	private String userDesignation;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date userLastLogin;
	private String userLogo;

}
