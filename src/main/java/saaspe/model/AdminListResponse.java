package saaspe.model;

import java.util.Date;

import lombok.Data;

@Data
public class AdminListResponse {

	private String userId;

	private String userName;

	private String role;

	private Date lastLogin;

	private String userEmail;

	private String userAvatar;

	private Date saaspeLastLogin;
}
