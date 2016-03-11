package microsys.security.service.impl;

import microsys.security.model.User;
import microsys.security.service.UserService;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

/**
 * Provides an in-memory implementation of a {@link UserService}.
 */
public class MemoryUserService implements UserService {
    @Nonnull
    private final ConcurrentHashMap<String, User> idMap = new ConcurrentHashMap<>();
    @Nonnull
    private final ConcurrentHashMap<String, User> nameMap = new ConcurrentHashMap<>();

    /**
     * @return the {@link ConcurrentHashMap} holding the user ids and the matching {@link User} objects
     */
    @Nonnull
    protected ConcurrentHashMap<String, User> getIdMap() {
        return this.idMap;
    }

    /**
     * @return the {@link ConcurrentHashMap} holding the user ids and the matching {@link User} objects
     */
    @Nonnull
    protected ConcurrentHashMap<String, User> getNameMap() {
        return this.nameMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> getById(@Nonnull final String id) {
        Objects.requireNonNull(id);
        return CompletableFuture.completedFuture(Optional.ofNullable(getIdMap().get(id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> getByName(@Nonnull final String userName) {
        Objects.requireNonNull(userName);
        return CompletableFuture.completedFuture(Optional.ofNullable(getNameMap().get(userName)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> save(@Nonnull final User user) {
        Objects.requireNonNull(user);
        final Optional<User> existingById = Optional.ofNullable(getIdMap().put(user.getId(), user));
        final Optional<User> existingByName = Optional.ofNullable(getNameMap().put(user.getUserName(), user));
        return CompletableFuture.completedFuture(existingById.isPresent() ? existingById : existingByName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<Optional<User>> remove(@Nonnull final String id) {
        Objects.requireNonNull(id);
        return CompletableFuture.completedFuture(Optional.ofNullable(getIdMap().remove(id)));
    }
}
