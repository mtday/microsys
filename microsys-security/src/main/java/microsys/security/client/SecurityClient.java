package microsys.security.client;

import com.google.gson.JsonParser;

import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.discovery.DiscoveryException;
import microsys.security.model.User;
import microsys.security.service.UserService;
import microsys.security.service.UserServiceException;
import microsys.service.model.ServiceEnvironment;
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
 * Provides remote access over REST to the security service.
 */
public class SecurityClient implements UserService {
    @Nonnull
    private final ServiceEnvironment serviceEnvironment;

    /**
     * @param serviceEnvironment the service environment
     */
    public SecurityClient(@Nonnull final ServiceEnvironment serviceEnvironment) {
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
     * @return a randomly chosen {@link Service} object from service discovery to use when connecting to the security
     * service
     * @throws DiscoveryException if there is a problem retrieving a random {@link Service}
     * @throws UserServiceException if there no random security {@link Service} objects are available
     */
    @Nonnull
    protected Service getRandom() throws DiscoveryException, UserServiceException {
        final Optional<Service> random = getServiceEnvironment().getDiscoveryManager().getRandom(ServiceType.SECURITY);
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
    @Nonnull
    protected Optional<User> handleResponse(@Nonnull final Response response) throws IOException, UserServiceException {
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
    @Nonnull
    protected Future<Optional<User>> get(@Nonnull final String url) {
        Objects.requireNonNull(url);
        return getServiceEnvironment().getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(getRandom().asUrl() + url).get().build();
            return handleResponse(getServiceEnvironment().getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> getById(@Nonnull final String id) {
        return get("id/" + Objects.requireNonNull(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> getByName(@Nonnull final String name) {
        return get("name/" + Objects.requireNonNull(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> save(@Nonnull final User user) {
        Objects.requireNonNull(user);
        return getServiceEnvironment().getExecutor().submit(() -> {
            final RequestBody body =
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), user.toJson().toString());
            final Request request = new Request.Builder().url(getRandom().asUrl()).post(body).build();
            return handleResponse(getServiceEnvironment().getHttpClient().newCall(request).execute());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> remove(@Nonnull final String id) {
        Objects.requireNonNull(id);
        return getServiceEnvironment().getExecutor().submit(() -> {
            final Request request = new Request.Builder().url(getRandom().asUrl() + id).delete().build();
            return handleResponse(getServiceEnvironment().getHttpClient().newCall(request).execute());
        });
    }
}
