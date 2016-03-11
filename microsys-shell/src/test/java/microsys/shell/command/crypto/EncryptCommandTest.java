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
import microsys.crypto.EncryptionType;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.discovery.DiscoveryManager;
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
 * Perform testing of the {@link EncryptCommand} class.
 */
public class EncryptCommandTest {
    @SuppressWarnings("unchecked")
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final Optional<URL> keystore = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("SHARED_SECRET", ConfigValueFactory.fromAnyRef("secret"));
        map.put(ConfigKeys.SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("SHARED_SECRET"));
        if (keystore.isPresent()) {
            map.put(ConfigKeys.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(ConfigKeys.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.get().getFile()));
            map.put(ConfigKeys.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        }
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.systemProperties())
                .withFallback(ConfigFactory.systemEnvironment());
        final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);

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
        final EncryptCommand encryptCommand = new EncryptCommand(shellEnvironment);

        final List<Registration> registrations = encryptCommand.getRegistrations();
        assertEquals(1, registrations.size());

        final Registration encrypt = registrations.get(0);
        assertEquals(new CommandPath("crypto", "encrypt"), encrypt.getPath());
        assertTrue(encrypt.getDescription().isPresent());
        assertEquals("encrypt the provided input data", encrypt.getDescription().get());
        assertTrue(encrypt.getOptions().isPresent());
        final SortedSet<Option> encryptOptions = encrypt.getOptions().get().getOptions();
        assertEquals(2, encryptOptions.size());
    }

    @Test
    public void testProcessPasswordBasedEncryption() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final EncryptCommand encryptCommand = new EncryptCommand(shellEnvironment);
        final Registration reg = encryptCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "encrypt");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "encrypt", "-t", EncryptionType.PASSWORD_BASED.name(), "-i", "hello"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = encryptCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        final String decrypted = shellEnvironment.getCryptoFactory().getPasswordBasedEncryption()
                .decryptString(output.iterator().next(), StandardCharsets.UTF_8);

        assertEquals("hello", decrypted);
    }

    @Test
    public void testProcessSymmetricKeyEncryption() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final EncryptCommand encryptCommand = new EncryptCommand(shellEnvironment);
        final Registration reg = encryptCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "encrypt");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "encrypt", "-t", EncryptionType.SYMMETRIC_KEY.name(), "-i", "hello"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = encryptCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        final String decrypted = shellEnvironment.getCryptoFactory().getSymmetricKeyEncryption()
                .decryptString(output.iterator().next(), StandardCharsets.UTF_8);

        assertEquals("hello", decrypted);
    }

    @Test
    public void testProcessPasswordBasedEncryptionException() throws Exception {
        final CryptoFactory cryptoFactory = Mockito.mock(CryptoFactory.class);
        Mockito.when(cryptoFactory.getPasswordBasedEncryption()).thenThrow(new RuntimeException("Fake"));
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        Mockito.when(shellEnvironment.getCryptoFactory()).thenReturn(cryptoFactory);

        final EncryptCommand encryptCommand = new EncryptCommand(shellEnvironment);
        final Registration reg = encryptCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "encrypt");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "encrypt", "-t", EncryptionType.PASSWORD_BASED.name(), "-i", ""));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = encryptCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("Failed to encrypt input: Fake", output.iterator().next());
    }
}
