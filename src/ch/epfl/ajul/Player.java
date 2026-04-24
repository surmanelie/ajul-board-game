package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;

/**
 * Représente un joueur d'une partie d'Ajul.
 *
 * @author ...
 */
@FunctionalInterface
public interface Player {

    /**
     * Retourne le coup que le joueur désire jouer, étant donné l'état de la partie.
     * On fait l'hypothèse que le joueur courant de l'état passé en argument est
     * le joueur auquel on applique la méthode.
     *
     * @param gameState l'état de la partie (en lecture seule)
     * @return le coup que le joueur désire jouer
     */
    Move nextMove(ReadOnlyGameState gameState);
}
