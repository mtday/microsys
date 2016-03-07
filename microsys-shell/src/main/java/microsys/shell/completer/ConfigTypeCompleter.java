package microsys.shell.completer;

import jline.console.completer.EnumCompleter;
import microsys.common.model.ConfigType;

/**
 * Responsible for performing tab-completions for the {@link ConfigType} enumeration values.
 */
public class ConfigTypeCompleter extends EnumCompleter {
    /**
     * Default constructor.
     */
    public ConfigTypeCompleter() {
        super(ConfigType.class);
    }
}
