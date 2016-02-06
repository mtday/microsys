package microsys.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link ServiceType} enumeration.
 */
public class ServiceTypeTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(ServiceType.values().length > 0);
        assertEquals(ServiceType.CONFIG, ServiceType.valueOf(ServiceType.CONFIG.name()));
    }
}
