package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/**
 * Méthodes statiques permettant d'empaqueter et de dépaqueter un coup
 * dans une valeur de type {@code short}.
 * <p>
 * Le coup est encodé sur 10 bits, les 6 bits de poids fort restant à 0 :
 * <ul>
 *   <li>4 bits pour la source ({@link TileSource}),</li>
 *   <li>3 bits pour la couleur ({@link TileKind.Colored}),</li>
 *   <li>3 bits pour la destination ({@link TileDestination}).</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkMove {

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
     * Construit un manipulateur de coups empaquetés.
     * <p>
     * Cette classe ne contient que des méthodes statiques ; ce constructeur
     * n'a donc pas vocation à être utilisé, mais il est conservé afin de
     * respecter les signatures attendues.
     */
    public PkMove() { }

    /**
     * Retourne le coup empaqueté correspondant à la source, à la couleur
     * et à la destination données.
     *
     * @param source la source des tuiles
     * @param color la couleur des tuiles prélevées
     * @param destination la destination des tuiles
     * @return le coup empaqueté correspondant
     */
    public static short pack(
            TileSource source,
            TileKind.Colored color,
            TileDestination destination
    ) {
        int sourceIndex = source.index();
        int colorIndex = color.index();
        int destinationIndex = destination.index();

        int packedMove =
                (sourceIndex << SOURCE_OFFSET)
                        | (colorIndex << COLOR_OFFSET)
                        | (destinationIndex << DESTINATION_OFFSET);

        return (short) packedMove;
    }

    /**
     * Retourne la source du coup empaqueté donné.
     *
     * @param pkMove le coup empaqueté
     * @return la source du coup empaqueté
     */
    public static TileSource source(short pkMove) {
        int unsignedPkMove = pkMove & 0xFFFF;
        int sourceIndex = (unsignedPkMove >>> SOURCE_OFFSET) & SOURCE_MASK;
        return TileSource.ALL.get(sourceIndex);
    }

    /**
     * Retourne la couleur du coup empaqueté donné.
     *
     * @param pkMove le coup empaqueté
     * @return la couleur du coup empaqueté
     */
    public static TileKind.Colored color(short pkMove) {
        int unsignedPkMove = pkMove & 0xFFFF;
        int colorIndex = (unsignedPkMove >>> COLOR_OFFSET) & COLOR_MASK;
        return TileKind.Colored.ALL.get(colorIndex);
    }

    /**
     * Retourne la destination du coup empaqueté donné.
     *
     * @param pkMove le coup empaqueté
     * @return la destination du coup empaqueté
     */
    public static TileDestination destination(short pkMove) {
        int unsignedPkMove = pkMove & 0xFFFF;
        int destinationIndex =
                (unsignedPkMove >>> DESTINATION_OFFSET) & DESTINATION_MASK;
        return TileDestination.ALL.get(destinationIndex);
    }
}