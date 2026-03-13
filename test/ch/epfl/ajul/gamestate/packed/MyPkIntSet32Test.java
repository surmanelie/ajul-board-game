package ch.epfl.ajul.gamestate.packed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyPkIntSet32Test {

    @Test
    void emptyContains0False() {
        assertFalse(PkIntSet32.contains(PkIntSet32.EMPTY, 0));
    }
    @Test
    void emptyContains31False() {
        assertFalse(PkIntSet32.contains(PkIntSet32.EMPTY, 31));
    }
    @Test
    void add0ToEmpty() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 0);
        assertTrue(PkIntSet32.contains(s, 0));
    }
    @Test
    void add31ToEmpty() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 31);
        assertTrue(PkIntSet32.contains(s, 31));
    }
    @Test
    void addMiddleToEmpty() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 15);
        assertTrue(PkIntSet32.contains(s, 15));
    }
    @Test
    void addDuplicateIsIdempotent() {
        int s1 = PkIntSet32.add(PkIntSet32.EMPTY, 5);
        int s2 = PkIntSet32.add(s1, 5);
        assertEquals(s1, s2);
    }
    @Test
    void removeAbsentDoesNothing() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 8);
        assertEquals(s, PkIntSet32.remove(s, 7));
    }
    @Test
    void removePresentRemoves() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 8);
        s = PkIntSet32.remove(s, 8);
        assertFalse(PkIntSet32.contains(s, 8));
    }
    @Test
    void addThenRemoveSameGivesEmpty() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 12);
        s = PkIntSet32.remove(s, 12);
        assertEquals(PkIntSet32.EMPTY, s);
    }
    @Test
    void containsAllEmptyEmpty() {
        assertTrue(PkIntSet32.containsAll(PkIntSet32.EMPTY, PkIntSet32.EMPTY));
    }
    @Test
    void containsAllSupersetSimple() {
        int a = PkIntSet32.EMPTY;
        a = PkIntSet32.add(a, 1);
        a = PkIntSet32.add(a, 3);
        int b = PkIntSet32.add(PkIntSet32.EMPTY, 1);
        assertTrue(PkIntSet32.containsAll(a, b));
    }
    @Test
    void containsAllMissingElementFalse() {
        int a = PkIntSet32.add(PkIntSet32.EMPTY, 1);
        int b = PkIntSet32.add(PkIntSet32.EMPTY, 2);
        assertFalse(PkIntSet32.containsAll(a, b));
    }
    @Test
    void containsAllEqualSets() {
        int a = PkIntSet32.EMPTY;
        a = PkIntSet32.add(a, 2);
        a = PkIntSet32.add(a, 9);
        assertTrue(PkIntSet32.containsAll(a, a));
    }
    @Test
    void containsAllWithEmptySubset() {
        int a = PkIntSet32.add(PkIntSet32.EMPTY, 9);
        assertTrue(PkIntSet32.containsAll(a, PkIntSet32.EMPTY));
    }
    @Test
    void addSeveralThenContainsEach() {
        int s = PkIntSet32.EMPTY;
        for (int i : new int[]{0, 4, 9, 17, 31}) s = PkIntSet32.add(s, i);
        for (int i : new int[]{0, 4, 9, 17, 31}) assertTrue(PkIntSet32.contains(s, i));
    }
    @Test
    void removeLowLeavesOthers() {
        int s = PkIntSet32.EMPTY;
        for (int i : new int[]{0, 1, 2}) s = PkIntSet32.add(s, i);
        s = PkIntSet32.remove(s, 0);
        assertFalse(PkIntSet32.contains(s, 0));
        assertTrue(PkIntSet32.contains(s, 1));
        assertTrue(PkIntSet32.contains(s, 2));
    }
    @Test
    void removeHighLeavesOthers() {
        int s = PkIntSet32.EMPTY;
        for (int i : new int[]{29, 30, 31}) s = PkIntSet32.add(s, i);
        s = PkIntSet32.remove(s, 31);
        assertFalse(PkIntSet32.contains(s, 31));
        assertTrue(PkIntSet32.contains(s, 29));
        assertTrue(PkIntSet32.contains(s, 30));
    }
    @Test
    void addAllBitsThenContainsAll() {
        int s = PkIntSet32.EMPTY;
        for (int i = 0; i < 32; i++) s = PkIntSet32.add(s, i);
        for (int i = 0; i < 32; i++) assertTrue(PkIntSet32.contains(s, i));
    }
    @Test
    void fullContainsPartial() {
        int full = -1;
        int partial = PkIntSet32.EMPTY;
        for (int i : new int[]{0, 7, 19, 31}) partial = PkIntSet32.add(partial, i);
        assertTrue(PkIntSet32.containsAll(full, partial));
    }
    @Test
    void partialDoesNotContainFull() {
        int partial = PkIntSet32.EMPTY;
        for (int i : new int[]{0, 7, 19, 31}) partial = PkIntSet32.add(partial, i);
        assertFalse(PkIntSet32.containsAll(partial, -1));
    }
    @Test
    void alternatingBitsContainExpected() {
        int s = 0x55555555;
        assertTrue(PkIntSet32.contains(s, 0));
        assertFalse(PkIntSet32.contains(s, 1));
        assertTrue(PkIntSet32.contains(s, 30));
        assertFalse(PkIntSet32.contains(s, 31));
    }
    @Test
    void addToAlternatingSetsMissingBit() {
        int s = 0x55555555;
        s = PkIntSet32.add(s, 1);
        assertTrue(PkIntSet32.contains(s, 1));
    }
    @Test
    void removeFromAlternatingClearsBit() {
        int s = 0x55555555;
        s = PkIntSet32.remove(s, 0);
        assertFalse(PkIntSet32.contains(s, 0));
        assertTrue(PkIntSet32.contains(s, 2));
    }
    @Test
    void addToEmptyHasExpectedRawBitPattern() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 5);
        assertEquals(1 << 5, s);
    }
    @Test
    void removeSingleBitReturnsEmpty() {
        int s = 1 << 5;
        assertEquals(PkIntSet32.EMPTY, PkIntSet32.remove(s, 5));
    }
    @Test
    void containsAfterRemoveFalse() {
        int s = PkIntSet32.add(PkIntSet32.EMPTY, 20);
        s = PkIntSet32.remove(s, 20);
        assertFalse(PkIntSet32.contains(s, 20));
    }
    @Test
    void fullContains0True() {
        assertTrue(PkIntSet32.contains(-1, 0));
    }
    @Test
    void fullContains31True() {
        assertTrue(PkIntSet32.contains(-1, 31));
    }
    @Test
    void containsAllDisjointFalse() {
        int a = PkIntSet32.EMPTY;
        a = PkIntSet32.add(a, 0);
        a = PkIntSet32.add(a, 2);
        int b = PkIntSet32.EMPTY;
        b = PkIntSet32.add(b, 1);
        b = PkIntSet32.add(b, 3);
        assertFalse(PkIntSet32.containsAll(a, b));
    }
    @Test
    void emptyRightHandSideAlwaysContained() {
        int a = PkIntSet32.EMPTY;
        a = PkIntSet32.add(a, 13);
        assertTrue(PkIntSet32.containsAll(a, PkIntSet32.EMPTY));
    }
    @Test
    void emptyLeftHandSideDoesNotContainNonEmpty() {
        int b = PkIntSet32.add(PkIntSet32.EMPTY, 13);
        assertFalse(PkIntSet32.containsAll(PkIntSet32.EMPTY, b));
    }
    @Test
    void removeAllOneByOneGivesEmpty() {
        int s = PkIntSet32.EMPTY;
        for (int i : new int[]{3, 8, 13, 18, 23}) s = PkIntSet32.add(s, i);
        for (int i : new int[]{3, 8, 13, 18, 23}) s = PkIntSet32.remove(s, i);
        assertEquals(PkIntSet32.EMPTY, s);
    }
    @Test
    void addOrderIndependent() {
        int a = PkIntSet32.EMPTY;
        a = PkIntSet32.add(a, 2);
        a = PkIntSet32.add(a, 9);
        int b = PkIntSet32.EMPTY;
        b = PkIntSet32.add(b, 9);
        b = PkIntSet32.add(b, 2);
        assertEquals(a, b);
    }
    @Test
    void removeOrderIndependent() {
        int a = -1;
        int b = -1;
        a = PkIntSet32.remove(a, 3);
        a = PkIntSet32.remove(a, 7);
        b = PkIntSet32.remove(b, 7);
        b = PkIntSet32.remove(b, 3);
        assertEquals(a, b);
    }
    @Test
    void addAndRemoveBoundaryBits() {
        int s = PkIntSet32.EMPTY;
        s = PkIntSet32.add(s, 0);
        s = PkIntSet32.add(s, 31);
        assertTrue(PkIntSet32.contains(s, 0));
        assertTrue(PkIntSet32.contains(s, 31));
        s = PkIntSet32.remove(s, 0);
        assertFalse(PkIntSet32.contains(s, 0));
        assertTrue(PkIntSet32.contains(s, 31));
    }


    @Test
    void containsAllBreaksWhenRemovingRequiredElement() {
        int a = PkIntSet32.EMPTY;
        a = PkIntSet32.add(a, 5);
        a = PkIntSet32.add(a, 9);

        int b = PkIntSet32.EMPTY;
        b = PkIntSet32.add(b, 9);

        assertTrue(PkIntSet32.containsAll(a, b));

        a = PkIntSet32.remove(a, 9);
        assertFalse(PkIntSet32.containsAll(a, b));
    }


    @Test
    void containsAllHoldsForConstructedSubset() {
        int a = 0;
        for (int i : new int[]{1, 3, 7, 12, 31}) a = PkIntSet32.add(a, i);

        int b = 0;
        for (int i : new int[]{3, 12}) b = PkIntSet32.add(b, i);

        assertTrue(PkIntSet32.containsAll(a, b));
    }

    @Test
    void containsAllIsReflexive() {
        int a = 0;
        for (int i : new int[]{0, 2, 5, 10, 31}) a = PkIntSet32.add(a, i);
        assertTrue(PkIntSet32.containsAll(a, a));
    }

    @Test
    void containsAllIsTransitive() {
        int a = 0;
        for (int i : new int[]{1, 3, 7, 12, 31}) a = PkIntSet32.add(a, i);

        int b = 0;
        for (int i : new int[]{3, 12, 31}) b = PkIntSet32.add(b, i);

        int c = 0;
        for (int i : new int[]{12}) c = PkIntSet32.add(c, i);

        assertTrue(PkIntSet32.containsAll(a, b));
        assertTrue(PkIntSet32.containsAll(b, c));
        assertTrue(PkIntSet32.containsAll(a, c));
    }


    @Test
    void addOnlyForcesThatBitToOne() {
        int original = 0x12345678;
        int i = 10;

        int s = PkIntSet32.add(original, i);

        for (int b = 0; b < 32; b++) {
            boolean expected = (b == i) || PkIntSet32.contains(original, b);
            assertEquals(expected, PkIntSet32.contains(s, b));
        }
    }

    @Test
    void removeOnlyForcesThatBitToZero() {
        int original = 0x12345678;
        int i = 10;

        int s = PkIntSet32.remove(original, i);

        for (int b = 0; b < 32; b++) {
            boolean expected = (b != i) && PkIntSet32.contains(original, b);
            assertEquals(expected, PkIntSet32.contains(s, b));
        }
    }

}
