package microsys.config.service.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.service.ConfigService;
import microsys.config.service.ConfigServiceException;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

/**
 * A {@link ConfigService} implementation that makes use of a {@link CuratorFramework} to
 * store dynamic system configuration information in zookeeper.
 */
public class CuratorConfigService implements ConfigService, TreeCacheListener {
    private final static Logger LOG = LoggerFactory.getLogger(CuratorConfigService.class);

    private final static String PATH = "/dynamic-config";

    @Nonnull
    private final ExecutorService executor;
    @Nonnull
    private final CuratorFramework curator;
    @Nonnull
    private final TreeCache treeCache;

    /**
     * @param executor used to execute asynchronous processing of the configuration service
     * @param curator the {@link CuratorFramework} used to communicate configuration information with zookeeper
     * @throws ConfigServiceException if there is a problem with zookeeper communications
     */
    public CuratorConfigService(@Nonnull final ExecutorService executor, @Nonnull final CuratorFramework curator)
            throws ConfigServiceException {
        this.executor = Objects.requireNonNull(executor);
        this.curator = Objects.requireNonNull(curator);

        try {
            if (this.curator.checkExists().forPath(PATH) == null) {
                this.curator.create().creatingParentsIfNeeded().forPath(PATH);
            }

            this.treeCache = new TreeCache(this.curator, PATH);
            this.treeCache.start();
        } catch (final Exception exception) {
            throw new ConfigServiceException("Failed to initialize config service", exception);
        }

        // Add this class as a listener.
        this.treeCache.getListenable().addListener(this);
    }

    /**
     * @return the {@link ExecutorService} used to execute asynchronous processing of the configuration service
     */
    @Nonnull
    protected ExecutorService getExecutor() {
        return this.executor;
    }

    /**
     * @return the {@link CuratorFramework} used to communicate configuration information with zookeeper
     */
    @Nonnull
    protected CuratorFramework getCurator() {
        return this.curator;
    }

    /**
     * @return the {@link TreeCache} that is managing the dynamic system configuration information
     */
    @Nonnull
    protected TreeCache getTreeCache() {
        return this.treeCache;
    }

    /**
     * @param key the configuration key for which a zookeeper path should be created
     * @return the zookeeper path representation of the provided key
     */
    @Nonnull
    protected String getPath(@Nonnull final String key) {
        return String.format("%s/%s", PATH, Objects.requireNonNull(key));
    }

    /**
     * @param bytes the configuration value as bytes as stored in zookeeper
     * @return the String value of the bytes
     */
    @Nonnull
    protected String getValue(@Nonnull final byte[] bytes) {
        return new String(Objects.requireNonNull(bytes), StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<ConfigKeyValueCollection> getAll() {
        return getExecutor().submit(() -> {
            final Collection<ConfigKeyValue> coll = new LinkedList<>();
            final Map<String, ChildData> data = getTreeCache().getCurrentChildren(PATH);
            data.entrySet().stream()
                    .forEach(e -> coll.add(new ConfigKeyValue(e.getKey(), getValue(e.getValue().getData()))));
            return new ConfigKeyValueCollection(coll);
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
            final Optional<ChildData> existing = Optional.ofNullable(getTreeCache().getCurrentData(getPath(key)));
            if (existing.isPresent()) {
                return Optional.of(new ConfigKeyValue(key, getValue(existing.get().getData())));
            }
            return Optional.empty();
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
            final Optional<ChildData> existing =
                    Optional.ofNullable(getTreeCache().getCurrentData(getPath(kv.getKey())));
            try {
                final String path = getPath(kv.getKey());
                final byte[] value = kv.getValue().getBytes(StandardCharsets.UTF_8);
                if (existing.isPresent()) {
                    getCurator().setData().forPath(path, value);
                } else {
                    getCurator().create().creatingParentsIfNeeded().forPath(path, value);
                }
            } catch (final Exception setException) {
                LOG.error("Failed to set configuration value for key: {}", kv.getKey());
                throw new ConfigServiceException(
                        "Failed to set configuration value for key: " + kv.getKey(), setException);
            }

            if (existing.isPresent()) {
                return Optional.of(new ConfigKeyValue(kv.getKey(), getValue(existing.get().getData())));
            }
            return Optional.empty();
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
            final Optional<ChildData> existing = Optional.ofNullable(getTreeCache().getCurrentData(getPath(key)));
            try {
                if (existing.isPresent()) {
                    getCurator().delete().forPath(getPath(key));
                }
            } catch (final Exception unsetException) {
                LOG.error("Failed to remove configuration value with key: {}", key);
                throw new ConfigServiceException(
                        "Failed to remove configuration value with key: " + key, unsetException);
            }

            if (existing.isPresent()) {
                return Optional.of(new ConfigKeyValue(key, getValue(existing.get().getData())));
            }
            return Optional.empty();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void childEvent(@Nonnull final CuratorFramework client, @Nonnull final TreeCacheEvent event)
            throws Exception {
        if (event.getData() != null) {
            if (event.getData().getData() != null) {
                LOG.info("Configuration {}: {} => {}", event.getType(), event.getData().getPath(),
                        new String(event.getData().getData(), StandardCharsets.UTF_8));
            } else {
                LOG.info("Configuration {}: {}", event.getType(), event.getData().getPath());
            }
        } else {
            LOG.info("Configuration {}", event.getType());
        }
    }
}
