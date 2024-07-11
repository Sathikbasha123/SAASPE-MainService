package saaspe.model;

import lombok.Data;

@Data
public class CreateHubSpotUserRequest {

    private boolean sendWelcomeEmail;
    private String email;

}