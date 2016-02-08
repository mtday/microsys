package microsys.service.client;

import com.google.gson.JsonParser;
import microsys.service.model.Service;
import microsys.service.model.ServiceMemory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
     * {@inheritDoc}
     */
    public Future<ServiceMemory> getMemory(final Service service) {
        Objects.requireNonNull(service);
        return getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(service.asUrl() + "service/memory").get().build();
            final Response response = getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    return new ServiceMemory(new JsonParser().parse(response.body().string()).getAsJsonObject());
                default:
                    throw new Exception(response.body().string());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public Future<Map<Service, ServiceMemory>> getMemory(final Collection<Service> services) {
        return getExecutor().submit(() -> {
            final Map<Service, Future<ServiceMemory>> futureMap = new HashMap<>();
            services.forEach(service -> futureMap.put(service, getMemory(service)));

            final Map<Service, ServiceMemory> serviceMap = new HashMap<>();
            for (final Map.Entry<Service, Future<ServiceMemory>> entry : futureMap.entrySet()) {
                try {
                    serviceMap.put(entry.getKey(), entry.getValue().get(10, TimeUnit.SECONDS));
                } catch (final InterruptedException | TimeoutException | ExecutionException failed) {
                    final String service = String.format("%s:%d (%s)",
                            entry.getKey().getHost(), entry.getKey().getPort(), entry.getKey().getType().name());
                    throw new Exception("Failed to retrieve service memory for " + service, failed);
                }
            }
            return serviceMap;
        });
    }
}
