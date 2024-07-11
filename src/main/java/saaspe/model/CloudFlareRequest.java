package saaspe.model;

import lombok.Data;

@Data
public class CloudFlareRequest {
	private String secret;
	private String response;
}
