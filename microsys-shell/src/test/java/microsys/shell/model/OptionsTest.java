package microsys.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jline.console.completer.StringsCompleter;
import microsys.shell.completer.CompletionTree;

import java.util.List;
import java.util.Optional;

/**
 * Perform testing on the {@link Options} class.
 */
public class OptionsTest {
    @Test
    public void testCompareTo() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false, Optional.empty());
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true, Optional.empty());
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false, Optional.empty());
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
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false, Optional.empty());
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true, Optional.empty());
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false, Optional.empty());
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
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false, Optional.empty());
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true, Optional.empty());
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false, Optional.empty());
        final Options a = new Options(oa, ob);
        final Options b = new Options(ob, oc);

        assertEquals(792738070, a.hashCode());
        assertEquals(1766747893, b.hashCode());
    }

    @Test
    public void testToString() {
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false, Optional.empty());
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true, Optional.empty());
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false, Optional.empty());
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
        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false, Optional.empty());
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true, Optional.empty());
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false, Optional.empty());
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

    @Test
    public void testGetCompletions() {
        final StringsCompleter ca = new StringsCompleter("a");
        final StringsCompleter cb = new StringsCompleter("b");

        final Option oa = new Option("a", "a", Optional.of("a"), Optional.of("a"), 1, true, false, Optional.of(ca));
        final Option ob = new Option("b", "b", Optional.empty(), Optional.empty(), 0, false, true, Optional.of(cb));
        final Option oc = new Option("c", "c", Optional.of("c"), Optional.empty(), 2, false, false, Optional.empty());
        final Option od = new Option("d", "d", Optional.empty(), Optional.empty(), 2, false, false, Optional.empty());
        final Options a = new Options(oa, ob);
        final Options b = new Options(ob, oc, od);

        final List<CompletionTree> la = a.getCompletions();
        final List<CompletionTree> lb = b.getCompletions();

        assertEquals(3, la.size());
        final CompletionTree la0 = la.get(0);
        assertEquals("Optional[-a]", la0.getCandidate().toString());
        assertFalse(la0.getCompleter().isPresent());
        assertEquals(1, la0.getChildren().size());
        final CompletionTree la00 = la0.getChildren().get(0);
        assertEquals("Optional[]", la00.getCandidate().toString());
        assertTrue(la00.getCompleter().isPresent());
        assertEquals(ca, la00.getCompleter().get());
        assertEquals(0, la00.getChildren().size());

        final CompletionTree la1 = la.get(1);
        assertEquals("Optional[--a]", la1.getCandidate().toString());
        assertFalse(la1.getCompleter().isPresent());
        assertEquals(1, la1.getChildren().size());
        final CompletionTree la10 = la1.getChildren().get(0);
        assertEquals("Optional[]", la10.getCandidate().toString());
        assertTrue(la10.getCompleter().isPresent());
        assertEquals(ca, la10.getCompleter().get());
        assertEquals(0, la10.getChildren().size());

        final CompletionTree la2 = la.get(2);
        assertEquals("Optional[-b]", la2.getCandidate().toString());
        assertFalse(la2.getCompleter().isPresent());
        assertEquals(1, la2.getChildren().size());
        final CompletionTree la20 = la2.getChildren().get(0);
        assertEquals("Optional[]", la20.getCandidate().toString());
        assertTrue(la20.getCompleter().isPresent());
        assertEquals(cb, la20.getCompleter().get());
        assertEquals(0, la20.getChildren().size());

        assertEquals(4, lb.size());
        final CompletionTree lb0 = la.get(0);
        assertEquals("Optional[-a]", lb0.getCandidate().toString());
        assertFalse(lb0.getCompleter().isPresent());
        assertEquals(1, lb0.getChildren().size());
        final CompletionTree lb00 = lb0.getChildren().get(0);
        assertEquals("Optional[]", lb00.getCandidate().toString());
        assertTrue(lb00.getCompleter().isPresent());
        assertEquals(ca, lb00.getCompleter().get());
        assertEquals(0, lb00.getChildren().size());

        final CompletionTree lb1 = lb.get(1);
        assertEquals("Optional[-c]", lb1.getCandidate().toString());
        assertFalse(lb1.getCompleter().isPresent());
        assertEquals(0, lb1.getChildren().size());

        final CompletionTree lb2 = lb.get(2);
        assertEquals("Optional[--c]", lb2.getCandidate().toString());
        assertFalse(lb2.getCompleter().isPresent());
        assertEquals(0, lb2.getChildren().size());

        final CompletionTree lb3 = lb.get(3);
        assertEquals("Optional[-d]", lb3.getCandidate().toString());
        assertFalse(lb3.getCompleter().isPresent());
        assertEquals(0, lb3.getChildren().size());
    }
}
