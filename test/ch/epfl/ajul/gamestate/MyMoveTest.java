
package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class MyMoveTest {

    @Test
    void packedRoundTripPreservesMove() {
        for (var s : TileSource.ALL) {
            for (var c : TileKind.Colored.ALL) {
                for (var d : TileDestination.ALL) {
                    Move m = new Move(s, c, d);
                    assertEquals(m, Move.ofPacked(m.packed()));
                }
            }
        }
    }

    @Test
    void moveRejectsNulls() {
        assertThrows(NullPointerException.class, () -> new Move(null, TileKind.Colored.A, TileDestination.PATTERN_1));
        assertThrows(NullPointerException.class, () -> new Move(TileSource.CENTER_AREA, null, TileDestination.PATTERN_1));
        assertThrows(NullPointerException.class, () -> new Move(TileSource.CENTER_AREA, TileKind.Colored.A, null));
    }
}
