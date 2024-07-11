package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "saaspe_application_logo_master")
@NoArgsConstructor
public class ApplicationLogoEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NUMBER")
    private Integer number;

    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    @Column(name = "APPLICATION_PAGE_URL")
    private String applicationPageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROVIDER_ID", foreignKey = @ForeignKey(name = "FK_PROVIDER_ID"))
    private ApplicationProviderDetails providerId;

    @Column(name = "DESCRIPTION", length = 5000)
    private String description;

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

    @Column(name = "BUID")
    private String buID;

    @Column(name = "OPID")
    private String opID = "SAASPE";

    @Column(name = "LOGO_URL")
    private String logoUrl;

    @Column(name = "CLOUD")
    private Boolean cloud;

}
