package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;

import java.util.Objects;

/**
 * Représente un coup du jeu, c'est-à-dire une prise de tuiles depuis une
 * source, d'une couleur donnée, vers une destination.
 * <p>
 * Cette classe est un {@code record}, donc une donnée immuable. Elle peut être
 * convertie vers et depuis une représentation empaquetée de type {@code short}
 * au moyen de {@link PkMove}.
 *
 * @param source la source des tuiles
 * @param tileColor la couleur des tuiles prélevées
 * @param destination la destination des tuiles
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public record Move(
        TileSource source,
        TileKind.Colored tileColor,
        TileDestination destination
) {

    /**
     * Nombre maximal de coups distincts possibles.
     * <p>
     * Borne supérieure : toutes les sources × toutes les couleurs × toutes les destinations.
     */
    public static final int MAX_MOVES =
            TileSource.COUNT * TileKind.Colored.COUNT * TileDestination.COUNT;

    /**
     * Construit un coup.
     *
     * @throws NullPointerException si l'un des paramètres est {@code null}
     */
    public Move {
        Objects.requireNonNull(source);
        Objects.requireNonNull(tileColor);
        Objects.requireNonNull(destination);
    }

    /**
     * Retourne le coup correspondant à la représentation empaquetée donnée.
     *
     * @param pkMove le coup empaqueté
     * @return le coup correspondant
     */
    public static Move ofPacked(short pkMove) {
        return new Move(
                PkMove.source(pkMove),
                PkMove.color(pkMove),
                PkMove.destination(pkMove)
        );
    }

    /**
     * Retourne la représentation empaquetée de ce coup.
     *
     * @return la représentation empaquetée de ce coup
     */
    public short packed() {
        return PkMove.pack(source, tileColor, destination);
    }
}