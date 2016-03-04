package microsys.security.service;

import javax.annotation.Nonnull;

/**
 * An exception thrown by the {@link UserService} when there are problems retrieving or storing user data in the system.
 */
public class UserServiceException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public UserServiceException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public UserServiceException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public UserServiceException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
