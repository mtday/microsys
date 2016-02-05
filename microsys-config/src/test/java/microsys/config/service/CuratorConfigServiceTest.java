package microsys.config.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Perform testing on the {@link CuratorConfigService} class.
 */
public class CuratorConfigServiceTest {
    private TestingServer testingServer;
    private CuratorFramework curator;

    @Before
    public void before() throws Exception {
        this.testingServer = new TestingServer();
        this.curator = CuratorFrameworkFactory.builder().namespace("namespace")
                .connectString(this.testingServer.getConnectString()).defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        this.curator.start();
    }

    @After
    public void after() throws Exception {
        if (this.curator != null) {
            this.curator.close();
        }
        if (this.testingServer != null) {
            this.testingServer.close();
        }
    }

    @Test
    public void test() throws Exception {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CuratorConfigService.class)).setLevel(Level.OFF);

        final CuratorConfigService svc = new CuratorConfigService(this.curator);

        final Map<String, String> map = svc.getAll();
        assertEquals(0, map.size());

        assertFalse(svc.set("key", "value").isPresent());

        // Wait a little to allow the value to be stored.
        TimeUnit.MILLISECONDS.sleep(300);

        final Map<String, String> afterSet = svc.getAll();
        assertEquals(1, afterSet.size());

        // Overwrite the previous value.
        final Optional<String> oldValue = svc.set("key", "new-value");
        assertTrue(oldValue.isPresent());
        assertEquals("value", oldValue.get());

        // Wait a little to allow the value to be stored.
        TimeUnit.MILLISECONDS.sleep(300);

        final Optional<String> get = svc.get("key");
        assertTrue(get.isPresent());
        assertEquals("new-value", get.get());

        final Optional<String> unset = svc.unset("key");
        assertTrue(unset.isPresent());
        assertEquals("new-value", unset.get());

        final Optional<String> getMissing = svc.get("missing");
        assertFalse(getMissing.isPresent());

        final Optional<String> unsetMissing = svc.unset("missing");
        assertFalse(unsetMissing.isPresent());
    }
}
