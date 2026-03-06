//package ch.epfl.ajul.gamestate.packed;
//
//public class PkPatterns {
//}

package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/**
 * Méthodes utilitaires pour manipuler le contenu des lignes de motif d'un joueur,
 * empaqueté dans un {@code int}.
 * <p>
 * Représentation (selon l'énoncé) :
 * <ul>
 *   <li>Pour chaque ligne de motif k (1..5) : 3 bits pour le nombre de tuiles (0..k),
 *       puis 3 bits pour l'index de la couleur (0..4), ou 0 si la ligne est vide.</li>
 *   <li>Les lignes sont stockées dans l'ordre des indices {@code PATTERN_1..PATTERN_5}.</li>
 *   <li>Les 2 bits de poids fort valent toujours 0 (donc seuls 30 bits sont utilisés).</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkPatterns {

    private static final int BITS_PER_COUNT = 3;
    private static final int BITS_PER_COLOR = 3;
    private static final int BITS_PER_LINE = BITS_PER_COUNT + BITS_PER_COLOR; // 6

    private static final int COUNT_MASK = (1 << BITS_PER_COUNT) - 1; // 0b111
    private static final int COLOR_MASK = (1 << BITS_PER_COLOR) - 1; // 0b111

    private static final int COLOR_OFFSET_IN_LINE = BITS_PER_COUNT; // 3

    /**
     * Construit un objet {@code PkPatterns}.
     * <p>
     * Cette classe étant uniquement composée de méthodes statiques, ce constructeur n'a pas vocation
     * à être utilisé, mais il est présent afin de satisfaire les vérifications de signatures.
     */
    public PkPatterns() { }

    /**
     * Lignes de motif vides.
     */
    public static final int EMPTY = 0;

    /**
     * Retourne le nombre de tuiles présentes sur la ligne de motif {@code line}
     * des lignes de motif empaquetées {@code pkPatterns}.
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
     * Retourne la couleur des tuiles présentes sur la ligne de motif {@code line}
     * des lignes de motif empaquetées {@code pkPatterns}.
     * <p>
     * La spécification ne définit pas le comportement si la ligne est vide ; on utilise donc
     * une assertion Java pour garantir que cette méthode est appelée avec une ligne non vide.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @return la couleur des tuiles sur {@code line}
     */
    public static TileKind.Colored color(int pkPatterns, TileDestination.Pattern line) {
        assert size(pkPatterns, line) > 0;

        int shift = lineShift(line) + COLOR_OFFSET_IN_LINE;
        int colorIndex = (pkPatterns >>> shift) & COLOR_MASK;

        // D'après l'énoncé : l'index couleur est entre 0 et 4 inclus.
        return TileKind.Colored.ALL.get(colorIndex);
    }

    /**
     * Retourne {@code true} ssi la ligne de motif {@code line} des lignes de motif empaquetées
     * {@code pkPatterns} est pleine.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @return {@code true} ssi la ligne est pleine
     */
    public static boolean isFull(int pkPatterns, TileDestination.Pattern line) {
        return size(pkPatterns, line) == line.capacity();
    }

    /**
     * Retourne {@code true} ssi la ligne de motif {@code line} des lignes de motif empaquetées
     * {@code pkPatterns} peut contenir des tuiles de couleur {@code color}, c.-à-d. si elle est vide
     * ou si elle contient déjà des tuiles de cette couleur (indépendamment du fait qu'elle soit pleine).
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @param color la couleur à vérifier
     * @return {@code true} ssi {@code line} peut contenir {@code color}
     */
    public static boolean canContain(int pkPatterns, TileDestination.Pattern line, TileKind.Colored color) {
        int s = size(pkPatterns, line);
        return s == 0 || color(pkPatterns, line) == color;
    }

    /**
     * Retourne des lignes de motif empaquetées identiques à {@code pkPatterns} mais avec
     * {@code tileCount} tuiles de couleur {@code color} ajoutées à la ligne {@code line}.
     * <p>
     * La spécification ne définit pas le comportement si l'ajout est invalide ; on utilise donc
     * des assertions Java pour garantir que la méthode est appelée avec des arguments valides.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif
     * @param tileCount le nombre de tuiles à ajouter
     * @param color la couleur des tuiles ajoutées
     * @return les nouvelles lignes de motif empaquetées
     */
    public static int withAddedTiles(int pkPatterns,
                                     TileDestination.Pattern line,
                                     int tileCount,
                                     TileKind.Colored color) {

        assert tileCount >= 0;
        assert canContain(pkPatterns, line, color);

        int oldCount = size(pkPatterns, line);
        int newCount = oldCount + tileCount;

        assert newCount <= line.capacity();

        int shift = lineShift(line);

        // 1) Met à jour le compteur (3 bits).
        int updated = setField(pkPatterns, shift, COUNT_MASK, newCount);

        // 2) Met à jour la couleur si la ligne était vide et qu'on ajoute au moins une tuile.
        if (oldCount == 0 && tileCount > 0) {
            updated = setField(updated, shift + COLOR_OFFSET_IN_LINE, COLOR_MASK, color.index());
        }

        return updated;
    }

    /**
     * Retourne des lignes de motif empaquetées identiques à {@code pkPatterns} mais avec
     * la ligne {@code line} vide.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @param line la ligne de motif à vider
     * @return les lignes de motif empaquetées après vidage
     */
    public static int withEmptyLine(int pkPatterns, TileDestination.Pattern line) {
        int shift = lineShift(line);
        int cleared = setField(pkPatterns, shift, COUNT_MASK, 0);
        cleared = setField(cleared, shift + COLOR_OFFSET_IN_LINE, COLOR_MASK, 0);
        return cleared;
    }

    /**
     * Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles se trouvant
     * sur les lignes de motif empaquetées {@code pkPatterns}.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkPatterns) {
        int acc = PkTileSet.EMPTY;

        for (var line : TileDestination.Pattern.ALL) {
            int s = size(pkPatterns, line);
            if (s > 0) {
                var c = color(pkPatterns, line);
                acc = PkTileSet.union(acc, PkTileSet.of(s, c));
            }
        }

        return acc;
    }

    /**
     * Retourne une représentation textuelle des lignes de motif empaquetées {@code pkPatterns}.
     * <p>
     * Elle contient cinq éléments (un par ligne), séparés par ", " et entourés de crochets.
     * Un élément contient la lettre de la couleur répétée {@code size} fois, puis des points "."
     * jusqu'à atteindre la capacité de la ligne.
     * Exemple : {@code [C, AA, AAA, EEE., .....]}.
     *
     * @param pkPatterns les lignes de motif empaquetées
     * @return une représentation textuelle des lignes de motif
     */
    public static String toString(int pkPatterns) {
        var j = new StringJoiner(", ", "[", "]");

        for (var line : TileDestination.Pattern.ALL) {
            int s = size(pkPatterns, line);
            int cap = line.capacity();

            String letters =
                    s == 0
                            ? ""
                            : color(pkPatterns, line).name().repeat(s);
            String dots = ".".repeat(cap - s);

            j.add(letters + dots);
        }

        return j.toString();
    }

    // ----- Méthodes utilitaires privées -----

    private static int lineShift(TileDestination.Pattern line) {
        return line.index() * BITS_PER_LINE;
    }

    private static int setField(int bits, int shift, int mask, int value) {
        int cleared = bits & ~(mask << shift);
        return cleared | ((value & mask) << shift);
    }
}
