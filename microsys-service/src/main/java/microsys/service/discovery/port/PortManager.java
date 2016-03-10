package microsys.service.discovery.port;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.VersionedValue;

import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import microsys.service.model.Reservation;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * This manager is used to reserve a unique port for a service, and does so in such a way as to prevent two services
 * from attempting to simultaneously attempting to start on the same port.
 */
public class PortManager implements AutoCloseable {
    private final static String PORT_RESERVATION_PATH = "/port-reservation";

    @Nonnull
    private final Config config;
    @Nonnull
    private final SharedCount portReservation;
    @Nonnull
    private final PortTester portTester;

    /**
     * @param config the static system configuration information
     * @param curator the {@link CuratorFramework} that is managing zookeeper operations
     * @throws PortReservationException if there is a problem with zookeeper communication
     */
    public PortManager(@Nonnull final Config config, @Nonnull final CuratorFramework curator)
            throws PortReservationException {
        this(config, curator, new DefaultPortTester());
    }

    /**
     * @param config the static system configuration information
     * @param curator the {@link CuratorFramework} that is managing zookeeper operations
     * @param portTester the {@link PortTester} used to verify that each port is available
     * @throws PortReservationException if there is a problem with zookeeper communication
     */
    public PortManager(
            @Nonnull final Config config, @Nonnull final CuratorFramework curator, @Nonnull final PortTester portTester)
            throws PortReservationException {
        this.config = Objects.requireNonNull(config);

        final int minPort = config.getInt(CommonConfig.SERVER_PORT_MIN.getKey()) - 1;
        this.portReservation = new SharedCount(curator, PORT_RESERVATION_PATH, minPort);
        try {
            this.portReservation.start();
        } catch (final Exception exception) {
            throw new PortReservationException("Failed to start the shared count", exception);
        }

        this.portTester = portTester;
    }

    /**
     * @return the static system configuration information
     */
    @Nonnull
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the {@link SharedCount} object that represents the available port information from zookeeper
     */
    @Nonnull
    protected SharedCount getPortReservation() {
        return this.portReservation;
    }

    /**
     * @return the {@link PortTester} used to verify that each port is available
     */
    @Nonnull
    protected PortTester getPortTester() {
        return this.portTester;
    }

    /**
     * @return the minimum port that will be reserved for service usage, based on the static system configuration
     */
    protected int getMinPort() {
        return getConfig().getInt(CommonConfig.SERVER_PORT_MIN.getKey());
    }

    /**
     * @return the maximum port that will be reserved for service usage, based on the static system configuration
     */
    protected int getMaxPort() {
        return getConfig().getInt(CommonConfig.SERVER_PORT_MAX.getKey());
    }

    /**
     * @param currentValue the current shared port number value from zookeeper
     * @return the next port number to use
     */
    protected int getNextPort(@Nonnull final VersionedValue<Integer> currentValue) {
        final int minPort = getMinPort();
        final int maxPort = getMaxPort();

        int next = Objects.requireNonNull(currentValue).getValue() + 1;
        if (next > maxPort) {
            next = minPort;
        }
        return next;
    }

    /**
     * @param type the {@link ServiceType} for which a reservation should be found
     * @param host the host on which the service should run
     * @return a {@link Reservation} representing the available host and port information
     * @throws PortReservationException if there is a problem reserving the port
     */
    @Nonnull
    public Reservation getReservation(@Nonnull final ServiceType type, @Nonnull final String host)
            throws PortReservationException {
        boolean successful = false;

        int totalAttempts = getMaxPort() - getMinPort() + 2;
        int reservedPort = 0;
        while (!successful && totalAttempts-- > 0) {
            final VersionedValue<Integer> currentValue = getPortReservation().getVersionedValue();
            reservedPort = getNextPort(currentValue);
            try {
                successful = getPortReservation().trySetCount(currentValue, reservedPort) && getPortTester()
                        .isAvailable(host, reservedPort);
            } catch (final Exception exception) {
                throw new PortReservationException("Failed to set the shared count", exception);
            }
        }

        if (!successful) {
            throw new PortReservationException("Failed to reserve a port, all of them are currently taken");
        }

        return new Reservation(type, host, reservedPort);
    }

    /**
     * Clean up the resources associated with this class.
     */
    public void close() {
        try {
            getPortReservation().close();
        } catch (final Exception ignored) {
            // Ignored.
        }
    }
}
