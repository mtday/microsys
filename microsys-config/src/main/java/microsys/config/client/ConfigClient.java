package microsys.config.client;

import com.google.gson.JsonParser;
import com.typesafe.config.Config;

import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.service.ConfigService;
import microsys.config.service.ConfigServiceException;
import microsys.crypto.CryptoFactory;
import microsys.discovery.DiscoveryException;
import microsys.discovery.DiscoveryManager;
import microsys.service.client.ServiceException;
import microsys.service.model.ServiceRequest;
import microsys.service.model.ServiceResponse;
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
    private final Config config;
    @Nonnull
    private final ExecutorService executor;
    @Nonnull
    private final DiscoveryManager discoveryManager;
    @Nonnull
    private final OkHttpClient httpClient;
    @Nonnull
    private final CryptoFactory cryptoFactory;

    /**
     * @param config the static system configuration information
     * @param executor used to execute asynchronous processing of the configuration client
     * @param discoveryManager the service discovery manager used to find configuration service end-points
     * @param httpClient the HTTP client used to perform REST communication
     * @param cryptoFactory the {@link CryptoFactory} used to verify response signatures
     */
    public ConfigClient(
            @Nonnull final Config config, @Nonnull final ExecutorService executor,
            @Nonnull final DiscoveryManager discoveryManager, @Nonnull final OkHttpClient httpClient,
            @Nonnull final CryptoFactory cryptoFactory) {
        this.config = Objects.requireNonNull(config);
        this.executor = Objects.requireNonNull(executor);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.cryptoFactory = Objects.requireNonNull(cryptoFactory);
    }

    /**
     * @return the static system configuration information
     */
    @Nonnull
    protected Config getConfig() {
        return this.config;
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
     * @return the {@link CryptoFactory} used to verify response signatures
     */
    @Nonnull
    protected CryptoFactory getCryptoFactory() {
        return this.cryptoFactory;
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
     * @throws ServiceException if there was a problem verifying the response signature
     * @throws ConfigServiceException if there was a problem with the remote security service
     */
    @Nonnull
    protected Optional<ConfigKeyValue> handleResponse(
            @Nonnull final ServiceRequest serviceRequest, @Nonnull final Response response)
            throws IOException, ServiceException, ConfigServiceException {
        Objects.requireNonNull(response);
        switch (response.code()) {
            case HttpServletResponse.SC_OK:
                ServiceResponse.verify(getConfig(), getCryptoFactory(), serviceRequest, response);
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
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl())
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).get().build();
            final Response response = getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    ServiceResponse.verify(getConfig(), getCryptoFactory(), serviceRequest, response);
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
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl() + key)
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).get().build();
            return handleResponse(serviceRequest, getHttpClient().newCall(request).execute());
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
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl())
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).post(body)
                    .build();
            return handleResponse(serviceRequest, getHttpClient().newCall(request).execute());
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
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl() + key)
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).delete().build();
            return handleResponse(serviceRequest, getHttpClient().newCall(request).execute());
        });
    }
}
