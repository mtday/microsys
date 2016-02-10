package microsys.service.discovery.port;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Provides the default implementation of the {@link PortTester} interface.
 */
public class DefaultPortTester implements PortTester {
    /**
     * {@inheritDoc}
     */
    public boolean isAvailable(final String host, final int port) {
        try (final Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) TimeUnit.SECONDS.toMillis(1));
            return false;
        } catch (final IOException ioException) {
            return true;
        }
    }
}
