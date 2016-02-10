package microsys.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;

import org.junit.Test;

import java.lang.management.MemoryUsage;

/**
 * Perform testing on the {@link ServiceMemory} class.
 */
public class ServiceMemoryTest {
    @Test
    public void testCompareTo() {
        final MemoryUsage m1 = new MemoryUsage(-1, 100, 200, 200);
        final MemoryUsage m2 = new MemoryUsage(-1, 350, 400, 400);
        final MemoryUsage m3 = new MemoryUsage(-1, 390, 400, 400);
        final ServiceMemory a = new ServiceMemory(m1, m2);
        final ServiceMemory b = new ServiceMemory(m2, m3);

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final MemoryUsage m1 = new MemoryUsage(-1, 100, 200, 200);
        final MemoryUsage m2 = new MemoryUsage(-1, 350, 400, 400);
        final MemoryUsage m3 = new MemoryUsage(-1, 390, 400, 400);
        final ServiceMemory a = new ServiceMemory(m1, m2);
        final ServiceMemory b = new ServiceMemory(m2, m3);

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(b, a);
        assertEquals(b, b);
    }

    @Test
    public void testHashCode() {
        final ServiceMemory m =
                new ServiceMemory(new MemoryUsage(-1, 100, 200, 200), new MemoryUsage(-1, 350, 400, 400));
        assertEquals(1602216758, m.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceMemory m =
                new ServiceMemory(new MemoryUsage(-1, 100, 200, 200), new MemoryUsage(-1, 350, 400, 400));
        assertEquals(
                "ServiceMemory[units=bytes,heapUsed=100,heapAvailable=200,heapUsedPercent=50.0,heapStatus=NORMAL,"
                        + "nonheapUsed=350,nonheapAvailable=400,nonheapUsedPercent=87.5,nonheapStatus=WARNING]",
                m.toString());
    }

    @Test
    public void testToJson() {
        final ServiceMemory m =
                new ServiceMemory(new MemoryUsage(-1, 100, 200, 200), new MemoryUsage(-1, 350, 400, 400));
        assertEquals("{\"heap\":{\"units\":\"bytes\",\"used\":100,\"available\":200,\"usedPercent\":50.0,"
                + "\"status\":\"NORMAL\"},\"nonheap\":{\"units\":\"bytes\",\"used\":350,\"available\":400,"
                + "\"usedPercent\":87.5,\"status\":\"WARNING\"}}", m.toJson().toString());
    }

    @Test
    public void testJsonConstructor() {
        final ServiceMemory original =
                new ServiceMemory(new MemoryUsage(-1, 100, 200, 200), new MemoryUsage(-1, 350, 400, 400));
        final ServiceMemory copy = new ServiceMemory(original.toJson());
        assertEquals(original, copy);
    }

    @Test
    public void testConverter() {
        final ServiceMemory.ServiceMemoryConverter converter = new ServiceMemory.ServiceMemoryConverter();
        final ServiceMemory original =
                new ServiceMemory(new MemoryUsage(-1, 100, 200, 200), new MemoryUsage(-1, 350, 400, 400));
        final JsonObject json = converter.doBackward(original);
        final ServiceMemory copy = converter.doForward(json);
        assertEquals(original, copy);
    }
}
