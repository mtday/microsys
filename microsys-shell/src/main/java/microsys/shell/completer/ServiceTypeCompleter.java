package microsys.shell.completer;

import jline.console.completer.EnumCompleter;
import microsys.common.model.service.ServiceType;

/**
 * Responsible for performing tab-completions for the {@link ServiceType} enumeration values.
 */
public class ServiceTypeCompleter extends EnumCompleter {
    /**
     * Default constructor.
     */
    public ServiceTypeCompleter() {
        super(ServiceType.class);
    }
}
