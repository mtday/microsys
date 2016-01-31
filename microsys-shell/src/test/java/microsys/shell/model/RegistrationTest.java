package microsys.shell.model;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Perform testing of the {@link Registration} class.
 */
public class RegistrationTest {
    @Test
    public void testCompareTo() {
        final CommandPath ca = new CommandPath("a");
        final Optional<Options> oa = Optional.empty();
        final Optional<String> da = Optional.of("a");

        final Option oba = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option obb = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath cb = new CommandPath("b");
        final Optional<Options> ob = Optional.of(new Options(oba, obb));
        final Optional<String> db = Optional.empty();

        final Registration a = new Registration(ca, oa, da);
        final Registration b = new Registration(cb, ob, db);

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final CommandPath ca = new CommandPath("a");
        final Optional<Options> oa = Optional.empty();
        final Optional<String> da = Optional.of("a");

        final Option oba = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option obb = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath cb = new CommandPath("b");
        final Optional<Options> ob = Optional.of(new Options(oba, obb));
        final Optional<String> db = Optional.empty();

        final Registration a = new Registration(ca, oa, da);
        final Registration b = new Registration(cb, ob, db);

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(b, a);
        assertEquals(b, b);
    }

    @Test
    public void testToString() {
        final CommandPath ca = new CommandPath("a");
        final Optional<Options> oa = Optional.empty();
        final Optional<String> da = Optional.of("a");

        final Option oba = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option obb = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath cb = new CommandPath("b");
        final Optional<Options> ob = Optional.of(new Options(oba, obb));
        final Optional<String> db = Optional.empty();

        final Registration a = new Registration(ca, oa, da);
        final Registration b = new Registration(cb, ob, db);

        assertEquals("Registration[path=a,options=Optional.empty,description=Optional[a]]", a.toString());
        assertEquals("Registration[path=b,options=Optional[Options[options=[Option[description=a,shortOption=a,"
                + "longOption=Optional[a],argName=Optional[a],arguments=1,required=true,optionalArg=false], "
                + "Option[description=b,shortOption=b,longOption=Optional.empty,argName=Optional.empty,"
                + "arguments=0,required=false,optionalArg=true]]]],description=Optional.empty]", b.toString());
    }

    @Test
    public void testHashCode() {
        final CommandPath ca = new CommandPath("a");
        final Optional<Options> oa = Optional.empty();
        final Optional<String> da = Optional.of("a");

        final Option oba = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option obb = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath cb = new CommandPath("b");
        final Optional<Options> ob = Optional.of(new Options(oba, obb));
        final Optional<String> db = Optional.empty();

        final Registration a = new Registration(ca, oa, da);
        final Registration b = new Registration(cb, ob, db);

        assertEquals(757, a.hashCode());
        assertEquals(758, b.hashCode());
    }
}
