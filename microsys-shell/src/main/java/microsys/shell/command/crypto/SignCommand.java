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
 * This command implements the {@code crypto sign} command in the shell.
 */
public class SignCommand extends BaseCryptoCommand {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public SignCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Registration> getRegistrations() {
        final Option input = getInputOption("the user data to sign");
        final Optional<Options> signOptions = Optional.of(new Options(input));

        final Optional<String> description = Optional.of("sign the provided input data");
        final CommandPath commandPath = new CommandPath("crypto", "sign");
        return Collections.singletonList(new Registration(commandPath, signOptions, description));
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

        try {
            final SymmetricKeyEncryption ske = cryptoFactory.getSymmetricKeyEncryption();
            writer.println(ske.signString(input, StandardCharsets.UTF_8));
        } catch (final Exception exception) {
            writer.println("Failed to sign input: " + exception.getMessage());
        }

        return CommandStatus.SUCCESS;
    }
}
