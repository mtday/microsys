package microsys.config.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.model.Model;
import microsys.common.util.CollectionComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
public class ConfigPath implements Model, Comparable<ConfigPath> {
    private final List<String> path;

    private ConfigPath(final List<String> path) {
        this.path = new ArrayList<>(path);
    }

    public ConfigPath(final Enum<?> e) {
        this(Arrays.asList(Objects.requireNonNull(e).name().toLowerCase().split("_")).stream()
                .filter(s -> !Strings.isNullOrEmpty(s)).collect(Collectors.toList()));
    }

    public ConfigPath(final JsonObject json) {
        // Validate the json object
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("path"), "Path field required");
        Preconditions.checkArgument(json.get("path").isJsonArray(), "Path field must be an array");

        // Make sure the path elements are strings.
        final JsonArray pathArr = json.get("path").getAsJsonArray();
        pathArr.forEach(pathElement -> {
            if (!pathElement.isJsonPrimitive()) {
                throw new IllegalArgumentException("Path element must be a string");
            }
        });

        this.path = new ArrayList<>();
        pathArr.forEach(pathElement -> this.path.add(pathElement.getAsString()));
    }

    public List<String> getPath() {
        return Collections.unmodifiableList(this.path);
    }

    @Override
    public int compareTo(final ConfigPath other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getPath(), other.getPath(), new CollectionComparator<String>());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ConfigPath) && compareTo((ConfigPath) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getPath());
        return hash.toHashCode();
    }

    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("path", getPath());
        return str.build();
    }

    @Override
    public JsonObject toJson() {
        final JsonArray arr = new JsonArray();
        getPath().forEach(arr::add);
        final JsonObject json = new JsonObject();
        json.add("path", arr);
        return json;
    }
}
