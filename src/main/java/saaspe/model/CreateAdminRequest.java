package saaspe.model;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class CreateAdminRequest {
	@NotEmpty
	private String userEmail;
	private String firstName;
	private String lastName;
	private String role;
	private String userMobileNumber;
	private List<String> access;
}
