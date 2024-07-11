package saaspe.adaptor.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZohoRefreshTokenResponse {
	 private String access_token;
	 private String scope;
	 private String api_domain;
	 private String token_type;
	 private String expires_in;
}
