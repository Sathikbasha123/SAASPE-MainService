package saaspe.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @NotEmpty(message = "The email address is required.")
    @Email(message = "The email address is invalid.", flags = { Flag.CASE_INSENSITIVE })
    private String emailAddress;

    @NotEmpty(message = "The Password field is required.")
    @Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters.")
    public String password;

}
