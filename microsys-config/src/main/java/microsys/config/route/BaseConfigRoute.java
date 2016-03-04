package microsys.config.route;

import com.typesafe.config.Config;

import microsys.config.service.ConfigService;
import microsys.service.BaseRoute;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The base class for config routes, provides easy access to a {@link ConfigService} object.
 */
public abstract class BaseConfigRoute extends BaseRoute {
    protected final static Object NO_CONTENT = "";

    @Nonnull
    private final ConfigService configService;

    /**
     * @param config the system configuration properties
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public BaseConfigRoute(@Nonnull final Config config, @Nonnull final ConfigService configService) {
        super(config);
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
