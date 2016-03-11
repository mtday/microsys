package microsys.curator;

import javax.annotation.Nonnull;

/**
 * An exception thrown by the curator utilities when there are problems with curator or zookeeper.
 */
public class CuratorException extends Exception {
    private final static long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public CuratorException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public CuratorException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public CuratorException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
