package microsys.shell.command.crypto;

import org.apache.commons.cli.CommandLine;

import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.crypto.EncryptionType;
import microsys.crypto.PasswordBasedEncryption;
import microsys.crypto.SymmetricKeyEncryption;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Options;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * This command implements the {@code crypto decrypt} command in the shell.
 */
public class DecryptCommand extends BaseCryptoCommand {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public DecryptCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Registration> getRegistrations() {
        final Option type = getTypeOption("the encryption type to use when decrypting the data");
        final Option input = getInputOption("the user data to decrypt");
        final Optional<Options> decryptOptions = Optional.of(new Options(type, input));

        final Optional<String> description = Optional.of("decrypt the provided input data");
        final CommandPath commandPath = new CommandPath("crypto", "decrypt");
        return Collections.singletonList(new Registration(commandPath, decryptOptions, description));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CommandStatus process(@Nonnull final UserCommand userCommand, @Nonnull final PrintWriter writer) {
        final CryptoFactory cryptoFactory = getShellEnvironment().getCryptoFactory();

        final CommandLine commandLine = userCommand.getCommandLine().get();
        final EncryptionType encryptionType = EncryptionType.valueOf(commandLine.getOptionValue("t").toUpperCase());
        final String input = commandLine.getOptionValue("i");

        try {
            if (encryptionType == EncryptionType.PASSWORD_BASED) {
                final PasswordBasedEncryption pbe = cryptoFactory.getPasswordBasedEncryption();
                writer.println(pbe.decryptString(input, StandardCharsets.UTF_8));
            } else {
                final SymmetricKeyEncryption ske = cryptoFactory.getSymmetricKeyEncryption();
                writer.println(ske.decryptString(input, StandardCharsets.UTF_8));
            }
        } catch (final EncryptionException exception) {
            writer.println("Failed to decrypt input: " + exception.getMessage());
        }

        return CommandStatus.SUCCESS;
    }
}
