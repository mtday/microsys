package microsys.service.model;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.model.Model;
import microsys.common.model.service.ServiceType;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing summary information about a service.
 */
public class ServiceInfo implements Model, Comparable<ServiceInfo> {
    @Nonnull
    private final ServiceType type;
    @Nonnull
    private final String systemName;
    @Nonnull
    private final String systemVersion;

    /**
     * @param type the type of service represented
     * @param systemName the name of the system in which this service is running
     * @param systemVersion the version of the service
     */
    public ServiceInfo(@Nonnull final ServiceType type, @Nonnull final String systemName, @Nonnull final String systemVersion) {
        this.type = Objects.requireNonNull(type);
        this.systemName = Objects.requireNonNull(systemName);
        this.systemVersion = Objects.requireNonNull(systemVersion);
    }

    /**
     * @param json the JSON representation of a {@link ServiceInfo} object
     */
    public ServiceInfo(@Nonnull final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("type"), "Type field required");
        Preconditions.checkArgument(json.get("type").isJsonPrimitive(), "Type field must be a primitive");
        Preconditions.checkArgument(json.has("systemName"), "System Name field required");
        Preconditions.checkArgument(json.get("systemName").isJsonPrimitive(), "System Name field must be a primitive");
        Preconditions.checkArgument(json.has("systemVersion"), "System Version field required");
        Preconditions.checkArgument(json.get("systemVersion").isJsonPrimitive(), "System Version field must be a primitive");

        this.type = ServiceType.valueOf(json.get("type").getAsString());
        this.systemName = json.get("systemName").getAsString();
        this.systemVersion = json.get("systemVersion").getAsString();
    }

    /**
     * @return the type of service represented
     */
    @Nonnull
    public ServiceType getType() {
        return this.type;
    }

    /**
     * @return the name of the system in which this service is running
     */
    @Nonnull
    public String getSystemName() {
        return this.systemName;
    }

    /**
     * @return the version of the service
     */
    @Nonnull
    public String getSystemVersion() {
        return this.systemVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final ServiceInfo other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getType().name(), other.getType().name());
        cmp.append(getSystemName(), other.getSystemName());
        cmp.append(getSystemVersion(), other.getSystemVersion());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof ServiceInfo) && compareTo((ServiceInfo) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getType().name());
        hash.append(getSystemName());
        hash.append(getSystemVersion());
        return hash.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("type", getType());
        str.append("systemName", getSystemName());
        str.append("systemVersion", getSystemVersion());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("type", getType().name());
        json.addProperty("systemName", getSystemName());
        json.addProperty("systemVersion", getSystemVersion());
        return json;
    }

    /**
     * Support conversions to and from {@link JsonObject} with this class.
     */
    public static class ServiceInfoConverter extends Converter<JsonObject, ServiceInfo> {
        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        protected ServiceInfo doForward(@Nonnull final JsonObject jsonObject) {
            return new ServiceInfo(Objects.requireNonNull(jsonObject));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        protected JsonObject doBackward(@Nonnull final ServiceInfo serviceInfo) {
            return Objects.requireNonNull(serviceInfo).toJson();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@CheckForNull final Object other) {
            return (other instanceof ServiceInfoConverter);
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
