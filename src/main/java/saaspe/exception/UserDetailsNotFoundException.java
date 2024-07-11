package saaspe.exception;

public class UserDetailsNotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UserDetailsNotFoundException() {
    }

    public UserDetailsNotFoundException(String message, String status) {
        super(message);
    }

}
