package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "saaspe_application_provider_details")
public class ApplicationProviderDetails {

    @NonNull
    @Column(name = "PROVIDER_NAME")
    private String providerName;

    @Id
    @Column(name = "PROVIDER_ID")
    private String providerId;

    @NonNull
    @Column(name = "PROVIDER_STATUS")
    private String providerStatus;

    @NonNull
    @Column(name = "DEPARTMENT_OWNER")
    private String departmentOwner;

    @NonNull
    @Column(name = "PROVIDER_CATEGORY")
    private String providerCategory;

    @NonNull
    @Column(name = "PROVIDER_WEBSITE_URL")
    private String providerWebsiteUrl;

    @NonNull
    @Column(name = "PROVIDER_CONTACT_NAME")
    private String providerContactName;

    @NonNull
    @Column(name = "PROVIDER_CONTACT_DESIGNATION")
    private String providerContactDesgination;

    @Column(name = "PROVIDER_CONTACT_EMAIL")
    private String providerContactEmail;

    @NonNull
    @Column(name = "PROVIDER_TYPE")
    private String providerType;

    @NonNull
    @Column(name = "PROVIDER_CONTACT_PHONE_NUMBER")
    private String providerContactPhoneNumber;

    @NonNull
    @Column(name = "CREATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdOn;

    @NonNull
    @Column(name = "UPDATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedOn;

    @NonNull
    @Column(name = "CREATED_BY")
    private String createdBy;

    @NonNull
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @NonNull
    @Column(name = "BUID")
    private String buID;

    @NonNull
    @Column(name = "OPID")
    private String opID = "SAASPE";

    @NonNull
    @Column(name = "LOGO_URL")
    private String logoUrl;

}
