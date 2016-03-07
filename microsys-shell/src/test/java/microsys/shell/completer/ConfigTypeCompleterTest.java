package microsys.shell.completer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import microsys.common.model.ConfigType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perform testing on the {@link ConfigTypeCompleter} class.
 */
public class ConfigTypeCompleterTest {
    @Test
    public void testEmpty() {
        final ConfigTypeCompleter completer = new ConfigTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("", 0, candidates);

        final List<CharSequence> expected =
                Arrays.asList(
                        ConfigType.values()).stream().map(ConfigType::name).map(String::toLowerCase).sorted()
                        .collect(Collectors.toList());

        assertEquals(expected.size(), candidates.size());
        assertEquals(0, position);
        assertEquals(expected.toString(), candidates.toString());
    }

    @Test
    public void testWhiteSpace() {
        final ConfigTypeCompleter completer = new ConfigTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete(" ", 1, candidates);

        // This is currently the expected behavior.
        assertEquals(0, candidates.size());
        assertEquals(-1, position);
        assertEquals("[]", candidates.toString());
    }

    @Test
    public void testPartial() {
        final ConfigTypeCompleter completer = new ConfigTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("st", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[static]", candidates.toString());
    }

    @Test
    public void testComplete() {
        final ConfigTypeCompleter completer = new ConfigTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("static", 6, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[static]", candidates.toString());
    }

    @Test
    public void testCompleteMiddle() {
        final ConfigTypeCompleter completer = new ConfigTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("static", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[static]", candidates.toString());
    }
}
