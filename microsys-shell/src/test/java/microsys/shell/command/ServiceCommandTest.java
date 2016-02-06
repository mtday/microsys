package microsys.shell.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.model.ServiceType;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.model.Service;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Perform testing of the {@link ServiceCommand} class.
 */
public class ServiceCommandTest {
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final SortedSet<Service> services = new TreeSet<>();
        services.add(new Service(ServiceType.CONFIG, "host1", 1234, false));
        services.add(new Service(ServiceType.HEALTH, "host1", 1235, false));
        services.add(new Service(ServiceType.WEB, "host2", 1236, true));
        services.add(new Service(ServiceType.WEB, "host2", 1237, true));
        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        return shellEnvironment;
    }

    @Test
    public void testGetRegistrations() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final ServiceCommand serviceCommand = new ServiceCommand(shellEnvironment);

        final List<Registration> registrations = serviceCommand.getRegistrations();
        assertEquals(2, registrations.size());

        final Registration list = registrations.get(0);
        assertEquals(new CommandPath("service", "list"), list.getPath());
        assertTrue(list.getDescription().isPresent());
        assertEquals("provides information about the available services", list.getDescription().get());
        assertTrue(list.getOptions().isPresent());
        final SortedSet<Option> listOptions = list.getOptions().get().getOptions();
        assertEquals(2, listOptions.size());

        final Registration restart = registrations.get(1);
        assertEquals(new CommandPath("service", "restart"), restart.getPath());
        assertTrue(restart.getDescription().isPresent());
        assertEquals("request the restart of a service", restart.getDescription().get());
        assertTrue(restart.getOptions().isPresent());
        final SortedSet<Option> restartOptions = restart.getOptions().get().getOptions();
        assertEquals(3, restartOptions.size());
    }

    @Test
    public void testProcessList() throws Exception {
        final ServiceCommand serviceCommand = new ServiceCommand(getShellEnvironment());

        final CommandPath commandPath = new CommandPath("service", "list");
        final Registration reg = new Registration(commandPath, Optional.empty(), Optional.empty());
        final UserCommand userCommand = new UserCommand(commandPath, reg, commandPath.getPath());
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(5, output.size());

        int line = 0;
        assertEquals("Displaying all 4 services:", output.get(line++));
        assertEquals("    CONFIG  host1  1234  (insecure)", output.get(line++));
        assertEquals("    HEALTH  host1  1235  (insecure)", output.get(line++));
        assertEquals("    WEB     host2  1236  (secure)", output.get(line++));
        assertEquals("    WEB     host2  1237  (secure)", output.get(line));
    }

    @Test
    public void testProcessListWithType() throws Exception {
        final ServiceCommand serviceCommand = new ServiceCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "list", "-t", ServiceType.CONFIG.name());
        final CommandPath commandPath = new CommandPath("service", "list");
        final Registration reg = serviceCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(2, output.size());

        int line = 0;
        assertEquals("Displaying the matching service (of 4 total):", output.get(line++));
        assertEquals("    CONFIG  host1  1234  (insecure)", output.get(line));
    }

    @Test
    public void testProcessListWithHost() throws Exception {
        final ServiceCommand serviceCommand = new ServiceCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "list", "-h", "host1");
        final CommandPath commandPath = new CommandPath("service", "list");
        final Registration reg = serviceCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Displaying 2 matching services (of 4 total):", output.get(line++));
        assertEquals("    CONFIG  host1  1234  (insecure)", output.get(line++));
        assertEquals("    HEALTH  host1  1235  (insecure)", output.get(line));
    }

    @Test
    public void testProcessListNoMatchingServices() throws Exception {
        final ServiceCommand serviceCommand = new ServiceCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "list", "-h", "missing");
        final CommandPath commandPath = new CommandPath("service", "list");
        final Registration reg = serviceCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());
        assertEquals("None of the running services (of which there are 4) match", output.get(0));
    }

    @Test
    public void testProcessListNoServices() throws Exception {
        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(new TreeSet<>());
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        final ServiceCommand serviceCommand = new ServiceCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "list");
        final CommandPath commandPath = new CommandPath("service", "list");
        final Registration reg = serviceCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());
        assertEquals("No services are running", output.get(0));
    }

    @Test
    public void testProcessListOneService() throws Exception {
        final SortedSet<Service> services = new TreeSet<>();
        services.add(new Service(ServiceType.CONFIG, "host1", 1234, false));
        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        final ServiceCommand serviceCommand = new ServiceCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "list");
        final CommandPath commandPath = new CommandPath("service", "list");
        final Registration reg = serviceCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(2, output.size());
        assertEquals("Displaying the single available service:", output.get(0));
        assertEquals("    CONFIG  host1  1234  (insecure)", output.get(1));
    }

    @Test
    public void testProcessListTwoServices() throws Exception {
        final SortedSet<Service> services = new TreeSet<>();
        services.add(new Service(ServiceType.CONFIG, "host1", 1234, false));
        services.add(new Service(ServiceType.HEALTH, "host1", 1235, false));
        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
        Mockito.when(discoveryManager.getAll()).thenReturn(services);
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        final ServiceCommand serviceCommand = new ServiceCommand(shellEnvironment);

        final List<String> input = Arrays.asList("service", "list");
        final CommandPath commandPath = new CommandPath("service", "list");
        final Registration reg = serviceCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());
        assertEquals("Displaying both available services:", output.get(0));
        assertEquals("    CONFIG  host1  1234  (insecure)", output.get(1));
        assertEquals("    HEALTH  host1  1235  (insecure)", output.get(2));
    }

    @Test
    public void testProcessRestart() throws Exception {
        final ServiceCommand serviceCommand = new ServiceCommand(getShellEnvironment());

        final List<String> input = Arrays.asList("service", "restart");
        final CommandPath commandPath = new CommandPath("service", "restart");
        final Registration reg = serviceCommand.getRegistrations().get(0);
        final UserCommand userCommand = new UserCommand(commandPath, reg, input);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = serviceCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);
    }
}
