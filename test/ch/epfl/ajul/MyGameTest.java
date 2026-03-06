package ch.epfl.ajul;

import ch.epfl.ajul.Game.PlayerDescription;
import ch.epfl.ajul.Game.PlayerDescription.PlayerKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de {@link Game}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class MyGameTest {

    private static PlayerDescription pd(int index, String name, PlayerKind kind) {
        return new PlayerDescription(PlayerId.ALL.get(index), name, kind);
    }

    @Test
    void constructorRejectsNullList() {
        assertThrows(NullPointerException.class, () -> new Game(null));
    }

    @Test
    void constructorRejectsEmptyPlayersList() {
        assertThrows(IllegalArgumentException.class, () -> new Game(List.of()));
    }

    @Test
    void constructorRejectsSinglePlayer() {
        assertThrows(IllegalArgumentException.class,
                () -> new Game(List.of(pd(0, "A", PlayerKind.HUMAN))));
    }

    @Test
    void constructorRejectsWrongPlayerIdOrder() {
        // ordre incorrect : P2 puis P1
        List<PlayerDescription> wrong = List.of(
                new PlayerDescription(PlayerId.ALL.get(1), "B", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.ALL.get(0), "A", PlayerKind.HUMAN)
        );
        assertThrows(IllegalArgumentException.class, () -> new Game(wrong));
    }

    @Test
    void constructorRejectsListContainingNullDescription() {
        List<PlayerDescription> l = new ArrayList<>();
        l.add(pd(0, "A", PlayerKind.HUMAN));
        l.add(null);
        assertThrows(NullPointerException.class, () -> new Game(l));
    }

    @Test
    void gameForTwoPlayersHasCorrectDerivedValues() {
        Game g = new Game(List.of(
                pd(0, "A", PlayerKind.HUMAN),
                pd(1, "B", PlayerKind.AI)
        ));

        assertEquals(2, g.playersCount());
        assertEquals(PlayerId.ALL.subList(0, 2), g.playerIds());

        // 2 joueurs -> 2n+1 = 5 fabriques
        assertEquals(5, g.factoriesCount());
        assertEquals(TileSource.Factory.ALL.subList(0, 5), g.factories());

        // sources = centre + fabriques = 1 + 5 = 6
        assertEquals(6, g.tileSourcesCount());
        assertEquals(TileSource.ALL.subList(0, 6), g.tileSources());

        // max centre = 3m+1 = 3*5 + 1 = 16
        assertEquals(16, g.centralAreaMaxSize());
    }

    @Test
    void gameForThreePlayersHasCorrectDerivedValues() {
        Game g = new Game(List.of(
                pd(0, "A", PlayerKind.HUMAN),
                pd(1, "B", PlayerKind.HUMAN),
                pd(2, "C", PlayerKind.AI)
        ));

        assertEquals(3, g.playersCount());
        assertEquals(PlayerId.ALL.subList(0, 3), g.playerIds());

        // 3 joueurs -> 7 fabriques, 8 sources, max centre = 22
        assertEquals(7, g.factoriesCount());
        assertEquals(TileSource.Factory.ALL.subList(0, 7), g.factories());

        assertEquals(8, g.tileSourcesCount());
        assertEquals(TileSource.ALL.subList(0, 8), g.tileSources());

        assertEquals(22, g.centralAreaMaxSize());
    }

    @Test
    void gameForFourPlayersHasCorrectDerivedValues() {
        Game g = new Game(List.of(
                pd(0, "A", PlayerKind.HUMAN),
                pd(1, "B", PlayerKind.HUMAN),
                pd(2, "C", PlayerKind.HUMAN),
                pd(3, "D", PlayerKind.AI)
        ));

        assertEquals(4, g.playersCount());
        assertEquals(PlayerId.ALL.subList(0, 4), g.playerIds());

        // 4 joueurs -> 9 fabriques, 10 sources, max centre = 28
        assertEquals(9, g.factoriesCount());
        assertEquals(TileSource.Factory.ALL.subList(0, 9), g.factories());

        assertEquals(10, g.tileSourcesCount());
        assertEquals(TileSource.ALL.subList(0, 10), g.tileSources());

        assertEquals(28, g.centralAreaMaxSize());
    }

    @Test
    void playerDescriptionsContainsExactlyTheGivenDescriptions() {
        var d1 = pd(0, "A", PlayerKind.HUMAN);
        var d2 = pd(1, "B", PlayerKind.AI);
        Game g = new Game(List.of(d1, d2));

        assertEquals(List.of(d1, d2), g.playerDescriptions());
    }

    @Test
    void playerDescriptionsListIsUnmodifiableCopy() {
        List<PlayerDescription> src = new ArrayList<>(List.of(
                pd(0, "A", PlayerKind.HUMAN),
                pd(1, "B", PlayerKind.AI)
        ));
        Game g = new Game(src);

        // modifier la liste source ne doit pas affecter le Game
        src.set(0, pd(0, "X", PlayerKind.AI));
        assertEquals("A", g.playerDescriptions().get(0).name());

        // la liste retournée doit être non modifiable
        assertThrows(UnsupportedOperationException.class,
                () -> g.playerDescriptions().add(pd(0, "Z", PlayerKind.HUMAN)));
    }
}