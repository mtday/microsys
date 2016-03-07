package microsys.shell.completer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import microsys.crypto.EncryptionType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perform testing on the {@link EncryptionTypeCompleter} class.
 */
public class EncryptionTypeCompleterTest {
    @Test
    public void testEmpty() {
        final EncryptionTypeCompleter completer = new EncryptionTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("", 0, candidates);

        final List<CharSequence> expected =
                Arrays.asList(
                        EncryptionType.values()).stream().map(EncryptionType::name).map(String::toLowerCase).sorted()
                        .collect(Collectors.toList());

        assertEquals(expected.size(), candidates.size());
        assertEquals(0, position);
        assertEquals(expected.toString(), candidates.toString());
    }

    @Test
    public void testWhiteSpace() {
        final EncryptionTypeCompleter completer = new EncryptionTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete(" ", 1, candidates);

        // This is currently the expected behavior.
        assertEquals(0, candidates.size());
        assertEquals(-1, position);
        assertEquals("[]", candidates.toString());
    }

    @Test
    public void testPartial() {
        final EncryptionTypeCompleter completer = new EncryptionTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("pa", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[password_based]", candidates.toString());
    }

    @Test
    public void testComplete() {
        final EncryptionTypeCompleter completer = new EncryptionTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("password_based", 14, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[password_based]", candidates.toString());
    }

    @Test
    public void testCompleteMiddle() {
        final EncryptionTypeCompleter completer = new EncryptionTypeCompleter();
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("password_based", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[password_based]", candidates.toString());
    }
}
