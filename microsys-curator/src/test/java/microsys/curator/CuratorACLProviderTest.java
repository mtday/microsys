package microsys.curator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.junit.Test;

import java.util.List;

/**
 * Perform testing on the {@link CuratorACLProvider} class.
 */
public class CuratorACLProviderTest {
    @Test
    public void test() {
        final CuratorACLProvider provider = new CuratorACLProvider();

        final List<ACL> defaultAcls = provider.getDefaultAcl();
        assertNotNull(defaultAcls);
        assertEquals(2, defaultAcls.size());
        assertEquals(ZooDefs.Perms.ALL, defaultAcls.get(0).getPerms());
        assertEquals("auth", defaultAcls.get(0).getId().getScheme());
        assertEquals("", defaultAcls.get(0).getId().getId());
        assertEquals(ZooDefs.Perms.READ, defaultAcls.get(1).getPerms());
        assertEquals("world", defaultAcls.get(1).getId().getScheme());
        assertEquals("anyone", defaultAcls.get(1).getId().getId());

        final List<ACL> rootPathAcls = provider.getAclForPath("/");
        assertNotNull(rootPathAcls);
        assertEquals(2, rootPathAcls.size());
        assertEquals(ZooDefs.Perms.ALL, rootPathAcls.get(0).getPerms());
        assertEquals("auth", rootPathAcls.get(0).getId().getScheme());
        assertEquals("", rootPathAcls.get(0).getId().getId());
        assertEquals(ZooDefs.Perms.READ, rootPathAcls.get(1).getPerms());
        assertEquals("world", rootPathAcls.get(1).getId().getScheme());
        assertEquals("anyone", rootPathAcls.get(1).getId().getId());

        final List<ACL> childPathAcls = provider.getAclForPath("/microsys/discovery");
        assertNotNull(childPathAcls);
        assertEquals(2, childPathAcls.size());
        assertEquals(ZooDefs.Perms.ALL, childPathAcls.get(0).getPerms());
        assertEquals("auth", childPathAcls.get(0).getId().getScheme());
        assertEquals("", childPathAcls.get(0).getId().getId());
        assertEquals(ZooDefs.Perms.READ, childPathAcls.get(1).getPerms());
        assertEquals("world", childPathAcls.get(1).getId().getScheme());
        assertEquals("anyone", childPathAcls.get(1).getId().getId());
    }
}
