package microsys.crypto.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Perform testing on the {@link HexUtils} class.
 */
public class HexUtilsTest {
    @Test
    public void testConstructor() {
        // Just for 100% coverage.
        assertEquals("012a7f", new HexUtils().bytesToHex(new byte[] { 0x01, 0x2A, 0x7F }));
    }

    @Test
    public void testRoundTrip() {
        final String original = "abcd1234ABCD";

        final byte[] bytes = HexUtils.hexToBytes(original);
        final String hex = HexUtils.bytesToHex(bytes);

        assertEquals(original.toLowerCase(), hex);
    }
}