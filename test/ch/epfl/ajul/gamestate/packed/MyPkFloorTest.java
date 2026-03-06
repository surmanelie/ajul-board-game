//package ch.epfl.ajul.gamestate.packed;
//
//public class MyPkFloorTest {
//}

package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de {@link PkFloor}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class MyPkFloorTest {

    @Test
    void emptyFloorHasSizeZeroAndEmptyString() {
        int f = PkFloor.EMPTY;

        assertEquals(0, PkFloor.size(f));
        assertEquals("[]", PkFloor.toString(f));
        assertFalse(PkFloor.containsFirstPlayerMarker(f));
        assertEquals(PkTileSet.EMPTY, PkFloor.asPkTileSet(f));
    }

    @Test
    void tilesAreAddedInTileKindOrder() {
        // Set "désordonné" : 2 B, 1 A, 1 E, 1 marker
        int s = PkTileSet.EMPTY;
        s = PkTileSet.union(s, PkTileSet.of(2, TileKind.Colored.B));
        s = PkTileSet.union(s, PkTileSet.of(1, TileKind.Colored.A));
        s = PkTileSet.union(s, PkTileSet.of(1, TileKind.Colored.E));
        s = PkTileSet.union(s, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));

        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, s);

        assertEquals(5, PkFloor.size(f));
        assertEquals(TileKind.Colored.A, PkFloor.tileAt(f, 0));
        assertEquals(TileKind.Colored.B, PkFloor.tileAt(f, 1));
        assertEquals(TileKind.Colored.B, PkFloor.tileAt(f, 2));
        assertEquals(TileKind.Colored.E, PkFloor.tileAt(f, 3));
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(f, 4));

        assertTrue(PkFloor.containsFirstPlayerMarker(f));
        assertEquals("[A, B, B, E, FIRST_PLAYER_MARKER]", PkFloor.toString(f));
    }

    @Test
    void nonMarkerTilesAreIgnoredWhenFloorIsFull() {
        // Remplit le floor avec 7 A
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(7, TileKind.Colored.A));
        assertEquals(7, PkFloor.size(f));
        assertEquals(TileKind.Colored.A, PkFloor.tileAt(f, 6));
        assertFalse(PkFloor.containsFirstPlayerMarker(f));

        // Ajout de 3 B : doit être ignoré (floor reste identique)
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(3, TileKind.Colored.B));
        assertEquals(f, f2);
    }

    @Test
    void markerReplacesLastTileWhenFloorIsFull() {
        // Remplit le floor avec 7 A
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(7, TileKind.Colored.A));
        assertEquals(7, PkFloor.size(f));
        assertFalse(PkFloor.containsFirstPlayerMarker(f));

        // Ajoute le marqueur : remplace la dernière tuile (position 6)
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(7, PkFloor.size(f2));
        assertTrue(PkFloor.containsFirstPlayerMarker(f2));
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(f2, 6));

        // Les 6 premières restent des A
        for (int i = 0; i < 6; i++) {
            assertEquals(TileKind.Colored.A, PkFloor.tileAt(f2, i));
        }
    }

    @Test
    void markerIsNotDuplicatedIfAlreadyPresent() {
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(1, PkFloor.size(f));
        assertTrue(PkFloor.containsFirstPlayerMarker(f));

        // Ajoute encore un marqueur : ne doit rien changer (toujours un seul marqueur)
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(f, f2);
        assertEquals(1, PkFloor.size(f2));
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(f2, 0));
    }

    @Test
    void asPkTileSetCountsExactlyTheFloorContent() {
        int s = PkTileSet.EMPTY;
        s = PkTileSet.union(s, PkTileSet.of(3, TileKind.Colored.C));
        s = PkTileSet.union(s, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));

        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, s);
        assertEquals(4, PkFloor.size(f));

        int back = PkFloor.asPkTileSet(f);
        assertEquals(3, PkTileSet.countOf(back, TileKind.Colored.C));
        assertEquals(1, PkTileSet.countOf(back, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void addingTooManyNonMarkerTilesFillsThenIgnoresOverflow() {
        // floor contient déjà 6 A
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(6, TileKind.Colored.A));
        assertEquals(6, PkFloor.size(f));

        // ajoute 3 B -> seule 1 tuile B rentre (capacité restante = 1)
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(3, TileKind.Colored.B));
        assertEquals(7, PkFloor.size(f2));
        assertEquals(TileKind.Colored.B, PkFloor.tileAt(f2, 6));

        // les 6 premières restent A
        for (int i = 0; i < 6; i++) {
            assertEquals(TileKind.Colored.A, PkFloor.tileAt(f2, i));
        }
    }

    @Test
    void orderIsRespectedAcrossMultipleCalls() {
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.D)); // D, D
        f = PkFloor.withAddedTiles(f, PkTileSet.of(1, TileKind.Colored.B));               // + B
        // L'ajout se fait "à la suite" : [D, D, B] (et pas trié globalement)
        assertEquals("[D, D, B]", PkFloor.toString(f));
    }
}
