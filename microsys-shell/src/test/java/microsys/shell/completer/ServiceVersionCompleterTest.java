package microsys.shell.completer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.model.ServiceType;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.model.Service;
import microsys.shell.model.ShellEnvironment;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;

/**
 * Perform testing on the {@link ServiceVersionCompleter} class.
 */
public class ServiceVersionCompleterTest {
    @Nonnull
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final SortedSet<Service> services = new TreeSet<>();
        services.add(new Service(ServiceType.CONFIG, "1.2.3", 1234, false, "1.2.3"));
        services.add(new Service(ServiceType.HEALTH, "1.2.3", 1235, false, "1.2.3"));
        services.add(new Service(ServiceType.WEB, "1.2.4", 1236, true, "1.2.4"));
        services.add(new Service(ServiceType.WEB, "1.2.4", 1237, true, "1.2.4"));
        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        return shellEnvironment;
    }

    @Test
    public void testEmpty() throws Exception {
        final ServiceVersionCompleter completer = new ServiceVersionCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("", 0, candidates);

        assertEquals(2, candidates.size());
        assertEquals(0, position);
        assertEquals("[1.2.3, 1.2.4]", candidates.toString());
    }

    @Test
    public void testWhiteSpace() throws Exception {
        final ServiceVersionCompleter completer = new ServiceVersionCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete(" ", 1, candidates);

        // This is currently the expected behavior.
        assertEquals(0, candidates.size());
        assertEquals(-1, position);
        assertEquals("[]", candidates.toString());
    }

    @Test
    public void testPartial() throws Exception {
        final ServiceVersionCompleter completer = new ServiceVersionCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("1.2", 3, candidates);

        assertEquals(2, candidates.size());
        assertEquals(0, position);
        assertEquals("[1.2.3, 1.2.4]", candidates.toString());
    }

    @Test
    public void testComplete() throws Exception {
        final ServiceVersionCompleter completer = new ServiceVersionCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("1.2.3", 4, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[1.2.3]", candidates.toString());
    }

    @Test
    public void testCompleteMiddle() throws Exception {
        final ServiceVersionCompleter completer = new ServiceVersionCompleter(getShellEnvironment());
        final List<CharSequence> candidates = new LinkedList<>();
        final int position = completer.complete("1.2.3", 2, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[1.2.3]", candidates.toString());
    }

    @Test
    public void testException() throws Exception {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.doThrow(new RuntimeException("Fake")).when(shellEnvironment).getDiscoveryManager();
        final ServiceVersionCompleter completer = new ServiceVersionCompleter(shellEnvironment);
        assertEquals(-1, completer.complete("1.2.3", 2, new LinkedList<>()));
    }
}
