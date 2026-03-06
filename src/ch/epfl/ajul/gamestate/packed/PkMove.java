package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/**
 * Méthodes utilitaires pour empaqueter et dépaqueter un coup ({@code Move}) dans un {@code short}.
 * <p>
 * Le coup est encodé sur 10 bits (les 6 bits de poids fort sont à 0) :
 * <ul>
 *   <li>4 bits : source ({@link TileSource})</li>
 *   <li>3 bits : couleur ({@link TileKind.Colored})</li>
 *   <li>3 bits : destination ({@link TileDestination})</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkMove {

    // === Layout (10 bits used)
    private static final int SOURCE_OFFSET = 0;
    private static final int SOURCE_BITS = 4;
    private static final int SOURCE_MASK = (1 << SOURCE_BITS) - 1;

    private static final int COLOR_OFFSET = SOURCE_OFFSET + SOURCE_BITS;
    private static final int COLOR_BITS = 3;
    private static final int COLOR_MASK = (1 << COLOR_BITS) - 1;

    private static final int DESTINATION_OFFSET = COLOR_OFFSET + COLOR_BITS;
    private static final int DESTINATION_BITS = 3;
    private static final int DESTINATION_MASK = (1 << DESTINATION_BITS) - 1;

    /**
     * Construit un objet {@code PkMove}.
     * <p>
     * Cette classe étant uniquement composée de méthodes statiques, ce constructeur n'a pas vocation
     * à être utilisé, mais il est présent afin de satisfaire les vérifications de signatures.
     */
    public PkMove() { }

    /**
     * Empaquète un coup défini par sa source, sa couleur et sa destination dans un {@code short}.
     *
     * @param source la source des tuiles
     * @param color la couleur des tuiles prélevées
     * @param destination la destination des tuiles
     * @return le coup empaqueté dans un {@code short}
     */
    public static short pack(TileSource source,
                             TileKind.Colored color,
                             TileDestination destination) {

        int s = source.index();
        int c = color.index();
        int d = destination.index();

        int packed =
                (s << SOURCE_OFFSET) |
                        (c << COLOR_OFFSET) |
                        (d << DESTINATION_OFFSET);

        return (short) packed;
    }

    /**
     * Extrait la source ({@link TileSource}) du coup empaqueté.
     *
     * @param pkMove le coup empaqueté
     * @return la source du coup
     */
    public static TileSource source(short pkMove) {
        int p = pkMove & 0xFFFF;
        int s = (p >>> SOURCE_OFFSET) & SOURCE_MASK;
        return TileSource.ALL.get(s);
    }

    /**
     * Extrait la couleur ({@link TileKind.Colored}) du coup empaqueté.
     *
     * @param pkMove le coup empaqueté
     * @return la couleur du coup
     */
    public static TileKind.Colored color(short pkMove) {
        int p = pkMove & 0xFFFF;
        int c = (p >>> COLOR_OFFSET) & COLOR_MASK;
        return TileKind.Colored.ALL.get(c);
    }

    /**
     * Extrait la destination ({@link TileDestination}) du coup empaqueté.
     *
     * @param pkMove le coup empaqueté
     * @return la destination du coup
     */
    public static TileDestination destination(short pkMove) {
        int p = pkMove & 0xFFFF;
        int d = (p >>> DESTINATION_OFFSET) & DESTINATION_MASK;
        return TileDestination.ALL.get(d);
    }
}