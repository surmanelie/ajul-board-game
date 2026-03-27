package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.Game.PlayerDescription;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Points;
import ch.epfl.ajul.PointsObserver;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkFloor;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;



class MyMutableGameStateTest {
    @Test
    void mutableGameStateConstructorsThrowOnNullArguments() {
        var game = game(2);
        var initialState = ImmutableGameState.initial(game);

        assertThrows(NullPointerException.class, () -> new MutableGameState(null));
        assertThrows(NullPointerException.class, () -> new MutableGameState(null, PointsObserver.EMPTY));
        assertThrows(NullPointerException.class, () -> new MutableGameState(initialState, null));
    }

    @Test
    void mutableGameStateConstructorCopiesInitialState() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        pkTileSources[1] = PkTileSet.of(4, TileKind.Colored.A);
        pkTileSources[2] = PkTileSet.of(2, TileKind.Colored.B);

        int[] pkPlayerStates = PkPlayerStates.initial(game).toArray();
        PkPlayerStates.setPkFloor(
                pkPlayerStates,
                PlayerId.P1,
                PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.C))
        );
        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P2, 7);

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.of(12, TileKind.Colored.D),
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.add(PkIntSet32.EMPTY, 1),
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P2
        );

        var gameState = new MutableGameState(initialState);

        assertSame(game, gameState.game());
        assertEquals(initialState.pkTileBag(), gameState.pkTileBag());
        assertArrayEquals(initialState.pkTileSources().toArray(), gameState.pkTileSources().toArray());
        assertEquals(initialState.pkUniqueTileSources(), gameState.pkUniqueTileSources());
        assertArrayEquals(initialState.pkPlayerStates().toArray(), gameState.pkPlayerStates().toArray());
        assertEquals(initialState.currentPlayerId(), gameState.currentPlayerId());
    }

    @Test
    void mutableGameStateFillFactoriesWorksWhenBagContainsExactlyNeededTiles() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[TileSource.CENTER_AREA.index()] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.of(20, TileKind.Colored.A),
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                PkPlayerStates.initial(game),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState);
        var rng = RandomGeneratorFactory.getDefault().create(2026);

        gameState.fillFactories(rng);

        assertEquals(80, PkTileSet.size(gameState.pkTileBag()));

        int totalColoredInFactories = 0;
        for (var factory : game.factories()) {
            int pkFactory = gameState.pkTileSources().get(factory.index());
            assertEquals(4, PkTileSet.size(pkFactory));
            totalColoredInFactories += PkTileSet.size(pkFactory);
        }
        assertEquals(20, totalColoredInFactories);

        assertEquals(
                1,
                PkTileSet.countOf(
                        gameState.pkTileSources().get(TileSource.CENTER_AREA.index()),
                        TileKind.FIRST_PLAYER_MARKER
                )
        );
    }

    @Test
    void mutableGameStateRegisterMoveFromFactoryMovesRemainingTilesToCenter() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        pkTileSources[1] = PkTileSet.union(
                PkTileSet.of(2, TileKind.Colored.A),
                PkTileSet.union(
                        PkTileSet.of(1, TileKind.Colored.B),
                        PkTileSet.of(1, TileKind.Colored.C)
                )
        );

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                PkPlayerStates.initial(game),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState);

        short pkMove = PkMove.pack(
                TileSource.FACTORY_1,
                TileKind.Colored.A,
                TileDestination.FLOOR
        );
        gameState.registerMove(pkMove);

        assertEquals(PkTileSet.EMPTY, gameState.pkTileSources().get(TileSource.FACTORY_1.index()));

        int expectedCenter = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        expectedCenter = PkTileSet.union(expectedCenter, PkTileSet.of(1, TileKind.Colored.B));
        expectedCenter = PkTileSet.union(expectedCenter, PkTileSet.of(1, TileKind.Colored.C));
        assertEquals(expectedCenter, gameState.pkTileSources().get(TileSource.CENTER_AREA.index()));

        int p1Floor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), PlayerId.P1);
        assertEquals(2, PkTileSet.countOf(PkFloor.asPkTileSet(p1Floor), TileKind.Colored.A));

        assertEquals(PlayerId.P2, gameState.currentPlayerId());
    }

    @Test
    void mutableGameStateRegisterMoveFromCenterTakesFirstPlayerMarker() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        pkTileSources[0] = PkTileSet.union(pkTileSources[0], PkTileSet.of(3, TileKind.Colored.B));

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.add(PkIntSet32.EMPTY, 0),
                PkPlayerStates.initial(game),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState);

        short pkMove = PkMove.pack(
                TileSource.CENTER_AREA,
                TileKind.Colored.B,
                TileDestination.Pattern.PATTERN_5
        );
        gameState.registerMove(pkMove);

        assertEquals(PkTileSet.EMPTY, gameState.pkTileSources().get(TileSource.CENTER_AREA.index()));

        int p1Floor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), PlayerId.P1);
        assertTrue(PkFloor.containsFirstPlayerMarker(p1Floor));
        assertEquals(1, PkFloor.size(p1Floor));

        int p1Patterns = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), PlayerId.P1);
        assertEquals(3, PkPatterns.size(p1Patterns, TileDestination.Pattern.PATTERN_5));
        assertEquals(TileKind.Colored.B, PkPatterns.color(p1Patterns, TileDestination.Pattern.PATTERN_5));

        assertEquals(PlayerId.P2, gameState.currentPlayerId());
    }

    @Test
    void mutableGameStateRegisterMoveToPatternSendsOverflowToFloor() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        pkTileSources[1] = PkTileSet.of(4, TileKind.Colored.A);

        int[] pkPlayerStates = PkPlayerStates.initial(game).toArray();
        int pkPatterns = PkPatterns.EMPTY;
        pkPatterns = PkPatterns.withAddedTiles(
                pkPatterns,
                TileDestination.Pattern.PATTERN_3,
                2,
                TileKind.Colored.A
        );
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, pkPatterns);

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.add(PkIntSet32.EMPTY, 1),
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState);

        short pkMove = PkMove.pack(
                TileSource.FACTORY_1,
                TileKind.Colored.A,
                TileDestination.Pattern.PATTERN_3
        );
        gameState.registerMove(pkMove);

        int actualPatterns = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), PlayerId.P1);
        assertEquals(3, PkPatterns.size(actualPatterns, TileDestination.Pattern.PATTERN_3));
        assertEquals(TileKind.Colored.A, PkPatterns.color(actualPatterns, TileDestination.Pattern.PATTERN_3));

        int actualFloor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), PlayerId.P1);
        assertEquals(3, PkTileSet.countOf(PkFloor.asPkTileSet(actualFloor), TileKind.Colored.A));
    }

    @Test
    void mutableGameStateEndRoundDoesNotNotifyFloorWhenEffectivePenaltyIsZero() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int[] pkPlayerStates = PkPlayerStates.initial(game).toArray();
        int pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.A));
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P1, pkFloor);

        var observer = new TestPointsObserver();

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState, observer);
        gameState.endRound();

        assertEquals(0, observer.floorCalls);
        assertEquals(0, PkPlayerStates.points(gameState.pkPlayerStates(), PlayerId.P1));
        assertEquals(PkFloor.EMPTY, PkPlayerStates.pkFloor(gameState.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndRoundSetsNextStartingPlayerAndReturnsMarkerToCenter() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];

        int[] pkPlayerStates = PkPlayerStates.initial(game).toArray();
        int p2Floor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P2, p2Floor);

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState);
        gameState.endRound();

        assertEquals(PlayerId.P2, gameState.currentPlayerId());
        assertEquals(
                1,
                PkTileSet.countOf(
                        gameState.pkTileSources().get(TileSource.CENTER_AREA.index()),
                        TileKind.FIRST_PLAYER_MARKER
                )
        );
        assertEquals(PkFloor.EMPTY, PkPlayerStates.pkFloor(gameState.pkPlayerStates(), PlayerId.P2));
    }

    @Test
    void mutableGameStateEndRoundAwardsPointsForNewIsolatedWallTile() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int[] pkPlayerStates = PkPlayerStates.initial(game).toArray();
        int pkPatterns = PkPatterns.withAddedTiles(
                PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                1,
                TileKind.Colored.A
        );
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, pkPatterns);

        var observer = new TestPointsObserver();

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState, observer);
        gameState.endRound();

        int pkWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), PlayerId.P1);
        assertTrue(PkWall.hasTileAt(
                pkWall,
                TileDestination.Pattern.PATTERN_1,
                TileKind.Colored.A
        ));
        assertEquals(1, PkPlayerStates.points(gameState.pkPlayerStates(), PlayerId.P1));

        assertEquals(1, observer.newWallTileCalls);
        assertEquals(PlayerId.P1, observer.lastNewWallTilePlayerId);
        assertEquals(TileDestination.Pattern.PATTERN_1, observer.lastLine);
        assertEquals(TileKind.Colored.A, observer.lastColor);
        assertEquals(1, observer.lastPoints);
    }

    @Test
    void mutableGameStateEndGameAwardsAllBonusesAndNotifiesObserver() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int[] pkPlayerStates = PkPlayerStates.initial(game).toArray();
        int pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                pkWall = PkWall.withTileAt(pkWall, line, color);
            }
        }
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, pkWall);

        var observer = new TestPointsObserver();

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        var gameState = new MutableGameState(initialState, observer);
        gameState.endGame();

        int expectedBonus =
                5 * Points.FULL_ROW_BONUS_POINTS
                        + 5 * Points.FULL_COLUMN_BONUS_POINTS
                        + 5 * Points.FULL_COLOR_BONUS_POINTS;

        assertEquals(expectedBonus, PkPlayerStates.points(gameState.pkPlayerStates(), PlayerId.P1));
        assertEquals(5, observer.fullRowCalls);
        assertEquals(5, observer.fullColumnCalls);
        assertEquals(5, observer.fullColorCalls);
    }

    private static Game game(int playerCount) {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN)
        );
        return new Game(players.subList(0, playerCount));
    }

    private static final class TestPointsObserver implements PointsObserver {
        int newWallTileCalls;
        int floorCalls;
        int fullRowCalls;
        int fullColumnCalls;
        int fullColorCalls;

        PlayerId lastNewWallTilePlayerId;
        TileDestination.Pattern lastLine;
        TileKind.Colored lastColor;
        int lastPoints;

        @Override
        public void newWallTile(PlayerId playerId, TileDestination.Pattern line,
                                TileKind.Colored color, int points) {
            newWallTileCalls += 1;
            lastNewWallTilePlayerId = playerId;
            lastLine = line;
            lastColor = color;
            lastPoints = points;
        }

        @Override
        public void floor(PlayerId playerId, int penalty) {
            floorCalls += 1;
            lastPoints = penalty;
        }

        @Override
        public void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {
            fullRowCalls += 1;
        }

        @Override
        public void fullColumn(PlayerId playerId, int column, int points) {
            fullColumnCalls += 1;
        }

        @Override
        public void fullColor(PlayerId playerId, TileKind.Colored color, int points) {
            fullColorCalls += 1;
        }
    }

    @Test
    void mutableGameStateFillFactoriesWorksWhenBagContainsFewerThanNeededTiles() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int[] pkPlayerStates = PkPlayerStates.initial(game).toArray();
        int floor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(5, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P1, floor);

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.of(6, TileKind.Colored.A),
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        var state = new MutableGameState(initialState);
        var rng = RandomGeneratorFactory.getDefault().create(2027);

        state.fillFactories(rng);

        int totalInFactories = 0;
        for (var factory : game.factories()) {
            totalInFactories += PkTileSet.size(state.pkTileSources().get(factory.index()));
        }
        assertEquals(20, totalInFactories);
    }

    @Test
    void mutableGameStateRegisterMoveToFloorMovesAllTakenTiles() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[1] = PkTileSet.of(4, TileKind.Colored.D);
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.add(PkIntSet32.EMPTY, 1),
                PkPlayerStates.initial(game),
                PlayerId.P1
        );

        var state = new MutableGameState(initialState);

        short pkMove = PkMove.pack(
                TileSource.FACTORY_1,
                TileKind.Colored.D,
                TileDestination.FLOOR
        );
        state.registerMove(pkMove);

        int floor = PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P1);
        assertEquals(4, PkTileSet.countOf(PkFloor.asPkTileSet(floor), TileKind.Colored.D));
    }

    @Test
    void mutableGameStateEndRoundLeavesCurrentPlayerUnchangedWhenMarkerWasNotTaken() {
        var game = game(2);

        int[] pkTileSources = new int[game.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        var initialState = new ImmutableGameState(
                game,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                PkPlayerStates.initial(game),
                PlayerId.P2
        );

        var state = new MutableGameState(initialState);
        state.endRound();

        assertEquals(PlayerId.P2, state.currentPlayerId());
        assertEquals(
                1,
                PkTileSet.countOf(state.pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER)
        );
    }

    @Test
    void pointsObserverEmptyExists() {
        assertNotNull(PointsObserver.EMPTY);
    }

    @Test
    void pointsObserverEmptyMethodsDoNothing() {
        assertDoesNotThrow(() -> {
            PointsObserver.EMPTY.newWallTile(
                    PlayerId.P1,
                    TileDestination.Pattern.PATTERN_1,
                    TileKind.Colored.A,
                    1
            );
            PointsObserver.EMPTY.floor(PlayerId.P1, 2);
            PointsObserver.EMPTY.fullRow(
                    PlayerId.P1,
                    TileDestination.Pattern.PATTERN_1,
                    2
            );
            PointsObserver.EMPTY.fullColumn(PlayerId.P1, 0, 7);
            PointsObserver.EMPTY.fullColor(PlayerId.P1, TileKind.Colored.A, 10);
        });
    }

    @Test
    void mutableGameStateFillFactoriesOnlyColoredTilesInFactories() {
        ImmutableGameState initial = ImmutableGameState.initial(game(2));
        MutableGameState s = new MutableGameState(initial);
        s.fillFactories(RandomGeneratorFactory.getDefault().create(2026));

        for (int i = 1; i <= game(2).factoriesCount(); i += 1) {
            int source = s.pkTileSources().get(i);
            assertEquals(
                    0,
                    PkTileSet.countOf(source, TileKind.FIRST_PLAYER_MARKER),
                    "factory " + i + " should not contain the first player marker"
            );
        }
    }

    @Test
    void mutableGameStateFillFactoriesCentralAreaUntouched() {
        ImmutableGameState initial = ImmutableGameState.initial(game(2));
        MutableGameState s = new MutableGameState(initial);

        int centerBefore = s.pkTileSources().get(0);
        s.fillFactories(RandomGeneratorFactory.getDefault().create(2026));

        assertEquals(centerBefore, s.pkTileSources().get(0));
    }

    @Test
    void mutableGameStateRegisterMoveEmptiesSourceFactory() {
        ImmutableGameState initial = ImmutableGameState.initial(game(2));
        MutableGameState s = new MutableGameState(initial);
        s.fillFactories(RandomGeneratorFactory.getDefault().create(2026));

        short[] validMoves = new short[Move.MAX_MOVES];
        int moveCount = s.validMoves(validMoves);

        short pkMove = -1;
        TileSource source = null;

        for (int i = 0; i < moveCount; i += 1) {
            TileSource candidate = PkMove.source(validMoves[i]);
            if (candidate instanceof TileSource.Factory) {
                pkMove = validMoves[i];
                source = candidate;
                break;
            }
        }

        assertNotEquals(-1, pkMove, "there should be at least one move from a factory");

        s.registerMove(pkMove);

        assertEquals(
                PkTileSet.EMPTY,
                s.pkTileSources().get(source.index()),
                "factory should be empty after move"
        );
    }

    @Test
    void mutableGameStateEndRoundClearsFloors() {
        ImmutableGameState initial = ImmutableGameState.initial(game(2));
        MutableGameState s = new MutableGameState(initial);
        s.fillFactories(RandomGeneratorFactory.getDefault().create(2026));

        short[] validMoves = new short[Move.MAX_MOVES];
        while (!s.isRoundOver()) {
            int moveCount = s.validMoves(validMoves);
            assertTrue(moveCount > 0);
            s.registerMove(validMoves[0]);
        }

        s.endRound();

        for (PlayerId id : s.playerIds()) {
            int floor = PkPlayerStates.pkFloor(s.pkPlayerStates(), id);
            assertEquals(
                    PkFloor.EMPTY,
                    floor,
                    "floor of " + id + " should be empty after endRound"
            );
        }
    }

    @Test
    void mutableGameStateEndGameNoPointsForEmptyWall() {
        ImmutableGameState initial = ImmutableGameState.initial(game(2));
        MutableGameState s = new MutableGameState(initial);

        s.endGame();

        for (PlayerId id : s.playerIds()) {
            assertEquals(
                    0,
                    PkPlayerStates.points(s.pkPlayerStates(), id),
                    id + " should have 0 points with empty wall"
            );
        }
    }

    @Test
    void mutableGameStateRegisterMoveAddsToPatternLine() {
        ImmutableGameState initial = ImmutableGameState.initial(game(2));
        MutableGameState s = new MutableGameState(initial);
        s.fillFactories(RandomGeneratorFactory.getDefault().create(2026));

        short[] validMoves = new short[Move.MAX_MOVES];
        int moveCount = s.validMoves(validMoves);

        short patternMove = -1;
        for (int i = 0; i < moveCount; i += 1) {
            if (PkMove.destination(validMoves[i]) instanceof TileDestination.Pattern) {
                patternMove = validMoves[i];
                break;
            }
        }

        assertNotEquals(-1, patternMove, "there should be at least one move to a pattern line");

        PlayerId player = s.currentPlayerId();
        int patternsBefore = PkPlayerStates.pkPatterns(s.pkPlayerStates(), player);

        s.registerMove(patternMove);

        int patternsAfter = PkPlayerStates.pkPatterns(s.pkPlayerStates(), player);
        assertNotEquals(patternsBefore, patternsAfter);
    }

    @Test
    void mutableGameStateEndRoundDeductsFloorPenaltyFromScore() {
        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, 10);

        int floor = PkFloor.withAddedTiles(
                PkFloor.EMPTY,
                PkTileSet.of(3, TileKind.Colored.B)
        );
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P1, floor);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial);
        s.endRound();

        int points = PkPlayerStates.points(s.pkPlayerStates(), PlayerId.P1);
        assertEquals(6, points);
    }

    @Test
    void mutableGameStateEndGameAddsPointsForFullRow() {
        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, color);
        }
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial);
        s.endGame();

        int points = PkPlayerStates.points(s.pkPlayerStates(), PlayerId.P1);
        assertEquals(Points.FULL_ROW_BONUS_POINTS, points);
    }

    @Test
    void mutableGameStateEndGameAddsPointsForFullColumn() {
        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, line, PkWall.colorAt(line, 0));
        }
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial);
        s.endGame();

        int points = PkPlayerStates.points(s.pkPlayerStates(), PlayerId.P1);
        assertEquals(Points.FULL_COLUMN_BONUS_POINTS, points);
    }

    @Test
    void mutableGameStateEndGameAddsPointsForFullColor() {
        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, line, TileKind.Colored.A);
        }
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial);
        s.endGame();

        int points = PkPlayerStates.points(s.pkPlayerStates(), PlayerId.P1);
        assertEquals(Points.FULL_COLOR_BONUS_POINTS, points);
    }

    @Test
    void mutableGameStateImmutableReturnsCorrectState() {
        ImmutableGameState initial = ImmutableGameState.initial(game(2));
        MutableGameState s = new MutableGameState(initial);

        ImmutableGameState copy = s.immutable();

        assertEquals(s.game(), copy.game());
        assertEquals(s.pkTileBag(), copy.pkTileBag());
        assertEquals(s.currentPlayerId(), copy.currentPlayerId());
        assertArrayEquals(s.pkTileSources().toArray(), copy.pkTileSources().toArray());
        assertArrayEquals(s.pkPlayerStates().toArray(), copy.pkPlayerStates().toArray());
    }

    @Test
    void mutableGameStateEndRoundCallsObserverForNewWallTile() {
        List<TileDestination.Pattern> notifiedLines = new ArrayList<>();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void newWallTile(PlayerId playerId, TileDestination.Pattern line,
                                    TileKind.Colored color, int points) {
                notifiedLines.add(line);
            }
        };

        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int pkPatterns = PkPatterns.withAddedTiles(
                PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                1,
                TileKind.Colored.A
        );
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, pkPatterns);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial, observer);
        s.endRound();

        assertTrue(notifiedLines.contains(TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void mutableGameStateEndGameCallsObserverForFullRow() {
        List<TileDestination.Pattern> fullRows = new ArrayList<>();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {
                fullRows.add(line);
            }
        };

        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, color);
        }
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial, observer);
        s.endGame();

        assertTrue(fullRows.contains(TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void mutableGameStateEndGameCallsObserverForFullColumn() {
        List<Integer> fullColumns = new ArrayList<>();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void fullColumn(PlayerId playerId, int column, int points) {
                fullColumns.add(column);
            }
        };

        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, line, PkWall.colorAt(line, 0));
        }
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial, observer);
        s.endGame();

        assertTrue(fullColumns.contains(0));
    }

    @Test
    void mutableGameStateEndGameCallsObserverForFullColor() {
        List<TileKind.Colored> fullColors = new ArrayList<>();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void fullColor(PlayerId playerId, TileKind.Colored color, int points) {
                fullColors.add(color);
            }
        };

        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, line, TileKind.Colored.A);
        }
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial, observer);
        s.endGame();

        assertTrue(fullColors.contains(TileKind.Colored.A));
    }

    @Test
    void mutableGameStateRegisterMoveUpdatesUniqueTileSourcesWhenTwoSourcesBecomeIdentical() {
        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        pkTileSources[1] = PkTileSet.union(
                PkTileSet.of(2, TileKind.Colored.A),
                PkTileSet.of(2, TileKind.Colored.B)
        );
        pkTileSources[2] = PkTileSet.of(2, TileKind.Colored.B);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                PkPlayerStates.initial(g),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial);

        short pkMove = PkMove.pack(
                TileSource.ALL.get(1),
                TileKind.Colored.A,
                TileDestination.FLOOR
        );
        s.registerMove(pkMove);

        int unique = s.pkUniqueTileSources();

        assertTrue(PkIntSet32.contains(unique, 0));
        assertTrue(PkIntSet32.contains(unique, 1) || PkIntSet32.contains(unique, 2));
        assertFalse(PkIntSet32.contains(unique, 1) && PkIntSet32.contains(unique, 2));
    }

    @Test
    void mutableGameStateEndRoundAwardsHorizontalGroupPoints() {
        Game g = game(2);

        TileDestination.Pattern targetLine = null;
        TileKind.Colored targetColor = null;
        int targetColumn = -1;

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            for (TileKind.Colored color : TileKind.Colored.ALL) {
                int column = PkWall.column(line, color);
                if (1 <= column && column <= 3) {
                    targetLine = line;
                    targetColor = color;
                    targetColumn = column;
                    break;
                }
            }
            if (targetLine != null) {
                break;
            }
        }

        assertNotNull(targetLine);
        assertNotNull(targetColor);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;

        TileKind.Colored leftColor = PkWall.colorAt(targetLine, targetColumn - 1);
        TileKind.Colored rightColor = PkWall.colorAt(targetLine, targetColumn + 1);

        wall = PkWall.withTileAt(wall, targetLine, leftColor);
        wall = PkWall.withTileAt(wall, targetLine, rightColor);

        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        int patterns = PkPatterns.withAddedTiles(
                PkPatterns.EMPTY,
                targetLine,
                targetLine.capacity(),
                targetColor
        );
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, patterns);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial);
        s.endRound();

        int points = PkPlayerStates.points(s.pkPlayerStates(), PlayerId.P1);
        assertEquals(3, points);
    }

    @Test
    void mutableGameStateEndGameCanAccumulateRowAndColumnBonuses() {
        Game g = game(2);

        int[] pkTileSources = new int[g.tileSourcesCount()];
        int[] pkPlayerStates = PkPlayerStates.initial(g).toArray();

        int wall = PkWall.EMPTY;

        for (TileKind.Colored color : TileKind.Colored.ALL) {
            wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, color);
        }

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, line, PkWall.colorAt(line, 0));
        }

        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        ImmutableGameState initial = new ImmutableGameState(
                g,
                PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(pkPlayerStates),
                PlayerId.P1
        );

        MutableGameState s = new MutableGameState(initial);
        s.endGame();

        int expected =
                Points.FULL_ROW_BONUS_POINTS
                        + Points.FULL_COLUMN_BONUS_POINTS;

        int points = PkPlayerStates.points(s.pkPlayerStates(), PlayerId.P1);
        assertEquals(expected, points);
    }
}