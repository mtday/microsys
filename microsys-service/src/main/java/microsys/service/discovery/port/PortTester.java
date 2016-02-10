package microsys.service.discovery.port;

/**
 * Defines the interface used to verify if a port is available.
 */
public interface PortTester {
    /**
     * @param host the host on which the port should be checked
     * @param port the port number to test and determine if it is available
     * @return whether the specified host and port are available
     * @throws PortReservationException if there is a problem verifying that the port is available
     */
    boolean isAvailable(String host, int port) throws PortReservationException;
}
