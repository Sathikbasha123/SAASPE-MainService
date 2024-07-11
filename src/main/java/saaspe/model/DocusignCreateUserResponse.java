package saaspe.model;

import lombok.Data;

@Data
public class DocusignCreateUserResponse {
	private String id;
	private float site_id;
	private String first_name;
	private String user_name;
	private String last_name;
	private String email;
	private String language_culture;
	private Object accounts;

}
