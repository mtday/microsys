package microsys.portres;

import microsys.common.model.service.Reservation;
import microsys.common.model.service.ServiceType;

import javax.annotation.Nonnull;

/**
 * This manager is used to reserve a unique port for a service, and should do so in such a way as to prevent two
 * services from attempting to simultaneously attempting to start on the same port.
 */
public interface PortManager extends AutoCloseable {
    /**
     * @param type the {@link ServiceType} for which a reservation should be found
     * @param host the host on which the service should run
     * @return a {@link Reservation} representing the available host and port information
     * @throws PortReservationException if there is a problem reserving the port
     */
    @Nonnull
    Reservation getReservation(@Nonnull ServiceType type, @Nonnull String host) throws PortReservationException;

    /**
     * Clean up the resources associated with this class.
     */
    void close();
}
