package saaspe.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import lombok.Data;
import saaspe.configuration.ValidPassword;

@Data
public class SignupRequest {

	@NotEmpty(message = "The full name is required.")
	@Size(min = 2, max = 250, message = "The length of full name must be between 5 and 250 characters.")
	private String designation;

	@NotEmpty(message = "The email address is required.")
	@Email(message = "The email address is invalid.", flags = { Flag.CASE_INSENSITIVE })
	private String emailAddress;

	@NotEmpty(message = "Password is required.")
	@ValidPassword
	private String password;

	@Size(min = 2, max = 250, message = "The length of firstName must be between 2 and 250 characters.")
	private String firstName;

	@NotEmpty(message = "Last name is required.")
	@Size(min = 2, max = 250, message = "The length of lastName must be between 2 and 250 characters.")
	private String lastName;

	@NotEmpty(message = "Mobile Number is required.")
	@Size(min = 2, max = 250, message = "The length of lastName must be between 2 and 250 characters.")
	private String userMobileNumber;

	private String verifyUrl;

}
