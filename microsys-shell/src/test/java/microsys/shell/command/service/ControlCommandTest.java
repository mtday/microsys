package microsys.shell.command.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.model.ServiceType;
import microsys.service.client.ServiceClient;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.model.Service;
import microsys.service.model.ServiceControlStatus;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
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
 * Perform testing of the {@link ControlCommand} class.
 */
public class ControlCommandTest {
    @SuppressWarnings("unchecked")
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final Service s1 = new Service(ServiceType.CONFIG, "host1", 1234, false, "1.2.3");
        final Service s2 = new Service(ServiceType.HEALTH, "host1", 1235, false, "1.2.3");
        final Service s3 = new Service(ServiceType.WEB, "host2", 1236, true, "1.2.4");
        final Service s4 = new Service(ServiceType.WEB, "host2", 1237, true, "1.2.4");
        final SortedSet<Service> services = new TreeSet<>(Arrays.asList(s1, s2, s3, s4));

        final ServiceControlStatus stat1 = new ServiceControlStatus(true, "stop");
        final ServiceControlStatus stat2 = new ServiceControlStatus(false, "stop");
        final ServiceControlStatus stat3 = new ServiceControlStatus(true, "restart");

        final Map<Service, ServiceControlStatus> stopMap = new TreeMap<>();
        stopMap.put(s1, stat1);
        stopMap.put(s2, stat2);

        final Map<Service, ServiceControlStatus> restartMap = new TreeMap<>();
        restartMap.put(s3, stat3);

        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        Mockito.when(serviceClient.stop((Collection<Service>) Mockito.anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(stopMap));
        Mockito.when(serviceClient.restart((Collection<Service>) Mockito.anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(restartMap));
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getServiceClient()).thenReturn(serviceClient);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        return shellEnvironment;
    }

    @Test
    public void testGetRegistrations() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final ControlCommand memCommand = new ControlCommand(shellEnvironment);

        final List<Registration> registrations = memCommand.getRegistrations();
        assertEquals(2, registrations.size());

        final Registration stop = registrations.get(0);
        assertEquals(new CommandPath("service", "control", "stop"), stop.getPath());
        assertTrue(stop.getDescription().isPresent());
        assertEquals("request the stop of one or more services", stop.getDescription().get());
        assertTrue(stop.getOptions().isPresent());
        final SortedSet<Option> stopOptions = stop.getOptions().get().getOptions();
        assertEquals(4, stopOptions.size());

        final Registration restart = registrations.get(1);
        assertEquals(new CommandPath("service", "control", "restart"), restart.getPath());
        assertTrue(restart.getDescription().isPresent());
        assertEquals("request the restart of one or more services", restart.getDescription().get());
        assertTrue(restart.getOptions().isPresent());
        final SortedSet<Option> restartOptions = restart.getOptions().get().getOptions();
        assertEquals(4, restartOptions.size());
    }

    @Test
    public void testProcess() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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
        assertEquals("    CONFIG  host1  1234  - stop in progress: true", output.get(line++));
        assertEquals("    HEALTH  host1  1235  - stop in progress: false", output.get(line));
    }

    @Test
    public void testProcessWithType() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "control", "restart", "-t", ServiceType.CONFIG.name());
        final CommandPath commandPath = new CommandPath("service", "control", "restart");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(2, output.size());

        int line = 0;
        assertEquals("Displaying the matching service (of 4 total):", output.get(line++));
        assertEquals("    WEB  host2  1236  - restart in progress: true", output.get(line));
    }

    @Test
    public void testProcessWithHost() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "control", "stop", "-h", "host1");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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
        assertEquals("    CONFIG  host1  1234  - stop in progress: true", output.get(line++));
        assertEquals("    HEALTH  host1  1235  - stop in progress: false", output.get(line));
    }

    @Test
    public void testProcessWithPort() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "control", "stop", "-p", "1234");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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
        assertEquals("    CONFIG  host1  1234  - stop in progress: true", output.get(line++));
        assertEquals("    HEALTH  host1  1235  - stop in progress: false", output.get(line));
    }

    @Test
    public void testProcessWithPortNotNumeric() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "control", "stop", "-p", "abcd");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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
        assertEquals("    CONFIG  host1  1234  - stop in progress: true", output.get(line++));
        assertEquals("    HEALTH  host1  1235  - stop in progress: false", output.get(line));
    }

    @Test
    public void testProcessWithVersion() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "control", "stop", "-v", "1.2.3");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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
        assertEquals("    CONFIG  host1  1234  - stop in progress: true", output.get(line++));
        assertEquals("    HEALTH  host1  1235  - stop in progress: false", output.get(line));
    }

    @Test
    public void testProcessNoMatchingServices() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "control", "stop", "-h", "missing");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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
        final ControlCommand memCommand = new ControlCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "control", "stop");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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

        final ServiceControlStatus stat1 = new ServiceControlStatus(true, "stop");
        final Map<Service, ServiceControlStatus> stopMap = new TreeMap<>();
        stopMap.put(s1, stat1);

        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        Mockito.when(serviceClient.stop((Collection<Service>) Mockito.anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(stopMap));
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getServiceClient()).thenReturn(serviceClient);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        final ControlCommand memCommand = new ControlCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "control", "stop");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(2, output.size());
        assertEquals("Displaying the single available service:", output.get(0));
        assertEquals("    CONFIG  host1  1234  - stop in progress: true", output.get(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessTwoServices() throws Exception {
        final Service s1 = new Service(ServiceType.CONFIG, "host1", 1234, false, "1.2.3");
        final Service s2 = new Service(ServiceType.HEALTH, "host1", 1235, false, "1.2.3");
        final SortedSet<Service> services = new TreeSet<>(Arrays.asList(s1, s2));

        final ServiceControlStatus stat1 = new ServiceControlStatus(true, "stop");
        final ServiceControlStatus stat2 = new ServiceControlStatus(false, "stop");

        final Map<Service, ServiceControlStatus> stopMap = new TreeMap<>();
        stopMap.put(s1, stat1);
        stopMap.put(s2, stat2);

        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        Mockito.when(serviceClient.stop((Collection<Service>) Mockito.anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(stopMap));
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        Mockito.when(shellEnvironment.getServiceClient()).thenReturn(serviceClient);
        final ControlCommand memCommand = new ControlCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "control", "stop");
        final CommandPath commandPath = new CommandPath("service", "control", "stop");
        final Registration reg = memCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = memCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());
        assertEquals("Displaying both available services:", output.get(0));
        assertEquals("    CONFIG  host1  1234  - stop in progress: true", output.get(1));
        assertEquals("    HEALTH  host1  1235  - stop in progress: false", output.get(2));
    }

    @Test
    public void testHandleMemoryException() throws Exception {
        final ControlCommand memCommand = new ControlCommand(getShellEnvironment());

        final CommandPath commandPath = new CommandPath("service", "control", "stop");
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
}
