package microsys.service.discovery.port;

/**
 * An exception thrown by the {@link PortManager} when there are problems reserving a service port.
 */
public class PortReservationException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public PortReservationException(final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public PortReservationException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public PortReservationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
