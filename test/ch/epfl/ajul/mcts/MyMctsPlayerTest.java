package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
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
}
