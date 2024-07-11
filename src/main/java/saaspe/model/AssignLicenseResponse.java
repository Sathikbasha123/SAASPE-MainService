package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class AssignLicenseResponse {
	
	private List<String> assigned;
	private List<String> unassigned;

}
