package microsys.shell.command.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.config.ConfigKeys;
import microsys.crypto.CryptoFactory;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.discovery.impl.CuratorDiscoveryManager;
import microsys.service.client.ServiceClient;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Perform testing of the {@link SignCommand} class.
 */
public class SignCommandTest {
    @SuppressWarnings("unchecked")
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final Optional<URL> keystore = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));

        System.setProperty("SHARED_SECRET", "secret");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("SHARED_SECRET"));
        if (keystore.isPresent()) {
            map.put(ConfigKeys.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(ConfigKeys.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.get().getFile()));
            map.put(ConfigKeys.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        }
        final Config config = ConfigFactory.parseMap(map);
        final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);

        final CuratorDiscoveryManager discoveryManager = Mockito.mock(CuratorDiscoveryManager.class);
        final ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        Mockito.when(shellEnvironment.getServiceClient()).thenReturn(serviceClient);
        Mockito.when(shellEnvironment.getDiscoveryManager()).thenReturn(discoveryManager);
        Mockito.when(shellEnvironment.getCryptoFactory()).thenReturn(cryptoFactory);
        return shellEnvironment;
    }

    @Test
    public void testGetRegistrations() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final SignCommand signCommand = new SignCommand(shellEnvironment);

        final List<Registration> registrations = signCommand.getRegistrations();
        assertEquals(1, registrations.size());

        final Registration sign = registrations.get(0);
        assertEquals(new CommandPath("crypto", "sign"), sign.getPath());
        assertTrue(sign.getDescription().isPresent());
        assertEquals("sign the provided input data", sign.getDescription().get());
        assertTrue(sign.getOptions().isPresent());
        final SortedSet<Option> signOptions = sign.getOptions().get().getOptions();
        assertEquals(1, signOptions.size());
    }

    @Test
    public void testProcess() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final SignCommand signCommand = new SignCommand(shellEnvironment);
        final Registration reg = signCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "sign");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "sign", "-i", "hello"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = signCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        final boolean verified = shellEnvironment.getCryptoFactory().getSymmetricKeyEncryption()
                .verifyString("hello", StandardCharsets.UTF_8, output.iterator().next());

        assertTrue(verified);
    }

    @Test
    public void testProcessException() throws Exception {
        final CryptoFactory cryptoFactory = Mockito.mock(CryptoFactory.class);
        Mockito.when(cryptoFactory.getSymmetricKeyEncryption()).thenThrow(new RuntimeException("Fake"));
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        Mockito.when(shellEnvironment.getCryptoFactory()).thenReturn(cryptoFactory);

        final SignCommand signCommand = new SignCommand(shellEnvironment);
        final Registration reg = signCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "sign");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "sign", "-i", ""));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = signCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("Failed to sign input: Fake", output.iterator().next());
    }
}
