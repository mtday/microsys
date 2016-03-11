package microsys.curator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Perform testing of the {@link CuratorException} class.
 */
public class CuratorExceptionTest {
    @Test
    public void testStringConstructor() {
        final CuratorException exception = new CuratorException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final CuratorException exception = new CuratorException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final CuratorException exception = new CuratorException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
