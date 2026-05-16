package ch.epfl.ajul.intarray;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyImmutableIntArrayTest {

    @Test
    void copyOfClonesInputArray() {
        var source = new int[]{1, 2, 3};
        var arr = ImmutableIntArray.copyOf(source);
        source[0] = 99;
        assertEquals(1, arr.get(0), "External modification must not affect the immutable array");
    }

    @Test
    void immutableReturnsSelf() {
        var arr = ImmutableIntArray.copyOf(new int[]{1, 2, 3});
        assertSame(arr, arr.immutable(), "immutable() must return this");
    }

    @Test
    void sizeIsCorrect() {
        var arr = ImmutableIntArray.copyOf(new int[]{10, 20, 30});
        assertEquals(3, arr.size());
    }

    @Test
    void sizeOfEmptyArrayIsZero() {
        var arr = ImmutableIntArray.copyOf(new int[0]);
        assertEquals(0, arr.size());
    }

    @Test
    void getReturnsCorrectValues() {
        var arr = ImmutableIntArray.copyOf(new int[]{7, 8, 9});
        assertEquals(7, arr.get(0));
        assertEquals(8, arr.get(1));
        assertEquals(9, arr.get(2));
    }

    @Test
    void getThrowsOnNegativeIndex() {
        var arr = ImmutableIntArray.copyOf(new int[]{1, 2, 3});
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(-1));
    }

    @Test
    void getThrowsOnIndexEqualToSize() {
        var arr = ImmutableIntArray.copyOf(new int[]{1, 2, 3});
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(3));
    }

    @Test
    void getThrowsOnIndexGreaterThanSize() {
        var arr = ImmutableIntArray.copyOf(new int[]{1, 2, 3});
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(100));
    }

    @Test
    void toArrayReturnsCopy() {
        var arr = ImmutableIntArray.copyOf(new int[]{1, 2, 3});
        var copy = arr.toArray();
        copy[0] = 99;
        assertEquals(1, arr.get(0), "toArray must return a defensive copy");
    }

    @Test
    void toArrayHasCorrectContent() {
        var source = new int[]{5, 10, 15};
        var arr = ImmutableIntArray.copyOf(source);
        var result = arr.toArray();
        assertArrayEquals(source, result);
    }

    @Test
    void implementsReadOnlyIntArray() {
        var arr = ImmutableIntArray.copyOf(new int[]{1});
        assertInstanceOf(ReadOnlyIntArray.class, arr);
    }
}
