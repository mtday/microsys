package microsys.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link CommonConfig} enumeration.
 */
public class CommonConfigTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(CommonConfig.values().length > 0);
        assertEquals(CommonConfig.SYSTEM_NAME, CommonConfig.valueOf(CommonConfig.SYSTEM_NAME.name()));
    }

    @Test
    public void testGetKey() {
        assertEquals("system.name", CommonConfig.SYSTEM_NAME.getKey());
    }
}
