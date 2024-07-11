package saaspe.model;

import java.util.Date;

import lombok.Data;

@Data
public class TokenCache {
	private String token;
	private String emailAddress;
	private String displayname;
	private Date expiryDate;

}
