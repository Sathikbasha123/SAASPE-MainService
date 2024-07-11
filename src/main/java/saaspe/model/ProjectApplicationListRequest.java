package saaspe.model;

import lombok.Data;

@Data
public class ProjectApplicationListRequest {
	private String applicationName;
	private String applicationStatus;
}
