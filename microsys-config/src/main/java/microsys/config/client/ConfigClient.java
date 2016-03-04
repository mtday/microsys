package microsys.config.client;

import com.google.gson.JsonParser;

import microsys.common.model.ServiceType;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.service.ConfigService;
import microsys.config.service.ConfigServiceException;
import microsys.service.discovery.DiscoveryException;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.model.Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides remote access over REST to the configuration service.
 */
public class ConfigClient implements ConfigService {
    @Nonnull
    private final ExecutorService executor;
    @Nonnull
    private final DiscoveryManager discoveryManager;
    @Nonnull
    private final OkHttpClient httpClient;

    /**
     * @param executor used to execute asynchronous processing of the configuration client
     * @param discoveryManager the service discovery manager used to find configuration service end-points
     * @param httpClient the HTTP client used to perform REST communication
     */
    public ConfigClient(
            @Nonnull final ExecutorService executor, @Nonnull final DiscoveryManager discoveryManager,
            @Nonnull final OkHttpClient httpClient) {
        this.executor = Objects.requireNonNull(executor);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * @return the {@link ExecutorService} used to execute asynchronous processing of the configuration client
     */
    @Nonnull
    protected ExecutorService getExecutor() {
        return this.executor;
    }

    /**
     * @return the service discovery manager used to find configuration service end-points
     */
    @Nonnull
    protected DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    /**
     * @return the service discovery manager used to find configuration service end-points
     */
    @Nonnull
    protected OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * @return a randomly chosen {@link Service} object from service discovery to use when connecting to the
     * configuration service
     * @throws DiscoveryException if there is a problem retrieving a random {@link Service}
     * @throws ConfigServiceException if there no random configuration {@link Service} objects are available
     */
    @Nonnull
    protected Service getRandom() throws DiscoveryException, ConfigServiceException {
        final Optional<Service> random = getDiscoveryManager().getRandom(ServiceType.CONFIG);
        if (!random.isPresent()) {
            throw new ConfigServiceException("Unable to find a running configuration service");
        }
        return random.get();
    }

    /**
     * @param response the {@link Response} to be processed
     * @return the {@link ConfigKeyValue} object parsed from the response data, if available
     * @throws IOException if there is a problem processing the response data
     * @throws ConfigServiceException if there was a problem with the remote security service
     */
    @Nonnull
    protected Optional<ConfigKeyValue> handleResponse(@Nonnull final Response response)
            throws IOException, ConfigServiceException {
        Objects.requireNonNull(response);
        switch (response.code()) {
            case HttpServletResponse.SC_OK:
                return Optional
                        .of(new ConfigKeyValue(new JsonParser().parse(response.body().string()).getAsJsonObject()));
            case HttpServletResponse.SC_NO_CONTENT:
            case HttpServletResponse.SC_NOT_FOUND:
                return Optional.empty();
            default:
                throw new ConfigServiceException(response.body().string());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<ConfigKeyValueCollection> getAll() {
        return getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(getRandom().asUrl()).get().build();
            final Response response = getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    return new ConfigKeyValueCollection(
                            new JsonParser().parse(response.body().string()).getAsJsonObject());
                default:
                    throw new ConfigServiceException(response.body().string());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<ConfigKeyValue>> get(@Nonnull final String key) {
        Objects.requireNonNull(key);
        return getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(getRandom().asUrl() + key).get().build();
            return handleResponse(getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<ConfigKeyValue>> set(@Nonnull final ConfigKeyValue kv) {
        Objects.requireNonNull(kv);
        return getExecutor().submit(() -> {
            final RequestBody body =
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), kv.toJson().toString());
            final Request request = new Request.Builder().url(getRandom().asUrl()).post(body).build();
            return handleResponse(getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<ConfigKeyValue>> unset(@Nonnull final String key) {
        Objects.requireNonNull(key);
        return getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(getRandom().asUrl() + key).delete().build();
            return handleResponse(getHttpClient().newCall(request).execute());
        });
    }
}
