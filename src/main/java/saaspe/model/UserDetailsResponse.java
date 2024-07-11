package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class UserDetailsResponse extends CommonResponse {

    private String userEmail;
    private Integer size;
    private String userId;
    private String userName;
    private String departmentName;
    private String userType;
    private String departmentId;

}