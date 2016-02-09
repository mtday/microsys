package microsys.shell.command.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.model.ServiceType;
import microsys.service.client.ServiceClient;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.model.Service;
import microsys.service.model.ServiceMemory;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

/**
 * Perform testing of the {@link MemoryCommand} class.
 */
public class MemoryCommandTest {
    @SuppressWarnings("unchecked")
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final Service s1 = new Service(ServiceType.CONFIG, "host1", 1234, false, "1.2.3");
        final Service s2 = new Service(ServiceType.HEALTH, "host1", 1235, false, "1.2.3");
        final Service s3 = new Service(ServiceType.WEB, "host2", 1236, true, "1.2.4");
        final Service s4 = new Service(ServiceType.WEB, "host2", 1237, true, "1.2.4");
        final SortedSet<Service> services = new TreeSet<>(Arrays.asList(s1, s2, s3, s4));

        final MemoryUsage mem1 = new MemoryUsage(0, 1234567, 2222222, 2222222);
        final MemoryUsage mem2 = new MemoryUsage(0, 1002222, 3000000, 3000000);
        final MemoryUsage mem3 = new MemoryUsage(0, 21212121, 50000000, 50000000);

        final Map<Service, ServiceMemory> memory = new TreeMap<>();
        memory.put(s1, new ServiceMemory(mem1, mem2));
        memory.put(s2, new ServiceMemory(mem2, mem3));

        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        Mockito.when(serviceClient.getMemory((Collection<Service>) Mockito.anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(memory));
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getServiceClient()).thenReturn(serviceClient);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        return shellEnvironment;
    }

    @Test
    public void testGetRegistrations() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final MemoryCommand memCommand = new MemoryCommand(shellEnvironment);

        final List<Registration> registrations = memCommand.getRegistrations();
        assertEquals(1, registrations.size());

        final Registration memory = registrations.get(0);
        assertEquals(new CommandPath("service", "memory"), memory.getPath());
        assertTrue(memory.getDescription().isPresent());
        assertEquals("display memory usage information for one or more services", memory.getDescription().get());
        assertTrue(memory.getOptions().isPresent());
        final SortedSet<Option> memoryOptions = memory.getOptions().get().getOptions();
        assertEquals(4, memoryOptions.size());
    }

    @Test
    public void testProcess() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = new Registration(commandPath, Optional.empty(), Optional.empty());
        final UserCommand userCommand = new UserCommand(commandPath, reg, commandPath.getPath());
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Displaying all 4 services:", output.get(line++));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(line++));
        assertEquals("    HEALTH  host1  1235  Heap: 978.73k of 2.86M (33.41%), Non-Heap: 20.23M of 47.68M (42.42%)",
                output.get(line));
    }

    @Test
    public void testProcessWithType() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "memory", "-t", ServiceType.CONFIG.name());
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Displaying the matching service (of 4 total):", output.get(line++));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(line++));
        assertEquals("    HEALTH  host1  1235  Heap: 978.73k of 2.86M (33.41%), Non-Heap: 20.23M of 47.68M (42.42%)",
                output.get(line));
    }

    @Test
    public void testProcessWithHost() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "memory", "-h", "host1");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Displaying 2 matching services (of 4 total):", output.get(line++));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(line++));
        assertEquals("    HEALTH  host1  1235  Heap: 978.73k of 2.86M (33.41%), Non-Heap: 20.23M of 47.68M (42.42%)",
                output.get(line));
    }

    @Test
    public void testProcessWithPort() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "memory", "-p", "1234");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Displaying the matching service (of 4 total):", output.get(line++));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(line++));
        assertEquals("    HEALTH  host1  1235  Heap: 978.73k of 2.86M (33.41%), Non-Heap: 20.23M of 47.68M (42.42%)",
                output.get(line));
    }

    @Test
    public void testProcessWithPortNotNumeric() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "memory", "-p", "abcd");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Displaying all 4 services:", output.get(line++));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(line++));
        assertEquals("    HEALTH  host1  1235  Heap: 978.73k of 2.86M (33.41%), Non-Heap: 20.23M of 47.68M (42.42%)",
                output.get(line));
    }

    @Test
    public void testProcessWithVersion() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "memory", "-v", "1.2.3");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Displaying 2 matching services (of 4 total):", output.get(line++));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(line++));
        assertEquals("    HEALTH  host1  1235  Heap: 978.73k of 2.86M (33.41%), Non-Heap: 20.23M of 47.68M (42.42%)",
                output.get(line));
    }

    @Test
    public void testProcessNoMatchingServices() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "memory", "-h", "missing");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());
        assertEquals("None of the running services (of which there are 4) match", output.get(0));
    }

    @Test
    public void testProcessNoServices() throws Exception {
        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(new TreeSet<>());
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        final MemoryCommand memCommand = new MemoryCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "memory");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());
        assertEquals("No services are running", output.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessOneService() throws Exception {
        final Service s1 = new Service(ServiceType.CONFIG, "host1", 1234, false, "1.2.3");
        final SortedSet<Service> services = new TreeSet<>(Collections.singletonList(s1));

        final MemoryUsage mem1 = new MemoryUsage(0, 1234567, 2222222, 2222222);
        final MemoryUsage mem2 = new MemoryUsage(0, 1002222, 3000000, 3000000);

        final Map<Service, ServiceMemory> memory = new TreeMap<>();
        memory.put(s1, new ServiceMemory(mem1, mem2));

        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        Mockito.when(serviceClient.getMemory((Collection<Service>) Mockito.anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(memory));
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getServiceClient()).thenReturn(serviceClient);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        final MemoryCommand memCommand = new MemoryCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "memory");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(2, output.size());
        assertEquals("Displaying the single available service:", output.get(0));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessTwoServices() throws Exception {
        final Service s1 = new Service(ServiceType.CONFIG, "host1", 1234, false, "1.2.3");
        final Service s2 = new Service(ServiceType.HEALTH, "host1", 1235, false, "1.2.3");
        final SortedSet<Service> services = new TreeSet<>(Arrays.asList(s1, s2));

        final MemoryUsage mem1 = new MemoryUsage(0, 1234567, 2222222, 2222222);
        final MemoryUsage mem2 = new MemoryUsage(0, 1002222, 3000000, 3000000);
        final MemoryUsage mem3 = new MemoryUsage(0, 21212121, 50000000, -1);

        final Map<Service, ServiceMemory> memory = new TreeMap<>();
        memory.put(s1, new ServiceMemory(mem1, mem2));
        memory.put(s2, new ServiceMemory(mem2, mem3));

        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        Mockito.when(serviceClient.getMemory((Collection<Service>) Mockito.anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(memory));
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        Mockito.when(shellEnvironment.getServiceClient()).thenReturn(serviceClient);
        final MemoryCommand memCommand = new MemoryCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "memory");
        final CommandPath commandPath = new CommandPath("service", "memory");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());
        assertEquals("Displaying both available services:", output.get(0));
        assertEquals("    CONFIG  host1  1234  Heap: 1.18M of 2.12M (55.56%), Non-Heap: 978.73k of 2.86M (33.41%)",
                output.get(1));
        assertEquals("    HEALTH  host1  1235  Heap: 978.73k of 2.86M (33.41%), Non-Heap: 20.23M of unknown (0.00%)",
                output.get(2));
    }

    @Test
    public void testHandleMemoryException() throws Exception {
        final MemoryCommand memCommand = new MemoryCommand(getShellEnvironment());

        final CommandPath commandPath = new CommandPath("service", "memory");
        final UserCommand userCommand = Mockito.mock(UserCommand.class);
        Mockito.when(userCommand.getCommandPath()).thenReturn(commandPath);
        Mockito.when(userCommand.getCommandLine()).thenThrow(new RuntimeException("Fake"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());
        assertEquals("Failed to retrieve available services: RuntimeException: Fake", output.get(0));
    }

    @Test
    public void testStringerReadable() {
        final List<Service> list = Collections.emptyList();
        final MemoryCommand.Stringer str = new MemoryCommand.Stringer(list);
        assertEquals("unknown", str.readable(-1));
        assertEquals("1b", str.readable(1));
        assertEquals("1.95k", str.readable(2000L));
        assertEquals("1.91M", str.readable(2000000L));
        assertEquals("1.86G", str.readable(2000000000L));
    }
}
