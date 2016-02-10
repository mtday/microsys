package microsys.service.discovery.port;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Perform testing of the {@link PortReservationException} class.
 */
public class PortReservationExceptionTest {
    @Test
    public void testStringConstructor() {
        final PortReservationException exception = new PortReservationException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final PortReservationException exception = new PortReservationException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final PortReservationException exception = new PortReservationException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
