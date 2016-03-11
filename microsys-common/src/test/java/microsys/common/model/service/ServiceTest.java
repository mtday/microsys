package microsys.common.model.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

/**
 * Perform testing on the {@link Service} class.
 */
public class ServiceTest {
    @Test
    public void testCompareTo() {
        final Service a = new Service(ServiceType.CONFIG, "host", 1234, true, "1.2.3");
        final Service b = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
        final Service c = new Service(ServiceType.CONFIG, "host", 1235, false, "1.2.3");
        final Service d = new Service(ServiceType.CONFIG, "host2", 1235, false, "1.2.3");
        final Service e = new Service(ServiceType.WEB, "host2", 1235, false, "1.2.3");

        assertEquals(1, a.compareTo(null));

        assertEquals(0, a.compareTo(a));
        assertEquals(1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(-20, a.compareTo(e));

        assertEquals(-1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(-1, b.compareTo(d));
        assertEquals(-20, b.compareTo(e));

        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));
        assertEquals(-20, c.compareTo(e));

        assertEquals(1, d.compareTo(a));
        assertEquals(1, d.compareTo(b));
        assertEquals(1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
        assertEquals(-20, d.compareTo(e));

        assertEquals(20, e.compareTo(a));
        assertEquals(20, e.compareTo(b));
        assertEquals(20, e.compareTo(c));
        assertEquals(20, e.compareTo(d));
        assertEquals(0, e.compareTo(e));
    }

    @Test
    public void testEquals() {
        final Service a = new Service(ServiceType.CONFIG, "host", 1234, true, "1.2.3");
        final Service b = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
        final Service c = new Service(ServiceType.CONFIG, "host", 1235, false, "1.2.3");
        final Service d = new Service(ServiceType.CONFIG, "host2", 1235, false, "1.2.3");
        final Service e = new Service(ServiceType.WEB, "host2", 1235, false, "1.2.3");

        assertNotEquals(a, null);

        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, e);

        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(b, d);
        assertNotEquals(b, e);

        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
        assertNotEquals(c, d);
        assertNotEquals(c, e);

        assertNotEquals(d, a);
        assertNotEquals(d, b);
        assertNotEquals(d, c);
        assertEquals(d, d);
        assertNotEquals(d, e);

        assertNotEquals(e, a);
        assertNotEquals(e, b);
        assertNotEquals(e, c);
        assertNotEquals(e, d);
        assertEquals(e, e);
    }

    @Test
    public void testHashCode() {
        final Service a = new Service(ServiceType.CONFIG, "host", 1234, true, "1.2.3");
        final Service b = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
        final Service c = new Service(ServiceType.CONFIG, "host", 1235, false, "1.2.3");
        final Service d = new Service(ServiceType.CONFIG, "host2", 1235, false, "1.2.3");
        final Service e = new Service(ServiceType.WEB, "host2", 1235, false, "1.2.3");

        assertEquals(-23230325, a.hashCode());
        assertEquals(-23230288, b.hashCode());
        assertEquals(-23228919, c.hashCode());
        assertEquals(972210211, d.hashCode());
        assertEquals(-843334923, e.hashCode());
    }

    @Test
    public void testToString() {
        final Service svc = new Service(ServiceType.CONFIG, "host", 1234, true, "1.2.3");
        assertEquals("Service[type=CONFIG,host=host,port=1234,secure=true,version=1.2.3]", svc.toString());
    }

    @Test
    public void testToJson() {
        final Service svc = new Service(ServiceType.CONFIG, "host", 1234, true, "1.2.3");
        assertEquals("{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":1234,\"secure\":true,\"version\":\"1.2.3\"}",
                svc.toJson().toString());
    }

    @Test
    public void testAsUrl() {
        final Service secure = new Service(ServiceType.CONFIG, "host", 1234, true, "1.2.3");
        final Service insecure = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
        assertEquals("https://host:1234/", secure.asUrl());
        assertEquals("http://host:1234/", insecure.asUrl());
    }

    @Test
    public void testJsonConstructor() {
        final Service original = new Service(ServiceType.CONFIG, "host", 1234, true, "1.2.3");
        final Service copy = new Service(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoType() {
        final String jsonStr = "{\"host\":\"host\",\"port\":1234,\"secure\":true,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorTypeWrongType() {
        final String jsonStr = "{\"type\":[],\"host\":\"host\",\"port\":1234,\"secure\":true,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoHost() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"port\":1234,\"secure\":true,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorHostWrongType() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":[],\"port\":1234,\"secure\":true,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoPort() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":\"host\",\"secure\":true,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorPortWrongType() {
        final String jsonStr =
                "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":[],\"secure\":true,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorPortNotInt() {
        final String jsonStr =
                "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":\"a\",\"secure\":true,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoSecure() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":1234,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorSecureWrongType() {
        final String jsonStr =
                "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":1234,\"secure\":[],\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test
    public void testJsonConstructorSecureNotBoolean() {
        final String jsonStr =
                "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":1234,\"secure\":5,\"version\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        final Service created = new Service(json);
        // anything other than "true" is assumed to be false.
        final Service expected = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
        assertEquals(expected, created);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoVersion() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":1234,\"secure\":true}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorVersionWrongType() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":1234,\"secure\":true,\"version\":[]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Service(json);
    }

    @Test
    public void testGetId() {
        final Service service = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
        assertEquals("host:1234", service.getId());
    }
}
