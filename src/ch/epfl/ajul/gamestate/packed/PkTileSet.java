package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.random.RandomGenerator;

/**
 * Méthodes utilitaires pour manipuler un ensemble de tuiles empaqueté dans un {@code int}.
 * <p>
 * Représentation (selon l'énoncé) :
 * <ul>
 *   <li>Comptes des tuiles colorées A..E : 5 champs de 6 bits (bits 0..29).</li>
 *   <li>Marqueur {@code FIRST_PLAYER_MARKER} : bit d'index 30 (0 ou 1).</li>
 *   <li>Bit 31 inutilisé (reste à 0).</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkTileSet {

    private static final int BITS_PER_COLOR = 6;
    private static final int COLOR_MASK = (1 << BITS_PER_COLOR) - 1;
    private static final int MARKER_BIT_INDEX = 30;

    /**
     * Construit un objet {@code PkTileSet}.
     * <p>
     * Cette classe étant uniquement composée de méthodes statiques, ce constructeur n'a pas vocation
     * à être utilisé, mais il est présent afin de satisfaire les vérifications de signatures.
     */
    public PkTileSet() { }

    /**
     * Ensemble vide.
     */
    public static final int EMPTY = 0;

    /**
     * Ensemble contenant 20 tuiles de chaque couleur (A..E), sans marqueur.
     */
    public static final int FULL_COLORED = computeFullColored();

    /**
     * Ensemble contenant 20 tuiles de chaque couleur (A..E) et le marqueur {@code FIRST_PLAYER_MARKER}.
     */
    public static final int FULL = union(FULL_COLORED, of(1, TileKind.FIRST_PLAYER_MARKER));

    /**
     * Construit l'ensemble empaqueté contenant {@code count} occurrences de {@code tileKind}
     * (et aucune autre tuile).
     *
     * @param count le nombre d'occurrences de la tuile
     * @param tileKind le type de tuile
     * @return l'ensemble empaqueté correspondant
     */
    public static int of(int count, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored c) {
            int shift = c.index() * BITS_PER_COLOR;
            return (count & COLOR_MASK) << shift;
        } else {
            return (count & 1) << MARKER_BIT_INDEX;
        }
    }

    /**
     * Indique si l'ensemble empaqueté est vide.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return {@code true} si et seulement si l'ensemble est vide
     */
    public static boolean isEmpty(int pkTileSet) {
        return pkTileSet == EMPTY;
    }

    /**
     * Retourne le nombre d'occurrences de {@code tileKind} dans l'ensemble empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile
     * @return le nombre d'occurrences de {@code tileKind}
     */
    public static int countOf(int pkTileSet, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored c) {
            int shift = c.index() * BITS_PER_COLOR;
            return (pkTileSet >>> shift) & COLOR_MASK;
        } else {
            return (pkTileSet >>> MARKER_BIT_INDEX) & 1;
        }
    }

    /**
     * Retourne la taille (nombre total de tuiles) de l'ensemble empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return le nombre total de tuiles dans l'ensemble
     */
    public static int size(int pkTileSet) {
        int s = 0;
        for (var c : TileKind.Colored.ALL) s += countOf(pkTileSet, c);
        s += countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        return s;
    }

    /**
     * Retourne l'ensemble composé uniquement des occurrences de {@code tileKind}
     * présentes dans {@code pkTileSet}.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile à extraire
     * @return l'ensemble empaqueté ne contenant que {@code tileKind}
     */
    public static int subsetOf(int pkTileSet, TileKind tileKind) {
        return of(countOf(pkTileSet, tileKind), tileKind);
    }

    /**
     * Ajoute une occurrence de {@code tileKind} à l'ensemble empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile à ajouter
     * @return l'ensemble empaqueté après ajout
     */
    public static int add(int pkTileSet, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored c) {
            return pkTileSet + (1 << (c.index() * BITS_PER_COLOR));
        } else {
            return pkTileSet + (1 << MARKER_BIT_INDEX);
        }
    }

    /**
     * Retire une occurrence de {@code tileKind} à l'ensemble empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile à retirer
     * @return l'ensemble empaqueté après retrait
     */
    public static int remove(int pkTileSet, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored c) {
            return pkTileSet - (1 << (c.index() * BITS_PER_COLOR));
        } else {
            return pkTileSet - (1 << MARKER_BIT_INDEX);
        }
    }

    /**
     * Retourne l'union (somme des multiplicités) de deux ensembles empaquetés.
     *
     * @param pkTileSet1 premier ensemble empaqueté
     * @param pkTileSet2 second ensemble empaqueté
     * @return l'union des deux ensembles
     */
    public static int union(int pkTileSet1, int pkTileSet2) {
        return pkTileSet1 + pkTileSet2;
    }

    /**
     * Retourne la différence {@code pkTileSet1 \ pkTileSet2}.
     *
     * @param pkTileSet1 ensemble empaqueté de départ
     * @param pkTileSet2 ensemble empaqueté à soustraire
     * @return la différence des deux ensembles
     */
    public static int difference(int pkTileSet1, int pkTileSet2) {
        return pkTileSet1 - pkTileSet2;
    }

    /**
     * Copie les tuiles colorées de {@code pkTileSet} dans {@code destination}, dans l'ordre des couleurs
     * (A puis B puis C puis D puis E), et retourne l'indice suivant la dernière position écrite.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param destination le tableau dans lequel écrire les tuiles colorées
     * @return l'indice suivant la dernière case remplie
     */
    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination) {
        int offset = 0;
        for (var c : TileKind.Colored.ALL) {
            int n = countOf(pkTileSet, c);
            Arrays.fill(destination, offset, offset + n, c);
            offset += n;
        }
        return offset;
    }

    /**
     * Échantillonne aléatoirement les tuiles colorées de {@code pkTileSet} par échantillonnage par réservoir
     * (reservoir sampling), en écrivant le résultat dans {@code destination} à partir de l'indice {@code offset},
     * et retourne le nouvel offset.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param destination le tableau de destination
     * @param offset l'indice à partir duquel écrire dans {@code destination}
     * @param randomGenerator le générateur aléatoire utilisé pour l'échantillonnage
     * @return l'indice suivant la dernière case remplie
     */
    public static int sampleColoredInto(int pkTileSet,
                                        TileKind.Colored[] destination,
                                        int offset,
                                        RandomGenerator randomGenerator) {

        int n = destination.length - offset;
        if (n < 0) throw new IllegalArgumentException();

        int i = 0;

        for (var c : TileKind.Colored.ALL) {
            int count = countOf(pkTileSet, c);
            for (int k = 0; k < count; k++) {
                if (i < n) {
                    destination[offset + i] = c;
                } else {
                    int j = randomGenerator.nextInt(i + 1);
                    if (j < n) destination[offset + j] = c;
                }
                i += 1;
            }
        }

        if (i < n) throw new IllegalArgumentException();

        return offset + size(pkTileSet);
    }

    /**
     * Retourne une représentation textuelle de l'ensemble empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return une représentation textuelle de l'ensemble
     */
    public static String toString(int pkTileSet) {
        StringJoiner j = new StringJoiner(",", "{", "}");

        for (var c : TileKind.Colored.ALL) {
            int n = countOf(pkTileSet, c);
            if (n > 0) j.add(n + "*" + c.name());
        }
        int m = countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        if (m > 0) j.add(m + "*" + TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER.name());

        return j.toString();
    }

    private static int computeFullColored() {
        int r = EMPTY;
        for (var c : TileKind.Colored.ALL) {
            r = union(r, of(20, c));
        }
        return r;
    }
}