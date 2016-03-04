package microsys.service.client;

import javax.annotation.Nonnull;

/**
 * An exception thrown by the {@link ServiceClient} when there are problems communicating with remote services.
 */
public class ServiceException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public ServiceException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public ServiceException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public ServiceException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
