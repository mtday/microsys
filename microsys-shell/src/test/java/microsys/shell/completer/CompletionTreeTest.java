package microsys.shell.completer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

/**
 * Perform testing of the {@link CompletionTree} class.
 */
public class CompletionTreeTest {
    @Test
    public void testEmptyConstructor() {
        // Used for building root nodes.
        final CompletionTree root = new CompletionTree();
        assertFalse(root.getCandidate().isPresent());
        assertFalse(root.getCompleter().isPresent());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    public void testCandidateConstructor() {
        final CompletionTree can = new CompletionTree("candidate");
        assertTrue(can.getCandidate().isPresent());
        assertEquals("candidate", can.getCandidate().get());
        assertFalse(can.getCompleter().isPresent());
        assertTrue(can.getChildren().isEmpty());
    }

    @Test
    public void testCandidateConstructorEmpty() {
        // An empty string is valid and will match any user input
        final CompletionTree can = new CompletionTree("");
        assertTrue(can.getCandidate().isPresent());
        assertEquals("", can.getCandidate().get());
        assertFalse(can.getCompleter().isPresent());
        assertTrue(can.getChildren().isEmpty());
    }

    @Test
    public void testCompleterConstructor() {
        final Completer c = new StringsCompleter("candidate");
        final CompletionTree comp = new CompletionTree(c);
        assertTrue(comp.getCandidate().isPresent());
        assertEquals("", comp.getCandidate().get());
        assertTrue(comp.getCompleter().isPresent());
        assertEquals(c, comp.getCompleter().get());
        assertTrue(comp.getChildren().isEmpty());
    }

    @Test
    public void testMergeSameCandidate() {
        final CompletionTree a1 = new CompletionTree("a").add(new CompletionTree("c1"));
        final CompletionTree a2 = new CompletionTree("a").add(new CompletionTree("c2"));
        a1.merge(a2);
        assertTrue(a1.getChild("c1").isPresent());
        assertTrue(a1.getChild("c2").isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDifferentCandidate() {
        final CompletionTree a = new CompletionTree("a");
        final CompletionTree b = new CompletionTree("b");

        a.merge(b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNoCandidate() {
        new CompletionTree("a").add(new CompletionTree());
    }

    @Test
    public void testAdd() {
        final CompletionTree a = new CompletionTree("a").add(new CompletionTree("b"));
        assertTrue(a.getCandidate().isPresent());
        assertEquals("a", a.getCandidate().get());
        assertFalse(a.getCompleter().isPresent());
        assertFalse(a.getChildren().isEmpty());
        assertEquals(1, a.getChildren().size());
        assertTrue(a.getChild("b").isPresent());
    }

    @Test
    public void testAddMultiple() {
        final CompletionTree a = new CompletionTree("a").add(new CompletionTree("b"), new CompletionTree("c"));
        assertTrue(a.getCandidate().isPresent());
        assertEquals("a", a.getCandidate().get());
        assertFalse(a.getCompleter().isPresent());
        assertFalse(a.getChildren().isEmpty());
        assertEquals(2, a.getChildren().size());
        assertTrue(a.getChild("b").isPresent());
        assertTrue(a.getChild("c").isPresent());
    }

    @Test
    public void testAddCollection() {
        final CompletionTree a = new CompletionTree("a").add(
                Arrays.asList(new CompletionTree("b"), new CompletionTree("c")));
        assertTrue(a.getCandidate().isPresent());
        assertEquals("a", a.getCandidate().get());
        assertFalse(a.getCompleter().isPresent());
        assertFalse(a.getChildren().isEmpty());
        assertEquals(2, a.getChildren().size());
        assertTrue(a.getChild("b").isPresent());
        assertTrue(a.getChild("c").isPresent());
    }

    @Test
    public void testGetChildrenMatchingEmpty() {
        final CompletionTree a = new CompletionTree("a").add(new CompletionTree("b"), new CompletionTree("c"));

        final List<CompletionTree> list = a.getChildrenMatching("");
        assertEquals(2, list.size());
        assertEquals("b", list.get(0).getCandidate().get());
        assertEquals("c", list.get(1).getCandidate().get());
    }

    @Test
    public void testGetChildrenMatchingPrefix() {
        final CompletionTree a = new CompletionTree("a").add(new CompletionTree("bcd"), new CompletionTree("cde"));

        final List<CompletionTree> list = a.getChildrenMatching("b");
        assertEquals(1, list.size());
        assertEquals("bcd", list.get(0).getCandidate().get());
    }

    @Test
    public void testGetChildrenMatchingComplete() {
        final CompletionTree a = new CompletionTree("a").add(new CompletionTree("bcd"), new CompletionTree("cde"));

        final List<CompletionTree> list = a.getChildrenMatching("bcd");
        assertEquals(1, list.size());
        assertEquals("bcd", list.get(0).getCandidate().get());
    }

    @Test
    public void testGetChildrenMatchingCompleter() {
        final StringsCompleter completer = new StringsCompleter("bcd");
        final CompletionTree a = new CompletionTree("a").add(new CompletionTree(completer));

        final List<CompletionTree> list = a.getChildrenMatching("whatever");
        assertEquals(1, list.size());
        assertTrue(list.get(0).getCandidate().isPresent());
        assertEquals("", list.get(0).getCandidate().get());
        assertTrue(list.get(0).getCompleter().isPresent());
        assertEquals(completer, list.get(0).getCompleter().get());
    }

    @Test
    public void testGetChildrenCandidates() {
        final CompletionTree a = new CompletionTree("a").add(new CompletionTree("bcd"), new CompletionTree("cde"));

        final SortedSet<String> candidates = a.getChildrenCandidates();
        assertEquals(2, candidates.size());
        assertEquals("[bcd, cde]", candidates.toString());
    }
}
