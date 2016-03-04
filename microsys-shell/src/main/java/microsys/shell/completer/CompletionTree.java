package microsys.shell.completer;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;

import jline.console.completer.Completer;
import microsys.common.util.OptionalComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Represents a tree of valid tab-completion responses for the shell.
 */
public class CompletionTree {
    @Nonnull
    private final Map<String, CompletionTree> children = new HashMap<>();
    @Nonnull
    private final Optional<String> candidate;
    @Nonnull
    private final Optional<Completer> completer;

    /**
     * Default constructor creates a "root" node with no candidate value or completer.
     */
    public CompletionTree() {
        this.candidate = Optional.empty();
        this.completer = Optional.empty();
    }

    /**
     * @param candidate the tab-completion value to return for this node
     */
    public CompletionTree(@Nonnull final String candidate) {
        this.candidate = Optional.of(Objects.requireNonNull(candidate));
        this.completer = Optional.empty();
    }

    /**
     * @param completer the {@link Completer} to use when finding valid tab-completion values
     */
    public CompletionTree(@Nonnull final Completer completer) {
        this.candidate = Optional.of(""); // Needs to exist as a blank string to match all.
        this.completer = Optional.of(Objects.requireNonNull(completer));
    }

    /**
     * @return the single tab-completion value matching this node, possibly empty for "root" nodes
     */
    @Nonnull
    public Optional<String> getCandidate() {
        return this.candidate;
    }

    /**
     * @return the {@link Completer} to use when finding valid tab-completion values, possibly empty
     */
    @Nonnull
    public Optional<Completer> getCompleter() {
        return this.completer;
    }

    /**
     * @param other the completion tree with the same candidate value as this node, whose children should be merged
     * into this node
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public CompletionTree merge(@Nonnull final CompletionTree other) {
        if (new OptionalComparator<String>().compare(getCandidate(), other.getCandidate()) != 0) {
            throw new IllegalArgumentException("Unable to merge differing candidates");
        }
        return add(other.getChildren());
    }

    /**
     * @param child the new child node to add into this node
     */
    protected void addChild(@Nonnull final CompletionTree child) {
        Objects.requireNonNull(child);
        Preconditions.checkArgument(child.getCandidate().isPresent(), "Child nodes must have a candidate value");

        final Optional<CompletionTree> existing = getChild(child.getCandidate().get());
        if (existing.isPresent()) {
            existing.get().merge(child);
        } else {
            this.children.put(child.getCandidate().get(), child);
        }
    }

    /**
     * @param children the new child nodes to add into this node
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public CompletionTree add(@Nonnull final Collection<CompletionTree> children) {
        Objects.requireNonNull(children).stream().forEach(this::addChild);
        return this;
    }

    /**
     * @param children the new child nodes to add into this node
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public CompletionTree add(@Nonnull final CompletionTree... children) {
        Arrays.asList(Objects.requireNonNull(children)).stream().forEach(this::addChild);
        return this;
    }

    /**
     * @return a {@link List} containing all of the children nodes being managed by this node
     */
    @Nonnull
    public List<CompletionTree> getChildren() {
        return new ArrayList<>(this.children.values());
    }

    /**
     * @param prefix the prefix text used to find matching children
     * @return a {@link List} containing all of the children nodes that have candidate values that start with the
     * provided prefix value
     */
    @Nonnull
    public List<CompletionTree> getChildrenMatching(@Nonnull final String prefix) {
        return this.children.entrySet().stream()
                .filter(e -> StringUtils.isEmpty(e.getKey()) || StringUtils.startsWith(e.getKey(), prefix))
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    /**
     * @param candidate the candidate value for which a child node should be retrieved
     * @return the requested child node, possibly empty if the specified candidate was not recognized as a valid
     * child node
     */
    @Nonnull
    public Optional<CompletionTree> getChild(@Nonnull final String candidate) {
        return Optional.ofNullable(this.children.get(Objects.requireNonNull(candidate)));
    }

    /**
     * @return a {@link SortedSet} containing all of the candidate values in child nodes
     */
    @Nonnull
    public SortedSet<String> getChildrenCandidates() {
        return new TreeSet<>(this.children.keySet());
    }
}
