package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserOnboardingRequest {

    private String firstName;

    private String lastName;

    private String userEmail;

    private String userType;

    private String userDesignation;

    private String userDepartment;

    private String userReportingManager;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date userJoiningDate;

}
