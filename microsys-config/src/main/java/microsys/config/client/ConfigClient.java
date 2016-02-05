package microsys.config.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import microsys.common.model.ServiceType;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.service.ConfigService;
import microsys.config.service.ConfigServiceException;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.model.Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Provides remote access over REST to the configuration service.
 */
public class ConfigClient implements ConfigService {
    private final DiscoveryManager discoveryManager;
    private final OkHttpClient httpClient;

    /**
     * @param discoveryManager the service discovery manager used to find configuration service end-points
     * @param httpClient the HTTP client used to perform REST communication
     */
    public ConfigClient(final DiscoveryManager discoveryManager, final OkHttpClient httpClient) {
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * @return the service discovery manager used to find configuration service end-points
     */
    protected DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    /**
     * @return the service discovery manager used to find configuration service end-points
     */
    protected OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<ConfigKeyValueCollection> getAll() {
        return new FutureTask<>(() -> {
            final Optional<Service> random = getDiscoveryManager().getRandom(ServiceType.CONFIG);
            if (!random.isPresent()) {
                throw new ConfigServiceException("Unable to find a running configuration service");
            }

            final Request request = new Request.Builder().url(random.get().asUrl()).get().build();
            final Response response = getHttpClient().newCall(request).execute();
            return new ConfigKeyValueCollection(new JsonParser().parse(response.body().string()).getAsJsonObject());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<ConfigKeyValue>> get(final String key) {
        Objects.requireNonNull(key);

        return new FutureTask<>(() -> {
            final Optional<Service> random = getDiscoveryManager().getRandom(ServiceType.CONFIG);
            if (!random.isPresent()) {
                throw new ConfigServiceException("Unable to find a running configuration service");
            }

            final Request request = new Request.Builder().url(random.get().asUrl() + key).get().build();
            final Response response = getHttpClient().newCall(request).execute();
            final JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
            if (json.has("key")) {
                return Optional.of(new ConfigKeyValue(json));
            }
            return Optional.empty();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<ConfigKeyValue>> set(final ConfigKeyValue kv) {
        Objects.requireNonNull(kv);

        return new FutureTask<>(() -> {
            final Optional<Service> random = getDiscoveryManager().getRandom(ServiceType.CONFIG);
            if (!random.isPresent()) {
                throw new ConfigServiceException("Unable to find a running configuration service");
            }

            final RequestBody body =
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), kv.toJson().toString());
            final Request request = new Request.Builder().url(random.get().asUrl()).post(body).build();
            final Response response = getHttpClient().newCall(request).execute();
            final JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
            if (json.has("key")) {
                return Optional.of(new ConfigKeyValue(json));
            }
            return Optional.empty();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<ConfigKeyValue>> unset(final String key) {
        Objects.requireNonNull(key);

        return new FutureTask<>(() -> {
            final Optional<Service> random = getDiscoveryManager().getRandom(ServiceType.CONFIG);
            if (!random.isPresent()) {
                throw new ConfigServiceException("Unable to find a running configuration service");
            }

            final Request request = new Request.Builder().url(random.get().asUrl() + key).delete().build();
            final Response response = getHttpClient().newCall(request).execute();
            final JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
            if (json.has("key")) {
                return Optional.of(new ConfigKeyValue(json));
            }
            return Optional.empty();
        });
    }
}
