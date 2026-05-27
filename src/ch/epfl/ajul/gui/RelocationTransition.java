package ch.epfl.ajul.gui;

import javafx.animation.Transition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Animation déplaçant un nœud graphique sur l'écran au fil du temps en interpolant
 * sa position depuis sa position de départ jusqu'à une coordonnée d'arrivée.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class RelocationTransition extends Transition {

    private final Node node;
    private final Point2D startPosition;
    private final Point2D endPosition;

    /**
     * Initialise la transition sur un noeud pour la durée correspondante.
     *
     * @param node        le nœud à déplacer
     * @param endPosition la position finale visée dans l'espace de coordonnées parent
     * @param duration    la durée de l'animation
     */
    public RelocationTransition(Node node, Point2D endPosition, Duration duration) {
        this.node = node;
        this.startPosition = node.localToParent(Point2D.ZERO);
        this.endPosition = endPosition;

        setCycleDuration(duration);
    }

    /**
     * Calcule et attribue les coordonnées du nœud au point d'avancement donné.
     * Cette méthode est appelée par la plateforme JavaFX de façon régulière pendant le cycle.
     *
     * @param frac l'avancement de l'animation entre 0.0 et 1.0
     */
    @Override
    protected void interpolate(double frac) {
        Point2D currentPosition = startPosition.interpolate(endPosition, frac);
        node.relocate(currentPosition.getX(), currentPosition.getY());
    }
}
