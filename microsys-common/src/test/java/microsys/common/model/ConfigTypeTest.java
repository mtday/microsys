package microsys.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link ConfigType} enumeration.
 */
public class ConfigTypeTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(ConfigType.values().length > 0);
        assertEquals(ConfigType.STATIC, ConfigType.valueOf(ConfigType.STATIC.name()));
    }
}
