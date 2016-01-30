package microsys.service.discovery;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.VersionedValue;

import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import microsys.service.model.Reservation;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

/**
 *
 */
public class PortManager {
    private final static String PORT_RESERVATION_PATH = "/port-reservation";

    private final Config config;
    private final SharedCount portReservation;

    /**
     * @param config the static system configuration information
     * @param curator the {@link CuratorFramework} that is managing zookeeper operations
     */
    public PortManager(final Config config, final CuratorFramework curator) throws Exception {
        this.config = Objects.requireNonNull(config);

        final int minPort = config.getInt(CommonConfig.SERVER_PORT_MIN.getKey());
        this.portReservation = new SharedCount(curator, PORT_RESERVATION_PATH, minPort);
        this.portReservation.start();
    }

    protected Config getConfig() {
        return this.config;
    }

    protected int getMinPort() {
        return getConfig().getInt(CommonConfig.SERVER_PORT_MIN.getKey());
    }

    protected int getMaxPort() {
        return getConfig().getInt(CommonConfig.SERVER_PORT_MAX.getKey());
    }

    protected SharedCount getPortReservation() {
        return this.portReservation;
    }

    protected int getNextPort(final VersionedValue<Integer> currentValue) {
        final int minPort = getMinPort();
        final int maxPort = getMaxPort();

        int next = currentValue.getValue() + 1;
        if (next > maxPort) {
            next = minPort;
        }
        return next;
    }

    protected boolean testPort(final String host, final int port) {
        try (final Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            return false; // This port is already in use.
        } catch (final IOException ioException) {
            return true; // Failed to connect, assuming the port is free.
        }
    }

    public Reservation reserveServicePort(final ServiceType type, final String host) throws Exception {
        boolean successful = false;

        int reservedPort = 0;
        while (!successful) {
            final VersionedValue<Integer> currentValue = getPortReservation().getVersionedValue();
            reservedPort = getNextPort(currentValue);
            successful = getPortReservation().trySetCount(currentValue, reservedPort) && testPort(host, reservedPort);
        }

        return new Reservation(type, host, reservedPort);
    }
}
