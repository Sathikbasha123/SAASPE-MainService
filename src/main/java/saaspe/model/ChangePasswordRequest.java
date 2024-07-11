package saaspe.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePasswordRequest {

    @NotEmpty(message = "Please enter OldPassword")
    @Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters")
    @JsonProperty("OldPassword")
    private String oldPassword;

    @NotEmpty(message = "Please enter new password ")
    @Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters")
    @JsonProperty("NewPassword")
    private String newPassword;

    @NotEmpty(message = "Please confirm new password")
    @Size(min = 8, max = 250, message = "The length of the password must be between 8 and 250 characters")
    @JsonProperty("ConfirmNewPassword")
    private String confirmNewPassword;

}
