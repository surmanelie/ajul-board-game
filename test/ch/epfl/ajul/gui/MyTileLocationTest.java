package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyTileLocationTest {

    // ─── OffBoard ────────────────────────────────────────────────────────────

    @Test
    void offBoardKindIsStored() {
        var loc = new TileLocation.OffBoard(TileKind.A, 0);
        assertEquals(TileKind.A, loc.kind());
    }

    @Test
    void offBoardIndexIsStored() {
        var loc = new TileLocation.OffBoard(TileKind.B, 7);
        assertEquals(7, loc.index());
    }

    @Test
    void offBoardWorksWithFirstPlayerMarker() {
        var loc = new TileLocation.OffBoard(TileKind.FIRST_PLAYER_MARKER, 0);
        assertEquals(TileKind.FIRST_PLAYER_MARKER, loc.kind());
    }

    @Test
    void offBoardImplementsTileLocation() {
        assertInstanceOf(TileLocation.class, new TileLocation.OffBoard(TileKind.C, 0));
    }

    @Test
    void offBoardEqualityByComponents() {
        var a = new TileLocation.OffBoard(TileKind.A, 3);
        var b = new TileLocation.OffBoard(TileKind.A, 3);
        assertEquals(a, b);
    }

    @Test
    void offBoardInequalityDifferentKind() {
        var a = new TileLocation.OffBoard(TileKind.A, 0);
        var b = new TileLocation.OffBoard(TileKind.B, 0);
        assertNotEquals(a, b);
    }

    @Test
    void offBoardInequalityDifferentIndex() {
        var a = new TileLocation.OffBoard(TileKind.A, 0);
        var b = new TileLocation.OffBoard(TileKind.A, 1);
        assertNotEquals(a, b);
    }

    // ─── OnSource ────────────────────────────────────────────────────────────

    @Test
    void onSourceSourceIsStored() {
        var loc = new TileLocation.OnSource(TileSource.CENTER_AREA, 0);
        assertEquals(TileSource.CENTER_AREA, loc.source());
    }

    @Test
    void onSourceIndexIsStored() {
        var loc = new TileLocation.OnSource(TileSource.FACTORY_3, 2);
        assertEquals(2, loc.index());
    }

    @Test
    void onSourceImplementsTileLocation() {
        assertInstanceOf(TileLocation.class, new TileLocation.OnSource(TileSource.FACTORY_1, 0));
    }

    @Test
    void onSourceEqualityByComponents() {
        var a = new TileLocation.OnSource(TileSource.FACTORY_2, 1);
        var b = new TileLocation.OnSource(TileSource.FACTORY_2, 1);
        assertEquals(a, b);
    }

    @Test
    void onSourceInequalityDifferentSource() {
        var a = new TileLocation.OnSource(TileSource.FACTORY_1, 0);
        var b = new TileLocation.OnSource(TileSource.FACTORY_2, 0);
        assertNotEquals(a, b);
    }

    @Test
    void onSourceInequalityDifferentIndex() {
        var a = new TileLocation.OnSource(TileSource.CENTER_AREA, 0);
        var b = new TileLocation.OnSource(TileSource.CENTER_AREA, 1);
        assertNotEquals(a, b);
    }

    // ─── OnPattern ───────────────────────────────────────────────────────────

    @Test
    void onPatternPlayerIdIsStored() {
        var loc = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_2, 0);
        assertEquals(PlayerId.P1, loc.playerId());
    }

    @Test
    void onPatternLineIsStored() {
        var loc = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_4, 1);
        assertEquals(TileDestination.Pattern.PATTERN_4, loc.line());
    }

    @Test
    void onPatternIndexIsStored() {
        var loc = new TileLocation.OnPattern(PlayerId.P2, TileDestination.Pattern.PATTERN_3, 2);
        assertEquals(2, loc.index());
    }

    @Test
    void onPatternImplementsTileLocation() {
        assertInstanceOf(TileLocation.class,
                new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_1, 0));
    }

    @Test
    void onPatternEqualityByComponents() {
        var a = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_5, 3);
        var b = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_5, 3);
        assertEquals(a, b);
    }

    @Test
    void onPatternInequalityDifferentPlayer() {
        var a = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_1, 0);
        var b = new TileLocation.OnPattern(PlayerId.P2, TileDestination.Pattern.PATTERN_1, 0);
        assertNotEquals(a, b);
    }

    @Test
    void onPatternInequalityDifferentLine() {
        var a = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_1, 0);
        var b = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_2, 0);
        assertNotEquals(a, b);
    }

    // ─── OnWall ──────────────────────────────────────────────────────────────

    @Test
    void onWallPlayerIdIsStored() {
        var loc = new TileLocation.OnWall(PlayerId.P2, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        assertEquals(PlayerId.P2, loc.playerId());
    }

    @Test
    void onWallLineIsStored() {
        var loc = new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_4, TileKind.Colored.B);
        assertEquals(TileDestination.Pattern.PATTERN_4, loc.line());
    }

    @Test
    void onWallColorIsStored() {
        var loc = new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_2, TileKind.Colored.C);
        assertEquals(TileKind.Colored.C, loc.color());
    }

    @Test
    void onWallImplementsTileLocation() {
        assertInstanceOf(TileLocation.class,
                new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }

    @Test
    void onWallEqualityByComponents() {
        var a = new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_3, TileKind.Colored.D);
        var b = new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_3, TileKind.Colored.D);
        assertEquals(a, b);
    }

    @Test
    void onWallInequalityDifferentColor() {
        var a = new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        var b = new TileLocation.OnWall(PlayerId.P1, TileDestination.Pattern.PATTERN_1, TileKind.Colored.B);
        assertNotEquals(a, b);
    }

    @Test
    void onWallAllPatternsWork() {
        for (var line : TileDestination.Pattern.values()) {
            for (var color : TileKind.Colored.values()) {
                var loc = new TileLocation.OnWall(PlayerId.P1, line, color);
                assertEquals(line, loc.line());
                assertEquals(color, loc.color());
            }
        }
    }

    // ─── OnFloor ─────────────────────────────────────────────────────────────

    @Test
    void onFloorPlayerIdIsStored() {
        var loc = new TileLocation.OnFloor(PlayerId.P3, 0);
        assertEquals(PlayerId.P3, loc.playerId());
    }

    @Test
    void onFloorIndexIsStored() {
        var loc = new TileLocation.OnFloor(PlayerId.P1, 4);
        assertEquals(4, loc.index());
    }

    @Test
    void onFloorAcceptsAllValidIndices() {
        for (int i = 0; i <= 6; i++) {
            final int idx = i;
            assertDoesNotThrow(() -> new TileLocation.OnFloor(PlayerId.P1, idx),
                    "index " + i + " should be valid");
        }
    }

    @Test
    void onFloorAcceptsIndex0() {
        assertDoesNotThrow(() -> new TileLocation.OnFloor(PlayerId.P1, 0));
    }

    @Test
    void onFloorAcceptsIndex6() {
        assertDoesNotThrow(() -> new TileLocation.OnFloor(PlayerId.P1, 6));
    }

    @Test
    void onFloorRejectsNegativeIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> new TileLocation.OnFloor(PlayerId.P1, -1));
    }

    @Test
    void onFloorRejectsIndex7() {
        assertThrows(IllegalArgumentException.class,
                () -> new TileLocation.OnFloor(PlayerId.P1, 7));
    }

    @Test
    void onFloorRejectsLargeIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> new TileLocation.OnFloor(PlayerId.P1, 100));
    }

    @Test
    void onFloorRejectsVeryNegativeIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> new TileLocation.OnFloor(PlayerId.P1, Integer.MIN_VALUE));
    }

    @Test
    void onFloorImplementsTileLocation() {
        assertInstanceOf(TileLocation.class, new TileLocation.OnFloor(PlayerId.P1, 0));
    }

    @Test
    void onFloorEqualityByComponents() {
        var a = new TileLocation.OnFloor(PlayerId.P2, 3);
        var b = new TileLocation.OnFloor(PlayerId.P2, 3);
        assertEquals(a, b);
    }

    @Test
    void onFloorInequalityDifferentPlayer() {
        var a = new TileLocation.OnFloor(PlayerId.P1, 0);
        var b = new TileLocation.OnFloor(PlayerId.P2, 0);
        assertNotEquals(a, b);
    }

    @Test
    void onFloorInequalityDifferentIndex() {
        var a = new TileLocation.OnFloor(PlayerId.P1, 0);
        var b = new TileLocation.OnFloor(PlayerId.P1, 1);
        assertNotEquals(a, b);
    }

    // ─── Cross-type inequality ───────────────────────────────────────────────

    @Test
    void differentSubtypesAreNotEqual() {
        var offBoard = new TileLocation.OffBoard(TileKind.A, 0);
        var onFloor  = new TileLocation.OnFloor(PlayerId.P1, 0);
        assertNotEquals(offBoard, onFloor);
    }

    @Test
    void onSourceAndOnPatternAreNotEqual() {
        var onSource  = new TileLocation.OnSource(TileSource.FACTORY_1, 0);
        var onPattern = new TileLocation.OnPattern(PlayerId.P1, TileDestination.Pattern.PATTERN_1, 0);
        assertNotEquals(onSource, onPattern);
    }
}
