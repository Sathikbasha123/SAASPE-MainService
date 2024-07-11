package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "MultiCloud_Details")
public class MultiCloudDetails {

    @Id
    @Column(name = "Cloud_Id")
    private String cloudId;

    @Column(name = "Provider_Name")
    private String providerName;

    @Column(name = "Client_Id")
    private String clientId;

    @Column(name = "Tenant_Id")
    private String tenantId;

    @Column(name = "Client_Secret")
    private String clientSecret;

    @Column(name = "Secret_access_key")
    private String secretAccessKey;

    @Column(name = "Access_key_Id")
    private String accessKeyId;

    @Column(name = "Api_key")
    private String apiKey;

    @Column(name = "CREATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdOn;

    @Column(name = "UPDATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedOn;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_BY")
    private String updatedBy;
}
