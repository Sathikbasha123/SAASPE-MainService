package saaspe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class newApplicationOwnerDetailsRequest {

	private String applicaitonOwnerName;

	private String applicationOwnerEmail;

	@JsonIgnore
	private Integer priority;

}
