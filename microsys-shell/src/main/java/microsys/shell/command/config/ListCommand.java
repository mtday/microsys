package microsys.shell.command.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import microsys.common.model.ConfigType;
import microsys.config.client.ConfigClient;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Options;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * This command implements the {@code config list} command in the shell.
 */
public class ListCommand extends BaseConfigCommand {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public ListCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Registration> getRegistrations() {
        final Option type = getTypeOption("the type of configuration to list");
        final Option filter = getFilterOption("provides text to match in the configuration to filter the output");
        final Optional<Options> options = Optional.of(new Options(type, filter));

        final Optional<String> description = Optional.of("display system configuration information");
        final CommandPath list = new CommandPath("config", "list");
        return Collections.singletonList(new Registration(list, options, description));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CommandStatus process(@Nonnull final UserCommand userCommand, @Nonnull final PrintWriter writer) {
        final Filter filter = new Filter(userCommand);

        final Collection<ConfigKeyValue> combined = new LinkedList<>();
        try {
            combined.addAll(fetchDynamic(filter, getShellEnvironment().getConfigClient()));
        } catch (final InterruptedException | ExecutionException failed) {
            writer.println("Failed to retrieve the dynamic system configuration: " + failed.getMessage());
        }
        combined.addAll(fetchStatic(filter, getShellEnvironment().getConfig()));
        write(new ConfigKeyValueCollection(combined), writer);

        return CommandStatus.SUCCESS;
    }

    /**
     * @param collection the collection of configuration keys and values to display to the user
     * @param writer the writer used to write the configuration information to the shell output
     */
    protected void write(@Nonnull final ConfigKeyValueCollection collection, @Nonnull final PrintWriter writer) {
        collection.asSet().forEach(kv -> writer.println(String.format("  %s => %s", kv.getKey(), kv.getValue())));
    }

    /**
     * @param filter the filter used to determine which configuration keys and values match
     * @param configClient the client used to talk to the remote configuration services
     * @return the matching configuration keys and values
     */
    @Nonnull
    protected Collection<ConfigKeyValue> fetchDynamic(
            @Nonnull final Filter filter, @Nonnull final ConfigClient configClient)
            throws ExecutionException, InterruptedException {
        if (!filter.fetchDynamic()) {
            return Collections.emptyList();
        }
        return Objects.requireNonNull(configClient).getAll().get().asSet().stream().filter(filter::matches)
                .collect(Collectors.toList());
    }

    /**
     * @param filter the filter used to determine which configuration keys and values match
     * @param config the static system configuration information
     * @return the matching configuration keys and values
     */
    @Nonnull
    protected Collection<ConfigKeyValue> fetchStatic(@Nonnull final Filter filter, @Nonnull final Config config) {
        if (!filter.fetchStatic()) {
            return Collections.emptyList();
        }
        return config.entrySet().stream().map(this::convert).filter(Optional::isPresent).map(Optional::get)
                .filter(filter::matches).collect(Collectors.toList());
    }

    /**
     * @param entry the static system configuration entry to be converted into a {@link ConfigKeyValue}
     * @return the converted {@link ConfigKeyValue}, possibly empty if the entry had an empty key or value
     */
    @Nonnull
    protected Optional<ConfigKeyValue> convert(@Nonnull final Map.Entry<String, ConfigValue> entry) {
        final String value = String.valueOf(Objects.requireNonNull(entry).getValue().unwrapped());
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(new ConfigKeyValue(entry.getKey(), value));
    }

    /**
     * Perform filtering on configuration keys and values based on the options provided in the shell.
     */
    protected static class Filter {
        @Nonnull
        private final Optional<ConfigType> configType;
        @Nonnull
        private final Optional<String> filter;

        /**
         * @param userCommand the user command from which this filter will be populated
         */
        public Filter(@Nonnull final UserCommand userCommand) {
            final Optional<CommandLine> commandLine = userCommand.getCommandLine();
            if (commandLine.isPresent()) {
                this.configType = Optional.ofNullable(commandLine.get().hasOption("t") ?
                        ConfigType.valueOf(commandLine.get().getOptionValue("t").toUpperCase()) : null);
                this.filter = Optional.ofNullable(
                        commandLine.get().hasOption("f") ? commandLine.get().getOptionValue("f") : null);
            } else {
                this.configType = Optional.empty();
                this.filter = Optional.empty();
            }
        }

        /**
         * @return the user-specified configuration type, if specified
         */
        @Nonnull
        public Optional<ConfigType> getConfigType() {
            return this.configType;
        }

        /**
         * @return the user-specified filter text, if specified
         */
        @Nonnull
        public Optional<String> getFilter() {
            return this.filter;
        }

        /**
         * @return whether dynamic configuration information should be included in the output
         */
        public boolean fetchDynamic() {
            return !getConfigType().isPresent() || getConfigType().get().equals(ConfigType.DYNAMIC);
        }

        /**
         * @return whether static configuration information should be included in the output
         */
        public boolean fetchStatic() {
            return !getConfigType().isPresent() || getConfigType().get().equals(ConfigType.STATIC);
        }

        /**
         * @param kv the {@link ConfigKeyValue} object to check to verify that it matches the filter
         * @return whether the configuration key or value matches the filter
         */
        public boolean matches(@Nonnull final ConfigKeyValue kv) {
            Objects.requireNonNull(kv);
            return !getFilter().isPresent() ||
                    StringUtils.containsIgnoreCase(kv.getKey(), getFilter().get()) ||
                    StringUtils.containsIgnoreCase(kv.getValue(), getFilter().get());
        }
    }
}
