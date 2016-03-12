package microsys.config.route;

import microsys.config.service.ConfigService;
import microsys.service.BaseRoute;
import microsys.service.model.ServiceEnvironment;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The base class for config routes, provides easy access to a {@link ConfigService} object.
 */
public abstract class BaseConfigRoute extends BaseRoute {
    @Nonnull
    private final ConfigService configService;

    /**
     * @param serviceEnvironment the service environment
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public BaseConfigRoute(
            @Nonnull final ServiceEnvironment serviceEnvironment, @Nonnull final ConfigService configService) {
        super(serviceEnvironment);
        this.configService = Objects.requireNonNull(configService);
    }

    /**
     * @return the {@link ConfigService} responsible for processing configuration requests
     */
    @Nonnull
    protected ConfigService getConfigService() {
        return this.configService;
    }
}
