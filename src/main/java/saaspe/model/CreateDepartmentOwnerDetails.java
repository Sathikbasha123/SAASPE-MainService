package saaspe.model;

import lombok.Data;

@Data
public class CreateDepartmentOwnerDetails {
	private String departmentOwnerName;
	private String departmentOwnerEmailAddress;
	private Integer priority;
}
