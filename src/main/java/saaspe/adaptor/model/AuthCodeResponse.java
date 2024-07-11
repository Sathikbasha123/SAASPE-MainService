package saaspe.adaptor.model;

import lombok.Data;

@Data
public class AuthCodeResponse {
	private String url;
	private Long uniqueId;
}
