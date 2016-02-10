package microsys.security.client;

import com.google.gson.JsonParser;

import microsys.common.model.ServiceType;
import microsys.security.model.User;
import microsys.security.service.UserService;
import microsys.security.service.UserServiceException;
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

import javax.servlet.http.HttpServletResponse;

/**
 * Provides remote access over REST to the security service.
 */
public class SecurityClient implements UserService {
    private final ExecutorService executor;
    private final DiscoveryManager discoveryManager;
    private final OkHttpClient httpClient;

    /**
     * @param executor used to execute asynchronous processing of the configuration client
     * @param discoveryManager the service discovery manager used to find configuration service end-points
     * @param httpClient the HTTP client used to perform REST communication
     */
    public SecurityClient(
            final ExecutorService executor, final DiscoveryManager discoveryManager, final OkHttpClient httpClient) {
        this.executor = Objects.requireNonNull(executor);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
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
     * @return a randomly chosen {@link Service} object from service discovery to use when connecting to the security
     * service
     * @throws DiscoveryException if there is a problem retrieving a random {@link Service}
     * @throws UserServiceException if there no random security {@link Service} objects are available
     */
    protected Service getRandom() throws DiscoveryException, UserServiceException {
        final Optional<Service> random = getDiscoveryManager().getRandom(ServiceType.SECURITY);
        if (!random.isPresent()) {
            throw new UserServiceException("Unable to find a running security service");
        }
        return random.get();
    }

    /**
     * @param response the {@link Response} to be processed
     * @return the {@link User} object parsed from the response data, if available
     * @throws IOException if there is a problem processing the response data
     * @throws UserServiceException if there was a problem with the remote security service
     */
    protected Optional<User> handleResponse(final Response response) throws IOException, UserServiceException {
        Objects.requireNonNull(response);
        switch (response.code()) {
            case HttpServletResponse.SC_OK:
                return Optional.of(new User(new JsonParser().parse(response.body().string()).getAsJsonObject()));
            case HttpServletResponse.SC_NO_CONTENT:
            case HttpServletResponse.SC_NOT_FOUND:
                return Optional.empty();
            default:
                throw new UserServiceException(response.body().string());
        }
    }

    /**
     * @param url the base url path from which a {@link User} object will be retrieved
     * @return an {@link Optional} {@link User}, possibly empty if not found, wrapped in a {@link Future}
     */
    protected Future<Optional<User>> get(final String url) {
        Objects.requireNonNull(url);
        return getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(getRandom().asUrl() + url).get().build();
            return handleResponse(getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> getById(final String id) {
        return get("id/" + Objects.requireNonNull(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> getByName(final String name) {
        return get("name/" + Objects.requireNonNull(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> save(final User user) {
        Objects.requireNonNull(user);
        return getExecutor().submit(() -> {
            final RequestBody body =
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), user.toJson().toString());
            final Request request = new Request.Builder().url(getRandom().asUrl()).post(body).build();
            return handleResponse(getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> remove(final String id) {
        Objects.requireNonNull(id);
        return getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(getRandom().asUrl() + id).delete().build();
            return handleResponse(getHttpClient().newCall(request).execute());
        });
    }
}
