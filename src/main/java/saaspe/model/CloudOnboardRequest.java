package saaspe.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class CloudOnboardRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String clientId;

    private String clientSecret;

    private String tenantId;

    private String accessKeyId;

    private String secretAccessKey;

    private String provider;
}
