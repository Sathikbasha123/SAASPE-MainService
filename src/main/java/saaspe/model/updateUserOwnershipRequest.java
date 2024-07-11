package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class updateUserOwnershipRequest {

	private List<applicatoinUpdateOwnershipRequest> applicationDetails;

	private List<applicatoinUpdateOwnershipRequest> projectDetails;

	private departmentUpdateOwnershipRequest departmentDetails;

}
