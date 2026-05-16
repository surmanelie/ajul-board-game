package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyPointsObserverTest {

    @Test
    void emptyConstantIsNotNull() {
        assertNotNull(PointsObserver.EMPTY);
    }

    @Test
    void emptyNewWallTileDoesNotThrow() {
        assertDoesNotThrow(() ->
                PointsObserver.EMPTY.newWallTile(
                        PlayerId.P1,
                        TileDestination.Pattern.PATTERN_1,
                        TileKind.Colored.A,
                        1));
    }

    @Test
    void emptyFloorDoesNotThrow() {
        assertDoesNotThrow(() ->
                PointsObserver.EMPTY.floor(PlayerId.P1, 3));
    }

    @Test
    void emptyFullRowDoesNotThrow() {
        assertDoesNotThrow(() ->
                PointsObserver.EMPTY.fullRow(
                        PlayerId.P1,
                        TileDestination.Pattern.PATTERN_5,
                        2));
    }

    @Test
    void emptyFullColumnDoesNotThrow() {
        assertDoesNotThrow(() ->
                PointsObserver.EMPTY.fullColumn(PlayerId.P2, 3, 7));
    }

    @Test
    void emptyFullColorDoesNotThrow() {
        assertDoesNotThrow(() ->
                PointsObserver.EMPTY.fullColor(PlayerId.P1, TileKind.Colored.E, 10));
    }

    @Test
    void customObserverReceivesNewWallTileEvent() {
        var log = new StringBuilder();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void newWallTile(PlayerId playerId, TileDestination.Pattern line,
                                    TileKind.Colored color, int points) {
                log.append("wall:").append(playerId).append(":").append(points);
            }
        };
        observer.newWallTile(PlayerId.P1, TileDestination.Pattern.PATTERN_2, TileKind.Colored.B, 5);
        assertEquals("wall:P1:5", log.toString());
    }

    @Test
    void customObserverReceivesFloorEvent() {
        var log = new StringBuilder();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void floor(PlayerId playerId, int penalty) {
                log.append("floor:").append(playerId).append(":").append(penalty);
            }
        };
        observer.floor(PlayerId.P2, 4);
        assertEquals("floor:P2:4", log.toString());
    }

    @Test
    void customObserverReceivesFullRowEvent() {
        var log = new StringBuilder();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {
                log.append("row:").append(points);
            }
        };
        observer.fullRow(PlayerId.P1, TileDestination.Pattern.PATTERN_3, 2);
        assertEquals("row:2", log.toString());
    }

    @Test
    void customObserverReceivesFullColumnEvent() {
        var log = new StringBuilder();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void fullColumn(PlayerId playerId, int column, int points) {
                log.append("col:").append(column).append(":").append(points);
            }
        };
        observer.fullColumn(PlayerId.P2, 3, 7);
        assertEquals("col:3:7", log.toString());
    }

    @Test
    void customObserverReceivesFullColorEvent() {
        var log = new StringBuilder();
        PointsObserver observer = new PointsObserver() {
            @Override
            public void fullColor(PlayerId playerId, TileKind.Colored color, int points) {
                log.append("color:").append(color).append(":").append(points);
            }
        };
        observer.fullColor(PlayerId.P1, TileKind.Colored.C, 10);
        assertEquals("color:C:10", log.toString());
    }

    @Test
    void defaultMethodsDoNothingOnEmptyObserver() {
        // These should complete silently with no side effects
        for (var player : PlayerId.ALL.subList(0, 2)) {
            PointsObserver.EMPTY.newWallTile(player, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A, 1);
            PointsObserver.EMPTY.floor(player, 2);
            PointsObserver.EMPTY.fullRow(player, TileDestination.Pattern.PATTERN_1, 2);
            PointsObserver.EMPTY.fullColumn(player, 0, 7);
            PointsObserver.EMPTY.fullColor(player, TileKind.Colored.A, 10);
        }
    }
}
