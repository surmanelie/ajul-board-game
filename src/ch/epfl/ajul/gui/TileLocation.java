package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/**
 * Représente un emplacement logique qu'une tuile peut occuper.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public sealed interface TileLocation permits 
        TileLocation.OffBoard, 
        TileLocation.OnSource, 
        TileLocation.OnPattern, 
        TileLocation.OnWall, 
        TileLocation.OnFloor {

    /**
     * Emplacement hors du plateau (sac ou couvercle).
     *
     * @param kind  la sorte de tuile
     * @param index l'index permettant de distinguer les différentes tuiles de même sorte
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    record OffBoard(TileKind kind, int index) implements TileLocation {}

    /**
     * Emplacement sur une source (zone centrale ou fabrique).
     *
     * @param source la source de tuiles
     * @param index  l'index de l'emplacement dans la source
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    record OnSource(TileSource source, int index) implements TileLocation {}

    /**
     * Emplacement sur une ligne de motif d'un joueur.
     *
     * @param playerId l'identité du joueur
     * @param line     l'identité de la ligne de motif
     * @param index    l'index sur la ligne
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    record OnPattern(PlayerId playerId, TileDestination.Pattern line, int index) implements TileLocation {}

    /**
     * Emplacement sur le mur d'un joueur.
     *
     * @param playerId l'identité du joueur
     * @param line     l'identité de la ligne de mur
     * @param color    la couleur de la tuile pouvant occuper l'emplacement
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    record OnWall(PlayerId playerId, TileDestination.Pattern line, TileKind.Colored color) implements TileLocation {}

    /**
     * Emplacement sur la ligne plancher d'un joueur.
     *
     * @param playerId l'identité du joueur
     * @param index    l'index sur la ligne (0 à 6)
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    record OnFloor(PlayerId playerId, int index) implements TileLocation {
        /**
         * Construit un emplacement sur le plancher du joueur donné à l'index donné.
         *
         * @throws IllegalArgumentException si {@code index} n'est pas compris entre 0 et 6 inclus
         */
        public OnFloor {
            if (index < 0 || index > 6) {
                throw new IllegalArgumentException();
            }
        }
    }
}
