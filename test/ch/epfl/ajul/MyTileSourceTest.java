package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static ch.epfl.ajul.TileSource.CenterArea;
import static ch.epfl.ajul.TileSource.Factory;
import static org.junit.jupiter.api.Assertions.*;

class MyTileSourceTest {

    @Test
    void centerAreaIndexIsZero() {
        assertEquals(0, TileSource.CENTER_AREA.index());
    }

    @Test
    void factoryIndicesStartAtOne() {
        assertEquals(1, TileSource.FACTORY_1.index());
    }

    @Test
    void factoryIndicesAreSequential() {
        assertEquals(1, Factory.FACTORY_1.index());
        assertEquals(2, Factory.FACTORY_2.index());
        assertEquals(3, Factory.FACTORY_3.index());
        assertEquals(4, Factory.FACTORY_4.index());
        assertEquals(5, Factory.FACTORY_5.index());
        assertEquals(6, Factory.FACTORY_6.index());
        assertEquals(7, Factory.FACTORY_7.index());
        assertEquals(8, Factory.FACTORY_8.index());
        assertEquals(9, Factory.FACTORY_9.index());
    }

    @Test
    void tilesPerFactoryIs4() {
        assertEquals(4, Factory.TILES_PER_FACTORY);
    }

    @Test
    void factoryCountIs9() {
        assertEquals(9, Factory.COUNT);
    }

    @Test
    void tileSourceCountIs10() {
        assertEquals(10, TileSource.COUNT);
    }

    @Test
    void allIndexAndGetAreInverse() {
        for (int i = 0; i < TileSource.COUNT; i += 1) {
            assertEquals(i, TileSource.ALL.get(i).index());
        }
    }

    @Test
    void factoryAllIndexAndGetAreInverse() {
        for (int i = 0; i < Factory.COUNT; i += 1) {
            assertEquals(i + 1, Factory.ALL.get(i).index());
        }
    }

    @Test
    void allFirstElementIsCenterArea() {
        assertSame(TileSource.CENTER_AREA, TileSource.ALL.get(0));
    }

    @Test
    void allContainsAllFactoriesAfterCenter() {
        for (int i = 0; i < Factory.COUNT; i += 1) {
            assertSame(Factory.ALL.get(i), TileSource.ALL.get(i + 1));
        }
    }

    @Test
    void centerAreaIsInstanceOfTileSource() {
        assertInstanceOf(TileSource.class, CenterArea.CENTER_AREA);
    }

    @Test
    void factoryAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection DataFlowIssue
            Factory.ALL.clear();
        });
    }

    @Test
    void factoryOrdinalIsIndexMinusOne() {
        for (var factory : Factory.values()) {
            assertEquals(factory.ordinal() + 1, factory.index());
        }
    }
}
