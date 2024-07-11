package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.UserDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOnboardingDetailsRequest {

    private String userId;

    private String firstName;

    private String lastName;

    private String userEmail;

    private String userDesigination;

    private String userReportingManager;

    private String userType;

    private DepartmentDetails departmentId;

    private String assignedTo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date joiningDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date onboardDate;

    private String approvedBy;

    private String remarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedOn;

    private String createdBy;

    private String updatedBy;

    private String buID;

    private String opID = "SAASPE";

    private String logoUrl;

    private UserDetails userDetails;
    
    
    
}
