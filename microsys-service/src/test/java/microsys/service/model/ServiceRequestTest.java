package microsys.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

/**
 * Perform testing on the {@link ServiceRequest} class.
 */
public class ServiceRequestTest {
    @Test
    public void testDefaultConstructor() {
        final ServiceRequest a = new ServiceRequest();

        assertNotNull(a.getRequestId());
    }

    @Test
    public void testCompareTo() {
        final ServiceRequest a = new ServiceRequest("id1");
        final ServiceRequest b = new ServiceRequest("id2");
        final ServiceRequest c = new ServiceRequest("id3");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-2, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(2, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ServiceRequest a = new ServiceRequest("id1");
        final ServiceRequest b = new ServiceRequest("id2");
        final ServiceRequest c = new ServiceRequest("id3");

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
    }

    @Test
    public void testHashCode() {
        final ServiceRequest a = new ServiceRequest("id1");
        final ServiceRequest b = new ServiceRequest("id2");
        final ServiceRequest c = new ServiceRequest("id3");

        assertEquals(104683, a.hashCode());
        assertEquals(104684, b.hashCode());
        assertEquals(104685, c.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceRequest res = new ServiceRequest("id");
        assertEquals("ServiceRequest[requestId=id]", res.toString());
    }

    @Test
    public void testToJson() {
        final ServiceRequest res = new ServiceRequest("id");
        assertEquals("{\"requestId\":\"id\"}", res.toJson().toString());
    }

    @Test
    public void testJsonConstructor() {
        final ServiceRequest original = new ServiceRequest("id");
        final ServiceRequest copy = new ServiceRequest(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoRequestId() {
        final JsonObject json = new JsonParser().parse("{}").getAsJsonObject();
        new ServiceRequest(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorRequestIdWrongType() {
        final String jsonStr = "{\"requestId\":[]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceRequest(json);
    }
}
