package microsys.shell.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import microsys.common.model.Model;
import microsys.common.util.CollectionComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 * An immutable class used to manage the options available to a command.
 */
public class Options implements Model, Comparable<Options> {
    private final SortedSet<Option> options = new TreeSet<>();

    /**
     * @param options the individual option objects supported for the command
     */
    public Options(final Set<Option> options) {
        this.options.addAll(options);
    }

    public Options(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("options"), "Options field is required");
        Preconditions.checkArgument(json.get("options").isJsonArray(), "Options field must be an array");

        final JsonArray optionArr = json.getAsJsonArray("options");
        optionArr.forEach(e -> Preconditions.checkArgument(e.isJsonObject(), "Option must be an object"));
        optionArr.forEach(e -> this.options.add(new Option(e.getAsJsonObject())));
    }

    /**
     * @return an unmodifiable sorted set of the options supported for the command
     */
    public SortedSet<Option> getOptions() {
        return Collections.unmodifiableSortedSet(this.options);
    }

    /**
     * @return the commons-cli options implementation corresponding to this object
     */
    public org.apache.commons.cli.Options asOptions() {
        final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        getOptions().stream().map(Option::asOption).forEach(options::addOption);
        return options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonArray optionArr = new JsonArray();
        getOptions().forEach(o -> optionArr.add(o.toJson()));
        final JsonObject json = new JsonObject();
        json.add("options", optionArr);
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("options", getOptions());
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Options other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getOptions(), other.getOptions(), new CollectionComparator<>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Options) && compareTo((Options) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getOptions());
        return hash.toHashCode();
    }
}
