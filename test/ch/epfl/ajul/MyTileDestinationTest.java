package ch.epfl.ajul;

import ch.epfl.ajul.TileDestination.Floor;
import ch.epfl.ajul.TileDestination.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyTileDestinationTest {

    @Test
    void patternIndicesAreOrdinals() {
        for (var pattern : Pattern.values()) {
            assertEquals(pattern.ordinal(), pattern.index());
        }
    }

    @Test
    void patternIndividualIndices() {
        assertEquals(0, TileDestination.PATTERN_1.index());
        assertEquals(1, TileDestination.PATTERN_2.index());
        assertEquals(2, TileDestination.PATTERN_3.index());
        assertEquals(3, TileDestination.PATTERN_4.index());
        assertEquals(4, TileDestination.PATTERN_5.index());
    }

    @Test
    void floorIndexIs5() {
        assertEquals(5, TileDestination.FLOOR.index());
    }

    @Test
    void patternCapacityIsOrdinalPlusOne() {
        for (var pattern : Pattern.values()) {
            assertEquals(pattern.ordinal() + 1, pattern.capacity());
        }
    }

    @Test
    void patternIndividualCapacities() {
        assertEquals(1, TileDestination.PATTERN_1.capacity());
        assertEquals(2, TileDestination.PATTERN_2.capacity());
        assertEquals(3, TileDestination.PATTERN_3.capacity());
        assertEquals(4, TileDestination.PATTERN_4.capacity());
        assertEquals(5, TileDestination.PATTERN_5.capacity());
    }

    @Test
    void floorCapacityIs7() {
        assertEquals(7, TileDestination.FLOOR.capacity());
    }

    @Test
    void patternCountIs5() {
        assertEquals(5, Pattern.COUNT);
    }

    @Test
    void patternAllHasFiveElements() {
        assertEquals(5, Pattern.ALL.size());
    }

    @Test
    void tileDestinationCountIs6() {
        assertEquals(6, TileDestination.COUNT);
    }

    @Test
    void tileDestinationAllHasSixElements() {
        assertEquals(6, TileDestination.ALL.size());
    }

    @Test
    void floorIsLastInAll() {
        assertSame(TileDestination.FLOOR, TileDestination.ALL.get(5));
    }

    @Test
    void patternAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection DataFlowIssue
            Pattern.ALL.clear();
        });
    }

    @Test
    void tileDestinationAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection DataFlowIssue
            TileDestination.ALL.clear();
        });
    }

    @Test
    void allIndexAndGetAreInverse() {
        for (int i = 0; i < TileDestination.COUNT; i += 1) {
            assertEquals(i, TileDestination.ALL.get(i).index());
        }
    }

    @Test
    void patternIndexAndAllGetAreInverse() {
        for (int i = 0; i < Pattern.COUNT; i += 1) {
            assertEquals(i, Pattern.ALL.get(i).index());
        }
    }

    @Test
    void patternIsInstanceOfTileDestination() {
        for (var pattern : Pattern.values()) {
            assertInstanceOf(TileDestination.class, pattern);
        }
    }

    @Test
    void floorIsInstanceOfTileDestination() {
        assertInstanceOf(TileDestination.class, TileDestination.FLOOR);
    }

    @Test
    void floorCountIs1() {
        assertEquals(1, Floor.COUNT);
    }
}
