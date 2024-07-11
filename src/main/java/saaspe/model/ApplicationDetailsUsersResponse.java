package saaspe.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDetailsUsersResponse {

	private String userId;
	private String userLogo;
	private String userName;
	private String userStatus;
	private String userEmail;
	private String userDesignation;
	private Date userLastLogin;

}
