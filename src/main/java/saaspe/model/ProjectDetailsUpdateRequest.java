package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class ProjectDetailsUpdateRequest {
	private String departmentId;
	private String projectName;
	private List<ProjectApplicationListRequest> applicationsInfo;
}
