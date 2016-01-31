package microsys.shell.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Perform testing on the {@link CommandPath} class.
 */
public class CommandPathTest {
    @Test
    public void testConstructorWithList() {
        final CommandPath a = new CommandPath(Arrays.asList("a", "b"));
        final CommandPath b = new CommandPath(Arrays.asList("a", "b", " ", "-i", "5"));

        assertEquals(Arrays.asList("a", "b").toString(), a.getPath().toString());
        assertEquals(Arrays.asList("a", "b").toString(), b.getPath().toString());
    }

    @Test
    public void testConstructorWithVarargs() {
        final CommandPath a = new CommandPath("a", "b");
        final CommandPath b = new CommandPath("a", "b", " ", "-i", "5");

        assertEquals(Arrays.asList("a", "b").toString(), a.getPath().toString());
        assertEquals(Arrays.asList("a", "b").toString(), b.getPath().toString());
    }

    @Test
    public void testIsPrefix() {
        final CommandPath a = new CommandPath("a");
        final CommandPath abc = new CommandPath("a", "b", "c");
        final CommandPath abcd = new CommandPath("a", "b", "c", "d");
        final CommandPath ott = new CommandPath("one", "two", "three");
        final CommandPath otw = new CommandPath("one", "tw");
        final CommandPath oth = new CommandPath("one", "three");

        assertTrue(abc.isPrefix(a));
        assertTrue(abc.isPrefix(abc));
        assertFalse(abc.isPrefix(abcd));
        assertTrue(ott.isPrefix(otw));
        assertFalse(ott.isPrefix(oth));
    }

    @Test
    public void testGetParent() {
        final CommandPath a = new CommandPath("a");
        final CommandPath ab = new CommandPath("a", "b");

        assertFalse(a.getParent().isPresent());
        assertEquals(a, ab.getParent().get());
    }

    @Test
    public void testGetChild() {
        final CommandPath a = new CommandPath("a");
        final CommandPath ab = new CommandPath("a", "b");

        assertFalse(a.getChild().isPresent());
        assertEquals(new CommandPath("b"), ab.getChild().get());
    }

    @Test
    public void testGetSize() {
        final CommandPath a = new CommandPath("a");
        final CommandPath ab = new CommandPath("a", "b");

        assertEquals(1, a.getSize());
        assertEquals(2, ab.getSize());
    }

    @Test
    public void testToString() {
        final CommandPath a = new CommandPath("a");
        final CommandPath ab = new CommandPath("a", "b");

        assertEquals("a", a.toString());
        assertEquals("a b", ab.toString());
    }

    @Test
    public void testCompareTo() {
        final CommandPath a = new CommandPath("a");
        final CommandPath ab = new CommandPath("a", "b");
        final CommandPath b = new CommandPath("b");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(ab));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, ab.compareTo(a));
        assertEquals(0, ab.compareTo(ab));
        assertEquals(-1, ab.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(1, b.compareTo(ab));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final CommandPath a = new CommandPath("a");
        final CommandPath ab = new CommandPath("a", "b");
        final CommandPath b = new CommandPath("b");

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, ab);
        assertNotEquals(a, b);
        assertNotEquals(ab, a);
        assertEquals(ab, ab);
        assertNotEquals(ab, b);
        assertNotEquals(b, a);
        assertNotEquals(b, ab);
        assertEquals(b, b);
    }

    @Test
    public void testHashCode() {
        final CommandPath a = new CommandPath("a");
        final CommandPath ab = new CommandPath("a", "b");
        final CommandPath b = new CommandPath("b");

        assertEquals(757, a.hashCode());
        assertEquals(4695, ab.hashCode());
        assertEquals(758, b.hashCode());
    }
}
