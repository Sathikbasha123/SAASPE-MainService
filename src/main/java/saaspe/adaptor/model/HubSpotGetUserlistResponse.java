package saaspe.adaptor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class HubSpotGetUserlistResponse {
	
	@JsonProperty("id")
	private String id;
	@JsonProperty("email")
    private String email;
	@JsonProperty("primaryTeamId")
    private String	primaryTeamId;
	@JsonProperty("superAdmin")
	private boolean superAdmin;

}
