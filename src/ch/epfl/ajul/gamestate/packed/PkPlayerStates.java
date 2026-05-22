package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

/**
 * Méthodes statiques permettant de manipuler les états empaquetés de tous les
 * joueurs d'une partie d'Ajul.
 * <p>
 * Les états empaquetés sont stockés dans un tableau de {@code 4n} entiers,
 * où {@code n} est le nombre de joueurs. Pour chaque joueur, on stocke,
 * dans cet ordre :
 * <ol>
 *   <li>le contenu empaqueté des lignes de motif,</li>
 *   <li>le contenu empaqueté de la ligne plancher,</li>
 *   <li>le contenu empaqueté du mur,</li>
 *   <li>le nombre de points.</li>
 * </ol>
 * Le bloc de 4 entiers du premier joueur commence à l'index 0, celui du
 * second à l'index 4, et ainsi de suite.
 * <p>
 * Les méthodes en lecture seule prennent le tableau sous la forme d'un
 * {@link ReadOnlyIntArray}, tandis que les méthodes modifiant l'état prennent
 * un tableau primitif de type {@code int[]}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkPlayerStates {

    /**
     * Construit un manipulateur d'états empaquetés de joueurs.
     * <p>
     * Cette classe ne contient que des méthodes statiques ; ce constructeur
     * n'a donc pas vocation à être utilisé, mais il est conservé afin de
     * respecter les signatures attendues.
     */
    public PkPlayerStates() { }

    private static final int INTS_PER_PLAYER = 4;

    private static final int PATTERNS_INDEX = 0;
    private static final int FLOOR_INDEX = 1;
    private static final int WALL_INDEX = 2;
    private static final int POINTS_INDEX = 3;

    /**
     * Crée l'état empaqueté initial des joueurs de la partie donnée.
     * <p>
     * Chaque joueur commence avec des lignes de motif vides, une ligne
     * plancher vide, un mur vide et 0 point.
     *
     * @param game la configuration de la partie
     * @return un tableau immuable de taille {@code 4 * game.playersCount()}
     * contenant l'état initial de tous les joueurs
     */
    public static ImmutableIntArray initial(Game game) {
        int playerCount = game.playersCount();
        return ImmutableIntArray.copyOf(new int[INTS_PER_PLAYER * playerCount]);
    }

    /**
     * Retourne le contenu empaqueté des lignes de motif du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @return le contenu empaqueté des lignes de motif du joueur
     */
    public static int pkPatterns(
            ReadOnlyIntArray pkPlayerStates,
            PlayerId playerId
    ) {
        return pkPlayerStates.get(baseIndex(playerId) + PATTERNS_INDEX);
    }

    /**
     * Retourne le contenu empaqueté de la ligne plancher du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @return le contenu empaqueté de la ligne plancher du joueur
     */
    public static int pkFloor(
            ReadOnlyIntArray pkPlayerStates,
            PlayerId playerId
    ) {
        return pkPlayerStates.get(baseIndex(playerId) + FLOOR_INDEX);
    }

    /**
     * Retourne le contenu empaqueté du mur du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @return le contenu empaqueté du mur du joueur
     */
    public static int pkWall(
            ReadOnlyIntArray pkPlayerStates,
            PlayerId playerId
    ) {
        return pkPlayerStates.get(baseIndex(playerId) + WALL_INDEX);
    }

    /**
     * Retourne le nombre de points du joueur donné.
     *
     * @param pkPlayerStates le tableau des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @return le score du joueur
     */
    public static int points(
            ReadOnlyIntArray pkPlayerStates,
            PlayerId playerId
    ) {
        return pkPlayerStates.get(baseIndex(playerId) + POINTS_INDEX);
    }

    /**
     * Remplace le contenu empaqueté des lignes de motif du joueur donné.
     *
     * @param pkPlayerStates le tableau modifiable des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @param pkPatterns le nouveau contenu empaqueté des lignes de motif
     */
    public static void setPkPatterns(
            int[] pkPlayerStates,
            PlayerId playerId,
            int pkPatterns
    ) {
        pkPlayerStates[baseIndex(playerId) + PATTERNS_INDEX] = pkPatterns;
    }

    /**
     * Remplace le contenu empaqueté de la ligne plancher du joueur donné.
     *
     * @param pkPlayerStates le tableau modifiable des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @param pkFloor le nouveau contenu empaqueté de la ligne plancher
     */
    public static void setPkFloor(
            int[] pkPlayerStates,
            PlayerId playerId,
            int pkFloor
    ) {
        pkPlayerStates[baseIndex(playerId) + FLOOR_INDEX] = pkFloor;
    }

    /**
     * Remplace le contenu empaqueté du mur du joueur donné.
     *
     * @param pkPlayerStates le tableau modifiable des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @param pkWall le nouveau contenu empaqueté du mur
     */
    public static void setPkWall(
            int[] pkPlayerStates,
            PlayerId playerId,
            int pkWall
    ) {
        pkPlayerStates[baseIndex(playerId) + WALL_INDEX] = pkWall;
    }

    /**
     * Ajoute le nombre de points donné au score du joueur donné.
     *
     * @param pkPlayerStates le tableau modifiable des états empaquetés des joueurs
     * @param playerId l'identité du joueur
     * @param pointsToAdd le nombre de points à ajouter, éventuellement négatif
     */
    public static void addPoints(
            int[] pkPlayerStates,
            PlayerId playerId,
            int pointsToAdd
    ) {
        int scoreIndex = baseIndex(playerId) + POINTS_INDEX;
        pkPlayerStates[scoreIndex] += pointsToAdd;
    }

    /**
     * Retourne l'index du premier entier du bloc associé au joueur donné.
     *
     * @param playerId l'identité du joueur
     * @return l'index du début du bloc du joueur
     */
    private static int baseIndex(PlayerId playerId) {
        return INTS_PER_PLAYER * playerId.ordinal();
    }
}