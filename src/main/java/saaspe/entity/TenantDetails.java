package saaspe.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "TENANT_DETAILS")
public class TenantDetails {

    @Id
    @Column(name = "TENANT_ID")
    private String tenantId;

    @NonNull
    @Column(name = "TENANT_NAME")
    private String tenantName;

    @NonNull
    @Column(name = "TENANT_CONTACT_NAME")
    private String tenantContactName;

    @NonNull
    @Column(name = "TENANT_CONTACT_EMAIL")
    private String tenantContactEmail;

    @NonNull
    @Column(name = "TENANT_CONTACT_ADDRESS")
    private String tenantContactAddress;

    @NonNull
    @Column(name = "TENANT_CONTACT_MOBILE_NO")
    private String tenantContactMobileNO;

    @NonNull
    @Column(name = "CREATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdOn;

    @Column(name = "UPDATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedOn;

    @NonNull
    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @JsonIgnore
    @OneToMany(mappedBy = "tenantId")
    private List<DepartmentDetails> departmentDetails = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tenantId")
    private List<ApplicationContractDetails> applicationContractDetails = new ArrayList<>();

}
