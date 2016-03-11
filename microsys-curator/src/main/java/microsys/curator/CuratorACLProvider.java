package microsys.curator;

import org.apache.curator.framework.api.ACLProvider;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Provides {@link ACL} security controls for zookeeper paths via curator.
 */
public class CuratorACLProvider implements ACLProvider {
    @Nonnull
    private final List<ACL> acls = new LinkedList<>();

    /**
     * Default constructor.
     */
    public CuratorACLProvider() {
        this.acls.addAll(ZooDefs.Ids.CREATOR_ALL_ACL);
        this.acls.addAll(ZooDefs.Ids.READ_ACL_UNSAFE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ACL> getDefaultAcl() {
        return this.acls;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ACL> getAclForPath(@Nonnull final String path) {
        return this.acls;
    }
}
