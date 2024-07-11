package saaspe.model;

import lombok.Data;

@Data
public class departmentUpdateOwnershipRequest {

	private String departmentOwnerEmail;
	
	private String departmentId;
}
