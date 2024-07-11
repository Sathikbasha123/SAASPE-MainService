package saaspe.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccessRoleResponse extends Response {

    private String role;
    private String[] access;
    private String currency;
    private Clm clm;
}
