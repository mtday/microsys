package microsys.security.route;

import com.typesafe.config.Config;

import microsys.security.service.UserService;
import microsys.service.BaseRoute;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The base class for user routes, provides easy access to a {@link UserService} object.
 */
public abstract class BaseUserRoute extends BaseRoute {
    protected final static Object NO_CONTENT = "";

    @Nonnull
    private final UserService userService;

    /**
     * @param config the system configuration properties
     * @param userService the {@link UserService} used to manage user accounts within the system
     */
    public BaseUserRoute(@Nonnull final Config config, @Nonnull final UserService userService) {
        super(config);
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
