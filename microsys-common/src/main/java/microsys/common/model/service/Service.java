package microsys.common.model.service;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.model.Model;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing the registration of a service for automatic discovery.
 */
public class Service implements Model, Comparable<Service> {
    @Nonnull
    private final ServiceType type;
    @Nonnull
    private final String host;
    private final int port;
    private final boolean secure;
    @Nonnull
    private final String version;

    /**
     * @param type    the type of service represented
     * @param host    the host on which the service is running
     * @param port    the port on which the service has bound
     * @param secure  whether the service is operating with SSL enabled on the connection
     * @param version the version of the service that is running
     */
    public Service(
            @Nonnull final ServiceType type, @Nonnull final String host, final int port, final boolean secure,
            @Nonnull final String version) {
        this.type = Objects.requireNonNull(type);
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.secure = secure;
        this.version = version;
    }

    /**
     * @param json the JSON representation of a {@link Service} object
     */
    public Service(@Nonnull final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("type"), "Type field required");
        Preconditions.checkArgument(json.get("type").isJsonPrimitive(), "Type field must be a primitive");
        Preconditions.checkArgument(json.has("host"), "Host field required");
        Preconditions.checkArgument(json.get("host").isJsonPrimitive(), "Host field must be a primitive");
        Preconditions.checkArgument(json.has("port"), "Port field required");
        Preconditions.checkArgument(json.get("port").isJsonPrimitive(), "Port field must be a primitive");
        Preconditions.checkArgument(json.has("secure"), "Secure field required");
        Preconditions.checkArgument(json.get("secure").isJsonPrimitive(), "Secure field must be a primitive");
        Preconditions.checkArgument(json.has("version"), "Version field required");
        Preconditions.checkArgument(json.get("version").isJsonPrimitive(), "Version field must be a primitive");

        this.type = ServiceType.valueOf(json.get("type").getAsString());
        this.host = json.get("host").getAsString();
        this.port = json.get("port").getAsInt();
        this.secure = json.get("secure").getAsBoolean();
        this.version = json.get("version").getAsString();
    }

    /**
     * @return the unique id used to describe this service
     */
    @Nonnull
    public String getId() {
        return String.format("%s:%d", getHost(), getPort());
    }

    /**
     * @return the type of service represented
     */
    @Nonnull
    public ServiceType getType() {
        return this.type;
    }

    /**
     * @return the host on which the service is running
     */
    @Nonnull
    public String getHost() {
        return this.host;
    }

    /**
     * @return the port on which the service has bound
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @return whether the service is running with SSL enabled
     */
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * @return the version of the service that is running
     */
    @Nonnull
    public String getVersion() {
        return this.version;
    }

    /**
     * @return a URL representation capable of being used to communicate with the service
     */
    @Nonnull
    public String asUrl() {
        return String.format("%s://%s:%d/", isSecure() ? "https" : "http", getHost(), getPort());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final Service other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getType().name(), other.getType().name());
        cmp.append(getHost(), other.getHost());
        cmp.append(getPort(), other.getPort());
        cmp.append(isSecure(), other.isSecure());
        cmp.append(getVersion(), other.getVersion());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof Service) && compareTo((Service) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getType().name());
        hash.append(getHost());
        hash.append(getPort());
        hash.append(isSecure());
        hash.append(getVersion());
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
        str.append("host", getHost());
        str.append("port", getPort());
        str.append("secure", isSecure());
        str.append("version", getVersion());
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
        json.addProperty("host", getHost());
        json.addProperty("port", getPort());
        json.addProperty("secure", isSecure());
        json.addProperty("version", getVersion());
        return json;
    }
}
