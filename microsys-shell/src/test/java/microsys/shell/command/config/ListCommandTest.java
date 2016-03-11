package microsys.shell.command.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.config.ConfigKeys;
import microsys.config.client.ConfigClient;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Perform testing of the {@link ListCommand} class.
 */
public class ListCommandTest {
    @SuppressWarnings("unchecked")
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.SERVER_HOSTNAME.getKey(), ConfigValueFactory.fromAnyRef("")); // empty value
        map.put(ConfigKeys.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef(true));
        map.put(ConfigKeys.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef("file"));
        map.put(ConfigKeys.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
        map.put(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        final Config config = ConfigFactory.parseMap(map);

        final ConfigKeyValueCollection collection =
                new ConfigKeyValueCollection(new ConfigKeyValue("remote-key", "remote-value"));

        final ConfigClient configClient = Mockito.mock(ConfigClient.class);
        Mockito.when(configClient.getAll()).thenReturn(CompletableFuture.completedFuture(collection));

        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getConfig()).thenReturn(config);
        Mockito.when(shellEnvironment.getConfigClient()).thenReturn(configClient);
        return shellEnvironment;
    }

    @Test
    public void testGetRegistrations() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final ListCommand listCommand = new ListCommand(shellEnvironment);

        final List<Registration> registrations = listCommand.getRegistrations();
        assertEquals(1, registrations.size());

        final Registration list = registrations.get(0);
        assertEquals(new CommandPath("config", "list"), list.getPath());
        assertTrue(list.getDescription().isPresent());
        assertEquals("display system configuration information", list.getDescription().get());
        assertTrue(list.getOptions().isPresent());
        final SortedSet<Option> listOptions = list.getOptions().get().getOptions();
        assertEquals(2, listOptions.size());
    }

    @Test
    public void testProcessNoParams() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final ListCommand listCommand = new ListCommand(shellEnvironment);
        final Registration reg = listCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("config", "list");
        final UserCommand userCommand = new UserCommand(commandPath, reg, Arrays.asList("config", "list"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = listCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> lines = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(5, lines.size());

        int line = 0;
        assertEquals("  remote-key => remote-value", lines.get(line++));
        assertEquals("  ssl.enabled => true", lines.get(line++));
        assertEquals("  ssl.keystore.file => file", lines.get(line++));
        assertEquals("  ssl.keystore.password => changeit", lines.get(line++));
        assertEquals("  ssl.keystore.type => JKS", lines.get(line));
    }

    @Test
    public void testProcessUnrecognizedParam() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final ListCommand listCommand = new ListCommand(shellEnvironment);
        final Registration reg = listCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("config", "list");
        final UserCommand userCommand = new UserCommand(commandPath, reg, Arrays.asList("config", "list", "-x"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = listCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> lines = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(5, lines.size());

        int line = 0;
        assertEquals("  remote-key => remote-value", lines.get(line++));
        assertEquals("  ssl.enabled => true", lines.get(line++));
        assertEquals("  ssl.keystore.file => file", lines.get(line++));
        assertEquals("  ssl.keystore.password => changeit", lines.get(line++));
        assertEquals("  ssl.keystore.type => JKS", lines.get(line));
    }

    @Test
    public void testProcessWithFilterOnKey() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final ListCommand listCommand = new ListCommand(shellEnvironment);
        final Registration reg = listCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("config", "list");
        final UserCommand userCommand = new UserCommand(commandPath, reg, Arrays.asList("config", "list", "-f", "key"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = listCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> lines = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(4, lines.size());

        int line = 0;
        assertEquals("  remote-key => remote-value", lines.get(line++));
        assertEquals("  ssl.keystore.file => file", lines.get(line++));
        assertEquals("  ssl.keystore.password => changeit", lines.get(line++));
        assertEquals("  ssl.keystore.type => JKS", lines.get(line));
    }

    @Test
    public void testProcessWithFilterOnValue() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final ListCommand listCommand = new ListCommand(shellEnvironment);
        final Registration reg = listCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("config", "list");
        final UserCommand userCommand = new UserCommand(commandPath, reg, Arrays.asList("config", "list", "-f", "val"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = listCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> lines = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, lines.size());

        assertEquals("  remote-key => remote-value", lines.iterator().next());
    }

    @Test
    public void testProcessStaticOnly() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final ListCommand listCommand = new ListCommand(shellEnvironment);
        final Registration reg = listCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("config", "list");
        final UserCommand userCommand =
                new UserCommand(commandPath, reg, Arrays.asList("config", "list", "-t", "static"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = listCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> lines = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(4, lines.size());

        int line = 0;
        assertEquals("  ssl.enabled => true", lines.get(line++));
        assertEquals("  ssl.keystore.file => file", lines.get(line++));
        assertEquals("  ssl.keystore.password => changeit", lines.get(line++));
        assertEquals("  ssl.keystore.type => JKS", lines.get(line));
    }

    @Test
    public void testProcessDynamicOnly() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final ListCommand listCommand = new ListCommand(shellEnvironment);
        final Registration reg = listCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("config", "list");
        final UserCommand userCommand =
                new UserCommand(commandPath, reg, Arrays.asList("config", "list", "-t", "dynamic"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = listCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> lines = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, lines.size());

        assertEquals("  remote-key => remote-value", lines.iterator().next());
    }

    @Test
    public void testProcessDynamicException() throws Exception {
        final Supplier<ConfigKeyValueCollection> supplier = () -> {
            throw new RuntimeException("Failed");
        };

        final ConfigClient configClient = Mockito.mock(ConfigClient.class);
        Mockito.when(configClient.getAll()).thenReturn(CompletableFuture.supplyAsync(supplier));

        final ShellEnvironment shellEnvironment = getShellEnvironment();
        Mockito.when(shellEnvironment.getConfigClient()).thenReturn(configClient);

        final ListCommand listCommand = new ListCommand(shellEnvironment);
        final Registration reg = listCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("config", "list");
        final UserCommand userCommand =
                new UserCommand(commandPath, reg, Arrays.asList("config", "list", "-t", "dynamic"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = listCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> lines = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, lines.size());

        assertEquals(
                "Failed to retrieve the dynamic system configuration: java.lang.RuntimeException: Failed",
                lines.iterator().next());
    }
}
