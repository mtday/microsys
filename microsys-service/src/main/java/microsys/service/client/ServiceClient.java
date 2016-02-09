package microsys.service.client;

import com.google.common.base.Converter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import microsys.service.model.Service;
import microsys.service.model.ServiceControlStatus;
import microsys.service.model.ServiceControlStatus.ServiceControlStatusConverter;
import microsys.service.model.ServiceInfo;
import microsys.service.model.ServiceInfo.ServiceInfoConverter;
import microsys.service.model.ServiceMemory;
import microsys.service.model.ServiceMemory.ServiceMemoryConverter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletResponse;

/**
 * Provides remote access over REST to the base service routes.
 */
public class ServiceClient {
    private final ExecutorService executor;
    private final OkHttpClient httpClient;

    /**
     * @param executor   used to execute asynchronous processing of the client
     * @param httpClient the HTTP client used to perform REST communication
     */
    public ServiceClient(final ExecutorService executor, final OkHttpClient httpClient) {
        this.executor = Objects.requireNonNull(executor);
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * @return the {@link ExecutorService} used to execute asynchronous processing of the configuration client
     */
    protected ExecutorService getExecutor() {
        return this.executor;
    }

    /**
     * @return the service discovery manager used to find configuration service end-points
     */
    protected OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Retrieve a {@link JsonObject} by calling the specified {@link Service} at the specified url path
     *
     * @param service   the {@link Service} to call with a REST request
     * @param url       the URL path to invoke with a {@code GET} request on the service
     * @param converter the {@link Converter} object used to transform the {@link JsonObject} into the return object
     * @return the {@link JsonObject} returned from the service
     */
    protected <T> T get(final Service service, final String url, final Converter<JsonObject, T> converter)
            throws Exception {
        final Request request = new Builder().url(service.asUrl() + url).get().build();
        final Response response = getHttpClient().newCall(request).execute();
        try (final ResponseBody responseBody = response.body()) {
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    return converter.convert(new JsonParser().parse(responseBody.string()).getAsJsonObject());
                default:
                    throw new Exception(responseBody.string());
            }
        }
    }

    /**
     * Retrieve a {@link Map} containing the provided {@link Service} objects mapped to the {@link JsonObject}
     * response by calling the specified {@link Service} at the specified url path.
     *
     * @param services  the {@link Service} objects to call with a REST request
     * @param url       the URL path to invoke with a {@code GET} request on the service
     * @param converter the {@link Converter} object used to transform the {@link JsonObject} into the return object
     * @return the {@link JsonObject} returned from the service
     */
    protected <T> Future<Map<Service, T>> getMap(
            final Collection<Service> services, final String url, final Converter<JsonObject, T> converter) {
        return getExecutor().submit(() -> {
            final Map<Service, Future<T>> futureMap = new HashMap<>();
            services.forEach(
                    service -> futureMap.put(service, getExecutor().submit(() -> get(service, url, converter))));

            final Map<Service, T> map = new HashMap<>();
            for (final Entry<Service, Future<T>> entry : futureMap.entrySet()) {
                try {
                    map.put(entry.getKey(), entry.getValue().get(10, TimeUnit.SECONDS));
                } catch (final InterruptedException | TimeoutException | ExecutionException failed) {
                    final String service =
                            String.format("%s:%d (%s)", entry.getKey().getHost(), entry.getKey().getPort(),
                                    entry.getKey().getType().name());
                    throw new Exception("Failed to retrieve response data for " + service, failed);
                }
            }
            return map;
        });
    }

    /**
     * @param service the {@link Service} for which a {@link ServiceInfo} will be retrieved
     * @return the {@link ServiceInfo} returned from the service wrapped in a {@link Future}
     */
    public Future<ServiceInfo> getInfo(final Service service) {
        Objects.requireNonNull(service);
        return getExecutor().submit(() -> get(service, "service/info", new ServiceInfoConverter()));
    }

    /**
     * @param services the {@link Collection} of {@link Service} objects for which a {@link ServiceInfo} will be
     *                 retrieved
     * @return a {@link Map} of {@link Service} object mapping to the associated {@link ServiceInfo} returned from the
     * individual services, wrapped in a {@link Future}
     */
    public Future<Map<Service, ServiceInfo>> getInfo(final Collection<Service> services) {
        Objects.requireNonNull(services);
        return getExecutor()
                .submit(() -> getMap(services, "service/info", new ServiceInfoConverter()).get(10, TimeUnit.SECONDS));
    }

    /**
     * @param service the {@link Service} for which a {@link ServiceMemory} will be retrieved
     * @return the {@link ServiceMemory} returned from the service wrapped in a {@link Future}
     */
    public Future<ServiceMemory> getMemory(final Service service) {
        Objects.requireNonNull(service);
        return getExecutor().submit(() -> get(service, "service/memory", new ServiceMemoryConverter()));
    }

    /**
     * @param services the {@link Collection} of {@link Service} objects for which a {@link ServiceMemory} will be
     *                 retrieved
     * @return a {@link Map} of {@link Service} object mapping to the associated {@link ServiceMemory} returned from
     * the individual services, wrapped in a {@link Future}
     */
    public Future<Map<Service, ServiceMemory>> getMemory(final Collection<Service> services) {
        Objects.requireNonNull(services);
        return getExecutor().submit(() -> getMap(services, "service/memory", new ServiceMemoryConverter())
                .get(10, TimeUnit.SECONDS));
    }

    /**
     * @param service the {@link Service} to be controlled
     * @param url     the URL path of the control operation
     * @return the resulting {@link ServiceControlStatus} returned from the service
     */
    protected Future<ServiceControlStatus> control(final Service service, final String url) {
        Objects.requireNonNull(service);
        return getExecutor().submit(() -> get(service, url, new ServiceControlStatusConverter()));
    }

    /**
     * @param services the {@link Collection} of {@link Service} objects to be controlled
     * @param url      the URL path of the control operation
     * @return a {@link Map} of {@link Service} object mapping to the associated {@link ServiceControlStatus} returned
     * from the individual controlled services, wrapped in a {@link Future}
     */
    protected Future<Map<Service, ServiceControlStatus>> control(final Collection<Service> services, final String url) {
        Objects.requireNonNull(services);
        return getExecutor()
                .submit(() -> getMap(services, url, new ServiceControlStatusConverter()).get(10, TimeUnit.SECONDS));
    }

    /**
     * @param service the {@link Service} to be stopped
     * @return the resulting {@link ServiceControlStatus} returned from the stopped service
     */
    public Future<ServiceControlStatus> stop(final Service service) {
        return control(service, "service/control/stop");
    }

    /**
     * @param services the {@link Collection} of {@link Service} objects to be stopped
     * @return a {@link Map} of {@link Service} object mapping to the associated {@link ServiceControlStatus} returned
     * from the individual stopped services, wrapped in a {@link Future}
     */
    public Future<Map<Service, ServiceControlStatus>> stop(final Collection<Service> services) {
        return control(services, "service/control/stop");
    }

    /**
     * @param service the {@link Service} to be restarted
     * @return the resulting {@link ServiceControlStatus} returned from the restarted service
     */
    public Future<ServiceControlStatus> restart(final Service service) {
        return control(service, "service/control/restart");
    }

    /**
     * @param services the {@link Collection} of {@link Service} objects to be restarted
     * @return a {@link Map} of {@link Service} object mapping to the associated {@link ServiceControlStatus} returned
     * from the individual restarted services, wrapped in a {@link Future}
     */
    public Future<Map<Service, ServiceControlStatus>> restart(final Collection<Service> services) {
        return control(services, "service/control/restart");
    }
}
