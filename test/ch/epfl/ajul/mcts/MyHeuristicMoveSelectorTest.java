package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyHeuristicMoveSelectorTest {

    private static Game game2() {
        var players = new ArrayList<Game.PlayerDescription>();
        players.add(new Game.PlayerDescription(PlayerId.P1, "A", Game.PlayerDescription.PlayerKind.HUMAN));
        players.add(new Game.PlayerDescription(PlayerId.P2, "B", Game.PlayerDescription.PlayerKind.HUMAN));
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

    /**
     * Crée un état de jeu où FACTORY_1 contient 'count' tuiles de couleur A.
     */
    private static S stateWithSourceA(Game g, int count) {
        int[] sources = new int[g.tileSourcesCount()];
        sources[TileSource.FACTORY_1.index()] = PkTileSet.of(count, TileKind.Colored.A);
        return new S(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), 0,
                ImmutableIntArray.copyOf(new int[4 * g.playersCount()]),
                PlayerId.P1);
    }

    // ─── Sélection avec un seul coup disponible ───────────────────────────────

    @Test
    void singleMoveAlwaysSelected() {
        var g = game2();
        var state = stateWithSourceA(g, 1);
        var rng = RandomGeneratorFactory.getDefault().create(0);

        short[] moves = {PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR)};
        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, 1);

        assertEquals(0, selected, "Only one move, must return index 0");
    }

    // ─── Exact fill prioritaire ───────────────────────────────────────────────

    @Test
    void exactFillPreferredOverFloor() {
        var g = game2();
        // FACTORY_1 a exactement 3 tuiles A → exactFill pour PATTERN_3 (capacité 3, 0 rempli)
        var state = stateWithSourceA(g, 3);
        var rng = RandomGeneratorFactory.getDefault().create(42);

        short[] moves = {
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.Pattern.PATTERN_3),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR)
        };
        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, 2);

        // La tuile est un exact fill → doit être préférée au plancher
        assertEquals(0, selected, "Exact fill move (index 0) must be preferred over floor (index 1)");
    }

    @Test
    void exactFillWithSingleTileForPattern1() {
        var g = game2();
        // FACTORY_1 a 1 tuile A → exact fill pour PATTERN_1 (capacité 1)
        var state = stateWithSourceA(g, 1);
        var rng = RandomGeneratorFactory.getDefault().create(0);

        short[] moves = {
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.Pattern.PATTERN_1),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR)
        };
        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, 2);
        assertEquals(0, selected, "Exact fill for PATTERN_1 (1 tile) must be preferred");
    }

    @Test
    void exactFillWithFourTilesForPattern4() {
        var g = game2();
        // FACTORY_1 a 4 tuiles A → exact fill pour PATTERN_4 (capacité 4)
        var state = stateWithSourceA(g, 4);
        var rng = RandomGeneratorFactory.getDefault().create(0);

        short[] moves = {
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.Pattern.PATTERN_4),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR)
        };
        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, 2);
        assertEquals(0, selected, "Exact fill for PATTERN_4 (4 tiles) must be preferred");
    }

    // ─── Partial fill prioritaire sur floor ───────────────────────────────────

    @Test
    void partialFillPreferredOverFloor() {
        var g = game2();
        // FACTORY_1 a 2 tuiles A, PATTERN_5 a capacité 5 → tilesAvailable(2) < missing(5) → partialFill
        var state = stateWithSourceA(g, 2);
        var rng = RandomGeneratorFactory.getDefault().create(0);

        short[] moves = {
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.Pattern.PATTERN_5),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR)
        };
        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, 2);
        assertEquals(0, selected, "Partial fill (index 0) must be preferred over floor (index 1)");
    }

    // ─── Retourne index valide ────────────────────────────────────────────────

    @Test
    void returnedIndexIsWithinBounds() {
        var g = game2();
        var state = stateWithSourceA(g, 2);
        var rng = RandomGeneratorFactory.getDefault().create(1234);

        short[] moves = {
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.Pattern.PATTERN_3),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR)
        };
        int count = 2;
        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, count);
        assertTrue(selected >= 0 && selected < count,
                "Selected index must be within [0, validMovesCount)");
    }

    @Test
    void movesWithMultipleColorsReturnValidIndex() {
        var g = game2();

        // FACTORY_1: 2 tuiles A + 1 tuile B
        int[] sources = new int[g.tileSourcesCount()];
        sources[TileSource.FACTORY_1.index()] = PkTileSet.union(
                PkTileSet.of(2, TileKind.Colored.A),
                PkTileSet.of(1, TileKind.Colored.B)
        );
        var state = new S(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), 0,
                ImmutableIntArray.copyOf(new int[4 * g.playersCount()]),
                PlayerId.P1);

        var rng = RandomGeneratorFactory.getDefault().create(0);
        short[] moves = {
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.Pattern.PATTERN_2),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.Pattern.PATTERN_1),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR),
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.FLOOR)
        };

        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, 4);
        assertTrue(selected >= 0 && selected < 4);
    }

    // ─── Cohérence avec validMovesCount ───────────────────────────────────────

    @Test
    void onlyFirstValidMovesCountedAreParsed() {
        var g = game2();
        var state = stateWithSourceA(g, 1);
        var rng = RandomGeneratorFactory.getDefault().create(0);

        // Only 1 valid move out of 2 slots
        short[] moves = {
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR),
                (short) 0 // should be ignored
        };
        int selected = HeuristicMoveSelector.selectMove(rng, state, moves, 1);
        assertEquals(0, selected);
    }
}
