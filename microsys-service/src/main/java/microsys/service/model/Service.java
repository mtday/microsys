package microsys.service.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.x.discovery.ServiceInstance;

import microsys.common.model.Model;
import microsys.common.model.ServiceType;

import java.util.Date;
import java.util.Objects;

/**
 *
 */
public class Service implements Model, Comparable<Service> {
    private final ServiceType type;
    private final String host;
    private final int port;
    private final boolean secure;

    public Service(final ServiceType type, final String host, final int port, final boolean secure) {
        this.type = Objects.requireNonNull(type);
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.secure = secure;
    }

    public Service(final ServiceInstance<String> service) {
        this(new JsonParser().parse(String.valueOf(Objects.requireNonNull(service).getPayload())).getAsJsonObject());
    }

    public Service(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("type"), "Type field required");
        Preconditions.checkArgument(json.get("type").isJsonPrimitive(), "Type field must be a primitive");
        Preconditions.checkArgument(json.has("host"), "Host field required");
        Preconditions.checkArgument(json.get("host").isJsonPrimitive(), "Host field must be a primitive");
        Preconditions.checkArgument(json.has("port"), "Port field required");
        Preconditions.checkArgument(json.get("port").isJsonPrimitive(), "Port field must be a primitive");
        Preconditions.checkArgument(json.has("secure"), "Secure field required");
        Preconditions.checkArgument(json.get("secure").isJsonPrimitive(), "Secure field must be a primitive");

        this.type = ServiceType.valueOf(json.get("type").getAsString());
        this.host = json.get("host").getAsString();
        this.port = json.get("port").getAsInt();
        this.secure = json.get("secure").getAsBoolean();
    }

    public String getId() {
        return String.format("%s:%d", getHost(), getPort());
    }

    public ServiceType getType() {
        return this.type;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public String asUrl() {
        return String.format("%s://%s:%d/", isSecure() ? "https" : "http", getHost(), getPort());
    }

    public ServiceInstance<String> asServiceInstance() {
        return new ServiceInstance<>(getType().name(), getId(), getHost(), getPort(), null, toJson().toString(),
                new Date().getTime(), org.apache.curator.x.discovery.ServiceType.DYNAMIC, null);
    }

    @Override
    public int compareTo(final Service other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getType(), other.getType());
        cmp.append(getHost(), other.getHost());
        cmp.append(getPort(), other.getPort());
        cmp.append(isSecure(), other.isSecure());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof Service) && compareTo((Service) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getType().name());
        hash.append(getHost());
        hash.append(getPort());
        hash.append(isSecure());
        return hash.toHashCode();
    }

    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("type", getType());
        str.append("host", getHost());
        str.append("port", getPort());
        str.append("secure", isSecure());
        return str.build();
    }

    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("type", getType().name());
        json.addProperty("host", getHost());
        json.addProperty("port", getPort());
        json.addProperty("secure", isSecure());
        return json;
    }
}
