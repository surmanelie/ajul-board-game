
package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

final class MyPkTileSetTest {

    @Test
    void emptyHasZeroSizeAndIsEmpty() {
        assertTrue(PkTileSet.isEmpty(PkTileSet.EMPTY));
        assertEquals(0, PkTileSet.size(PkTileSet.EMPTY));
        for (var c : TileKind.Colored.ALL) {
            assertEquals(0, PkTileSet.countOf(PkTileSet.EMPTY, c));
        }
        assertEquals(0, PkTileSet.countOf(PkTileSet.EMPTY, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void fullColoredHasTwentyOfEachColorAndNoMarker() {
        int s = PkTileSet.FULL_COLORED;
        for (var c : TileKind.Colored.ALL) {
            assertEquals(20, PkTileSet.countOf(s, c));
        }
        assertEquals(0, PkTileSet.countOf(s, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(100, PkTileSet.size(s)); // 5 * 20
    }

    @Test
    void fullHasTwentyOfEachColorAndMarker() {
        int s = PkTileSet.FULL;
        for (var c : TileKind.Colored.ALL) {
            assertEquals(20, PkTileSet.countOf(s, c));
        }
        assertEquals(1, PkTileSet.countOf(s, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(101, PkTileSet.size(s));
    }

    @Test
    void ofAndCountOfAreConsistent() {
        int s = PkTileSet.of(7, TileKind.Colored.C);
        assertEquals(7, PkTileSet.countOf(s, TileKind.Colored.C));
        assertEquals(0, PkTileSet.countOf(s, TileKind.Colored.A));
        assertEquals(0, PkTileSet.countOf(s, TileKind.FIRST_PLAYER_MARKER));

        int m = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        assertEquals(1, PkTileSet.countOf(m, TileKind.FIRST_PLAYER_MARKER));
        for (var c : TileKind.Colored.ALL) {
            assertEquals(0, PkTileSet.countOf(m, c));
        }
    }

    @Test
    void addAndRemoveChangeCountsByOne() {
        int s = PkTileSet.EMPTY;

        s = PkTileSet.add(s, TileKind.Colored.B);
        assertEquals(1, PkTileSet.countOf(s, TileKind.Colored.B));

        s = PkTileSet.add(s, TileKind.Colored.B);
        assertEquals(2, PkTileSet.countOf(s, TileKind.Colored.B));

        s = PkTileSet.remove(s, TileKind.Colored.B);
        assertEquals(1, PkTileSet.countOf(s, TileKind.Colored.B));

        s = PkTileSet.add(s, TileKind.FIRST_PLAYER_MARKER);
        assertEquals(1, PkTileSet.countOf(s, TileKind.FIRST_PLAYER_MARKER));

        s = PkTileSet.remove(s, TileKind.FIRST_PLAYER_MARKER);
        assertEquals(0, PkTileSet.countOf(s, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void unionAndDifferenceBehaveOnSmallExample() {
        int a = PkTileSet.of(2, TileKind.Colored.A);
        int b = PkTileSet.of(3, TileKind.Colored.A);

        int u = PkTileSet.union(a, b);
        assertEquals(5, PkTileSet.countOf(u, TileKind.Colored.A));

        int d = PkTileSet.difference(u, a);
        assertEquals(3, PkTileSet.countOf(d, TileKind.Colored.A));
    }

    @Test
    void subsetOfExtractsOnlyThatKind() {
        int s = PkTileSet.union(
                PkTileSet.of(4, TileKind.Colored.D),
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));

        int onlyD = PkTileSet.subsetOf(s, TileKind.Colored.D);
        assertEquals(4, PkTileSet.countOf(onlyD, TileKind.Colored.D));
        assertEquals(0, PkTileSet.countOf(onlyD, TileKind.FIRST_PLAYER_MARKER));

        int onlyM = PkTileSet.subsetOf(s, TileKind.FIRST_PLAYER_MARKER);
        assertEquals(1, PkTileSet.countOf(onlyM, TileKind.FIRST_PLAYER_MARKER));
        for (var c : TileKind.Colored.ALL) {
            assertEquals(0, PkTileSet.countOf(onlyM, c));
        }
    }

    @Test
    void copyColoredIntoWritesInColorOrderAndReturnsOffset() {
        int s = PkTileSet.union(
                PkTileSet.of(2, TileKind.Colored.A),
                PkTileSet.of(1, TileKind.Colored.C));

        TileKind.Colored[] dst = new TileKind.Colored[3];
        int end = PkTileSet.copyColoredInto(s, dst);

        assertEquals(3, end);
        assertArrayEquals(
                new TileKind.Colored[]{TileKind.Colored.A, TileKind.Colored.A, TileKind.Colored.C},
                dst
        );
    }

    @Test
    void sampleColoredIntoFillsSegmentAndIsDeterministicWithSeed() {
        // Build: 5 tiles total (A,A,B,C,D)
        int s = PkTileSet.EMPTY;
        s = PkTileSet.add(s, TileKind.Colored.A);
        s = PkTileSet.add(s, TileKind.Colored.A);
        s = PkTileSet.add(s, TileKind.Colored.B);
        s = PkTileSet.add(s, TileKind.Colored.C);
        s = PkTileSet.add(s, TileKind.Colored.D);

        var rng = RandomGeneratorFactory.getDefault().create(2026);

        TileKind.Colored[] dst = new TileKind.Colored[5];
        int end = PkTileSet.sampleColoredInto(s, dst, 0, rng);

        assertEquals(5, end);

        // We don't force an exact permutation here (depends on exact reservoir implementation),
        // but we ensure the result is a permutation of the multiset.
        TileKind.Colored[] sorted = dst.clone();
        Arrays.sort(sorted);
        assertArrayEquals(
                new TileKind.Colored[]{TileKind.Colored.A, TileKind.Colored.A, TileKind.Colored.B, TileKind.Colored.C, TileKind.Colored.D},
                sorted
        );
    }

    @Test
    void toStringContainsAllNonZeroKindsInOrder() {
        int s = PkTileSet.union(
                PkTileSet.union(PkTileSet.of(1, TileKind.Colored.A), PkTileSet.of(2, TileKind.Colored.E)),
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));

        String t = PkTileSet.toString(s);

        // Basic structure
        assertTrue(t.startsWith("{"));
        assertTrue(t.endsWith("}"));

        // Order constraints: A before E before FIRST_PLAYER_MARKER
        int a = t.indexOf("1*A");
        int e = t.indexOf("2*E");
        int m = t.indexOf("1*FIRST_PLAYER_MARKER");

        assertTrue(a >= 0);
        assertTrue(e >= 0);
        assertTrue(m >= 0);
        assertTrue(a < e);
        assertTrue(e < m);
    }
}