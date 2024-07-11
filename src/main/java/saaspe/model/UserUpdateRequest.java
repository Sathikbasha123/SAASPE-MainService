package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String userDepartment;

    private String userDesignation;

    private String userType;

    private String userReportingManager;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date userOnboardedDate;

}
