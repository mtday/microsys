package microsys.shell.completer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import microsys.common.model.service.ServiceType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perform testing on the {@link ServiceTypeCompleter} class.
 */
public class ServiceTypeCompleterTest {
    @Test
    public void testEmpty() {
        final ServiceTypeCompleter completer = new ServiceTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("", 0, candidates);

        final List<CharSequence> expected =
                Arrays.asList(ServiceType.values()).stream().map(ServiceType::name).map(String::toLowerCase).sorted()
                        .collect(Collectors.toList());

        assertEquals(expected.size(), candidates.size());
        assertEquals(0, position);
        assertEquals(expected.toString(), candidates.toString());
    }

    @Test
    public void testWhiteSpace() {
        final ServiceTypeCompleter completer = new ServiceTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete(" ", 1, candidates);

        // This is currently the expected behavior.
        assertEquals(0, candidates.size());
        assertEquals(-1, position);
        assertEquals("[]", candidates.toString());
    }

    @Test
    public void testPartial() {
        final ServiceTypeCompleter completer = new ServiceTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("co", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[config]", candidates.toString());
    }

    @Test
    public void testComplete() {
        final ServiceTypeCompleter completer = new ServiceTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("config", 6, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[config]", candidates.toString());
    }

    @Test
    public void testCompleteMiddle() {
        final ServiceTypeCompleter completer = new ServiceTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("config", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[config]", candidates.toString());
    }
}
