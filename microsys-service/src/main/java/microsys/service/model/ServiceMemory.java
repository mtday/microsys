package microsys.service.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Converter;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.model.Model;

import java.lang.management.MemoryUsage;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing the memory used and available for a service
 */
public class ServiceMemory implements Model, Comparable<ServiceMemory> {
    private final long heapUsed;
    private final long heapAvailable;
    private final long nonheapUsed;
    private final long nonheapAvailable;

    /**
     * @param heap    the {@link MemoryUsage} describing the service heap memory
     * @param nonheap the {@link MemoryUsage} describing the service non-heap memory
     */
    public ServiceMemory(@Nonnull final MemoryUsage heap, @Nonnull final MemoryUsage nonheap) {
        Objects.requireNonNull(heap);
        Objects.requireNonNull(nonheap);

        this.heapUsed = heap.getUsed();
        this.heapAvailable = heap.getMax();
        this.nonheapUsed = nonheap.getUsed();
        this.nonheapAvailable = nonheap.getMax();
    }

    /**
     * @param json the JSON representation of a {@link ServiceMemory} object
     */
    public ServiceMemory(@Nonnull final JsonObject json) {
        Objects.requireNonNull(json);
        checkArgument(json.has("heap"), "Heap field required");
        checkArgument(json.get("heap").isJsonObject(), "Heap field must be an object");
        checkArgument(json.has("nonheap"), "Non-Heap field required");
        checkArgument(json.get("nonheap").isJsonObject(), "Non-Heap field must be an object");

        final JsonObject heap = json.get("heap").getAsJsonObject();
        checkArgument(heap.has("used"), "Heap Used field required");
        checkArgument(heap.get("used").isJsonPrimitive(), "Heap Used field must be a primitive");
        checkArgument(heap.has("available"), "Heap Available field required");
        checkArgument(heap.get("available").isJsonPrimitive(), "Heap Available field must be a primitive");

        final JsonObject nonheap = json.get("nonheap").getAsJsonObject();
        checkArgument(nonheap.has("used"), "Non-Heap Used field required");
        checkArgument(nonheap.get("used").isJsonPrimitive(), "Non-Heap Used field must be a primitive");
        checkArgument(nonheap.has("available"), "Non-Heap Available field required");
        checkArgument(nonheap.get("available").isJsonPrimitive(), "Non-Heap Available field must be a primitive");

        this.heapUsed = heap.get("used").getAsLong();
        this.heapAvailable = heap.get("available").getAsLong();
        this.nonheapUsed = nonheap.get("used").getAsLong();
        this.nonheapAvailable = nonheap.get("available").getAsLong();
    }

    /**
     * @return the type of unit the memory numbers are in
     */
    @Nonnull
    public String getUnits() {
        return "bytes";
    }

    /**
     * @return the amount of heap memory in use
     */
    public long getHeapUsed() {
        return this.heapUsed;
    }

    /**
     * @return the amount of heap memory available
     */
    public long getHeapAvailable() {
        return this.heapAvailable;
    }

    /**
     * @return the amount of heap memory used, as a percentage
     */
    public double getHeapUsedPercent() {
        return ((double) getHeapUsed()) / getHeapAvailable() * 100d;
    }

    /**
     * @return the status of the heap memory usage of the service, typically {@code "NORMAL"} unless the heap memory is
     * higher than 80%, in which case {@code "WARNING"} is returned
     */
    @Nonnull
    public String getHeapStatus() {
        return getHeapUsedPercent() > 80 ? "WARNING" : "NORMAL";
    }

    /**
     * @return the amount of non-heap memory in use
     */
    public long getNonHeapUsed() {
        return this.nonheapUsed;
    }

    /**
     * @return the amount of non-heap memory available
     */
    public long getNonHeapAvailable() {
        return this.nonheapAvailable;
    }

    /**
     * @return the amount of non-heap memory used, as a percentage
     */
    public double getNonHeapUsedPercent() {
        return ((double) getNonHeapUsed()) / getNonHeapAvailable() * 100d;
    }

    /**
     * @return the status of the non-heap memory usage of the service, typically {@code "NORMAL"} unless the non-heap
     * memory is higher than 80%, in which case {@code "WARNING"} is returned
     */
    @Nonnull
    public String getNonHeapStatus() {
        return getNonHeapUsedPercent() > 80 ? "WARNING" : "NORMAL";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final ServiceMemory other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getHeapUsed(), other.getHeapUsed());
        cmp.append(getHeapAvailable(), other.getHeapAvailable());
        cmp.append(getHeapUsedPercent(), other.getHeapUsedPercent());
        cmp.append(getHeapStatus(), other.getHeapStatus());
        cmp.append(getNonHeapUsed(), other.getNonHeapUsed());
        cmp.append(getNonHeapAvailable(), other.getNonHeapAvailable());
        cmp.append(getNonHeapUsedPercent(), other.getNonHeapUsedPercent());
        cmp.append(getNonHeapStatus(), other.getNonHeapStatus());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof ServiceMemory) && compareTo((ServiceMemory) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getHeapUsed());
        hash.append(getHeapAvailable());
        hash.append(getHeapUsedPercent());
        hash.append(getHeapStatus());
        hash.append(getNonHeapUsed());
        hash.append(getNonHeapAvailable());
        hash.append(getNonHeapUsedPercent());
        hash.append(getNonHeapStatus());
        return hash.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("units", getUnits());
        str.append("heapUsed", getHeapUsed());
        str.append("heapAvailable", getHeapAvailable());
        str.append("heapUsedPercent", getHeapUsedPercent());
        str.append("heapStatus", getHeapStatus());
        str.append("nonheapUsed", getNonHeapUsed());
        str.append("nonheapAvailable", getNonHeapAvailable());
        str.append("nonheapUsedPercent", getNonHeapUsedPercent());
        str.append("nonheapStatus", getNonHeapStatus());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JsonObject toJson() {
        final JsonObject heap = new JsonObject();
        heap.addProperty("units", getUnits());
        heap.addProperty("used", getHeapUsed());
        heap.addProperty("available", getHeapAvailable());
        heap.addProperty("usedPercent", getHeapUsedPercent());
        heap.addProperty("status", getHeapStatus());

        final JsonObject nonheap = new JsonObject();
        nonheap.addProperty("units", getUnits());
        nonheap.addProperty("used", getNonHeapUsed());
        nonheap.addProperty("available", getNonHeapAvailable());
        nonheap.addProperty("usedPercent", getNonHeapUsedPercent());
        nonheap.addProperty("status", getNonHeapStatus());

        final JsonObject json = new JsonObject();
        json.add("heap", heap);
        json.add("nonheap", nonheap);
        return json;
    }

    /**
     * Support conversions to and from {@link JsonObject} with this class.
     */
    public static class ServiceMemoryConverter extends Converter<JsonObject, ServiceMemory> {
        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        protected ServiceMemory doForward(@Nonnull final JsonObject jsonObject) {
            return new ServiceMemory(Objects.requireNonNull(jsonObject));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        protected JsonObject doBackward(@Nonnull final ServiceMemory serviceMemory) {
            return Objects.requireNonNull(serviceMemory).toJson();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@CheckForNull final Object other) {
            return (other instanceof ServiceMemoryConverter);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }
    }
}
