package microsys.common.util;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Perform comparisons between two {@link Collection} objects.
 */
public class CollectionComparator<T> implements Comparator<Collection<T>> {
    @Nonnull
    private final Optional<Comparator<T>> comparator;

    /**
     * Default constructor, uses natural ordering of the elements in the collections.
     */
    public CollectionComparator() {
        this.comparator = Optional.empty();
    }

    /**
     * @param comparator the {@link Comparator} used to perform comparisons on the objects within the collections
     */
    public CollectionComparator(@Nonnull final Comparator<T> comparator) {
        this.comparator = Optional.of(Objects.requireNonNull(comparator));
    }

    /**
     * @return the {@link Comparator} used to perform comparisons on the objects within the collections, possibly empty
     * in which case the natural ordering will be used
     */
    @Nonnull
    public Optional<Comparator<T>> getComparator() {
        return this.comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(@Nullable final Collection<T> a, @Nullable final Collection<T> b) {
        if (a != null && b != null) {
            final CompareToBuilder cmp = new CompareToBuilder();
            final Iterator<T> iterA = a.iterator();
            final Iterator<T> iterB = b.iterator();

            while (cmp.toComparison() == 0 && iterA.hasNext() && iterB.hasNext()) {
                if (getComparator().isPresent()) {
                    cmp.append(iterA.next(), iterB.next(), getComparator().get());
                } else {
                    cmp.append(iterA.next(), iterB.next());
                }
            }

            if (cmp.toComparison() == 0) {
                if (iterA.hasNext()) {
                    return 1;
                } else if (iterB.hasNext()) {
                    return -1;
                }
            }

            return cmp.toComparison();
        } else if (a != null) {
            return 1;
        } else if (b != null) {
            return -1;
        } else {
            return 0;
        }
    }
}
