package microsys.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Perform testing on the {@link UserCommand} class.
 */
public class UserCommandTest {
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

        final List<String> ua = Arrays.asList("a");
        final List<String> ub = Arrays.asList("b");

        final Registration ra = new Registration(ca, oa, da);
        final Registration rb = new Registration(cb, ob, db);

        final UserCommand a = new UserCommand(ca, ra, ua);
        final UserCommand b = new UserCommand(cb, rb, ub);

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

        final List<String> ua = Arrays.asList("a");
        final List<String> ub = Arrays.asList("b");

        final Registration ra = new Registration(ca, oa, da);
        final Registration rb = new Registration(cb, ob, db);

        final UserCommand a = new UserCommand(ca, ra, ua);
        final UserCommand b = new UserCommand(cb, rb, ub);

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(b, a);
        assertEquals(b, b);
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

        final List<String> ua = Arrays.asList("a");
        final List<String> ub = Arrays.asList("b");

        final Registration ra = new Registration(ca, oa, da);
        final Registration rb = new Registration(cb, ob, db);

        final UserCommand a = new UserCommand(ca, ra, ua);
        final UserCommand b = new UserCommand(cb, rb, ub);

        assertEquals(1925571, a.hashCode());
        assertEquals(1926978, b.hashCode());
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

        final List<String> ua = Arrays.asList("a");
        final List<String> ub = Arrays.asList("b");

        final Registration ra = new Registration(ca, oa, da);
        final Registration rb = new Registration(cb, ob, db);

        final UserCommand a = new UserCommand(ca, ra, ua);
        final UserCommand b = new UserCommand(cb, rb, ub);

        assertEquals("UserCommand[commandPath=a,registration=Registration[path=a,options=Optional.empty,"
                + "description=Optional[a]],userInput=[a]]", a.toString());
        assertEquals(
                "UserCommand[commandPath=b,registration=Registration[path=b,"
                        + "options=Optional[Options[options=[Option[description=a,shortOption=a,"
                        + "longOption=Optional[a],argName=Optional[a],arguments=1,required=true,optionalArg=false], "
                        + "Option[description=b,shortOption=b,longOption=Optional.empty,argName=Optional.empty,"
                        + "arguments=0,required=false,optionalArg=true]]]],description=Optional.empty],userInput=[b]]",
                b.toString());
    }

    @Test
    public void testGetCommandLine() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath c = new CommandPath("a");
        final Optional<Options> o = Optional.of(new Options(oa, ob));
        final Optional<String> d = Optional.empty();

        final List<String> u = Arrays.asList("a", "-a", "5");
        final Registration r = new Registration(c, o, d);
        final UserCommand cmd = new UserCommand(c, r, u);

        final Optional<CommandLine> commandLine = cmd.getCommandLine();
        assertTrue(commandLine.isPresent());
        assertEquals("5", commandLine.get().getOptionValue('a'));
    }

    @Test
    public void testGetCommandLineInvalid() throws ParseException {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath c = new CommandPath("a");
        final Optional<Options> o = Optional.of(new Options(oa, ob));
        final Optional<String> d = Optional.empty();

        final List<String> u = Arrays.asList("a"); // Not providing the required parameter
        final Registration r = new Registration(c, o, d);
        final UserCommand cmd = new UserCommand(c, r, u);

        assertFalse(cmd.getCommandLine().isPresent());
    }

    @Test
    public void testGetCommandLineNoOptions() throws ParseException {
        final CommandPath c = new CommandPath("a");
        final Optional<Options> o = Optional.empty();
        final Optional<String> d = Optional.empty();

        final List<String> u = Arrays.asList("a");
        final Registration r = new Registration(c, o, d);
        final UserCommand cmd = new UserCommand(c, r, u);

        assertFalse(cmd.getCommandLine().isPresent());
    }

    @Test
    public void testValidateCommandLine() throws ParseException {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath c = new CommandPath("a");
        final Optional<Options> o = Optional.of(new Options(oa, ob));
        final Optional<String> d = Optional.empty();

        final List<String> u = Arrays.asList("a", "-a", "5");
        final Registration r = new Registration(c, o, d);
        final UserCommand cmd = new UserCommand(c, r, u);

        cmd.validateCommandLine();
    }

    @Test(expected = ParseException.class)
    public void testValidateCommandLineInvalid() throws ParseException {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final CommandPath c = new CommandPath("a");
        final Optional<Options> o = Optional.of(new Options(oa, ob));
        final Optional<String> d = Optional.empty();

        final List<String> u = Arrays.asList("a"); // Not providing the required parameter
        final Registration r = new Registration(c, o, d);
        final UserCommand cmd = new UserCommand(c, r, u);

        cmd.validateCommandLine();
    }
}
