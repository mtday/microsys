package microsys.shell.completer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.discovery.DiscoveryManager;
import microsys.shell.model.ShellEnvironment;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;

/**
 * Perform testing on the {@link ServiceHostCompleter} class.
 */
public class ServiceHostCompleterTest {
    @Nonnull
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final SortedSet<Service> services = new TreeSet<>();
        services.add(new Service(ServiceType.CONFIG, "host1", 1234, false, "1.2.3"));
        services.add(new Service(ServiceType.HEALTH, "host1", 1235, false, "1.2.3"));
        services.add(new Service(ServiceType.WEB, "host2", 1236, true, "1.2.4"));
        services.add(new Service(ServiceType.WEB, "host2", 1237, true, "1.2.4"));
        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        return shellEnvironment;
    }

    @Test
    public void testEmpty() throws Exception {
        final ServiceHostCompleter completer = new ServiceHostCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("", 0, candidates);

        assertEquals(2, candidates.size());
        assertEquals(0, position);
        assertEquals("[host1, host2]", candidates.toString());
    }

    @Test
    public void testWhiteSpace() throws Exception {
        final ServiceHostCompleter completer = new ServiceHostCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete(" ", 1, candidates);

        // This is currently the expected behavior.
        assertEquals(0, candidates.size());
        assertEquals(-1, position);
        assertEquals("[]", candidates.toString());
    }

    @Test
    public void testPartial() throws Exception {
        final ServiceHostCompleter completer = new ServiceHostCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("ho", 2, candidates);

        assertEquals(2, candidates.size());
        assertEquals(0, position);
        assertEquals("[host1, host2]", candidates.toString());
    }

    @Test
    public void testComplete() throws Exception {
        final ServiceHostCompleter completer = new ServiceHostCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("host1", 4, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[host1]", candidates.toString());
    }

    @Test
    public void testCompleteMiddle() throws Exception {
        final ServiceHostCompleter completer = new ServiceHostCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("host1", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[host1]", candidates.toString());
    }

    @Test
    public void testException() throws Exception {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.doThrow(new RuntimeException("Fake")).when(shellEnvironment).getDiscoveryManager();
        final ServiceHostCompleter completer = new ServiceHostCompleter(shellEnvironment);
        assertEquals(-1, completer.complete("host1", 2, new LinkedList<>()));
    }
}
