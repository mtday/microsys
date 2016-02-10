package microsys.service.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Perform testing of the {@link ServiceException} class.
 */
public class ServiceExceptionTest {
    @Test
    public void testStringConstructor() {
        final ServiceException exception = new ServiceException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final ServiceException exception = new ServiceException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final ServiceException exception = new ServiceException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
