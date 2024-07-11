package saaspe.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotFoundException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean sucess;

    private String message;
}
