package microsys.shell.command.crypto;

import microsys.crypto.CryptoFactory;
import microsys.crypto.SymmetricKeyEncryption;
import microsys.shell.model.*;
import org.apache.commons.cli.CommandLine;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This command implements the {@code crypto verify} command in the shell.
 */
public class VerifyCommand extends BaseCryptoCommand {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public VerifyCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Registration> getRegistrations() {
        final Option input = getInputOption("the user data to verify");
        final Option signature = getSignatureOption("the known signature to verify");
        final Optional<Options> verifyOptions = Optional.of(new Options(input, signature));

        final Optional<String> description = Optional.of("verify the provided input data");
        final CommandPath commandPath = new CommandPath("crypto", "verify");
        return Collections.singletonList(new Registration(commandPath, verifyOptions, description));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CommandStatus process(@Nonnull final UserCommand userCommand, @Nonnull final PrintWriter writer) {
        final CryptoFactory cryptoFactory = getShellEnvironment().getCryptoFactory();

        final CommandLine commandLine = userCommand.getCommandLine().get();
        final String input = commandLine.getOptionValue("i");
        final String signature = commandLine.getOptionValue("s");

        try {
            final SymmetricKeyEncryption ske = cryptoFactory.getSymmetricKeyEncryption();
            final boolean verified = ske.verifyString(input, StandardCharsets.UTF_8, signature);
            writer.println("Verified Successfully: " + (verified ? "Yes" : "No"));
        } catch (final Exception exception) {
            writer.println("Failed to verify input: " + exception.getMessage());
        }

        return CommandStatus.SUCCESS;
    }
}
