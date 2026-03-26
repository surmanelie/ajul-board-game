package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Game.PlayerDescription;
import ch.epfl.ajul.Game.PlayerDescription.PlayerKind;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyPkPlayerStatesTest {

    // --- Helpers ---

    private static Game gameWithNPlayers(int n) {
        // Hypothèse projet Ajul: 2..4 joueurs
        var desc = new ArrayList<PlayerDescription>(n);
        for (int i = 0; i < n; i++) {
            PlayerId id = PlayerId.ALL.get(i);
            desc.add(new PlayerDescription(id, "P" + (i + 1), PlayerKind.HUMAN));
        }
        return new Game(List.copyOf(desc));
    }

    private static int base(PlayerId pid) {
        return 4 * pid.ordinal();
    }

    // --- Tests ---

    @Test
    void initialHasCorrectSizeAndAllZerosForAllValidPlayerCounts() {
        for (int n = 2; n <= 4; n++) {
            Game g = gameWithNPlayers(n);

            ImmutableIntArray ps = PkPlayerStates.initial(g);

            assertEquals(4 * n, ps.size());
            for (int i = 0; i < ps.size(); i++) {
                assertEquals(0, ps.get(i));
            }
        }
    }

    @Test
    void gettersReadCorrectSlotsForEachPlayer() {
        Game g = gameWithNPlayers(4);

        // On crée un tableau "contrôlé" : chaque slot reçoit une valeur unique.
        int[] raw = new int[4 * g.playersCount()];
        for (int i = 0; i < raw.length; i++) raw[i] = 10_000 + i;

        var ps = ImmutableIntArray.copyOf(raw);

        for (PlayerId pid : g.playerIds()) {
            int b = base(pid);
            assertEquals(raw[b + 0], PkPlayerStates.pkPatterns(ps, pid));
            assertEquals(raw[b + 1], PkPlayerStates.pkFloor(ps, pid));
            assertEquals(raw[b + 2], PkPlayerStates.pkWall(ps, pid));
            assertEquals(raw[b + 3], PkPlayerStates.points(ps, pid));
        }
    }

    @Test
    void settersOnlyModifyTheTargetSlot() {
        Game g = gameWithNPlayers(3);
        int n = g.playersCount();

        int[] raw = new int[4 * n];
        for (int i = 0; i < raw.length; i++) raw[i] = i; // sentinelles

        PlayerId p2 = g.playerIds().get(1); // P2
        int b = base(p2);

        PkPlayerStates.setPkPatterns(raw, p2, 111);
        assertEquals(111, raw[b + 0]);
        assertEquals(b + 1, raw[b + 1]);
        assertEquals(b + 2, raw[b + 2]);
        assertEquals(b + 3, raw[b + 3]);

        PkPlayerStates.setPkFloor(raw, p2, 222);
        assertEquals(111, raw[b + 0]);
        assertEquals(222, raw[b + 1]);
        assertEquals(b + 2, raw[b + 2]);
        assertEquals(b + 3, raw[b + 3]);

        PkPlayerStates.setPkWall(raw, p2, 333);
        assertEquals(111, raw[b + 0]);
        assertEquals(222, raw[b + 1]);
        assertEquals(333, raw[b + 2]);
        assertEquals(b + 3, raw[b + 3]);

        // Vérifie aussi que les autres joueurs n'ont pas bougé
        for (PlayerId pid : g.playerIds()) {
            if (pid == p2) continue;
            int bb = base(pid);
            assertEquals(bb + 0, raw[bb + 0]);
            assertEquals(bb + 1, raw[bb + 1]);
            assertEquals(bb + 2, raw[bb + 2]);
            assertEquals(bb + 3, raw[bb + 3]);
        }
    }

    @Test
    void addPointsAccumulatesAndCanBeNegativeWithoutAffectingOtherPlayers() {
        Game g = gameWithNPlayers(4);
        int[] raw = new int[4 * g.playersCount()];

        PlayerId p3 = g.playerIds().get(2);
        int b = base(p3);

        assertEquals(0, raw[b + 3]);

        PkPlayerStates.addPoints(raw, p3, 10);
        assertEquals(10, raw[b + 3]);

        PkPlayerStates.addPoints(raw, p3, -4);
        assertEquals(6, raw[b + 3]);

        PkPlayerStates.addPoints(raw, p3, 0);
        assertEquals(6, raw[b + 3]);

        // Personne d'autre ne doit avoir changé
        for (PlayerId pid : g.playerIds()) {
            if (pid == p3) continue;
            assertEquals(0, raw[base(pid) + 3]);
        }
    }

    @Test
    void randomScenarioMatchesDirectArrayAccess() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        Game g = gameWithNPlayers(4);

        for (int t = 0; t < 200; t++) {
            int[] raw = new int[4 * g.playersCount()];
            for (int i = 0; i < raw.length; i++) raw[i] = rng.nextInt();

            var ps = ImmutableIntArray.copyOf(raw);

            for (PlayerId pid : g.playerIds()) {
                int b = base(pid);
                assertEquals(raw[b + 0], PkPlayerStates.pkPatterns(ps, pid));
                assertEquals(raw[b + 1], PkPlayerStates.pkFloor(ps, pid));
                assertEquals(raw[b + 2], PkPlayerStates.pkWall(ps, pid));
                assertEquals(raw[b + 3], PkPlayerStates.points(ps, pid));
            }
        }
    }

    @Test
    void initialIsIndependentFromLaterGameListMutations() {
        var players = new java.util.ArrayList<ch.epfl.ajul.Game.PlayerDescription>();
        players.add(new ch.epfl.ajul.Game.PlayerDescription(ch.epfl.ajul.PlayerId.P1, "P1", ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN));
        players.add(new ch.epfl.ajul.Game.PlayerDescription(ch.epfl.ajul.PlayerId.P2, "P2", ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN));
        var g = new ch.epfl.ajul.Game(players);

        var ps = ch.epfl.ajul.gamestate.packed.PkPlayerStates.initial(g);
        players.clear(); // si Game était mal défensif, ça pourrait casser plus loin

        assertEquals(8, ps.size());
        for (int i = 0; i < ps.size(); i++) assertEquals(0, ps.get(i));
    }

    @Test
    void baseIndexWorksOnFirstAndLastPlayer() {
        var g = gameWithNPlayers(4);
        int[] raw = new int[16];
        // Sentinelles faciles à lire
        raw[0] = 100; raw[1] = 101; raw[2] = 102; raw[3] = 103;       // P1
        raw[12] = 400; raw[13] = 401; raw[14] = 402; raw[15] = 403;   // P4

        var ps = ch.epfl.ajul.intarray.ImmutableIntArray.copyOf(raw);

        assertEquals(100, ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkPatterns(ps, ch.epfl.ajul.PlayerId.P1));
        assertEquals(103, ch.epfl.ajul.gamestate.packed.PkPlayerStates.points(ps, ch.epfl.ajul.PlayerId.P1));
        assertEquals(400, ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkPatterns(ps, ch.epfl.ajul.PlayerId.P4));
        assertEquals(403, ch.epfl.ajul.gamestate.packed.PkPlayerStates.points(ps, ch.epfl.ajul.PlayerId.P4));
    }

    @Test
    void addPointsDoesNotChangeOtherFields() {
        var g = gameWithNPlayers(2);
        int[] raw = new int[8];
        // fixe des valeurs non nulles
        raw[0] = 11; raw[1] = 22; raw[2] = 33; raw[3] = 44;

        ch.epfl.ajul.gamestate.packed.PkPlayerStates.addPoints(raw, ch.epfl.ajul.PlayerId.P1, 5);

        assertEquals(11, raw[0]);
        assertEquals(22, raw[1]);
        assertEquals(33, raw[2]);
        assertEquals(49, raw[3]);
    }

    @Test
    void settersWorkOnBoundaryPlayersP1AndP4WithoutAffectingOthers() {
        Game g = gameWithNPlayers(4);
        int[] raw = new int[4 * g.playersCount()];
        for (int i = 0; i < raw.length; i++) raw[i] = 1_000 + i; // sentinelles

        // --- P1 (début du tableau) ---
        PlayerId p1 = PlayerId.P1;
        int b1 = base(p1);

        PkPlayerStates.setPkPatterns(raw, p1, 111_111);
        assertEquals(111_111, raw[b1 + 0]);
        assertEquals(1_000 + (b1 + 1), raw[b1 + 1]);
        assertEquals(1_000 + (b1 + 2), raw[b1 + 2]);
        assertEquals(1_000 + (b1 + 3), raw[b1 + 3]);

        PkPlayerStates.setPkFloor(raw, p1, 222_222);
        assertEquals(111_111, raw[b1 + 0]);
        assertEquals(222_222, raw[b1 + 1]);
        assertEquals(1_000 + (b1 + 2), raw[b1 + 2]);
        assertEquals(1_000 + (b1 + 3), raw[b1 + 3]);

        PkPlayerStates.setPkWall(raw, p1, 333_333);
        assertEquals(111_111, raw[b1 + 0]);
        assertEquals(222_222, raw[b1 + 1]);
        assertEquals(333_333, raw[b1 + 2]);
        assertEquals(1_000 + (b1 + 3), raw[b1 + 3]);

        PkPlayerStates.addPoints(raw, p1, 7);
        assertEquals(111_111, raw[b1 + 0]);
        assertEquals(222_222, raw[b1 + 1]);
        assertEquals(333_333, raw[b1 + 2]);
        assertEquals((1_000 + (b1 + 3)) + 7, raw[b1 + 3]);

        // --- P4 (fin du tableau) ---
        PlayerId p4 = PlayerId.P4;
        int b4 = base(p4);

        PkPlayerStates.setPkWall(raw, p4, 444_444);
        assertEquals(444_444, raw[b4 + 2]);

        PkPlayerStates.addPoints(raw, p4, -3);
        assertEquals((1_000 + (b4 + 3)) - 3, raw[b4 + 3]);

        // --- Vérifie que P2 et P3 sont inchangés (hors leurs sentinelles) ---
        for (PlayerId pid : List.of(PlayerId.P2, PlayerId.P3)) {
            int bb = base(pid);
            assertEquals(1_000 + (bb + 0), raw[bb + 0]);
            assertEquals(1_000 + (bb + 1), raw[bb + 1]);
            assertEquals(1_000 + (bb + 2), raw[bb + 2]);
            assertEquals(1_000 + (bb + 3), raw[bb + 3]);
        }
    }

    @Test
    void multiPlayerMixedOperationsDoNotInterfereAndUseCorrectOffsets() {
        Game g = gameWithNPlayers(4);
        int[] raw = new int[4 * g.playersCount()];
        for (int i = 0; i < raw.length; i++) raw[i] = 10_000 + i; // sentinelles

        PlayerId p1 = PlayerId.P1;
        PlayerId p2 = PlayerId.P2;
        PlayerId p3 = PlayerId.P3;
        PlayerId p4 = PlayerId.P4;

        int b1 = base(p1), b2 = base(p2), b3 = base(p3), b4 = base(p4);

        // Opérations mélangées
        PkPlayerStates.setPkPatterns(raw, p1, 101);
        PkPlayerStates.setPkFloor(raw, p1, 102);
        PkPlayerStates.setPkWall(raw, p1, 103);

        PkPlayerStates.addPoints(raw, p2, 50);

        PkPlayerStates.setPkWall(raw, p3, 303);
        PkPlayerStates.addPoints(raw, p3, -7);

        // Vérifs P1
        assertEquals(101, raw[b1 + 0]);
        assertEquals(102, raw[b1 + 1]);
        assertEquals(103, raw[b1 + 2]);
        assertEquals(10_000 + (b1 + 3), raw[b1 + 3]); // points P1 inchangés (sauf sentinelle)

        // Vérifs P2
        assertEquals(10_000 + (b2 + 0), raw[b2 + 0]);
        assertEquals(10_000 + (b2 + 1), raw[b2 + 1]);
        assertEquals(10_000 + (b2 + 2), raw[b2 + 2]);
        assertEquals((10_000 + (b2 + 3)) + 50, raw[b2 + 3]);

        // Vérifs P3
        assertEquals(10_000 + (b3 + 0), raw[b3 + 0]);
        assertEquals(10_000 + (b3 + 1), raw[b3 + 1]);
        assertEquals(303, raw[b3 + 2]);
        assertEquals((10_000 + (b3 + 3)) - 7, raw[b3 + 3]);

        // Vérifs P4 intact
        assertEquals(10_000 + (b4 + 0), raw[b4 + 0]);
        assertEquals(10_000 + (b4 + 1), raw[b4 + 1]);
        assertEquals(10_000 + (b4 + 2), raw[b4 + 2]);
        assertEquals(10_000 + (b4 + 3), raw[b4 + 3]);
    }
}
