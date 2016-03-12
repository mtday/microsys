package microsys.service;

import microsys.service.model.ServiceEnvironment;
import spark.Route;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The base class for routes in this system.
 */
public abstract class BaseRoute implements Route {
    @Nonnull
    private final static String NO_CONTENT = "";

    @Nonnull
    private final ServiceEnvironment serviceEnvironment;

    /**
     * @param serviceEnvironment the service environment
     */
    public BaseRoute(@Nonnull final ServiceEnvironment serviceEnvironment) {
        this.serviceEnvironment = Objects.requireNonNull(serviceEnvironment);
    }

    /**
     * @return the service environment
     */
    @Nonnull
    public ServiceEnvironment getServiceEnvironment() {
        return this.serviceEnvironment;
    }

    /**
     * @return the response to send back to the client when no content is being returned
     */
    @Nonnull
    public String getNoContent() {
        return NO_CONTENT;
    }
}
