package microsys.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Perform testing of the {@link CollectionComparatorTest} class.
 */
public class CollectionComparatorTest {
    @Test
    public void testCompareBothNull() {
        final Collection<Integer> a = null;
        final Collection<Integer> b = null;

        Assert.assertEquals(0, new CollectionComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareFirstNull() {
        final Collection<Integer> a = null;
        final Collection<Integer> b = Collections.singleton(1);

        Assert.assertEquals(-1, new CollectionComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareSecondNull() {
        final Collection<Integer> a = Collections.singleton(1);
        final Collection<Integer> b = null;

        Assert.assertEquals(1, new CollectionComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareBothWithValuesSameLength() {
        final Collection<Integer> a = Collections.singleton(1);
        final Collection<Integer> b = Collections.singleton(1);

        Assert.assertEquals(0, new CollectionComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareBothWithValuesFirstLonger() {
        final Collection<Integer> a = Arrays.asList(1, 2);
        final Collection<Integer> b = Collections.singleton(1);

        Assert.assertEquals(1, new CollectionComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareBothWithValuesSecondLonger() {
        final Collection<Integer> a = Collections.singleton(1);
        final Collection<Integer> b = Arrays.asList(1, 2);

        Assert.assertEquals(-1, new CollectionComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareBothWithSameLengthDifferentValues() {
        final Collection<Integer> a = Arrays.asList(1, 2);
        final Collection<Integer> b = Arrays.asList(1, 3);

        Assert.assertEquals(-1, new CollectionComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareBothCustomComparator() {
        final Collection<Optional<Integer>> a = Collections.singleton(Optional.of(1));
        final Collection<Optional<Integer>> b = Arrays.asList(Optional.of(1), Optional.empty());

        Assert.assertEquals(-1, new CollectionComparator<Optional<Integer>>(new OptionalComparator<>()).compare(a, b));
    }
}
