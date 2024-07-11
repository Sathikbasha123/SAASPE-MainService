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
import lombok.NonNull;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "saaspe_user_offboarding")
public class UserOffboardingDetails{

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @NonNull
    @Column(name = "USER_EMAIL")
    private String userEmail;

    @NonNull
    @Column(name = "USER_NAME")
    private String userName;

    @NonNull
    @Column(name = "USER_DESIGINATION")
    private String userDesigination;

    @NonNull
    @Column(name = "ASSIGNED_TO")
    private String assignedTo;

    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "OFFBOARD_DATE")
    private Date offboardDate;

    @NonNull
    @Column(name = "APPROVED_BY")
    private String approvedBy;

    @NonNull
    @Column(name = "REMARKS")
    private String remarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "CREATED_ON")
    private Date createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "UPDATED_ON")
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

}
