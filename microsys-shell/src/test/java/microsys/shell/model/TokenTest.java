package microsys.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Perform testing on the {@link Token} class.
 */
public class TokenTest {
    @Test
    public void testCompareTo() {
        final Token a = new Token(0, "desc");
        final Token b = new Token(0, "description");
        final Token c = new Token(4, "description");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-7, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(7, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final Token a = new Token(0, "desc");
        final Token b = new Token(0, "description");
        final Token c = new Token(4, "description");

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
    }

    @Test
    public void testHashCode() {
        final Token a = new Token(0, "desc");
        final Token b = new Token(0, "description");
        final Token c = new Token(4, "description");

        assertEquals(3103098, a.hashCode());
        assertEquals(-1724522779, b.hashCode());
        assertEquals(-1724522631, c.hashCode());
    }

    @Test
    public void testToString() {
        final Token a = new Token(0, "desc");
        final Token b = new Token(0, "description");
        final Token c = new Token(4, "description");

        assertEquals("desc", a.toString());
        assertEquals("description", b.toString());
        assertEquals("description", c.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePosition() {
        new Token(-1, "hello");
    }
}
