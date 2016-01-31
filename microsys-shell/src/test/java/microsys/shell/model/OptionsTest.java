package microsys.shell.model;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Perform testing on the {@link Options} class.
 */
public class OptionsTest {
    @Test
    public void testCompareTo() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false);
        final Options a = new Options(oa, ob);
        final Options b = new Options(ob, oc);

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false);
        final Options a = new Options(oa, ob);
        final Options b = new Options(ob, oc);

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(b, a);
        assertEquals(b, b);
    }

    @Test
    public void testHashCode() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false);
        final Options a = new Options(oa, ob);
        final Options b = new Options(ob, oc);

        assertEquals(792738070, a.hashCode());
        assertEquals(1766747893, b.hashCode());
    }

    @Test
    public void testToString() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false);
        final Options a = new Options(oa, ob);
        final Options b = new Options(ob, oc);

        assertEquals("Options[options=[Option[description=a,shortOption=a,longOption=Optional[a],argName=Optional[a],"
                + "arguments=1,required=true,optionalArg=false], Option[description=b,shortOption=b,"
                + "longOption=Optional.empty,argName=Optional.empty,arguments=0,required=false,optionalArg=true]]]",
                a.toString());
        assertEquals("Options[options=[Option[description=b,shortOption=b,longOption=Optional.empty,argName=Optional"
                + ".empty,arguments=0,required=false,optionalArg=true], Option[description=c,shortOption=c,"
                + "longOption=Optional[c],argName=Optional.empty,arguments=2,required=false,optionalArg=false]]]",
                b.toString());
    }

    @Test
    public void testAsOptions() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false);
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true);
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false);
        final Options a = new Options(oa, ob);
        final Options b = new Options(ob, oc);

        final org.apache.commons.cli.Options ao = a.asOptions();
        final org.apache.commons.cli.Options bo = b.asOptions();

        final org.apache.commons.cli.Option aoa = ao.getOption("a");
        final org.apache.commons.cli.Option aob = ao.getOption("b");
        final org.apache.commons.cli.Option bob = bo.getOption("b");
        final org.apache.commons.cli.Option boc = bo.getOption("c");

        assertEquals(oa.getDescription(), aoa.getDescription());
        assertEquals(oa.getShortOption(), aoa.getOpt());
        assertEquals(oa.getLongOption().get(), aoa.getLongOpt());
        assertEquals(oa.getArgName().get(), aoa.getArgName());
        assertEquals(oa.getArguments(), aoa.getArgs());
        assertEquals(oa.isRequired(), aoa.isRequired());
        assertEquals(oa.hasOptionalArg(), aoa.hasOptionalArg());

        assertEquals(ob.getDescription(), aob.getDescription());
        assertEquals(ob.getShortOption(), aob.getOpt());
        assertNull(aob.getLongOpt());
        assertNull(aob.getArgName());
        assertEquals(ob.getArguments(), aob.getArgs());
        assertEquals(ob.isRequired(), aob.isRequired());
        assertEquals(ob.hasOptionalArg(), aob.hasOptionalArg());

        assertEquals(ob.getDescription(), bob.getDescription());
        assertEquals(ob.getShortOption(), bob.getOpt());
        assertNull(bob.getLongOpt());
        assertNull(bob.getArgName());
        assertEquals(ob.getArguments(), bob.getArgs());
        assertEquals(ob.isRequired(), bob.isRequired());
        assertEquals(ob.hasOptionalArg(), bob.hasOptionalArg());

        assertEquals(oc.getDescription(), boc.getDescription());
        assertEquals(oc.getShortOption(), boc.getOpt());
        assertEquals(oc.getLongOption().get(), boc.getLongOpt());
        assertNull(boc.getArgName());
        assertEquals(oc.getArguments(), boc.getArgs());
        assertEquals(oc.isRequired(), boc.isRequired());
        assertEquals(oc.hasOptionalArg(), boc.hasOptionalArg());
    }
}
