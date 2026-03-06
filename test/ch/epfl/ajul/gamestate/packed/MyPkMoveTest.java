
package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class MyPkMoveTest {

    @Test
    void packThenUnpackGivesSameFields() {
        for (var s : TileSource.ALL) {
            for (var c : TileKind.Colored.ALL) {
                for (var d : TileDestination.ALL) {
                    short pk = PkMove.pack(s, c, d);
                    assertSame(s, PkMove.source(pk));
                    assertSame(c, PkMove.color(pk));
                    assertSame(d, PkMove.destination(pk));
                }
            }
        }
    }

    @Test
    void packIsDeterministic() {
        short a = PkMove.pack(TileSource.FACTORY_3, TileKind.Colored.D, TileDestination.PATTERN_4);
        short b = PkMove.pack(TileSource.FACTORY_3, TileKind.Colored.D, TileDestination.PATTERN_4);
        assertEquals(a, b);
    }
}