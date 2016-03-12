package microsys.security.route;

import microsys.security.service.UserService;
import microsys.service.BaseRoute;
import microsys.service.model.ServiceEnvironment;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The base class for user routes, provides easy access to a {@link UserService} object.
 */
public abstract class BaseUserRoute extends BaseRoute {
    @Nonnull
    private final UserService userService;

    /**
     * @param serviceEnvironment the service environment
     * @param userService the {@link UserService} used to manage user accounts within the system
     */
    public BaseUserRoute(@Nonnull final ServiceEnvironment serviceEnvironment, @Nonnull final UserService userService) {
        super(serviceEnvironment);
        this.userService = Objects.requireNonNull(userService);
    }

    /**
     * @return the {@link UserService} responsible for processing user requests
     */
    @Nonnull
    protected UserService getUserService() {
        return this.userService;
    }
}
