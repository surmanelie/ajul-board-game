package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

/**
 * Méthodes statiques permettant de manipuler les états empaquetés de tous les joueurs
 * d'une partie d'Ajul.
 * <p>
 * Les états empaquetés sont stockés dans un tableau de {@code 4n} entiers, où {@code n}
 * est le nombre de joueurs. Pour chaque joueur, on stocke (dans cet ordre) :
 * <ol>
 *   <li>le contenu (empaqueté) des lignes de motif,</li>
 *   <li>le contenu (empaqueté) de la ligne plancher,</li>
 *   <li>le contenu (empaqueté) du mur,</li>
 *   <li>le nombre de points (score).</li>
 * </ol>
 * Le bloc de 4 entiers du premier joueur commence à l'index 0, celui du second à l'index 4,
 * etc.
 * <p>
 * Les méthodes en lecture seule prennent le tableau sous forme de {@link ReadOnlyIntArray},
 * tandis que les méthodes modifiant l'état prennent un tableau primitif {@code int[]}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkPlayerStates {

    /**
     * Crée un tableau immuable contenant l'état empaqueté initial des joueurs de la partie donnée :
     * lignes de motif vides, ligne plancher vide, mur vide, et 0 points pour chacun.
     *
     * @param game la configuration de la partie
     * @return un tableau immuable de taille {@code 4 * game.playersCount()} contenant l'état initial
     */
    public static ImmutableIntArray initial(Game game) {
        int n = game.playersCount();
        return ImmutableIntArray.copyOf(new int[4 * n]);
    }

    /**
     * Extrait du tableau l'entier représentant le contenu empaqueté des lignes de motif du joueur donné.
     *
     * @param pkPlayerStates tableau des états empaquetés des joueurs (lecture seule)
     * @param playerId identité du joueur
     * @return le contenu empaqueté des lignes de motif
     */
    public static int pkPatterns(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(baseIndex(playerId) + 0);
    }

    /**
     * Extrait du tableau l'entier représentant le contenu empaqueté de la ligne plancher du joueur donné.
     *
     * @param pkPlayerStates tableau des états empaquetés des joueurs (lecture seule)
     * @param playerId identité du joueur
     * @return le contenu empaqueté de la ligne plancher
     */
    public static int pkFloor(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(baseIndex(playerId) + 1);
    }

    /**
     * Extrait du tableau l'entier représentant le contenu empaqueté du mur du joueur donné.
     *
     * @param pkPlayerStates tableau des états empaquetés des joueurs (lecture seule)
     * @param playerId identité du joueur
     * @return le contenu empaqueté du mur
     */
    public static int pkWall(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(baseIndex(playerId) + 2);
    }

    /**
     * Extrait du tableau l'entier représentant le nombre de points du joueur donné.
     *
     * @param pkPlayerStates tableau des états empaquetés des joueurs (lecture seule)
     * @param playerId identité du joueur
     * @return le score du joueur
     */
    public static int points(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(baseIndex(playerId) + 3);
    }

    /**
     * Modifie le tableau en remplaçant le contenu empaqueté des lignes de motif du joueur donné.
     *
     * @param pkPlayerStates tableau modifiable des états empaquetés des joueurs
     * @param playerId identité du joueur
     * @param pkPatterns nouveau contenu empaqueté des lignes de motif
     */
    public static void setPkPatterns(int[] pkPlayerStates, PlayerId playerId, int pkPatterns) {
        pkPlayerStates[baseIndex(playerId) + 0] = pkPatterns;
    }

    /**
     * Modifie le tableau en remplaçant le contenu empaqueté de la ligne plancher du joueur donné.
     *
     * @param pkPlayerStates tableau modifiable des états empaquetés des joueurs
     * @param playerId identité du joueur
     * @param pkFloor nouveau contenu empaqueté de la ligne plancher
     */
    public static void setPkFloor(int[] pkPlayerStates, PlayerId playerId, int pkFloor) {
        pkPlayerStates[baseIndex(playerId) + 1] = pkFloor;
    }

    /**
     * Modifie le tableau en remplaçant le contenu empaqueté du mur du joueur donné.
     *
     * @param pkPlayerStates tableau modifiable des états empaquetés des joueurs
     * @param playerId identité du joueur
     * @param pkWall nouveau contenu empaqueté du mur
     */
    public static void setPkWall(int[] pkPlayerStates, PlayerId playerId, int pkWall) {
        pkPlayerStates[baseIndex(playerId) + 2] = pkWall;
    }

    /**
     * Ajoute {@code pointsToAdd} (éventuellement négatif) au score du joueur donné.
     *
     * @param pkPlayerStates tableau modifiable des états empaquetés des joueurs
     * @param playerId identité du joueur
     * @param pointsToAdd nombre de points à ajouter (peut être négatif)
     */
    public static void addPoints(int[] pkPlayerStates, PlayerId playerId, int pointsToAdd) {
        int i = baseIndex(playerId) + 3;
        pkPlayerStates[i] += pointsToAdd;
    }

    /**
     * Retourne l'index du premier entier du bloc de 4 entiers associé au joueur donné.
     *
     * @param playerId identité du joueur
     * @return l'index de début de bloc (multiple de 4)
     */
    private static int baseIndex(PlayerId playerId) {
        return 4 * playerId.ordinal();
    }
}