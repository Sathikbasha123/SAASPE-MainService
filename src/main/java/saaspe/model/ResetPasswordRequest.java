package saaspe.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ResetPasswordRequest {

	@NotEmpty(message = "The email address is required.")
    @Email(message = "The email address is invalid.", flags = { Flag.CASE_INSENSITIVE })
	private String emailAddress;
	
	@NotEmpty(message = "Password reset code is required")
	private String passwordResetCode;

	@NotEmpty(message = "Please enter new password")
	@Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters")
	private String newPassword;

	@NotEmpty(message = "Please confirm new password")
	@Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters")
	private String confirmNewPassword;

}
