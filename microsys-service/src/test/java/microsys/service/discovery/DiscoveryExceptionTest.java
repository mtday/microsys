package microsys.service.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Perform testing of the {@link DiscoveryException} class.
 */
public class DiscoveryExceptionTest {
    @Test
    public void testStringConstructor() {
        final DiscoveryException exception = new DiscoveryException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final DiscoveryException exception = new DiscoveryException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final DiscoveryException exception = new DiscoveryException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
