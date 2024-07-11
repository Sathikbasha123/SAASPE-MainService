package saaspe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDetailsUsersResponse {

    private String userLogo;
    private String userName;
    private String userStatus;
    private String userEmail;
    private String userDesignation;
    private Integer userApplicationCount;

}
