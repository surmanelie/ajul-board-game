package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.Arrays;
import java.util.List;

/**
 * Permet de calculer le rang des différents joueurs à la fin d'une partie d'Ajul.
 * 
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class RankComputer {

    /**
     * Empêche l'instanciation de cette classe utilitaire.
     */
    private RankComputer() {
    }

    /**
     * Calcule le rang des différents joueurs et les place dans le tableau donné.
     *
     * @param gameState l'état de la partie
     * @param ranks le tableau qui recevra les rangs
     */
    public static void playersRank(ReadOnlyGameState gameState, int[] ranks) {
        List<PlayerId> playerIds = gameState.playerIds();
        int playerCount = playerIds.size();
        int[] packedScores = new int[playerCount];

        ReadOnlyIntArray pkPlayerStates = gameState.pkPlayerStates();

        for (int i = 0; i < playerCount; i++) {
            PlayerId playerId = playerIds.get(i);
            int points = PkPlayerStates.points(pkPlayerStates, playerId);
            int pkWall = PkPlayerStates.pkWall(pkPlayerStates, playerId);

            int fullRows = 0;
            for (TileDestination.Pattern pattern : TileDestination.Pattern.ALL) {
                if (PkWall.isRowFull(pkWall, pattern)) {
                    fullRows++;
                }
            }

            int score = (points << 3) | fullRows;
            packedScores[i] = (score << 2) | i;
        }

        Arrays.sort(packedScores);

        int currentRank = 0;
        int previousScore = -1;

        for (int i = 0; i < playerCount; i++) {
            int packed = packedScores[playerCount - 1 - i];
            int score = packed >> 2;
            int playerIndex = packed & 0b11;

            if (i == 0 || score != previousScore) {
                currentRank = i;
            }

            ranks[playerIndex] = currentRank;
            previousScore = score;
        }
    }
}
