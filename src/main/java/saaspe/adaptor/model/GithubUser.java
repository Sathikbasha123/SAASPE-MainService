package saaspe.adaptor.model;

import lombok.Data;

@Data
public class GithubUser {

	private String login;
	private Long id;
	private String type;
	private String siteAdmin;
}
