
package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/**
 * Méthodes utilitaires pour manipuler le contenu de la ligne plancher d'un joueur,
 * empaqueté dans un {@code int}.
 * <p>
 * Représentation (selon l'énoncé) :
 * <ul>
 *   <li>bits 0..2 : taille de la ligne plancher (0..7)</li>
 *   <li>7 emplacements ensuite, chacun sur 3 bits, contenant la sorte de tuile à cette position</li>
 *   <li>les 8 bits de poids fort valent toujours 0 (donc seuls 24 bits sont utilisés)</li>
 * </ul>
 * <p>
 * Afin de pouvoir représenter "pas de tuile" par la valeur 0 tout en conservant les indices
 * de {@link TileKind} (qui vont de 0 à 5), on stocke dans chaque emplacement :
 * <ul>
 *   <li>0 si l'emplacement est vide</li>
 *   <li>{@code tileKind.index() + 1} sinon</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkFloor {

    private static final int MAX_SIZE = 7;

    private static final int SIZE_BITS = 3;
    private static final int TILE_BITS = 3;

    private static final int SIZE_MASK = (1 << SIZE_BITS) - 1; // 0b111
    private static final int TILE_MASK = (1 << TILE_BITS) - 1; // 0b111

    private static final int TILES_OFFSET = SIZE_BITS; // 3

    /**
     * Construit un objet {@code PkFloor}.
     * <p>
     * Cette classe étant uniquement composée de méthodes statiques, ce constructeur n'a pas vocation
     * à être utilisé, mais il est présent afin de satisfaire les vérifications de signatures.
     */
    public PkFloor() { }

    /**
     * Ligne plancher vide.
     */
    public static final int EMPTY = 0;

    /**
     * Retourne la taille de la ligne plancher empaquetée {@code pkFloor}.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return la taille de la ligne plancher
     */
    public static int size(int pkFloor) {
        return pkFloor & SIZE_MASK;
    }

    /**
     * Retourne la tuile à la position {@code i} (0 <= i < size) de la ligne plancher empaquetée {@code pkFloor}.
     * <p>
     * La spécification ne définit pas le comportement si {@code i} n'est pas un indice valide ;
     * on utilise donc une assertion Java.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @param i l'indice de la tuile
     * @return la tuile à la position {@code i}
     */
    public static TileKind tileAt(int pkFloor, int i) {
        int s = size(pkFloor);
        assert 0 <= i && i < s;

        int stored = tileField(pkFloor, i);
        assert stored != 0;

        return TileKind.ALL.get(stored - 1);
    }

    /**
     * Retourne {@code true} ssi la ligne plancher empaquetée {@code pkFloor} contient le marqueur de premier joueur.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return {@code true} ssi le marqueur de premier joueur est présent
     */
    public static boolean containsFirstPlayerMarker(int pkFloor) {
        int s = size(pkFloor);

        for (int i = 0; i < s; i++) {
            if (tileAt(pkFloor, i) == TileKind.FIRST_PLAYER_MARKER) return true;
        }
        return false;
    }

    /**
     * Retourne l'ensemble de tuiles empaqueté correspondant au contenu de la ligne plancher empaquetée {@code pkFloor}.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkFloor) {
        int acc = PkTileSet.EMPTY;
        int s = size(pkFloor);

        for (int i = 0; i < s; i++) {
            TileKind t = tileAt(pkFloor, i);
            acc = PkTileSet.union(acc, PkTileSet.of(1, t));
        }

        return acc;
    }

    /**
     * Retourne une représentation textuelle de la ligne plancher empaquetée {@code pkFloor}.
     * <p>
     * Elle contient {@code size(pkFloor)} éléments, séparés par ", " et entourés de crochets.
     * Exemple : {@code [FIRST_PLAYER_MARKER, B, B]}.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return une représentation textuelle de la ligne plancher
     */
    public static String toString(int pkFloor) {
        var j = new StringJoiner(", ", "[", "]");
        int s = size(pkFloor);

        for (int i = 0; i < s; i++) {
            j.add(tileAt(pkFloor, i).toString());
        }
        return j.toString();
    }

    /**
     * Retourne une ligne plancher empaquetée identique à {@code pkFloor} mais avec les tuiles de {@code pkTileSet}
     * ajoutées à la suite.
     * <p>
     * Les tuiles sont ajoutées dans l'ordre des sortes de tuiles (A, B, C, D, E, puis marqueur).
     * Si la ligne plancher est pleine, les tuiles en surplus sont ignorées, sauf le marqueur de premier joueur :
     * s'il doit être ajouté alors que la ligne plancher est pleine, il remplace la dernière tuile.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @param pkTileSet l'ensemble de tuiles empaqueté à ajouter
     * @return la nouvelle ligne plancher empaquetée
     */
    public static int withAddedTiles(int pkFloor, int pkTileSet) {
        int s = size(pkFloor);
        assert 0 <= s && s <= MAX_SIZE;

        boolean hasMarker = containsFirstPlayerMarker(pkFloor);
        int updated = pkFloor;

        for (TileKind kind : TileKind.ALL) {
            int count = PkTileSet.countOf(pkTileSet, kind);

            for (int k = 0; k < count; k++) {
                if (kind == TileKind.FIRST_PLAYER_MARKER) {
                    if (hasMarker) continue;

                    if (s < MAX_SIZE) {
                        updated = setTile(updated, s, kind);
                        s += 1;
                        updated = setSize(updated, s);
                        hasMarker = true;
                    } else {
                        // Ligne pleine : le marqueur remplace la dernière tuile.
                        updated = setTile(updated, MAX_SIZE - 1, kind);
                        hasMarker = true;
                    }
                } else {
                    if (s < MAX_SIZE) {
                        updated = setTile(updated, s, kind);
                        s += 1;
                        updated = setSize(updated, s);
                    } else {
                        // Surplus ignoré.
                    }
                }
            }
        }

        return updated;
    }

    // ----- Méthodes utilitaires privées -----

    private static int tileShift(int i) {
        return TILES_OFFSET + i * TILE_BITS;
    }

    private static int tileField(int pkFloor, int i) {
        return (pkFloor >>> tileShift(i)) & TILE_MASK;
    }

    private static int setSize(int pkFloor, int newSize) {
        int cleared = pkFloor & ~SIZE_MASK;
        return cleared | (newSize & SIZE_MASK);
    }

    private static int setTile(int pkFloor, int i, TileKind kind) {
        int stored = kind.index() + 1; // 0 réservé pour "vide"
        int shift = tileShift(i);
        int cleared = pkFloor & ~(TILE_MASK << shift);
        return cleared | ((stored & TILE_MASK) << shift);
    }
}