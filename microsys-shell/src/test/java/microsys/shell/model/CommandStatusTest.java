package microsys.shell.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Perform testing on the {@link CommandStatus} enumeration.
 */
public class CommandStatusTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(CommandStatus.values().length > 0);
        assertEquals(CommandStatus.SUCCESS, CommandStatus.valueOf("SUCCESS"));
    }
}
