package microsys.config.route;

import com.google.common.net.MediaType;
import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;

import microsys.config.model.ConfigKeyValue;
import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Unset and remove a configuration value based on the user-provided key.
 */
public class Unset extends BaseConfigRoute {
    /**
     * @param config the system configuration properties
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public Unset(@Nonnull final Config config, @Nonnull final ConfigService configService) {
        super(config, configService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Object handle(@Nonnull final Request request, @Nonnull final Response response)
            throws ExecutionException, InterruptedException, TimeoutException {
        final String key = request.params("key");

        if (StringUtils.isEmpty(key)) {
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            return "Invalid configuration key";
        } else {
            final Future<Optional<ConfigKeyValue>> future = getConfigService().unset(key);
            final Optional<ConfigKeyValue> oldValue = future.get(10, TimeUnit.SECONDS);

            if (oldValue.isPresent()) {
                response.status(HttpServletResponse.SC_OK);
                response.type(MediaType.JSON_UTF_8.type());
                return oldValue.get().toJson();
            } else {
                response.status(HttpServletResponse.SC_NO_CONTENT);
                return NO_CONTENT;
            }
        }
    }
}
