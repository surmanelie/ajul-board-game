package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/**
 * Méthodes utilitaires pour manipuler les lignes de motif empaquetées d'un joueur.
 * <p>
 * Représentation :
 * <ul>
 *   <li>pour chaque ligne de motif k (1..5) : 3 bits pour le nombre de tuiles
 *   (0..k), puis 3 bits pour l'index de la couleur (0..4), ou 0 si la ligne
 *   est vide,</li>
 *   <li>les lignes sont stockées dans l'ordre des indices
 *   {@code PATTERN_1..PATTERN_5},</li>
 *   <li>les 2 bits de poids fort valent toujours 0, donc seuls 30 bits sont
 *   utilisés.</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class PkPatterns {

    private static final int BITS_PER_COUNT = 3;
    private static final int BITS_PER_COLOR = 3;
    private static final int BITS_PER_LINE = BITS_PER_COUNT + BITS_PER_COLOR;

    private static final int COUNT_MASK = (1 << BITS_PER_COUNT) - 1;
    private static final int COLOR_MASK = (1 << BITS_PER_COLOR) - 1;

    private static final int COLOR_OFFSET_IN_LINE = BITS_PER_COUNT;

    /**
     * Lignes de motif vides.
     */
    public static final int EMPTY = 0;

    /**
     * Construit un manipulateur de lignes de motif empaquetées.
     * <p>
     * Cette classe ne contient que des méthodes statiques ; ce constructeur
     * n'a donc pas vocation à être utilisé, mais il est conservé afin de
     * respecter les signatures attendues.
     */
    public PkPatterns() { }

    /**
     * Retourne le nombre de tuiles présentes sur la ligne de motif donnée.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @return le nombre de tuiles présentes sur {@code line}
     */
    public static int size(int pkPatterns, TileDestination.Pattern line) {
        int shift = lineShift(line);
        return (pkPatterns >>> shift) & COUNT_MASK;
    }

    /**
     * Retourne la couleur des tuiles présentes sur la ligne de motif donnée.
     * <p>
     * Une assertion garantit que la méthode n'est appelée que sur une ligne
     * non vide.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @return la couleur des tuiles présentes sur {@code line}
     */
    public static TileKind.Colored color(
            int pkPatterns,
            TileDestination.Pattern line
    ) {
        assert size(pkPatterns, line) > 0;

        int shift = lineShift(line) + COLOR_OFFSET_IN_LINE;
        int colorIndex = (pkPatterns >>> shift) & COLOR_MASK;

        return TileKind.Colored.ALL.get(colorIndex);
    }

    /**
     * Retourne {@code true} ssi la ligne de motif donnée est pleine.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @return {@code true} ssi la ligne est pleine
     */
    public static boolean isFull(int pkPatterns, TileDestination.Pattern line) {
        return size(pkPatterns, line) == line.capacity();
    }

    /**
     * Retourne {@code true} ssi la ligne de motif donnée peut contenir des
     * tuiles de la couleur donnée.
     * <p>
     * C'est le cas si la ligne est vide, ou si elle contient déjà des tuiles
     * de cette couleur.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @param color la couleur à vérifier
     * @return {@code true} ssi {@code line} peut contenir {@code color}
     */
    public static boolean canContain(
            int pkPatterns,
            TileDestination.Pattern line,
            TileKind.Colored color
    ) {
        int lineSize = size(pkPatterns, line);
        return lineSize == 0 || color(pkPatterns, line) == color;
    }

    /**
     * Retourne des lignes de motif empaquetées identiques à {@code pkPatterns},
     * mais avec {@code tileCount} tuiles de couleur {@code color} ajoutées à
     * la ligne {@code line}.
     * <p>
     * Des assertions garantissent que l'ajout est valide.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @param tileCount le nombre de tuiles à ajouter
     * @param color la couleur des tuiles ajoutées
     * @return les lignes de motif empaquetées après ajout
     */
    public static int withAddedTiles(
            int pkPatterns,
            TileDestination.Pattern line,
            int tileCount,
            TileKind.Colored color
    ) {
        assert tileCount >= 0;
        assert canContain(pkPatterns, line, color);

        int oldCount = size(pkPatterns, line);
        int newCount = oldCount + tileCount;

        assert newCount <= line.capacity();

        int shift = lineShift(line);
        int updatedPatterns = setField(pkPatterns, shift, COUNT_MASK, newCount);

        if (oldCount == 0 && tileCount > 0) {
            updatedPatterns = setField(
                    updatedPatterns,
                    shift + COLOR_OFFSET_IN_LINE,
                    COLOR_MASK,
                    color.index()
            );
        }

        return updatedPatterns;
    }

    /**
     * Retourne des lignes de motif empaquetées identiques à {@code pkPatterns},
     * mais avec la ligne donnée vidée.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif à vider
     * @return les lignes de motif empaquetées après vidage
     */
    public static int withEmptyLine(int pkPatterns, TileDestination.Pattern line) {
        int shift = lineShift(line);
        int clearedPatterns = setField(pkPatterns, shift, COUNT_MASK, 0);
        clearedPatterns =
                setField(clearedPatterns, shift + COLOR_OFFSET_IN_LINE, COLOR_MASK, 0);
        return clearedPatterns;
    }

    /**
     * Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles
     * présentes sur les lignes de motif empaquetées données.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkPatterns) {
        int packedTileSet = PkTileSet.EMPTY;

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int lineSize = size(pkPatterns, line);
            if (lineSize > 0) {
                TileKind.Colored lineColor = color(pkPatterns, line);
                packedTileSet =
                        PkTileSet.union(packedTileSet, PkTileSet.of(lineSize, lineColor));
            }
        }

        return packedTileSet;
    }

    /**
     * Retourne une représentation textuelle des lignes de motif empaquetées.
     * <p>
     * Elle contient cinq éléments, un par ligne, séparés par {@code ", "} et
     * entourés de crochets. Chaque élément contient la lettre de la couleur
     * répétée {@code size} fois, puis des points jusqu'à atteindre la capacité
     * de la ligne.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @return une représentation textuelle des lignes de motif
     */
    public static String toString(int pkPatterns) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int lineSize = size(pkPatterns, line);
            int lineCapacity = line.capacity();

            String letters = lineSize == 0
                    ? ""
                    : color(pkPatterns, line).name().repeat(lineSize);
            String dots = ".".repeat(lineCapacity - lineSize);

            joiner.add(letters + dots);
        }

        return joiner.toString();
    }

    /**
     * Retourne le décalage, en bits, correspondant à la ligne donnée.
     */
    private static int lineShift(TileDestination.Pattern line) {
        return line.index() * BITS_PER_LINE;
    }

    /**
     * Remplace le champ de bits désigné par {@code mask} et {@code shift}
     * par la valeur donnée.
     */
    private static int setField(int bits, int shift, int mask, int value) {
        int clearedBits = bits & ~(mask << shift);
        return clearedBits | ((value & mask) << shift);
    }
}