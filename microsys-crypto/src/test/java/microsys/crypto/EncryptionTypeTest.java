package microsys.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link EncryptionType} enumeration.
 */
public class EncryptionTypeTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(EncryptionType.values().length > 0);
        assertEquals(EncryptionType.PASSWORD_BASED, EncryptionType.valueOf(EncryptionType.PASSWORD_BASED.name()));
    }
}
