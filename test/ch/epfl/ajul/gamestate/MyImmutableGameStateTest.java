package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyImmutableGameStateTest {

    private static Game game2() {
        var p1 = new Game.PlayerDescription(PlayerId.P1, "p1", Game.PlayerDescription.PlayerKind.HUMAN);
        var p2 = new Game.PlayerDescription(PlayerId.P2, "p2", Game.PlayerDescription.PlayerKind.HUMAN);
        return new Game(List.of(p1, p2));
    }

    @Test
    void immutableGameStateCompactCtorRejectsNulls() {
        var g = game2();
        var sources = ImmutableIntArray.copyOf(new int[g.tileSourcesCount()]);
        var ps = PkPlayerStates.initial(g);

        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(null, 0, sources, 0, ps, PlayerId.P1));
        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(g, 0, null, 0, ps, PlayerId.P1));
        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(g, 0, sources, 0, null, PlayerId.P1));
        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(g, 0, sources, 0, ps, null));
    }

    @Test
    void immutableReturnsSelf() {
        var s = ImmutableGameState.initial(game2());
        assertSame(s, s.immutable());
    }

    @Test
    void initialStateHasExpectedBagAndSources() {
        var g = game2();
        var s = ImmutableGameState.initial(g);

        // Sac = toutes les tuiles colorées (sans marqueur)
        assertEquals(PkTileSet.FULL_COLORED, s.pkTileBag());
        assertEquals(0, PkTileSet.countOf(s.pkTileBag(), TileKind.FIRST_PLAYER_MARKER));

        // Sources: taille correcte, centrale = marqueur, fabriques vides
        assertEquals(g.tileSourcesCount(), s.pkTileSources().size());
        assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER), s.pkTileSources().get(0));
        for (int i = 1; i < s.pkTileSources().size(); i++) {
            assertEquals(PkTileSet.EMPTY, s.pkTileSources().get(i));
        }

        // Sources uniques : vide
        assertEquals(0, s.pkUniqueTileSources());
    }

    @Test
    void initialStateHasExpectedPlayersAndCurrentPlayer() {
        var g = game2();
        var s = ImmutableGameState.initial(g);

        assertEquals(4 * g.playersCount(), s.pkPlayerStates().size());
        for (var pid : g.playerIds()) {
            // Tout doit être à 0 au départ (patterns, floor, wall, points)
            assertEquals(0, ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkPatterns(s.pkPlayerStates(), pid));
            assertEquals(0, ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkFloor(s.pkPlayerStates(), pid));
            assertEquals(0, ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkWall(s.pkPlayerStates(), pid));
            assertEquals(0, ch.epfl.ajul.gamestate.packed.PkPlayerStates.points(s.pkPlayerStates(), pid));
        }

        assertEquals(g.playerIds().get(0), s.currentPlayerId());
    }

    @Test
    void initialStateHasSameObservableContents() {
        var g = game2();
        var s1 = ImmutableGameState.initial(g);
        var s2 = ImmutableGameState.initial(g);

        assertEquals(s1.game(), s2.game());
        assertEquals(s1.pkTileBag(), s2.pkTileBag());
        assertEquals(s1.pkUniqueTileSources(), s2.pkUniqueTileSources());
        assertEquals(s1.currentPlayerId(), s2.currentPlayerId());

        // comparer le contenu des ImmutableIntArray élément par élément
        assertEquals(s1.pkTileSources().size(), s2.pkTileSources().size());
        for (int i = 0; i < s1.pkTileSources().size(); i++) {
            assertEquals(s1.pkTileSources().get(i), s2.pkTileSources().get(i));
        }

        assertEquals(s1.pkPlayerStates().size(), s2.pkPlayerStates().size());
        for (int i = 0; i < s1.pkPlayerStates().size(); i++) {
            assertEquals(s1.pkPlayerStates().get(i), s2.pkPlayerStates().get(i));
        }
    }

    @Test
    void constructorDefensivelyCopiesTileSources() {
        var g = game2();

        int[] raw = new int[g.tileSourcesCount()];
        raw[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        var sources = ImmutableIntArray.copyOf(raw);
        var ps = PkPlayerStates.initial(g);

        var s = new ImmutableGameState(g, PkTileSet.FULL_COLORED, sources, 0, ps, PlayerId.P1);

        // on mutile le tableau initial
        raw[0] = PkTileSet.of(1, TileKind.Colored.A);

        // l'état ne doit pas changer (car ImmutableIntArray a copié)
        assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER), s.pkTileSources().get(0));
    }
}
