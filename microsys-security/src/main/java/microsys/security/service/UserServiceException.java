package microsys.security.service;

/**
 * An exception thrown by the {@link UserService} when there are problems retrieving or storing user data in the system.
 */
public class UserServiceException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public UserServiceException(final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public UserServiceException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public UserServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
