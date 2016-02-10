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
 * An immutable object representing a reservation for a service and port. Prevents two different services from
 * attempting to start and listen on the same host and port.
 */
public class Reservation implements Model, Comparable<Reservation> {
    private final ServiceType type;
    private final String host;
    private final int port;

    /**
     * @param type the type of service for which the reservation was put in place
     * @param host the host on which the service will run
     * @param port the port on which the service will run
     */
    public Reservation(final ServiceType type, final String host, final int port) {
        this.type = Objects.requireNonNull(type);
        this.host = Objects.requireNonNull(host);
        this.port = port;
    }

    /**
     * @param json the {@link JsonObject} from which the reservation should be built
     */
    public Reservation(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("type"), "Type field required");
        Preconditions.checkArgument(json.get("type").isJsonPrimitive(), "Type field must be a primitive");
        Preconditions.checkArgument(json.has("host"), "Host field required");
        Preconditions.checkArgument(json.get("host").isJsonPrimitive(), "Host field must be a primitive");
        Preconditions.checkArgument(json.has("port"), "Port field required");
        Preconditions.checkArgument(json.get("port").isJsonPrimitive(), "Port field must be a primitive");

        this.type = ServiceType.valueOf(json.get("type").getAsString());
        this.host = json.get("host").getAsString();
        this.port = json.get("port").getAsInt();
    }

    /**
     * @return the type of service for which the reservation was put in place
     */
    public ServiceType getType() {
        return this.type;
    }

    /**
     * @return the host on which the service will run
     */
    public String getHost() {
        return this.host;
    }

    /**
     * @return the port on which the service will run
     */
    public int getPort() {
        return this.port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Reservation other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getType().name(), other.getType().name());
        cmp.append(getHost(), other.getHost());
        cmp.append(getPort(), other.getPort());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Reservation) && compareTo((Reservation) other) == 0;
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
        return hash.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("type", getType().name());
        str.append("host", getHost());
        str.append("port", getPort());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("type", getType().name());
        json.addProperty("host", getHost());
        json.addProperty("port", getPort());
        return json;
    }
}
