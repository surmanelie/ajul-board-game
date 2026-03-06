package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de {@link PkPatterns}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class MyPkPatternsTest {

    @Test
    void emptyHasAllLinesEmptyAndCorrectString() {
        int p = PkPatterns.EMPTY;

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            assertEquals(0, PkPatterns.size(p, line));
            assertFalse(PkPatterns.isFull(p, line));
        }

        assertEquals("[., .., ..., ...., .....]", PkPatterns.toString(p));
        assertEquals(PkTileSet.EMPTY, PkPatterns.asPkTileSet(p));
    }

    @Test
    void withAddedTilesUpdatesSizeAndString() {
        int p = PkPatterns.EMPTY;

        // Ajoute 2 tuiles A sur la ligne 3 (cap 3)
        p = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_3, 2, TileKind.Colored.A);

        assertEquals(2, PkPatterns.size(p, TileDestination.Pattern.PATTERN_3));
        assertTrue(PkPatterns.canContain(p, TileDestination.Pattern.PATTERN_3, TileKind.Colored.A));
        assertFalse(PkPatterns.canContain(p, TileDestination.Pattern.PATTERN_3, TileKind.Colored.B));
        assertFalse(PkPatterns.isFull(p, TileDestination.Pattern.PATTERN_3));

        assertEquals("[., .., AA., ...., .....]", PkPatterns.toString(p));
    }

    @Test
    void isFullBecomesTrueExactlyAtCapacity() {
        int p = PkPatterns.EMPTY;

        p = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.C);
        assertTrue(PkPatterns.isFull(p, TileDestination.Pattern.PATTERN_1));

        p = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_5, 5, TileKind.Colored.E);
        assertTrue(PkPatterns.isFull(p, TileDestination.Pattern.PATTERN_5));
        assertEquals("[., .., ..., ...., EEEEE]", PkPatterns.toString(p));
    }

    @Test
    void withEmptyLineClearsCountAndColor() {
        int p = PkPatterns.EMPTY;
        p = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_4, 3, TileKind.Colored.B);

        p = PkPatterns.withEmptyLine(p, TileDestination.Pattern.PATTERN_4);

        assertEquals(0, PkPatterns.size(p, TileDestination.Pattern.PATTERN_4));
        assertEquals("[., .., ..., ...., .....]", PkPatterns.toString(p));
    }

    @Test
    void asPkTileSetAggregatesAllLines() {
        int p = PkPatterns.EMPTY;
        p = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.D);
        p = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_5, 1, TileKind.Colored.A);

        int expected = PkTileSet.union(
                PkTileSet.of(2, TileKind.Colored.D),
                PkTileSet.of(1, TileKind.Colored.A)
        );

        assertEquals(expected, PkPatterns.asPkTileSet(p));
    }

    @Test
    void withAddedTilesDoesNotAffectOtherLines() {
        int p = PkPatterns.EMPTY;

        int p2 = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_3, 2, TileKind.Colored.A);

        // Les autres lignes doivent rester vides
        assertEquals(0, PkPatterns.size(p2, TileDestination.Pattern.PATTERN_1));
        assertEquals(0, PkPatterns.size(p2, TileDestination.Pattern.PATTERN_2));
        assertEquals(0, PkPatterns.size(p2, TileDestination.Pattern.PATTERN_4));
        assertEquals(0, PkPatterns.size(p2, TileDestination.Pattern.PATTERN_5));
    }

    @Test
    void withEmptyLineDoesNotAffectOtherLines() {
        int p = PkPatterns.EMPTY;
        p = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.D);
        p = PkPatterns.withAddedTiles(p, TileDestination.Pattern.PATTERN_5, 1, TileKind.Colored.A);

        int p2 = PkPatterns.withEmptyLine(p, TileDestination.Pattern.PATTERN_2);

        assertEquals(0, PkPatterns.size(p2, TileDestination.Pattern.PATTERN_2));
        // La ligne 5 doit rester intacte
        assertEquals(1, PkPatterns.size(p2, TileDestination.Pattern.PATTERN_5));
        assertEquals("[., .., ..., ...., A....]", PkPatterns.toString(p2));
    }
}