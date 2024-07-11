package saaspe.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignUpResponse implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String message;
    private boolean success;

}
