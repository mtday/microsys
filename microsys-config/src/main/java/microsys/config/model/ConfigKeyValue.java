package microsys.config.model;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
 * Represents a dynamic configuration key and value pair.
 */
public class ConfigKeyValue implements Model, Comparable<ConfigKeyValue> {
    @Nonnull
    private final String key;
    @Nonnull
    private final String value;

    /**
     * @param key the configuration key
     * @param value the configuration value
     */
    public ConfigKeyValue(@Nonnull final String key, @Nonnull final String value) {
        Preconditions.checkArgument(isNotEmpty(key), "Invalid empty key");
        Preconditions.checkArgument(isNotEmpty(value), "Invalid empty value");

        this.key = key;
        this.value = value;
    }

    /**
     * @param json a {@link JsonObject} from which a {@link ConfigKeyValue} will be parsed
     */
    public ConfigKeyValue(@Nonnull final JsonObject json) {
        // Validate the json object
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("key"), "Key field required");
        Preconditions.checkArgument(json.get("key").isJsonPrimitive(), "Key field must be a primitive");
        Preconditions.checkArgument(isNotEmpty(json.get("key").getAsString()), "Key field must not be empty");
        Preconditions.checkArgument(json.has("value"), "Value field required");
        Preconditions.checkArgument(json.get("value").isJsonPrimitive(), "Value field must be a primitive");
        Preconditions.checkArgument(isNotEmpty(json.get("value").getAsString()), "Value field must not be empty");

        this.key = json.get("key").getAsString();
        this.value = json.get("value").getAsString();
    }

    /**
     * @return the configuration key
     */
    @Nonnull
    public String getKey() {
        return this.key;
    }

    /**
     * @return the configuration value
     */
    @Nonnull
    public String getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final ConfigKeyValue other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getKey(), other.getKey());
        cmp.append(getValue(), other.getValue());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof ConfigKeyValue) && compareTo((ConfigKeyValue) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getKey());
        hash.append(getValue());
        return hash.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("key", getKey());
        str.append("value", getValue());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("key", getKey());
        json.addProperty("value", getValue());
        return json;
    }
}
