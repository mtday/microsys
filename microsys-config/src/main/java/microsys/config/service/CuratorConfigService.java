package microsys.config.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A {@link ConfigService} implementation that makes use of a {@link CuratorFramework} to
 * store dynamic system configuration information in zookeeper.
 */
public class CuratorConfigService implements ConfigService, TreeCacheListener {
    private final static Logger LOG = LoggerFactory.getLogger(CuratorConfigService.class);

    private final static String PATH = "/dynamic-config";

    private final CuratorFramework curator;
    private final TreeCache treeCache;

    /**
     * @param curator the {@link CuratorFramework} used to communicate configuration information with zookeeper
     * @throws Exception if there is a problem with zookeeper communications
     */
    public CuratorConfigService(final CuratorFramework curator) throws Exception {
        this.curator = Objects.requireNonNull(curator);

        if (this.curator.checkExists().forPath(PATH) == null) {
            this.curator.create().creatingParentsIfNeeded().forPath(PATH);
        }

        this.treeCache = new TreeCache(this.curator, PATH);
        this.treeCache.start();

        // Add this class as a listener.
        this.treeCache.getListenable().addListener(this);
    }

    /**
     * @return the {@link CuratorFramework} used to communicate configuration information with zookeeper
     */
    protected CuratorFramework getCurator() {
        return this.curator;
    }

    /**
     * @return the {@link TreeCache} that is managing the dynamic system configuration information
     */
    protected TreeCache getTreeCache() {
        return this.treeCache;
    }

    /**
     * @param key the configuration key for which a zookeeper path should be created
     * @return the zookeeper path representation of the provided key
     */
    protected String getPath(final String key) {
        return String.format("%s/%s", PATH, Objects.requireNonNull(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAll() throws ConfigServiceException {
        final Map<String, String> map = new TreeMap<>();
        getTreeCache().getCurrentChildren(PATH).entrySet().stream()
                .forEach(e -> map.put(e.getKey(), new String(e.getValue().getData(), StandardCharsets.UTF_8)));
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> get(final String key) throws ConfigServiceException {
        Objects.requireNonNull(key);
        final Optional<ChildData> existing = Optional.ofNullable(getTreeCache().getCurrentData(getPath(key)));
        if (existing.isPresent()) {
            return Optional.of(new String(existing.get().getData(), StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> set(final String key, final String value) throws ConfigServiceException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        final Optional<ChildData> existing = Optional.ofNullable(getTreeCache().getCurrentData(getPath(key)));
        try {
            if (existing.isPresent()) {
                getCurator().setData().forPath(getPath(key), value.getBytes(StandardCharsets.UTF_8));
            } else {
                getCurator().create().creatingParentsIfNeeded()
                        .forPath(getPath(key), value.getBytes(StandardCharsets.UTF_8));
            }
        } catch (final Exception setException) {
            LOG.error("Failed to set configuration value for key: {}", key);
            throw new ConfigServiceException("Failed to set configuration value for key: " + key, setException);
        }

        if (existing.isPresent()) {
            return Optional.of(new String(existing.get().getData(), StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> unset(final String key) throws ConfigServiceException {
        Objects.requireNonNull(key);

        final Optional<ChildData> existing = Optional.ofNullable(getTreeCache().getCurrentData(getPath(key)));
        try {
            if (existing.isPresent()) {
                getCurator().delete().forPath(getPath(key));
            }
        } catch (final Exception unsetException) {
            LOG.error("Failed to remove configuration value with key: {}", key);
            throw new ConfigServiceException("Failed to remove configuration value with key: " + key, unsetException);
        }

        if (existing.isPresent()) {
            return Optional.of(new String(existing.get().getData(), StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
        LOG.info("Configuration {}: {} => {}", event.getType(), event.getData().getPath(),
                new String(event.getData().getData(), StandardCharsets.UTF_8));
    }
}
