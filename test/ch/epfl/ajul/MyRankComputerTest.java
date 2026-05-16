package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyRankComputerTest {

    private static Game game(int playerCount) {
        var players = new ArrayList<Game.PlayerDescription>();
        for (int i = 0; i < playerCount; i++) {
            players.add(new Game.PlayerDescription(
                    PlayerId.ALL.get(i), "P" + (i + 1),
                    Game.PlayerDescription.PlayerKind.HUMAN));
        }
        return new Game(players);
    }

    private record S(
            Game game,
            int pkTileBag,
            ImmutableIntArray pkTileSources,
            int pkUniqueTileSources,
            ImmutableIntArray pkPlayerStates,
            PlayerId currentPlayerId
    ) implements ReadOnlyGameState {}

    private static S stateWithPoints(Game g, int[] pointsPerPlayer) {
        int[] ps = new int[4 * g.playersCount()];
        for (int i = 0; i < g.playersCount(); i++) {
            PkPlayerStates.addPoints(ps, PlayerId.ALL.get(i), pointsPerPlayer[i]);
        }
        int[] sources = new int[g.tileSourcesCount()];
        return new S(g, PkTileSet.EMPTY, ImmutableIntArray.copyOf(sources), 0,
                ImmutableIntArray.copyOf(ps), PlayerId.P1);
    }

    // ─── 2 joueurs ───────────────────────────────────────────────────────────

    @Test
    void twoPlayersHigherScoreGetsRankZero() {
        var g = game(2);
        var state = stateWithPoints(g, new int[]{20, 5});
        int[] ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        assertEquals(0, ranks[0], "P1 (20 pts) should be rank 0");
        assertEquals(1, ranks[1], "P2 (5 pts) should be rank 1");
    }

    @Test
    void twoPlayersLowerScoreGetsWorseRank() {
        var g = game(2);
        var state = stateWithPoints(g, new int[]{5, 20});
        int[] ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        assertTrue(ranks[0] > ranks[1], "P1 (5 pts) should rank worse than P2 (20 pts)");
    }

    @Test
    void twoPlayersTiedScoreGetsSameRank() {
        var g = game(2);
        var state = stateWithPoints(g, new int[]{10, 10});
        int[] ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        assertEquals(ranks[0], ranks[1], "Equal points → same rank");
    }

    @Test
    void twoPlayersZeroPointsBothSameRank() {
        var g = game(2);
        var state = stateWithPoints(g, new int[]{0, 0});
        int[] ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        assertEquals(ranks[0], ranks[1]);
    }

    // ─── Tiebreaker : lignes complètes ───────────────────────────────────────

    @Test
    void fullRowBreaksTieWhenPointsAreEqual() {
        var g = game(2);
        int[] ps = new int[4 * g.playersCount()];
        PkPlayerStates.addPoints(ps, PlayerId.P1, 10);
        PkPlayerStates.addPoints(ps, PlayerId.P2, 10);

        // P1 a 1 ligne complète sur le mur (ligne PATTERN_1, toutes les couleurs)
        int pkWallP1 = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL) {
            pkWallP1 = PkWall.withTileAt(pkWallP1, TileDestination.Pattern.PATTERN_1, color);
        }
        PkPlayerStates.setPkWall(ps, PlayerId.P1, pkWallP1);

        int[] sources = new int[g.tileSourcesCount()];
        var state = new S(g, PkTileSet.EMPTY, ImmutableIntArray.copyOf(sources), 0,
                ImmutableIntArray.copyOf(ps), PlayerId.P1);

        int[] ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        assertTrue(ranks[0] < ranks[1],
                "P1 (1 full row) should rank better than P2 (0 full rows) with equal points");
    }

    @Test
    void moreFullRowsGivesBetterRank() {
        var g = game(2);
        int[] ps = new int[4 * g.playersCount()];
        PkPlayerStates.addPoints(ps, PlayerId.P1, 10);
        PkPlayerStates.addPoints(ps, PlayerId.P2, 10);

        // P1: 2 lignes complètes, P2: 1 ligne complète
        int pkWallP1 = PkWall.EMPTY;
        int pkWallP2 = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL) {
            pkWallP1 = PkWall.withTileAt(pkWallP1, TileDestination.Pattern.PATTERN_1, color);
            pkWallP1 = PkWall.withTileAt(pkWallP1, TileDestination.Pattern.PATTERN_2, color);
            pkWallP2 = PkWall.withTileAt(pkWallP2, TileDestination.Pattern.PATTERN_1, color);
        }
        PkPlayerStates.setPkWall(ps, PlayerId.P1, pkWallP1);
        PkPlayerStates.setPkWall(ps, PlayerId.P2, pkWallP2);

        int[] sources = new int[g.tileSourcesCount()];
        var state = new S(g, PkTileSet.EMPTY, ImmutableIntArray.copyOf(sources), 0,
                ImmutableIntArray.copyOf(ps), PlayerId.P1);

        int[] ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        assertTrue(ranks[0] < ranks[1], "P1 (2 rows) should beat P2 (1 row)");
    }

    // ─── 3 joueurs ───────────────────────────────────────────────────────────

    @Test
    void threePlayersOrderedScores() {
        var g = game(3);
        var state = stateWithPoints(g, new int[]{30, 20, 10});
        int[] ranks = new int[3];
        RankComputer.playersRank(state, ranks);
        assertEquals(0, ranks[0]);
        assertEquals(1, ranks[1]);
        assertEquals(2, ranks[2]);
    }

    @Test
    void threePlayersReversedScores() {
        var g = game(3);
        var state = stateWithPoints(g, new int[]{10, 20, 30});
        int[] ranks = new int[3];
        RankComputer.playersRank(state, ranks);
        assertEquals(2, ranks[0]);
        assertEquals(1, ranks[1]);
        assertEquals(0, ranks[2]);
    }

    @Test
    void threePlayersAllTiedGetSameRank() {
        var g = game(3);
        var state = stateWithPoints(g, new int[]{10, 10, 10});
        int[] ranks = new int[3];
        RankComputer.playersRank(state, ranks);
        assertEquals(ranks[0], ranks[1]);
        assertEquals(ranks[1], ranks[2]);
    }

    @Test
    void threePlayersFirstTwoTied() {
        var g = game(3);
        var state = stateWithPoints(g, new int[]{20, 20, 10});
        int[] ranks = new int[3];
        RankComputer.playersRank(state, ranks);
        assertEquals(ranks[0], ranks[1], "P1 and P2 are tied");
        assertTrue(ranks[2] > ranks[0], "P3 ranks worse than P1/P2");
    }

    // ─── 4 joueurs ───────────────────────────────────────────────────────────

    @Test
    void fourPlayersAllDifferentScores() {
        var g = game(4);
        var state = stateWithPoints(g, new int[]{40, 30, 20, 10});
        int[] ranks = new int[4];
        RankComputer.playersRank(state, ranks);
        assertEquals(0, ranks[0]);
        assertEquals(1, ranks[1]);
        assertEquals(2, ranks[2]);
        assertEquals(3, ranks[3]);
    }

    @Test
    void fourPlayersAllSameScore() {
        var g = game(4);
        var state = stateWithPoints(g, new int[]{5, 5, 5, 5});
        int[] ranks = new int[4];
        RankComputer.playersRank(state, ranks);
        assertEquals(ranks[0], ranks[1]);
        assertEquals(ranks[1], ranks[2]);
        assertEquals(ranks[2], ranks[3]);
    }

    @Test
    void fourPlayersWinnerHasHighestScore() {
        var g = game(4);
        var state = stateWithPoints(g, new int[]{5, 50, 5, 5});
        int[] ranks = new int[4];
        RankComputer.playersRank(state, ranks);
        assertEquals(0, ranks[1], "P2 with 50 pts should be rank 0");
    }

    @Test
    void rankArrayIsFilledCorrectly() {
        var g = game(2);
        var state = stateWithPoints(g, new int[]{10, 5});
        int[] ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        // ranks must be non-negative
        for (int r : ranks) {
            assertTrue(r >= 0);
        }
    }
}
