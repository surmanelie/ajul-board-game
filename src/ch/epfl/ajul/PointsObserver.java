package ch.epfl.ajul;

/**
 * Représente un observateur de points, c.-à-d. un objet qui est informé chaque fois
 * que les points d'un joueur changent.
 * <p>
 * Toutes les méthodes de cette interface sont des méthodes par défaut dont le corps
 * est vide.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public interface PointsObserver {

    /**
     * Observateur de points vide.
     */
    PointsObserver EMPTY = new PointsObserver() {};

    /**
     * Est destinée à être appelée lorsque le joueur d'identité {@code playerId} a ajouté
     * à la ligne {@code line} de son mur une tuile de couleur {@code color} et remporté
     * ainsi {@code points} points.
     *
     * @param playerId l'identité du joueur
     * @param line la ligne du mur
     * @param color la couleur de la tuile ajoutée
     * @param points le nombre de points remportés
     */
    default void newWallTile(PlayerId playerId, TileDestination.Pattern line,
                             TileKind.Colored color, int points) {}

    /**
     * Est destinée à être appelée à la fin d'une manche lorsque le joueur d'identité
     * {@code playerId} a perdu {@code penalty} points en raison de la présence de
     * tuiles sur sa ligne plancher.
     *
     * @param playerId l'identité du joueur
     * @param penalty le nombre de points perdus
     */
    default void floor(PlayerId playerId, int penalty) {}

    /**
     * Est destinée à être appelée à la fin de la partie lorsque le joueur d'identité
     * {@code playerId} a gagné {@code points} points de bonus car la ligne
     * {@code line} de son mur est complète.
     *
     * @param playerId l'identité du joueur
     * @param line la ligne complète
     * @param points le nombre de points de bonus gagnés
     */
    default void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {}

    /**
     * Est destinée à être appelée à la fin de la partie lorsque le joueur d'identité
     * {@code playerId} a gagné {@code points} points de bonus car la colonne
     * {@code column} de son mur est complète.
     *
     * @param playerId l'identité du joueur
     * @param column l'index de la colonne complète
     * @param points le nombre de points de bonus gagnés
     */
    default void fullColumn(PlayerId playerId, int column, int points) {}

    /**
     * Est destinée à être appelée à la fin de la partie lorsque le joueur d'identité
     * {@code playerId} a gagné {@code points} points de bonus car la couleur
     * {@code color} est complète sur son mur.
     *
     * @param playerId l'identité du joueur
     * @param color la couleur complète
     * @param points le nombre de points de bonus gagnés
     */
    default void fullColor(PlayerId playerId, TileKind.Colored color, int points) {}
}