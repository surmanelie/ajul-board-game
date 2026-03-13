package ch.epfl.ajul.gamestate.packed;


import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class MyPkWallTest {

    private static int figure4Wall() {
        int wall = PkWall.EMPTY;
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.C);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.D);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.A);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_3, TileKind.Colored.E);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_3, TileKind.Colored.B);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_4, TileKind.Colored.A);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_5, TileKind.Colored.B);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_5, TileKind.Colored.C);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_5, TileKind.Colored.D);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_5, TileKind.Colored.E);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_5, TileKind.Colored.A);
        return wall;
    }

    private static int fullRowWall(TileDestination.Pattern line) {
        int wall = PkWall.EMPTY;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            wall = PkWall.withTileAt(wall, line, color);
        }
        return wall;
    }

    private static int fullColumnWall(int column) {
        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, line, PkWall.colorAt(line, column));
        }
        return wall;
    }

    private static int fullColorWall(TileKind.Colored color) {
        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, line, color);
        }
        return wall;
    }

    @Test
    void emptyConstantIsZero() {
        assertEquals(0, PkWall.EMPTY);
    }
    @Test
    void wallWidthIsFive() {
        assertEquals(5, PkWall.WALL_WIDTH);
    }
    @Test
    void wallHeightIsFive() {
        assertEquals(5, PkWall.WALL_HEIGHT);
    }
    @Test
    void indexOf_PATTERN_1_A() {
        assertEquals(0, PkWall.indexOf(TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }
    @Test
    void indexOf_PATTERN_1_B() {
        assertEquals(1, PkWall.indexOf(TileDestination.Pattern.PATTERN_1, TileKind.Colored.B));
    }
    @Test
    void indexOf_PATTERN_1_C() {
        assertEquals(2, PkWall.indexOf(TileDestination.Pattern.PATTERN_1, TileKind.Colored.C));
    }
    @Test
    void indexOf_PATTERN_1_D() {
        assertEquals(3, PkWall.indexOf(TileDestination.Pattern.PATTERN_1, TileKind.Colored.D));
    }
    @Test
    void indexOf_PATTERN_1_E() {
        assertEquals(4, PkWall.indexOf(TileDestination.Pattern.PATTERN_1, TileKind.Colored.E));
    }
    @Test
    void indexOf_PATTERN_2_A() {
        assertEquals(6, PkWall.indexOf(TileDestination.Pattern.PATTERN_2, TileKind.Colored.A));
    }
    @Test
    void indexOf_PATTERN_2_B() {
        assertEquals(7, PkWall.indexOf(TileDestination.Pattern.PATTERN_2, TileKind.Colored.B));
    }
    @Test
    void indexOf_PATTERN_2_C() {
        assertEquals(8, PkWall.indexOf(TileDestination.Pattern.PATTERN_2, TileKind.Colored.C));
    }
    @Test
    void indexOf_PATTERN_2_D() {
        assertEquals(9, PkWall.indexOf(TileDestination.Pattern.PATTERN_2, TileKind.Colored.D));
    }
    @Test
    void indexOf_PATTERN_2_E() {
        assertEquals(5, PkWall.indexOf(TileDestination.Pattern.PATTERN_2, TileKind.Colored.E));
    }
    @Test
    void indexOf_PATTERN_3_A() {
        assertEquals(12, PkWall.indexOf(TileDestination.Pattern.PATTERN_3, TileKind.Colored.A));
    }
    @Test
    void indexOf_PATTERN_3_B() {
        assertEquals(13, PkWall.indexOf(TileDestination.Pattern.PATTERN_3, TileKind.Colored.B));
    }
    @Test
    void indexOf_PATTERN_3_C() {
        assertEquals(14, PkWall.indexOf(TileDestination.Pattern.PATTERN_3, TileKind.Colored.C));
    }
    @Test
    void indexOf_PATTERN_3_D() {
        assertEquals(10, PkWall.indexOf(TileDestination.Pattern.PATTERN_3, TileKind.Colored.D));
    }
    @Test
    void indexOf_PATTERN_3_E() {
        assertEquals(11, PkWall.indexOf(TileDestination.Pattern.PATTERN_3, TileKind.Colored.E));
    }
    @Test
    void indexOf_PATTERN_4_A() {
        assertEquals(18, PkWall.indexOf(TileDestination.Pattern.PATTERN_4, TileKind.Colored.A));
    }
    @Test
    void indexOf_PATTERN_4_B() {
        assertEquals(19, PkWall.indexOf(TileDestination.Pattern.PATTERN_4, TileKind.Colored.B));
    }
    @Test
    void indexOf_PATTERN_4_C() {
        assertEquals(15, PkWall.indexOf(TileDestination.Pattern.PATTERN_4, TileKind.Colored.C));
    }
    @Test
    void indexOf_PATTERN_4_D() {
        assertEquals(16, PkWall.indexOf(TileDestination.Pattern.PATTERN_4, TileKind.Colored.D));
    }
    @Test
    void indexOf_PATTERN_4_E() {
        assertEquals(17, PkWall.indexOf(TileDestination.Pattern.PATTERN_4, TileKind.Colored.E));
    }
    @Test
    void indexOf_PATTERN_5_A() {
        assertEquals(24, PkWall.indexOf(TileDestination.Pattern.PATTERN_5, TileKind.Colored.A));
    }
    @Test
    void indexOf_PATTERN_5_B() {
        assertEquals(20, PkWall.indexOf(TileDestination.Pattern.PATTERN_5, TileKind.Colored.B));
    }
    @Test
    void indexOf_PATTERN_5_C() {
        assertEquals(21, PkWall.indexOf(TileDestination.Pattern.PATTERN_5, TileKind.Colored.C));
    }
    @Test
    void indexOf_PATTERN_5_D() {
        assertEquals(22, PkWall.indexOf(TileDestination.Pattern.PATTERN_5, TileKind.Colored.D));
    }
    @Test
    void indexOf_PATTERN_5_E() {
        assertEquals(23, PkWall.indexOf(TileDestination.Pattern.PATTERN_5, TileKind.Colored.E));
    }
    @Test
    void pattern1AColumn0() {
        assertEquals(0, PkWall.column(TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }
    @Test
    void pattern1EColumn4() {
        assertEquals(4, PkWall.column(TileDestination.Pattern.PATTERN_1, TileKind.Colored.E));
    }
    @Test
    void pattern2EColumn0() {
        assertEquals(0, PkWall.column(TileDestination.Pattern.PATTERN_2, TileKind.Colored.E));
    }
    @Test
    void pattern2AColumn1() {
        assertEquals(1, PkWall.column(TileDestination.Pattern.PATTERN_2, TileKind.Colored.A));
    }
    @Test
    void pattern3DColumn0() {
        assertEquals(0, PkWall.column(TileDestination.Pattern.PATTERN_3, TileKind.Colored.D));
    }
    @Test
    void pattern3CColumn4() {
        assertEquals(4, PkWall.column(TileDestination.Pattern.PATTERN_3, TileKind.Colored.C));
    }
    @Test
    void pattern4CColumn0() {
        assertEquals(0, PkWall.column(TileDestination.Pattern.PATTERN_4, TileKind.Colored.C));
    }
    @Test
    void pattern4BColumn4() {
        assertEquals(4, PkWall.column(TileDestination.Pattern.PATTERN_4, TileKind.Colored.B));
    }
    @Test
    void pattern5BColumn0() {
        assertEquals(0, PkWall.column(TileDestination.Pattern.PATTERN_5, TileKind.Colored.B));
    }
    @Test
    void pattern5AColumn4() {
        assertEquals(4, PkWall.column(TileDestination.Pattern.PATTERN_5, TileKind.Colored.A));
    }
    @Test
    void colorAt_PATTERN_1_column0() {
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 0));
    }
    @Test
    void colorAt_PATTERN_1_column1() {
        assertEquals(TileKind.Colored.B, PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 1));
    }
    @Test
    void colorAt_PATTERN_1_column2() {
        assertEquals(TileKind.Colored.C, PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 2));
    }
    @Test
    void colorAt_PATTERN_1_column3() {
        assertEquals(TileKind.Colored.D, PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 3));
    }
    @Test
    void colorAt_PATTERN_1_column4() {
        assertEquals(TileKind.Colored.E, PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 4));
    }
    @Test
    void colorAt_PATTERN_2_column0() {
        assertEquals(TileKind.Colored.E, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 0));
    }
    @Test
    void colorAt_PATTERN_2_column1() {
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 1));
    }
    @Test
    void colorAt_PATTERN_2_column2() {
        assertEquals(TileKind.Colored.B, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 2));
    }
    @Test
    void colorAt_PATTERN_2_column3() {
        assertEquals(TileKind.Colored.C, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 3));
    }
    @Test
    void colorAt_PATTERN_2_column4() {
        assertEquals(TileKind.Colored.D, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 4));
    }
    @Test
    void colorAt_PATTERN_3_column0() {
        assertEquals(TileKind.Colored.D, PkWall.colorAt(TileDestination.Pattern.PATTERN_3, 0));
    }
    @Test
    void colorAt_PATTERN_3_column1() {
        assertEquals(TileKind.Colored.E, PkWall.colorAt(TileDestination.Pattern.PATTERN_3, 1));
    }
    @Test
    void colorAt_PATTERN_3_column2() {
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_3, 2));
    }
    @Test
    void colorAt_PATTERN_3_column3() {
        assertEquals(TileKind.Colored.B, PkWall.colorAt(TileDestination.Pattern.PATTERN_3, 3));
    }
    @Test
    void colorAt_PATTERN_3_column4() {
        assertEquals(TileKind.Colored.C, PkWall.colorAt(TileDestination.Pattern.PATTERN_3, 4));
    }
    @Test
    void colorAt_PATTERN_4_column0() {
        assertEquals(TileKind.Colored.C, PkWall.colorAt(TileDestination.Pattern.PATTERN_4, 0));
    }
    @Test
    void colorAt_PATTERN_4_column1() {
        assertEquals(TileKind.Colored.D, PkWall.colorAt(TileDestination.Pattern.PATTERN_4, 1));
    }
    @Test
    void colorAt_PATTERN_4_column2() {
        assertEquals(TileKind.Colored.E, PkWall.colorAt(TileDestination.Pattern.PATTERN_4, 2));
    }
    @Test
    void colorAt_PATTERN_4_column3() {
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_4, 3));
    }
    @Test
    void colorAt_PATTERN_4_column4() {
        assertEquals(TileKind.Colored.B, PkWall.colorAt(TileDestination.Pattern.PATTERN_4, 4));
    }
    @Test
    void colorAt_PATTERN_5_column0() {
        assertEquals(TileKind.Colored.B, PkWall.colorAt(TileDestination.Pattern.PATTERN_5, 0));
    }
    @Test
    void colorAt_PATTERN_5_column1() {
        assertEquals(TileKind.Colored.C, PkWall.colorAt(TileDestination.Pattern.PATTERN_5, 1));
    }
    @Test
    void colorAt_PATTERN_5_column2() {
        assertEquals(TileKind.Colored.D, PkWall.colorAt(TileDestination.Pattern.PATTERN_5, 2));
    }
    @Test
    void colorAt_PATTERN_5_column3() {
        assertEquals(TileKind.Colored.E, PkWall.colorAt(TileDestination.Pattern.PATTERN_5, 3));
    }
    @Test
    void colorAt_PATTERN_5_column4() {
        assertEquals(TileKind.Colored.A, PkWall.colorAt(TileDestination.Pattern.PATTERN_5, 4));
    }
    @Test
    void emptyWallHasNoTileAtPattern1A() {
        assertFalse(PkWall.hasTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }
    @Test
    void withTileAtOnEmptyAddsTile() {
        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        assertTrue(PkWall.hasTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }
    @Test
    void withTileAtDoesNotAffectOtherCell() {
        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        assertFalse(PkWall.hasTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.B));
    }
    @Test
    void withTileAtAddsTwoDifferentTiles() {
        int wall = PkWall.EMPTY;
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.C);
        assertTrue(PkWall.hasTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
        assertTrue(PkWall.hasTileAt(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.C));
    }
    @Test
    void withTileAtSameTileTwiceIsIdempotent() {
        int wall1 = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_3, TileKind.Colored.B);
        int wall2 = PkWall.withTileAt(wall1, TileDestination.Pattern.PATTERN_3, TileKind.Colored.B);
        assertEquals(wall1, wall2);
    }
    @Test
    void figure4HGroupSizeOfPattern1AIs1() {
        assertEquals(1, PkWall.hGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }
    @Test
    void figure4VGroupSizeOfPattern1AIs1() {
        assertEquals(1, PkWall.vGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }
    @Test
    void figure4HGroupSizeOfPattern1CIs2() {
        assertEquals(2, PkWall.hGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_1, TileKind.Colored.C));
    }
    @Test
    void figure4VGroupSizeOfPattern2AIs2() {
        assertEquals(2, PkWall.vGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_2, TileKind.Colored.A));
    }
    @Test
    void figure4VGroupSizeOfPattern3BIs3() {
        assertEquals(3, PkWall.vGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_3, TileKind.Colored.B));
    }
    @Test
    void figure4HGroupSizeOfPattern5BIs5() {
        assertEquals(5, PkWall.hGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_5, TileKind.Colored.B));
    }
    @Test
    void figure4HGroupSizeOfPattern5EIs5() {
        assertEquals(5, PkWall.hGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_5, TileKind.Colored.E));
    }
    @Test
    void figure4VGroupSizeOfPattern5EIs3() {
        assertEquals(3, PkWall.vGroupSize(figure4Wall(), TileDestination.Pattern.PATTERN_5, TileKind.Colored.E));
    }
    @Test
    void hasFullRowFalseOnEmpty() {
        assertFalse(PkWall.hasFullRow(PkWall.EMPTY));
    }
    @Test
    void isRowFullFalseOnPartialRow() {
        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        assertFalse(PkWall.isRowFull(wall, TileDestination.Pattern.PATTERN_1));
    }
    @Test
    void isRowFullTrueOnCompletePattern5() {
        assertTrue(PkWall.isRowFull(fullRowWall(TileDestination.Pattern.PATTERN_5), TileDestination.Pattern.PATTERN_5));
    }
    @Test
    void hasFullRowTrueWhenOneRowFull() {
        assertTrue(PkWall.hasFullRow(fullRowWall(TileDestination.Pattern.PATTERN_3)));
    }
    @Test
    void isColumnFullTrueOnCompleteColumn2() {
        assertTrue(PkWall.isColumnFull(fullColumnWall(2), 2));
    }
    @Test
    void isColumnFullFalseOnPartialColumn2() {
        int wall = PkWall.EMPTY;
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 2));
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_2, PkWall.colorAt(TileDestination.Pattern.PATTERN_2, 2));
        assertFalse(PkWall.isColumnFull(wall, 2));
    }
    @Test
    void isColorFullTrueOnCompleteColorA() {
        assertTrue(PkWall.isColorFull(fullColorWall(TileKind.Colored.A), TileKind.Colored.A));
    }
    @Test
    void asPkTileSetEmptyWallIsEmpty() {
        int set = PkWall.asPkTileSet(PkWall.EMPTY);
        for (TileKind t : TileKind.ALL) assertEquals(0, PkTileSet.countOf(set, t));
    }
    @Test
    void asPkTileSetSingleAHasCountOneForA() {
        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        int set = PkWall.asPkTileSet(wall);
        assertEquals(1, PkTileSet.countOf(set, TileKind.A));
        assertEquals(0, PkTileSet.countOf(set, TileKind.B));
        assertEquals(0, PkTileSet.countOf(set, TileKind.C));
        assertEquals(0, PkTileSet.countOf(set, TileKind.D));
        assertEquals(0, PkTileSet.countOf(set, TileKind.E));
        assertEquals(1, PkTileSet.size(set));
    }
    @Test
    void asPkTileSetFigure4CountsColorsCorrectly() {
        int set = PkWall.asPkTileSet(figure4Wall());
        assertEquals(4, PkTileSet.countOf(set, TileKind.A));
        assertEquals(2, PkTileSet.countOf(set, TileKind.B));
        assertEquals(2, PkTileSet.countOf(set, TileKind.C));
        assertEquals(2, PkTileSet.countOf(set, TileKind.D));
        assertEquals(2, PkTileSet.countOf(set, TileKind.E));
        assertEquals(12, PkTileSet.size(set));
    }
    @Test
    void asPkTileSetFullColorAHasFiveAOnly() {
        int set = PkWall.asPkTileSet(fullColorWall(TileKind.Colored.A));
        assertEquals(5, PkTileSet.countOf(set, TileKind.A));
        assertEquals(0, PkTileSet.countOf(set, TileKind.B));
        assertEquals(0, PkTileSet.countOf(set, TileKind.C));
        assertEquals(0, PkTileSet.countOf(set, TileKind.D));
        assertEquals(0, PkTileSet.countOf(set, TileKind.E));
    }
    @Test
    void toStringEmptyWallMatchesSpec() {
        assertEquals("[abcde, eabcd, deabc, cdeab, bcdea]", PkWall.toString(PkWall.EMPTY));
    }
    @Test
    void toStringSingleTileAtPattern1A() {
        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        assertEquals("[Abcde, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wall));
    }
    @Test
    void toStringFigure4MatchesSpec() {
        assertEquals("[AbCDe, eAbcd, dEaBc, cdeAb, BCDEA]", PkWall.toString(figure4Wall()));
    }


    @Test
    void indexOfAndHasTileAtAgreeForAllCells() {
        // Vérifie la cohérence fondamentale : avecTileAt(pos) => hasTileAt(pos)
        // et que indexOf renvoie un index unique dans [0..24].
        boolean[] seen = new boolean[PkWall.WALL_WIDTH * PkWall.WALL_HEIGHT];

        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                int idx = PkWall.indexOf(line, color);
                assertTrue(0 <= idx && idx < seen.length);
                assertFalse(seen[idx], "indexOf doit être injective sur (line,color)");
                seen[idx] = true;

                int wall = PkWall.withTileAt(PkWall.EMPTY, line, color);
                assertTrue(PkWall.hasTileAt(wall, line, color));
            }
        }

        for (boolean b : seen) assertTrue(b, "toutes les cases 0..24 doivent être atteintes");
    }

    @Test
    void fullColumnWorksAlsoForBoundaryColumns0And4() {
        assertTrue(PkWall.isColumnFull(fullColumnWall(0), 0));
        assertTrue(PkWall.isColumnFull(fullColumnWall(4), 4));

        // Et un mur qui n'a qu'une case de colonne 0 ne doit pas être full
        int partial = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1,
                PkWall.colorAt(TileDestination.Pattern.PATTERN_1, 0));
        assertFalse(PkWall.isColumnFull(partial, 0));
    }

    @Test
    void isColorFullWorksAlsoForAnotherColorThanA() {
        assertTrue(PkWall.isColorFull(fullColorWall(TileKind.Colored.E), TileKind.Colored.E));

        int partial = PkWall.EMPTY;
        partial = PkWall.withTileAt(partial, TileDestination.Pattern.PATTERN_1, TileKind.Colored.E);
        partial = PkWall.withTileAt(partial, TileDestination.Pattern.PATTERN_2, TileKind.Colored.E);
        assertFalse(PkWall.isColorFull(partial, TileKind.Colored.E));
    }

    @Test
    void asPkTileSetForFullRowIsFiveTilesOneOfEachColor() {
        int wall = fullRowWall(TileDestination.Pattern.PATTERN_3); // une ligne complète = 5 tuiles

        int set = PkWall.asPkTileSet(wall);
        assertEquals(5, PkTileSet.size(set));

        for (var c : TileKind.Colored.ALL) {
            assertEquals(1, PkTileSet.countOf(set, c));
        }
        assertEquals(0, PkTileSet.countOf(set, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void groupSizesAreOneOnIsolatedSingleTile() {
        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_2, TileKind.Colored.C);

        assertEquals(1, PkWall.hGroupSize(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.C));
        assertEquals(1, PkWall.vGroupSize(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.C));
    }

    @Test
    void groupSizesOnFullRowAreFiveHorizontallyAndOneVertically() {
        var line = TileDestination.Pattern.PATTERN_4;
        int wall = fullRowWall(line);

        for (var color : TileKind.Colored.ALL) {
            assertEquals(5, PkWall.hGroupSize(wall, line, color));
            assertEquals(1, PkWall.vGroupSize(wall, line, color));
        }
    }

    @Test
    void toStringIsDeterministicAndHasCorrectFormat() {
        int wall = figure4Wall();
        String s1 = PkWall.toString(wall);
        String s2 = PkWall.toString(wall);

        assertEquals(s1, s2);

        // format minimal : [....., ....., ....., ....., .....] -> 5 lignes séparées par ", "
        assertTrue(s1.startsWith("["));
        assertTrue(s1.endsWith("]"));
        assertEquals(4, s1.chars().filter(ch -> ch == ',').count(), "doit contenir 4 virgules (5 segments)");
    }

    @Test
    void colorAtAndColumnAreInverseForAllLinesAndColors() {
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                int col = PkWall.column(line, color);
                assertEquals(color, PkWall.colorAt(line, col));
            }
        }
    }

    @Test
    void indexOfIsInjectiveOnAllCells() {
        var seen = new boolean[PkWall.WALL_WIDTH * PkWall.WALL_HEIGHT];
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                int idx = PkWall.indexOf(line, color);
                assertTrue(0 <= idx && idx < seen.length);
                assertFalse(seen[idx]);
                seen[idx] = true;
            }
        }
        for (boolean b : seen) assertTrue(b);
    }

    @Test
    void fullRowImpliesHorizontalGroupSizeFiveEverywhere() {
        for (var line : TileDestination.Pattern.ALL) {
            int wall = fullRowWall(line);
            for (var color : TileKind.Colored.ALL) {
                assertEquals(5, PkWall.hGroupSize(wall, line, color));
            }
        }
    }

    @Test
    void fullColumnImpliesVerticalGroupSizeFiveEverywhere() {
        for (int col = 0; col < PkWall.WALL_WIDTH; col++) {
            int wall = fullColumnWall(col);
            for (var line : TileDestination.Pattern.ALL) {
                var color = PkWall.colorAt(line, col);
                assertEquals(5, PkWall.vGroupSize(wall, line, color));
            }
        }
    }

    @Test
    void asPkTileSetCountsMatchHasTileAt() {
        int wall = figure4Wall(); // ou un autre mur non trivial
        int set = PkWall.asPkTileSet(wall);

        for (var color : TileKind.Colored.ALL) {
            int expected = 0;
            for (var line : TileDestination.Pattern.ALL) {
                if (PkWall.hasTileAt(wall, line, color)) expected++;
            }
            assertEquals(expected, PkTileSet.countOf(set, color));
        }
        // Marker doit toujours être 0 dans un mur
        assertEquals(0, PkTileSet.countOf(set, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void toStringHasCorrectFormatAndUppercaseCount() {
        int wall = figure4Wall();
        String s = PkWall.toString(wall);

        assertTrue(s.startsWith("[") && s.endsWith("]"));
        // 5 lignes séparées par ", "
        var inside = s.substring(1, s.length() - 1);
        var rows = inside.split(", ");
        assertEquals(5, rows.length);
        for (var r : rows) assertEquals(5, r.length());

        // nombre de majuscules = nombre de tuiles posées
        int uppercase = 0;
        for (char c : inside.toCharArray())
            if (c >= 'A' && c <= 'Z') uppercase++;

        // figure4Wall contient 12 tuiles
        assertEquals(12, uppercase);
    }

}
