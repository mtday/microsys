package microsys.security.service;

import microsys.security.model.User;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Provides an in-memory implementation of a {@link UserService}.
 */
public class MemoryUserService implements UserService {
    private final ConcurrentHashMap<String, User> idMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> nameMap = new ConcurrentHashMap<>();

    /**
     * @return the {@link ConcurrentHashMap} holding the user ids and the matching {@link User} objects
     */
    protected ConcurrentHashMap<String, User> getIdMap() {
        return this.idMap;
    }

    /**
     * @return the {@link ConcurrentHashMap} holding the user ids and the matching {@link User} objects
     */
    protected ConcurrentHashMap<String, User> getNameMap() {
        return this.nameMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> getById(final String id) {
        Objects.requireNonNull(id);
        return CompletableFuture.completedFuture(Optional.ofNullable(getIdMap().get(id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> getByName(final String userName) {
        Objects.requireNonNull(userName);
        return CompletableFuture.completedFuture(Optional.ofNullable(getNameMap().get(userName)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> save(final User user) {
        Objects.requireNonNull(user);
        final Optional<User> existingById = Optional.ofNullable(getIdMap().put(user.getId(), user));
        final Optional<User> existingByName = Optional.ofNullable(getNameMap().put(user.getUserName(), user));
        return CompletableFuture.completedFuture(existingById.isPresent() ? existingById : existingByName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Optional<User>> remove(final String id) {
        Objects.requireNonNull(id);
        return CompletableFuture.completedFuture(Optional.ofNullable(getIdMap().remove(id)));
    }
}
