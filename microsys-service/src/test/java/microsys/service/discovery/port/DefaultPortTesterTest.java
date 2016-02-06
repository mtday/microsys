package microsys.service.discovery.port;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.ServerSocket;

/**
 * Perform testing on the {@link DefaultPortTester} class.
 */
public class DefaultPortTesterTest {
    @Test
    public void testIsAvailableWhenAvailable() throws Exception {
        assertTrue(new DefaultPortTester().isAvailable("localhost", 6000));
    }

    @Test
    public void testIsAvailableWhenNotAvailable() throws Exception {
        try (@SuppressWarnings("unused") final ServerSocket listener = new ServerSocket(6001)) {
            assertFalse(new DefaultPortTester().isAvailable("localhost", 6001));
        }
    }
}
