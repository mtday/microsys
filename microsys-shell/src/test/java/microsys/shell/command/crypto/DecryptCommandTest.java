package microsys.shell.command.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.config.CommonConfig;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionType;
import microsys.service.client.ServiceClient;
import microsys.service.discovery.DiscoveryManager;
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
 * Perform testing of the {@link DecryptCommand} class.
 */
public class DecryptCommandTest {
    @SuppressWarnings("unchecked")
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final Optional<URL> keystore = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("SHARED_SECRET", ConfigValueFactory.fromAnyRef("secret"));
        map.put(CommonConfig.SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("SHARED_SECRET"));
        if (keystore.isPresent()) {
            map.put(CommonConfig.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(CommonConfig.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.get().getFile()));
            map.put(CommonConfig.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        }
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.systemProperties())
                .withFallback(ConfigFactory.systemEnvironment());
        final CryptoFactory cryptoFactory = new CryptoFactory(config);

        final DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
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
        final DecryptCommand decryptCommand = new DecryptCommand(shellEnvironment);

        final List<Registration> registrations = decryptCommand.getRegistrations();
        assertEquals(1, registrations.size());

        final Registration decrypt = registrations.get(0);
        assertEquals(new CommandPath("crypto", "decrypt"), decrypt.getPath());
        assertTrue(decrypt.getDescription().isPresent());
        assertEquals("decrypt the provided input data", decrypt.getDescription().get());
        assertTrue(decrypt.getOptions().isPresent());
        final SortedSet<Option> decryptOptions = decrypt.getOptions().get().getOptions();
        assertEquals(2, decryptOptions.size());
    }

    @Test
    public void testProcessPasswordBasedEncryption() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final String encrypted = shellEnvironment.getCryptoFactory().getPasswordBasedEncryption()
                .encryptString("hello", StandardCharsets.UTF_8);
        final DecryptCommand decryptCommand = new DecryptCommand(shellEnvironment);
        final Registration reg = decryptCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "decrypt");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "decrypt", "-t", EncryptionType.PASSWORD_BASED.name(), "-i", encrypted));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = decryptCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("hello", output.iterator().next());
    }

    @Test
    public void testProcessSymmetricKeyEncryption() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final String encrypted = shellEnvironment.getCryptoFactory().getSymmetricKeyEncryption()
                .encryptString("hello", StandardCharsets.UTF_8);
        final DecryptCommand decryptCommand = new DecryptCommand(shellEnvironment);
        final Registration reg = decryptCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "decrypt");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "decrypt", "-t", EncryptionType.SYMMETRIC_KEY.name(), "-i", encrypted));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = decryptCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("hello", output.iterator().next());
    }

    @Test
    public void testProcessSymmetricKeyEncryptionException() throws Exception {
        final DecryptCommand decryptCommand = new DecryptCommand(getShellEnvironment());
        final Registration reg = decryptCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "decrypt");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "decrypt", "-t", EncryptionType.SYMMETRIC_KEY.name(), "-i", "  "));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = decryptCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("Failed to decrypt input: Failed to decrypt data", output.iterator().next());
    }
}
