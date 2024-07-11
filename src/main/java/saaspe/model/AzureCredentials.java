package saaspe.model;

import lombok.Data;

@Data
public class AzureCredentials {

    private String emailAddress;

    private String tenantId;

    private Boolean adminConsent;

    private String redirectUri;

    private String authCode;
    
}
