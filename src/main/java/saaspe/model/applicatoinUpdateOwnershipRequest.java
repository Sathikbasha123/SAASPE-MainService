package saaspe.model;

import lombok.Data;

@Data
public class applicatoinUpdateOwnershipRequest {

	private String applicationId;

	private String applicationOwnerEmail;

	private String projectId;

	private String projectOwnerEmail;

}
