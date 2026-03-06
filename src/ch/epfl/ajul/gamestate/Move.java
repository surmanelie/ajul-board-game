package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;

import java.util.Objects;

/**
 * Représente un coup du jeu : une prise de tuiles depuis une source, d'une couleur donnée,
 * vers une destination.
 * <p>
 * Cette classe est un {@code record} (donnée immuable). Elle peut être convertie vers et depuis
 * une représentation empaquetée ({@code short}) via {@link PkMove}.
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
     * Nombre maximal de coups distincts possibles, calculé à partir du nombre de sources,
     * du nombre de tuiles par fabrique et du nombre de destinations.
     */
    public static final int MAX_MOVES =
            TileSource.Factory.COUNT *
                    TileSource.Factory.TILES_PER_FACTORY *
                    TileDestination.COUNT;

    /**
     * Construit un {@code Move} à partir de sa représentation empaquetée.
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
     * @return le coup empaqueté
     */
    public short packed() {
        return PkMove.pack(source, tileColor, destination);
    }
}