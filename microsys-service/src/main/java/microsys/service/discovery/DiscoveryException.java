package microsys.service.discovery;

/**
 * An exception thrown by the {@link DiscoveryManager} when there are problems retrieving or storing service discovery
 * metadata.
 */
public class DiscoveryException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public DiscoveryException(final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public DiscoveryException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public DiscoveryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
