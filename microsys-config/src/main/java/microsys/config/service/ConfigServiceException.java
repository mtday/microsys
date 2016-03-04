package microsys.config.service;

import javax.annotation.Nonnull;

/**
 * An exception thrown by the {@link ConfigService} when there are problems retrieving or storing dynamic system
 * configuration data.
 */
public class ConfigServiceException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public ConfigServiceException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public ConfigServiceException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public ConfigServiceException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
