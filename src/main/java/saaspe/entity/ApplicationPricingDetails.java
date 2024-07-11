package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "saaspe_application_pricing_details")
public class ApplicationPricingDetails {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", foreignKey = @ForeignKey(name = "FK_CATEGORY_ID"))
    private ApplicationCategoryMaster categoryId;

    @NonNull
    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", foreignKey = @ForeignKey(name = "FK_APPLICATOIN_ID"))
    private ApplicationDetails applicationId;

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

    @NonNull
    @Column(name = "BUID")
    private String buID;

    @NonNull
    @Column(name = "OPID")
    private String opID = "SAASPE";

    @NonNull
    @Id
    @Column(name = "LICENSE_ID")
    private String licenseId;

    @NonNull
    @Column(name = "LICENSE_COST_PER_USER")
    private Integer licenseCostPerUser;

    @NonNull
    @Column(name = "CURRENCY")
    private String currency;

}
