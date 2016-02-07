package microsys.shell.command;

import ch.qos.logback.classic.Level;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Options;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Perform testing of the {@link HelpCommand} class.
 */
public class HelpCommandTest {
    @Test
    public void testGetRegistrations() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final HelpCommand helpCommand = new HelpCommand(shellEnvironment);

        final List<Registration> registrations = helpCommand.getRegistrations();
        assertEquals(1, registrations.size());

        final Registration help = registrations.get(0);
        assertEquals(new CommandPath("help"), help.getPath());
        assertTrue(help.getDescription().isPresent());
        assertEquals("display usage information for available shell commands", help.getDescription().get());
        assertFalse(help.getOptions().isPresent());
    }

    @Test
    public void testProcessMultipleMatches() {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);
        final HelpCommand helpCommand = new HelpCommand(shellEnvironment);

        final CommandPath commandPath = new CommandPath("help", "service");
        final UserCommand userCommand =
                new UserCommand(commandPath, helpCommand.getRegistrations().get(0), commandPath.getPath());

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = helpCommand.process(userCommand, writer);

        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(3, output.size());

        int line = 0;
        assertEquals("Showing help for commands that begin with: service", output.get(line++));
        assertEquals("  service list     provides information about the available services", output.get(line++));
        assertEquals("  service restart  request the restart of a service", output.get(line));
    }

    @Test
    public void testProcessSingleMatches() {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);
        final HelpCommand helpCommand = new HelpCommand(shellEnvironment);

        final CommandPath commandPath = new CommandPath("help", "service", "list");
        final UserCommand userCommand =
                new UserCommand(commandPath, helpCommand.getRegistrations().get(0), commandPath.getPath());

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = helpCommand.process(userCommand, writer);

        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(6, output.size());

        int line = 0;
        assertEquals("Showing help for commands that begin with: service list", output.get(line++));
        assertEquals("  service list  provides information about the available services", output.get(line++));
        assertEquals("    -h  --host <host>  the host to list", output.get(line++));
        assertEquals("    -p  --port <port>  the port to list", output.get(line++));
        assertEquals("    -t  --type <type>  the service type to list", output.get(line++));
        assertEquals("    -v  --version <version>  the version to list", output.get(line));
    }

    @Test
    public void testProcessAll() {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);
        final HelpCommand helpCommand = new HelpCommand(shellEnvironment);

        final CommandPath commandPath = new CommandPath("help");
        final UserCommand userCommand =
                new UserCommand(commandPath, helpCommand.getRegistrations().get(0), commandPath.getPath());

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = helpCommand.process(userCommand, writer);

        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(5, output.size());

        int line = 0;
        assertEquals("  exit             exit the shell", output.get(line++));
        assertEquals("  help             display usage information for available shell commands", output.get(line++));
        assertEquals("  quit             exit the shell", output.get(line++));
        assertEquals("  service list     provides information about the available services", output.get(line++));
        assertEquals("  service restart  request the restart of a service", output.get(line));
    }

    @Test
    public void testGetDescriptionNoRegistrationDescription() {
        final HelpCommand helpCommand = new HelpCommand(Mockito.mock(ShellEnvironment.class));
        final Registration reg = new Registration(new CommandPath("nodesc"), Optional.empty(), Optional.empty());

        assertEquals("  nodesc", helpCommand.getDescription(reg, OptionalInt.of(8)));
    }

    @Test
    public void testGetOptionsNoOptions() {
        final HelpCommand helpCommand = new HelpCommand(Mockito.mock(ShellEnvironment.class));
        final Registration reg = new Registration(new CommandPath("nooptions"), Optional.empty(), Optional.empty());

        assertEquals(0, helpCommand.getOptions(reg).size());
    }

    @Test
    public void testGetOptionsNoLongOptionRequired() {
        final Option option =
                new Option("desc", "s", Optional.empty(), Optional.empty(), 0, true, false, Optional.empty());
        final Options options = new Options(option);

        final HelpCommand helpCommand = new HelpCommand(Mockito.mock(ShellEnvironment.class));
        final Registration reg = new Registration(new CommandPath("opt"), Optional.of(options), Optional.empty());

        final List<String> output = helpCommand.getOptions(reg);
        assertEquals(1, output.size());
        assertEquals("    -s  (required)  desc", output.get(0));
    }

    @Test
    public void testGetOptionsWithOptionalArg() {
        final Option option =
                new Option("desc", "s", Optional.empty(), Optional.of("argname"), 0, false, true, Optional.empty());
        final Options options = new Options(option);

        final HelpCommand helpCommand = new HelpCommand(Mockito.mock(ShellEnvironment.class));
        final Registration reg = new Registration(new CommandPath("opt"), Optional.of(options), Optional.empty());

        final List<String> output = helpCommand.getOptions(reg);
        assertEquals(1, output.size());
        assertEquals("    -s [argname]  desc", output.get(0));
    }

    @Test
    public void testGetOptionsWithMultipleArgs() {
        final Option option =
                new Option("desc", "s", Optional.of("long"), Optional.empty(), 3, false, false, Optional.empty());
        final Options options = new Options(option);

        final HelpCommand helpCommand = new HelpCommand(Mockito.mock(ShellEnvironment.class));
        final Registration reg = new Registration(new CommandPath("opt"), Optional.of(options), Optional.empty());

        final List<String> output = helpCommand.getOptions(reg);
        assertEquals(1, output.size());
        assertEquals("    -s  --long <3 arguments>  desc", output.get(0));
    }
}
