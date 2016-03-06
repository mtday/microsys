package microsys.shell.command.crypto;

import microsys.shell.completer.EncryptionTypeCompleter;
import microsys.shell.model.Command;
import microsys.shell.model.Option;
import microsys.shell.model.ShellEnvironment;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * This command provides some of the common functionality between the crypto commands.
 */
public abstract class BaseCryptoCommand extends Command {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public BaseCryptoCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the encryption type
     */
    @Nonnull
    protected Option getTypeOption(@Nonnull final String description) {
        return new Option(description, "t", Optional.of("type"), Optional.of("type"), 1, true, false,
                Optional.of(new EncryptionTypeCompleter()));
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the user data to process
     */
    @Nonnull
    protected Option getInputOption(@Nonnull final String description) {
        return new Option(description, "i", Optional.of("input"), Optional.of("input"), 1, true, false,
                Optional.empty());
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the signature to verify
     */
    @Nonnull
    protected Option getSignatureOption(@Nonnull final String description) {
        return new Option(description, "s", Optional.of("signature"), Optional.of("signature"), 1, true, false,
                Optional.empty());
    }
}
