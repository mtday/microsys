package microsys.service.client;

/**
 * An exception thrown by the {@link ServiceClient} when there are problems communicating with remote services.
 */
public class ServiceException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public ServiceException(final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public ServiceException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public ServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
