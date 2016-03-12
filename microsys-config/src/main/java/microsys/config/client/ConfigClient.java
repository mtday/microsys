package microsys.config.client;

import com.google.gson.JsonParser;

import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.service.ConfigService;
import microsys.config.service.ConfigServiceException;
import microsys.discovery.DiscoveryException;
import microsys.service.client.ServiceException;
import microsys.service.model.ServiceEnvironment;
import microsys.service.model.ServiceRequest;
import microsys.service.model.ServiceResponse;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides remote access over REST to the configuration service.
 */
public class ConfigClient implements ConfigService {
    @Nonnull
    private final ServiceEnvironment serviceEnvironment;

    /**
     * @param serviceEnvironment the service environment
     */
    public ConfigClient(@Nonnull final ServiceEnvironment serviceEnvironment) {
        this.serviceEnvironment = Objects.requireNonNull(serviceEnvironment);
    }

    /**
     * @return the service environment
     */
    @Nonnull
    protected ServiceEnvironment getServiceEnvironment() {
        return this.serviceEnvironment;
    }

    /**
     * @return a randomly chosen {@link Service} object from service discovery to use when connecting to the
     * configuration service
     * @throws DiscoveryException if there is a problem retrieving a random {@link Service}
     * @throws ConfigServiceException if there no random configuration {@link Service} objects are available
     */
    @Nonnull
    protected Service getRandom() throws DiscoveryException, ConfigServiceException {
        final Optional<Service> random = getServiceEnvironment().getDiscoveryManager().getRandom(ServiceType.CONFIG);
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
                ServiceResponse.verify(getServiceEnvironment(), serviceRequest, response);
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
        return getServiceEnvironment().getExecutor().submit(() -> {
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl())
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).get().build();
            final Response response = getServiceEnvironment().getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    ServiceResponse.verify(getServiceEnvironment(), serviceRequest, response);
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
        return getServiceEnvironment().getExecutor().submit(() -> {
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl() + key)
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).get().build();
            return handleResponse(serviceRequest, getServiceEnvironment().getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<ConfigKeyValue>> set(@Nonnull final ConfigKeyValue kv) {
        Objects.requireNonNull(kv);
        return getServiceEnvironment().getExecutor().submit(() -> {
            final RequestBody body =
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), kv.toJson().toString());
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl())
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).post(body)
                    .build();
            return handleResponse(serviceRequest, getServiceEnvironment().getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<ConfigKeyValue>> unset(@Nonnull final String key) {
        Objects.requireNonNull(key);
        return getServiceEnvironment().getExecutor().submit(() -> {
            final ServiceRequest serviceRequest = new ServiceRequest();
            final Request request = new Request.Builder().url(getRandom().asUrl() + key)
                    .header(ServiceRequest.SERVICE_REQUEST_HEADER, serviceRequest.toJson().toString()).delete().build();
            return handleResponse(serviceRequest, getServiceEnvironment().getHttpClient().newCall(request).execute());
        });
    }
}
