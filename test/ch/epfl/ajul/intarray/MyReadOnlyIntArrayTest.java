package ch.epfl.ajul.intarray;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyReadOnlyIntArrayTest {

    private ReadOnlyIntArray makeImmutable(int... values) {
        return ImmutableIntArray.copyOf(values);
    }

    private ReadOnlyIntArray makeMutable(int... values) {
        return MutableIntArray.wrapping(values);
    }

    @Test
    void immutableSizeIsCorrect() {
        assertEquals(4, makeImmutable(1, 2, 3, 4).size());
    }

    @Test
    void mutableSizeIsCorrect() {
        assertEquals(3, makeMutable(10, 20, 30).size());
    }

    @Test
    void immutableGetIsCorrect() {
        var arr = makeImmutable(1, 2, 3);
        assertEquals(2, arr.get(1));
    }

    @Test
    void mutableGetIsCorrect() {
        var arr = makeMutable(4, 5, 6);
        assertEquals(5, arr.get(1));
    }

    @Test
    void immutableGetThrowsWhenOutOfBounds() {
        var arr = makeImmutable(1, 2, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(3));
    }

    @Test
    void mutableGetThrowsWhenOutOfBounds() {
        var arr = makeMutable(1, 2, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(3));
    }

    @Test
    void immutableImmutableReturnsSelf() {
        var arr = ImmutableIntArray.copyOf(new int[]{1, 2});
        assertSame(arr, arr.immutable());
    }

    @Test
    void mutableImmutableReturnsCopyWithSameContent() {
        var arr = makeMutable(7, 8, 9);
        var immutable = arr.immutable();
        assertEquals(arr.size(), immutable.size());
        for (int i = 0; i < arr.size(); i += 1) {
            assertEquals(arr.get(i), immutable.get(i));
        }
    }

    @Test
    void toArrayReturnsCorrectContent() {
        var arr = makeImmutable(3, 1, 4, 1, 5);
        assertArrayEquals(new int[]{3, 1, 4, 1, 5}, arr.toArray());
    }

    @Test
    void toArrayReturnsCopy() {
        var arr = makeImmutable(1, 2, 3);
        var copy1 = arr.toArray();
        var copy2 = arr.toArray();
        assertNotSame(copy1, copy2);
    }
}
