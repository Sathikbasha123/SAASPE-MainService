package saaspe.adaptor.model;

import java.util.Date;

import lombok.Data;

@Data
public class GitlabUser {
	private String name;
	private Integer id;
	private String state;
	private String email;
	private Date createdAt;
	private String avatarUrl;
	private Date currentSignInAt;
	private Date lastActivityOn;
	private String username;
	private String accessLevel;

}
