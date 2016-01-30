package microsys.service.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.model.Model;
import microsys.common.model.ServiceType;

import java.util.Objects;

/**
 *
 */
public class Reservation implements Model, Comparable<Reservation> {
    private final ServiceType type;
    private final String host;
    private final int port;

    public Reservation(final ServiceType type, final String host, final int port) {
        this.type = Objects.requireNonNull(type);
        this.host = Objects.requireNonNull(host);
        this.port = port;
    }

    public static Reservation fromJson(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("type"), "Type field required");
        Preconditions.checkArgument(json.get("type").isJsonPrimitive(), "Type field must be a primitive");
        Preconditions.checkArgument(json.has("host"), "Host field required");
        Preconditions.checkArgument(json.get("host").isJsonPrimitive(), "Host field must be a primitive");
        Preconditions.checkArgument(json.has("port"), "Port field required");
        Preconditions.checkArgument(json.get("port").isJsonPrimitive(), "Port field must be a primitive");

        final ServiceType type = ServiceType.valueOf(json.get("host").getAsString());
        final String host = json.get("host").getAsString();
        final int port = json.get("port").getAsInt();

        return new Reservation(type, host, port);
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

    @Override
    public int compareTo(final Reservation other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getType(), other.getType());
        cmp.append(getHost(), other.getHost());
        cmp.append(getPort(), other.getPort());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof Reservation) && compareTo((Reservation) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getType());
        hash.append(getHost());
        hash.append(getPort());
        return hash.toHashCode();
    }

    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("type", getType().name());
        str.append("host", getHost());
        str.append("port", getPort());
        return str.build();
    }

    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("type", getType().name());
        json.addProperty("host", getHost());
        json.addProperty("port", getPort());
        return json;
    }
}
