package microsys.shell;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;
import microsys.shell.model.CommandPath;
import microsys.shell.model.Registration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for managing all of the command registrations.
 */
public class RegistrationManager {
    private final Map<CommandPath, Registration> registrations = new TreeMap<>();

    /**
     *
     */
    public RegistrationManager() {
        // TODO: Find all of the commands
    }

    /**
     * @return the available registrations
     */
    public Map<CommandPath, Registration> getRegistrations() {
        return Collections.unmodifiableMap(this.registrations);
    }


}
