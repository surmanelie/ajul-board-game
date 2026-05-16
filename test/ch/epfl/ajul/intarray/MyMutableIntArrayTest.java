package ch.epfl.ajul.intarray;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyMutableIntArrayTest {

    @Test
    void wrappingSharesUnderlyingArray() {
        var source = new int[]{1, 2, 3};
        var arr = MutableIntArray.wrapping(source);
        source[0] = 99;
        assertEquals(99, arr.get(0), "wrapping must not clone the array");
    }

    @Test
    void sizeIsCorrect() {
        var arr = MutableIntArray.wrapping(new int[]{10, 20, 30});
        assertEquals(3, arr.size());
    }

    @Test
    void sizeOfEmptyArrayIsZero() {
        var arr = MutableIntArray.wrapping(new int[0]);
        assertEquals(0, arr.size());
    }

    @Test
    void getReturnsCorrectValues() {
        var arr = MutableIntArray.wrapping(new int[]{7, 8, 9});
        assertEquals(7, arr.get(0));
        assertEquals(8, arr.get(1));
        assertEquals(9, arr.get(2));
    }

    @Test
    void getThrowsOnNegativeIndex() {
        var arr = MutableIntArray.wrapping(new int[]{1, 2, 3});
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(-1));
    }

    @Test
    void getThrowsOnIndexEqualToSize() {
        var arr = MutableIntArray.wrapping(new int[]{1, 2, 3});
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(3));
    }

    @Test
    void immutableReturnsCopyNotSelf() {
        var arr = MutableIntArray.wrapping(new int[]{1, 2, 3});
        assertNotSame(arr, arr.immutable());
    }

    @Test
    void immutableIsImmutable() {
        var source = new int[]{1, 2, 3};
        var arr = MutableIntArray.wrapping(source);
        var immutable = arr.immutable();
        source[0] = 99;
        assertEquals(1, immutable.get(0), "immutable() must snapshot the current values");
    }

    @Test
    void immutableReturnsSelf() {
        var source = new int[]{1, 2, 3};
        var arr = MutableIntArray.wrapping(source);
        var immutable = arr.immutable();
        assertSame(immutable, immutable.immutable());
    }

    @Test
    void toArrayReturnsCopy() {
        var source = new int[]{1, 2, 3};
        var arr = MutableIntArray.wrapping(source);
        var copy = arr.toArray();
        copy[0] = 99;
        assertEquals(1, arr.get(0), "toArray must return a defensive copy");
    }

    @Test
    void toArrayHasCorrectContent() {
        var source = new int[]{5, 10, 15};
        var arr = MutableIntArray.wrapping(source);
        assertArrayEquals(source, arr.toArray());
    }

    @Test
    void implementsReadOnlyIntArray() {
        var arr = MutableIntArray.wrapping(new int[]{1});
        assertInstanceOf(ReadOnlyIntArray.class, arr);
    }
}
