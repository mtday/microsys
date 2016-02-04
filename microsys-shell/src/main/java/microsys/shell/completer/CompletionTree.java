package microsys.shell.completer;

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

/**
 *
 */
public class CompletionTree {
    private final Map<String, CompletionTree> children = new HashMap<>();
    private final Optional<String> candidate;
    private final Optional<Completer> completer;

    public CompletionTree() {
        this.candidate = Optional.empty();
        this.completer = Optional.empty();
    }

    public CompletionTree(final String candidate) {
        this.candidate = Optional.of(Objects.requireNonNull(candidate));
        this.completer = Optional.empty();
    }

    public CompletionTree(final String candidate, final Completer completer) {
        this.candidate = Optional.of(Objects.requireNonNull(candidate));
        this.completer = Optional.of(Objects.requireNonNull(completer));
    }

    public Optional<String> getCandidate() {
        return this.candidate;
    }

    public Optional<Completer> getCompleter() {
        return this.completer;
    }

    public void merge(final CompletionTree other) {
        if (new OptionalComparator<String>().compare(getCandidate(), other.getCandidate()) == 0) {
            add(other.getChildren());
        } else {
            throw new IllegalArgumentException("Unable to merge differing candidates");
        }
    }

    protected void addChild(final CompletionTree child) {
        Objects.requireNonNull(child);

        if (child.getCandidate().isPresent()) {
            final Optional<CompletionTree> existing = getChild(child.getCandidate().get());
            if (existing.isPresent()) {
                existing.get().merge(child);
            } else {
                this.children.put(child.getCandidate().get(), child);
            }
        }
    }

    public void add(final Collection<CompletionTree> children) {
        Objects.requireNonNull(children).stream().forEach(this::addChild);
    }

    public void add(final CompletionTree... children) {
        Arrays.asList(Objects.requireNonNull(children)).stream().forEach(this::addChild);
    }

    public List<CompletionTree> getChildren() {
        return new ArrayList<>(this.children.values());
    }

    public List<CompletionTree> getChildrenMatching(final String prefix) {
        return this.children.entrySet().stream().filter(e -> StringUtils.startsWith(e.getKey(), prefix))
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public Optional<CompletionTree> getChild(final String candidate) {
        return Optional.ofNullable(this.children.get(Objects.requireNonNull(candidate)));
    }

    public SortedSet<String> getChildrenCandidates() {
        return new TreeSet<>(this.children.keySet());
    }
}
