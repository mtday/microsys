package microsys.config.route;

import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;

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
 * Update (or create) a configuration value based on the user-provided data.
 */
public class Set extends BaseConfigRoute {
    /**
     * @param config the system configuration properties
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public Set(@Nonnull final Config config, @Nonnull final ConfigService configService) {
        super(config, configService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Object handle(@Nonnull final Request request, @Nonnull final Response response)
            throws ExecutionException, InterruptedException, TimeoutException {
        try {
            final String body = request.body();
            Preconditions.checkArgument(body != null, "Configuration key and value must be provided");

            final ConfigKeyValue kv = new ConfigKeyValue(new JsonParser().parse(body).getAsJsonObject());
            final Future<Optional<ConfigKeyValue>> future = getConfigService().set(kv);
            final Optional<ConfigKeyValue> oldValue = future.get(10, TimeUnit.SECONDS);

            if (oldValue.isPresent()) {
                response.status(HttpServletResponse.SC_OK);
                response.type(MediaType.JSON_UTF_8.type());
                return oldValue.get().toJson();
            } else {
                response.status(HttpServletResponse.SC_NO_CONTENT);
                return NO_CONTENT;
            }
        } catch (final IllegalArgumentException badInput) {
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            return badInput.getMessage();
        }
    }
}
