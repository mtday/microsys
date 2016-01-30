package microsys.shell.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Launch the shell.
 */
public class Runner {
    /**
     * Create the shell.
     */
    protected Runner() {
        final Config config = ConfigFactory.load();
    }

    /**
     * @param args the command-line arguments
     */
    public static void main(final String... args) {
        new Runner();
    }
}
