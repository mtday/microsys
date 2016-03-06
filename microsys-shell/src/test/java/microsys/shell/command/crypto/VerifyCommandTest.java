package microsys.shell.command.crypto;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import microsys.common.config.CommonConfig;
import microsys.crypto.CryptoFactory;
import microsys.service.client.ServiceClient;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.model.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Perform testing of the {@link VerifyCommand} class.
 */
public class VerifyCommandTest {
    @SuppressWarnings("unchecked")
    protected ShellEnvironment getShellEnvironment() throws Exception {
        final Optional<URL> keystore = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));

        System.setProperty("SHARED_SECRET", "secret");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("SHARED_SECRET"));
        if (keystore.isPresent()) {
            map.put(CommonConfig.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(CommonConfig.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.get().getFile()));
            map.put(CommonConfig.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        }
        final Config config = ConfigFactory.parseMap(map);
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
        final VerifyCommand verifyCommand = new VerifyCommand(shellEnvironment);

        final List<Registration> registrations = verifyCommand.getRegistrations();
        assertEquals(1, registrations.size());

        final Registration verify = registrations.get(0);
        assertEquals(new CommandPath("crypto", "verify"), verify.getPath());
        assertTrue(verify.getDescription().isPresent());
        assertEquals("verify the provided input data", verify.getDescription().get());
        assertTrue(verify.getOptions().isPresent());
        final SortedSet<Option> verifyOptions = verify.getOptions().get().getOptions();
        assertEquals(2, verifyOptions.size());
    }

    @Test
    public void testProcessVerificationSuccess() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final String signature = shellEnvironment.getCryptoFactory().getSymmetricKeyEncryption()
                .signString("hello", StandardCharsets.UTF_8);
        final VerifyCommand verifyCommand = new VerifyCommand(shellEnvironment);
        final Registration reg = verifyCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "verify");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "verify", "-i", "hello", "-s", signature));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = verifyCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("Verified Successfully: Yes", output.iterator().next());
    }

    @Test
    public void testProcessVerificationFailed() throws Exception {
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        final String signature = shellEnvironment.getCryptoFactory().getSymmetricKeyEncryption()
                .signString("hello", StandardCharsets.UTF_8);
        final VerifyCommand verifyCommand = new VerifyCommand(shellEnvironment);
        final Registration reg = verifyCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "verify");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "verify", "-i", "wrong", "-s", signature));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = verifyCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("Verified Successfully: No", output.iterator().next());
    }

    @Test
    public void testProcessException() throws Exception {
        final CryptoFactory cryptoFactory = Mockito.mock(CryptoFactory.class);
        Mockito.when(cryptoFactory.getSymmetricKeyEncryption()).thenThrow(new RuntimeException("Fake"));
        final ShellEnvironment shellEnvironment = getShellEnvironment();
        Mockito.when(shellEnvironment.getCryptoFactory()).thenReturn(cryptoFactory);

        final VerifyCommand verifyCommand = new VerifyCommand(shellEnvironment);
        final Registration reg = verifyCommand.getRegistrations().iterator().next();
        final CommandPath commandPath = new CommandPath("crypto", "verify");
        final UserCommand userCommand = new UserCommand(commandPath, reg,
                Arrays.asList("crypto", "verify", "-i", "input", "-s", "signature"));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = verifyCommand.process(userCommand, writer);
        assertEquals(CommandStatus.SUCCESS, status);

        final List<String> output = Arrays.asList(stringWriter.getBuffer().toString().split(System.lineSeparator()));
        assertEquals(1, output.size());

        assertEquals("Failed to verify input: Fake", output.iterator().next());
    }
}
