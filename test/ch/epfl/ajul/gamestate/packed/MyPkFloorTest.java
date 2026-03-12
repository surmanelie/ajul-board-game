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
        assertEquals("[A, A, A, A, A, A, A]", PkFloor.toString(f));
        assertFalse(PkFloor.containsFirstPlayerMarker(f));

        // Ajout de 3 B : doit être ignoré (floor inchangé au niveau du contenu)
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(3, TileKind.Colored.B));
        assertEquals(7, PkFloor.size(f2));
        assertEquals("[A, A, A, A, A, A, A]", PkFloor.toString(f2));
        assertFalse(PkFloor.containsFirstPlayerMarker(f2));
    }

    @Test
    void markerReplacesLastTileWhenFloorIsFull() {
        // Remplit le floor avec 7 A
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(7, TileKind.Colored.A));
        assertEquals(7, PkFloor.size(f));
        assertFalse(PkFloor.containsFirstPlayerMarker(f));

        // Ajoute le marqueur : le contenu final doit contenir le marqueur et rester de taille 7
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(7, PkFloor.size(f2));
        assertTrue(PkFloor.containsFirstPlayerMarker(f2));

        // Le marqueur doit apparaître dans la représentation textuelle
        assertTrue(PkFloor.toString(f2).contains("FIRST_PLAYER_MARKER"));
    }

    @Test
    void markerIsNotDuplicatedIfAlreadyPresent() {
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(1, PkFloor.size(f));
        assertTrue(PkFloor.containsFirstPlayerMarker(f));
        assertEquals("[FIRST_PLAYER_MARKER]", PkFloor.toString(f));

        // Ajoute encore un marqueur : ne doit rien changer au contenu (toujours un seul marqueur)
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(1, PkFloor.size(f2));
        assertTrue(PkFloor.containsFirstPlayerMarker(f2));
        assertEquals("[FIRST_PLAYER_MARKER]", PkFloor.toString(f2));
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

        // ajoute 3 B -> seule 1 tuile B rentre (capacité restante = 1), puis normalisation par ordre
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(3, TileKind.Colored.B));
        assertEquals(7, PkFloor.size(f2));

        // Le contenu doit être 6 A + 1 B, en ordre canonique
        assertEquals("[A, A, A, A, A, A, B]", PkFloor.toString(f2));
    }

    @Test
    void orderIsCanonicalAcrossMultipleCalls() {
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.D)); // D, D
        f = PkFloor.withAddedTiles(f, PkTileSet.of(1, TileKind.Colored.B));               // + B
        // Canonique : tri par TileKind::index -> [B, D, D]
        assertEquals("[B, D, D]", PkFloor.toString(f));
    }

    @Test
    void markerDoesNotDuplicateWhenAlreadyPresentEvenIfFloorIsFull() {
        int f = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(7, TileKind.Colored.A));
        int f2 = PkFloor.withAddedTiles(f, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));

        assertEquals(7, PkFloor.size(f2));
        assertTrue(PkFloor.containsFirstPlayerMarker(f2));

        // Ajoute encore un marker : doit être sans effet sur le contenu
        int f3 = PkFloor.withAddedTiles(f2, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(7, PkFloor.size(f3));
        assertTrue(PkFloor.containsFirstPlayerMarker(f3));
        assertEquals(PkFloor.toString(f2), PkFloor.toString(f3));
    }
}