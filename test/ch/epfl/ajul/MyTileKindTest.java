package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.TileKind.Colored;
import static ch.epfl.ajul.TileKind.FirstPlayerMarker;
import static org.junit.jupiter.api.Assertions.*;

class MyTileKindTest {

    @Test
    void coloredIndexMatchesOrdinal() {
        for (var color : Colored.values()) {
            assertEquals(color.ordinal(), color.index());
        }
    }

    @Test
    void coloredIndividualIndices() {
        assertEquals(0, Colored.A.index());
        assertEquals(1, Colored.B.index());
        assertEquals(2, Colored.C.index());
        assertEquals(3, Colored.D.index());
        assertEquals(4, Colored.E.index());
    }

    @Test
    void firstPlayerMarkerIndexIs5() {
        assertEquals(5, FirstPlayerMarker.FIRST_PLAYER_MARKER.index());
    }

    @Test
    void coloredTilesCountIs20ForAllColors() {
        for (var color : Colored.values()) {
            assertEquals(20, color.tilesCount());
        }
    }

    @Test
    void firstPlayerMarkerTilesCountIs1() {
        assertEquals(1, FirstPlayerMarker.FIRST_PLAYER_MARKER.tilesCount());
    }

    @Test
    void coloredCountIs5() {
        assertEquals(5, Colored.COUNT);
    }

    @Test
    void tileKindCountIs6() {
        assertEquals(6, TileKind.COUNT);
    }

    @Test
    void coloredAllHasFiveElements() {
        assertEquals(5, Colored.ALL.size());
    }

    @Test
    void tileKindAllHasSixElements() {
        assertEquals(6, TileKind.ALL.size());
    }

    @Test
    void tileKindAllEndsWithFirstPlayerMarker() {
        assertSame(TileKind.FIRST_PLAYER_MARKER, TileKind.ALL.get(TileKind.COUNT - 1));
    }

    @Test
    void coloredAllContainsAllColorsInOrder() {
        var all = Colored.ALL;
        assertSame(Colored.A, all.get(0));
        assertSame(Colored.B, all.get(1));
        assertSame(Colored.C, all.get(2));
        assertSame(Colored.D, all.get(3));
        assertSame(Colored.E, all.get(4));
    }

    @Test
    void tileKindAllIndexAndGetAreInverse() {
        for (var i = 0; i < TileKind.COUNT; i += 1) {
            assertEquals(i, TileKind.ALL.get(i).index());
        }
    }

    @Test
    void coloredIsInstanceOfTileKind() {
        for (var color : Colored.values()) {
            assertInstanceOf(TileKind.class, color);
        }
    }

    @Test
    void firstPlayerMarkerIsInstanceOfTileKind() {
        assertInstanceOf(TileKind.class, TileKind.FIRST_PLAYER_MARKER);
    }

    @Test
    void shuffleEmptyArrayDoesNotThrow() {
        var rng = RandomGeneratorFactory.getDefault().create(0);
        assertDoesNotThrow(() -> Colored.shuffle(new Colored[0], rng));
    }

    @Test
    void shuffleSingleElementArrayIsUnchanged() {
        var rng = RandomGeneratorFactory.getDefault().create(0);
        var array = new Colored[]{Colored.C};
        Colored.shuffle(array, rng);
        assertSame(Colored.C, array[0]);
    }

    @Test
    void shufflePreservesAllElements() {
        var rng = RandomGeneratorFactory.getDefault().create(123);
        var array = Colored.values();
        var before = array.clone();
        Colored.shuffle(array, rng);
        Arrays.sort(array);
        Arrays.sort(before);
        assertArrayEquals(before, array);
    }

    @Test
    void shuffleWithSameSeedProducesSameResult() {
        var array1 = Colored.values();
        var array2 = Colored.values();
        var rng1 = RandomGeneratorFactory.getDefault().create(999);
        var rng2 = RandomGeneratorFactory.getDefault().create(999);
        Colored.shuffle(array1, rng1);
        Colored.shuffle(array2, rng2);
        assertArrayEquals(array1, array2);
    }
}
