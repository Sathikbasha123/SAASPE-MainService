package saaspe.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class DeptUserPasswordRequest {

    @NotEmpty(message = "The Password field is required.")
    @Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters.")
    private String password;

    @NotEmpty(message = "The confirm password field is required.")
    @Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters.")
    private String confirmPassword;

    @NotEmpty(message = "The confirm password field is required.")
    private String userEmail;

    @NotEmpty(message = "The confirm password field is required.")
    private String verificationCode;

}
