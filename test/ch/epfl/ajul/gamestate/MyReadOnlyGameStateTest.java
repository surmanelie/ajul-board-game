package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class MyReadOnlyGameStateTest {

    // Impl minimale pour tester les méthodes default
    private record S(
            Game game,
            int pkTileBag,
            ImmutableIntArray pkTileSources,
            int pkUniqueTileSources,
            ImmutableIntArray pkPlayerStates,
            PlayerId currentPlayerId
    ) implements ReadOnlyGameState { }

    private static Game game2() {
        var p1 = new Game.PlayerDescription(PlayerId.P1, "p1", Game.PlayerDescription.PlayerKind.HUMAN);
        var p2 = new Game.PlayerDescription(PlayerId.P2, "p2", Game.PlayerDescription.PlayerKind.HUMAN);
        return new Game(List.of(p1, p2));
    }

    @Test
    void isRoundOverTrueWhenAllSourcesHaveNoColoredTiles_evenIfMarkerPresent() {
        var g = game2();

        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER); // centrale: seulement le marqueur

        var st = new S(
                g,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(all),
                0,
                PkPlayerStates.initial(g),
                PlayerId.P1
        );

        assertTrue(st.isRoundOver());
        assertFalse(st.isGameOver());
    }

    @Test
    void isRoundOverFalseWhenAtLeastOneSourceHasAColoredTile() {
        var g = game2();

        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        all[1] = PkTileSet.of(1, TileKind.A); // 1 tuile colorée

        var st = new S(
                g,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(all),
                0,
                PkPlayerStates.initial(g),
                PlayerId.P1
        );

        assertFalse(st.isRoundOver());
        assertFalse(st.isGameOver());
    }

    @Test
    void isGameOverTrueOnlyIfRoundOverAndSomePlayerHasFullRowOnWall() {
        var g = game2();

        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER); // round over

        int pkWall = PkWall.EMPTY;
        for (var c : TileKind.Colored.ALL) {
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_1, c);
        }

        int[] ps = new int[4 * g.playersCount()];
        PkPlayerStates.setPkWall(ps, PlayerId.P1, pkWall);

        var st = new S(
                g,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(all),
                0,
                ImmutableIntArray.copyOf(ps),
                PlayerId.P1
        );

        assertTrue(st.isRoundOver());
        assertTrue(st.isGameOver());
    }

    @Test
    void pkDiscardedTilesIsFullMinusBagMinusSourcesMinusPlayersBoards() {
        var g = game2();

        int bag = PkTileSet.remove(PkTileSet.FULL_COLORED, TileKind.A);

        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.union(
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                PkTileSet.of(1, TileKind.B)
        );

        int pkWallP1 = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, TileKind.Colored.C);

        int[] ps = new int[4 * g.playersCount()];
        PkPlayerStates.setPkWall(ps, PlayerId.P1, pkWallP1);

        var st = new S(
                g,
                bag,
                ImmutableIntArray.copyOf(all),
                0,
                ImmutableIntArray.copyOf(ps),
                PlayerId.P1
        );

        int expected = PkTileSet.FULL;
        expected = PkTileSet.difference(expected, bag);
        expected = PkTileSet.difference(expected, all[0]);
        expected = PkTileSet.difference(expected, PkWall.asPkTileSet(pkWallP1));

        assertEquals(expected, st.pkDiscardedTiles());
    }

    @Test
    void uniqueValidMovesOnlyConsidersSourcesMarkedAsUnique() {
        var g = game2();

        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        all[1] = PkTileSet.of(1, TileKind.A);
        all[2] = PkTileSet.of(1, TileKind.E);

        int unique = PkIntSet32.EMPTY;
        unique = PkIntSet32.add(unique, 2);

        var st = new S(
                g,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(all),
                unique,
                PkPlayerStates.initial(g),
                PlayerId.P1
        );

        short[] moves = new short[Move.MAX_MOVES];
        int n = st.uniqueValidMoves(moves);

        var src2 = TileSource.ALL.get(2);
        for (int i = 0; i < n; i++) {
            assertEquals(src2, PkMove.source(moves[i]));
        }
    }

    @Test
    void validMovesThrowsIfDestinationTooSmall() {
        var g = game2();
        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        var st = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(all), 0, PkPlayerStates.initial(g), PlayerId.P1);

        assertThrows(IllegalArgumentException.class, () -> st.validMoves(new short[Move.MAX_MOVES - 1]));
    }

    @Test
    void uniqueValidMovesThrowsIfDestinationTooSmall() {
        var g = game2();
        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        var st = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(all), 0, PkPlayerStates.initial(g), PlayerId.P1);

        assertThrows(IllegalArgumentException.class, () -> st.uniqueValidMoves(new short[Move.MAX_MOVES - 1]));
    }

    @Test
    void markerOnlySourceGeneratesNoMoves() {
        var g = game2();

        int[] all = new int[g.tileSourcesCount()];
        all[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        var st = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(all), 0, PkPlayerStates.initial(g), PlayerId.P1);

        short[] moves = new short[Move.MAX_MOVES];
        assertEquals(0, st.validMoves(moves));
    }

    @Test
    void isRoundOverTrueWhenNoColoredTilesInAnySource() {
        var g = game2();
        int[] src = new int[g.tileSourcesCount()];
        src[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER); // marker seulement
        var state = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src), 0, PkPlayerStates.initial(g), PlayerId.P1);

        assertTrue(state.isRoundOver());
    }

    @Test
    void isRoundOverFalseWhenAnySourceHasColoredTile() {
        var g = game2();
        int[] src = new int[g.tileSourcesCount()];
        src[0] = PkTileSet.union(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER), PkTileSet.of(1, TileKind.Colored.A));
        var state = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src), 0, PkPlayerStates.initial(g), PlayerId.P1);

        assertFalse(state.isRoundOver());
    }

    @Test
    void pkDiscardedTilesIsFullWhenNothingIsInBagSourcesOrPlayers() {
        var g = game2();
        int[] src = new int[g.tileSourcesCount()]; // tout vide
        var ps = PkPlayerStates.initial(g);        // tout vide
        var state = new S(g, PkTileSet.EMPTY, ImmutableIntArray.copyOf(src), 0, ps, PlayerId.P1);

        assertEquals(PkTileSet.FULL, state.pkDiscardedTiles()); // inclut marqueur
    }

    @Test
    void pkDiscardedTilesIsEmptyInInitialConfigurationLikeFactoryMethod() {
        var g = game2();
        int[] src = new int[g.tileSourcesCount()];
        src[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        var ps = PkPlayerStates.initial(g);
        var state = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src), 0, ps, PlayerId.P1);

        assertEquals(PkTileSet.EMPTY, state.pkDiscardedTiles());
    }


    @Test
    void validMovesAlwaysIncludesFloorMoveForEachColorPresentInAConsideredSource() {
        var g = game2();

        int[] src = new int[g.tileSourcesCount()];
        // Source 1 (première fabrique) contient 2 couleurs
        src[1] = PkTileSet.union(PkTileSet.of(2, TileKind.Colored.A), PkTileSet.of(1, TileKind.Colored.C));

        var state = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src), 0, PkPlayerStates.initial(g), PlayerId.P1);

        short[] moves = new short[Move.MAX_MOVES];
        int n = state.validMoves(moves);

        boolean hasAFloor = false;
        boolean hasCFloor = false;
        for (int i = 0; i < n; i++) {
            short pk = moves[i];
            if (PkMove.source(pk) == TileSource.ALL.get(1) && PkMove.destination(pk) == TileDestination.FLOOR) {
                if (PkMove.color(pk) == TileKind.Colored.A) hasAFloor = true;
                if (PkMove.color(pk) == TileKind.Colored.C) hasCFloor = true;
            }
        }
        assertTrue(hasAFloor);
        assertTrue(hasCFloor);
    }

    @Test
    void uniqueValidMovesReturnsZeroWhenUniqueSetIsEmptyEvenIfFactoriesHaveTiles() {
        var g = game2();

        int[] src = new int[g.tileSourcesCount()];
        src[1] = PkTileSet.of(1, TileKind.Colored.A);
        src[2] = PkTileSet.of(1, TileKind.Colored.B);

        var st = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src), 0, PkPlayerStates.initial(g), PlayerId.P1);

        short[] moves = new short[Move.MAX_MOVES];
        assertEquals(0, st.uniqueValidMoves(moves));
    }

    @Test
    void validMovesDoesNotDependOnTileMultiplicityOnlyOnPresence() {
        var g = game2();

        int[] src1 = new int[g.tileSourcesCount()];
        src1[1] = PkTileSet.of(1, TileKind.Colored.A);

        int[] src2 = new int[g.tileSourcesCount()];
        src2[1] = PkTileSet.of(5, TileKind.Colored.A);

        var st1 = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src1), 0, PkPlayerStates.initial(g), PlayerId.P1);
        var st2 = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src2), 0, PkPlayerStates.initial(g), PlayerId.P1);

        short[] m1 = new short[Move.MAX_MOVES];
        short[] m2 = new short[Move.MAX_MOVES];

        int n1 = st1.validMoves(m1);
        int n2 = st2.validMoves(m2);

        assertEquals(n1, n2);
    }

    @Test
    void validMovesAlwaysContainsFloorMoveForPresentColor() {
        var g = game2();

        int[] src = new int[g.tileSourcesCount()];
        src[1] = PkTileSet.of(1, TileKind.Colored.A); // une seule couleur présente

        var st = new S(g, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(src), 0,
                PkPlayerStates.initial(g), PlayerId.P1);

        short[] moves = new short[Move.MAX_MOVES];
        int n = st.validMoves(moves);

        boolean hasAFloor = false;
        for (int i = 0; i < n; i++) {
            if (PkMove.source(moves[i]) == TileSource.ALL.get(1)
                    && PkMove.color(moves[i]) == TileKind.Colored.A
                    && PkMove.destination(moves[i]) == TileDestination.FLOOR) {
                hasAFloor = true;
                break;
            }
        }
        assertTrue(hasAFloor);
    }



}