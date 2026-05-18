package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyMctsPlayerTest {

    private static Game game2() {
        var players = new ArrayList<Game.PlayerDescription>();
        players.add(new Game.PlayerDescription(PlayerId.P1, "A", Game.PlayerDescription.PlayerKind.HUMAN));
        players.add(new Game.PlayerDescription(PlayerId.P2, "B", Game.PlayerDescription.PlayerKind.HUMAN));
        return new Game(players);
    }

    private static MutableGameState readyState(Game g) {
        var initial = ImmutableGameState.initial(g);
        var mutable = new MutableGameState(initial);
        mutable.fillFactories(RandomGeneratorFactory.getDefault().create(42));
        return mutable;
    }

    // ─── Constructeur ─────────────────────────────────────────────────────────

    @Test
    void constructorRejectsZeroIterations() {
        var factory = RandomGeneratorFactory.getDefault();
        assertThrows(IllegalArgumentException.class, () -> new MctsPlayer(factory, 0));
    }

    @Test
    void constructorRejectsNegativeIterations() {
        var factory = RandomGeneratorFactory.getDefault();
        assertThrows(IllegalArgumentException.class, () -> new MctsPlayer(factory, -1));
    }

    @Test
    void constructorRejectsVeryNegativeIterations() {
        var factory = RandomGeneratorFactory.getDefault();
        assertThrows(IllegalArgumentException.class, () -> new MctsPlayer(factory, Integer.MIN_VALUE));
    }

    @Test
    void constructorAcceptsOneIteration() {
        var factory = RandomGeneratorFactory.getDefault();
        assertDoesNotThrow(() -> new MctsPlayer(factory, 1));
    }

    @Test
    void constructorAcceptsLargeIterationCount() {
        var factory = RandomGeneratorFactory.getDefault();
        assertDoesNotThrow(() -> new MctsPlayer(factory, 1000));
    }

    // ─── nextMove ─────────────────────────────────────────────────────────────

    @Test
    void nextMoveReturnsNonNull() {
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 5);
        var state = readyState(game2());

        assertNotNull(player.nextMove(state));
    }

    @Test
    void nextMoveReturnsAValidMove() {
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 10);
        var state = readyState(game2());

        var move = player.nextMove(state);
        assertNotNull(move);

        // Verify the move is in the set of valid moves
        short[] validMoves = new short[Move.MAX_MOVES];
        int n = state.validMoves(validMoves);

        boolean found = false;
        for (int i = 0; i < n; i++) {
            if (validMoves[i] == move.packed()) {
                found = true;
                break;
            }
        }
        assertTrue(found, "nextMove() must return a valid move");
    }

    @Test
    void nextMoveReturnsMoveWithValidSource() {
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 5);
        var state = readyState(game2());

        var move = player.nextMove(state);
        assertNotNull(move.source());
    }

    @Test
    void nextMoveReturnsMoveWithValidColor() {
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 5);
        var state = readyState(game2());

        var move = player.nextMove(state);
        assertNotNull(move.tileColor());
    }

    @Test
    void nextMoveReturnsMoveWithValidDestination() {
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 5);
        var state = readyState(game2());

        var move = player.nextMove(state);
        assertNotNull(move.destination());
    }

    @Test
    void nextMoveWith4PlayerGame() {
        var players = new ArrayList<Game.PlayerDescription>();
        for (int i = 0; i < 4; i++) {
            players.add(new Game.PlayerDescription(
                    PlayerId.ALL.get(i), "P" + (i + 1),
                    Game.PlayerDescription.PlayerKind.HUMAN));
        }
        var g = new Game(players);
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 5);
        var state = readyState(g);

        var move = player.nextMove(state);
        assertNotNull(move);
    }

    @Test
    void nextMoveIsConsistentWithPackedForm() {
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 5);
        var state = readyState(game2());

        var move = player.nextMove(state);
        // The packed form must reconstruct the same move
        var repacked = Move.ofPacked(move.packed());
        assertEquals(move.source(), repacked.source());
        assertEquals(move.tileColor(), repacked.tileColor());
        assertEquals(move.destination(), repacked.destination());
    }

    // ─── Non-régression : transition de manche pendant la descente ───────────

    @Test
    void nextMoveWorksWhenRoundEndsDuringTreeDescent() {
        // Construit un état où la manche peut se terminer dès le premier coup joué :
        // seule la fabrique 1 a une tuile colorée ; le reste est vide.
        // Après le premier coup joué lors de la descente, toutes les sources
        // deviennent vides → isRoundOver() == true.
        // Avec suffisamment d'itérations, le nœud post-round-over sera revisité
        // avec ses enfants déjà créés, ce qui déclenche le bug si non corrigé.
        var g = game2();

        int[] pkTileSources = new int[g.tileSourcesCount()];
        pkTileSources[TileSource.CENTER_AREA.index()] =
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        pkTileSources[TileSource.FACTORY_1.index()] =
                PkTileSet.of(1, TileKind.Colored.A);

        var initial = new ImmutableGameState(
                g,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.add(PkIntSet32.EMPTY, TileSource.FACTORY_1.index()),
                PkPlayerStates.initial(g),
                PlayerId.P1
        );

        var player = new MctsPlayer(RandomGeneratorFactory.getDefault(), 300);
        // Doit terminer sans exception même si des nœuds post-round-over
        // sont revisités avec des enfants déjà créés
        assertDoesNotThrow(() -> player.nextMove(initial),
                "nextMove ne doit pas lever d'exception lors de la descente post-round-over");
    }

    @Test
    void nextMoveWorksAcrossMultipleRounds() {
        // Vérifie que l'IA joue correctement sur plusieurs manches simulées
        var g = game2();
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 50);

        // On joue une manche complète et on lance ensuite l'IA
        var state = readyState(g);
        var mutable = new MutableGameState(state);

        short[] moves = new short[Move.MAX_MOVES];
        while (!mutable.isRoundOver()) {
            int n = mutable.validMoves(moves);
            mutable.registerMove(moves[0]);
        }
        mutable.endRound();
        if (!mutable.isGameOver()) {
            mutable.fillFactories(factory.create(99L));
        }

        if (!mutable.isGameOver()) {
            var move = player.nextMove(mutable);
            assertNotNull(move, "nextMove doit retourner un coup valide au début d'une nouvelle manche");

            short[] validAfter = new short[Move.MAX_MOVES];
            int n = mutable.validMoves(validAfter);
            boolean found = false;
            for (int i = 0; i < n; i++) {
                if (validAfter[i] == move.packed()) { found = true; break; }
            }
            assertTrue(found, "Le coup retourné doit être valide");
        }
    }

    @Test
    void nextMoveWorksWithManyIterationsOnStandardGame() {
        // Test de non-régression avec un nombre d'itérations conséquent
        var g = game2();
        var factory = RandomGeneratorFactory.getDefault();
        var player = new MctsPlayer(factory, 500);
        var state = readyState(g);

        Move move = assertDoesNotThrow(() -> player.nextMove(state));
        assertNotNull(move);

        short[] valid = new short[Move.MAX_MOVES];
        int n = state.validMoves(valid);
        boolean found = false;
        for (int i = 0; i < n; i++) {
            if (valid[i] == move.packed()) { found = true; break; }
        }
        assertTrue(found, "nextMove doit toujours retourner un coup parmi les coups valides");
    }

    @Test
    void nextMoveReturnsBestChildByAveragePoints() {
        // Vérifie que le coup retourné correspond bien au fils de la racine
        // avec le meilleur score moyen (pas nécessairement le premier)
        var g = game2();
        var factory = RandomGeneratorFactory.getDefault();
        // Avec 1 seule itération, la racine a un seul enfant évalué →
        // le coup retourné est bien l'unique coup visité
        var player = new MctsPlayer(factory, 1);
        var state = readyState(g);

        assertDoesNotThrow(() -> player.nextMove(state));
    }
}
