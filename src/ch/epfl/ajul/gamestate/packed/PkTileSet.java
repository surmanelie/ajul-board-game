package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.random.RandomGenerator;

/**
 * Méthodes utilitaires pour manipuler un ensemble de tuiles empaqueté dans
 * une valeur de type {@code int}.
 * <p>
 * Représentation :
 * <ul>
 *   <li>les nombres de tuiles colorées A..E sont stockés dans 5 champs de
 *   6 bits, sur les bits 0 à 29,</li>
 *   <li>le marqueur {@code FIRST_PLAYER_MARKER} est stocké dans le bit
 *   d'index 30,</li>
 *   <li>le bit 31 est inutilisé et vaut toujours 0.</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkTileSet {

    private static final int BITS_PER_COLOR = 6;
    private static final int COLOR_MASK = (1 << BITS_PER_COLOR) - 1;
    private static final int MARKER_BIT_INDEX = 30;
    private static final int FULL_COLORED_COUNT = 20;

    /**
     * Ensemble vide.
     */
    public static final int EMPTY = 0;

    /**
     * Ensemble contenant 20 tuiles de chaque couleur, sans marqueur.
     */
    public static final int FULL_COLORED = computeFullColored();

    /**
     * Ensemble contenant 20 tuiles de chaque couleur et le marqueur de
     * premier joueur.
     */
    public static final int FULL =
            union(FULL_COLORED, of(1, TileKind.FIRST_PLAYER_MARKER));

    /**
     * Construit un manipulateur d'ensembles de tuiles empaquetés.
     * <p>
     * Cette classe ne contient que des méthodes statiques ; ce constructeur
     * n'a donc pas vocation à être utilisé, mais il est conservé afin de
     * respecter les signatures attendues.
     */
    public PkTileSet() { }

    /**
     * Retourne l'ensemble empaqueté contenant exactement {@code count}
     * occurrences de {@code tileKind}.
     *
     * @param count le nombre d'occurrences de la tuile
     * @param tileKind le type de tuile
     * @return l'ensemble empaqueté correspondant
     */
    public static int of(int count, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored coloredTile) {
            int shift = coloredTile.index() * BITS_PER_COLOR;
            return (count & COLOR_MASK) << shift;
        } else {
            return (count & 1) << MARKER_BIT_INDEX;
        }
    }

    /**
     * Retourne {@code true} si et seulement si l'ensemble empaqueté est vide.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return {@code true} si et seulement si l'ensemble est vide
     */
    public static boolean isEmpty(int pkTileSet) {
        return pkTileSet == EMPTY;
    }

    /**
     * Retourne le nombre d'occurrences du type de tuile donné dans l'ensemble
     * empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile
     * @return le nombre d'occurrences de {@code tileKind}
     */
    public static int countOf(int pkTileSet, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored coloredTile) {
            int shift = coloredTile.index() * BITS_PER_COLOR;
            return (pkTileSet >>> shift) & COLOR_MASK;
        } else {
            return (pkTileSet >>> MARKER_BIT_INDEX) & 1;
        }
    }

    /**
     * Retourne le nombre total de tuiles dans l'ensemble empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return le nombre total de tuiles dans l'ensemble
     */
    public static int size(int pkTileSet) {
        int partialSums = pkTileSet + (pkTileSet >>> BITS_PER_COLOR);
        return (partialSums & COLOR_MASK)
                + ((partialSums >>> (2 * BITS_PER_COLOR)) & COLOR_MASK)
                + ((partialSums >>> (4 * BITS_PER_COLOR)) & COLOR_MASK);
    }

    /**
     * Retourne le sous-ensemble composé uniquement des occurrences du type de
     * tuile donné présentes dans {@code pkTileSet}.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile à extraire
     * @return l'ensemble empaqueté ne contenant que {@code tileKind}
     */
    public static int subsetOf(int pkTileSet, TileKind tileKind) {
        return of(countOf(pkTileSet, tileKind), tileKind);
    }

    /**
     * Retourne un ensemble empaqueté identique à {@code pkTileSet}, mais avec
     * une occurrence supplémentaire de {@code tileKind}.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile à ajouter
     * @return l'ensemble empaqueté après ajout
     */
    public static int add(int pkTileSet, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored coloredTile) {
            return pkTileSet + (1 << (coloredTile.index() * BITS_PER_COLOR));
        } else {
            return pkTileSet + (1 << MARKER_BIT_INDEX);
        }
    }

    /**
     * Retourne un ensemble empaqueté identique à {@code pkTileSet}, mais avec
     * une occurrence de moins de {@code tileKind}.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param tileKind le type de tuile à retirer
     * @return l'ensemble empaqueté après retrait
     */
    public static int remove(int pkTileSet, TileKind tileKind) {
        if (tileKind instanceof TileKind.Colored coloredTile) {
            return pkTileSet - (1 << (coloredTile.index() * BITS_PER_COLOR));
        } else {
            return pkTileSet - (1 << MARKER_BIT_INDEX);
        }
    }

    /**
     * Retourne l'union de deux ensembles empaquetés.
     *
     * @param pkTileSet1 le premier ensemble empaqueté
     * @param pkTileSet2 le second ensemble empaqueté
     * @return l'union des deux ensembles
     */
    public static int union(int pkTileSet1, int pkTileSet2) {
        return pkTileSet1 + pkTileSet2;
    }

    /**
     * Retourne la différence {@code pkTileSet1 \ pkTileSet2}.
     *
     * @param pkTileSet1 l'ensemble empaqueté de départ
     * @param pkTileSet2 l'ensemble empaqueté à soustraire
     * @return la différence des deux ensembles
     */
    public static int difference(int pkTileSet1, int pkTileSet2) {
        return pkTileSet1 - pkTileSet2;
    }

    /**
     * Copie les tuiles colorées de {@code pkTileSet} dans le tableau donné,
     * dans l'ordre des couleurs A, B, C, D puis E.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param destination le tableau dans lequel écrire les tuiles colorées
     * @return l'indice suivant la dernière case remplie
     */
    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination) {
        int offset = 0;
        for (TileKind.Colored coloredTile : TileKind.Colored.ALL) {
            int count = countOf(pkTileSet, coloredTile);
            Arrays.fill(destination, offset, offset + count, coloredTile);
            offset += count;
        }
        return offset;
    }

    /**
     * Échantillonne aléatoirement les tuiles colorées de {@code pkTileSet}
     * par échantillonnage par réservoir, les écrit dans {@code destination}
     * à partir de {@code offset}, puis retourne le nouvel offset.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @param destination le tableau de destination
     * @param offset l'indice à partir duquel écrire dans {@code destination}
     * @param randomGenerator le générateur aléatoire utilisé
     * @return l'indice suivant la dernière case remplie
     * @throws IllegalArgumentException si l'offset est invalide ou si le nombre
     * de tuiles colorées disponibles est insuffisant
     */
    public static int sampleColoredInto(
            int pkTileSet,
            TileKind.Colored[] destination,
            int offset,
            RandomGenerator randomGenerator
    ) {
        int sampleSize = destination.length - offset;
        if (sampleSize < 0) {
            throw new IllegalArgumentException();
        }

        int seenCount = 0;

        for (TileKind.Colored coloredTile : TileKind.Colored.ALL) {
            int count = countOf(pkTileSet, coloredTile);
            for (int k = 0; k < count; k += 1) {
                if (seenCount < sampleSize) {
                    destination[offset + seenCount] = coloredTile;
                } else {
                    int randomIndex = randomGenerator.nextInt(seenCount + 1);
                    if (randomIndex < sampleSize) {
                        destination[offset + randomIndex] = coloredTile;
                    }
                }
                seenCount += 1;
            }
        }

        if (seenCount < sampleSize) {
            throw new IllegalArgumentException();
        }

        return offset + seenCount;
    }

    /**
     * Retourne une représentation textuelle de l'ensemble empaqueté.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return une représentation textuelle de l'ensemble
     */
    public static String toString(int pkTileSet) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");

        for (TileKind.Colored coloredTile : TileKind.Colored.ALL) {
            int count = countOf(pkTileSet, coloredTile);
            if (count > 0) {
                joiner.add(count + "*" + coloredTile.name());
            }
        }

        int markerCount = countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        if (markerCount > 0) {
            joiner.add(markerCount + "*" + TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER.name());
        }
        return joiner.toString();
    }

    /**
     * Construit l'ensemble contenant 20 tuiles de chaque couleur, sans marqueur.
     *
     * @return l'ensemble contenant toutes les tuiles colorées
     */
    private static int computeFullColored() {
        int fullColoredSet = EMPTY;
        for (TileKind.Colored coloredTile : TileKind.Colored.ALL) {
            fullColoredSet = union(
                    fullColoredSet,
                    of(FULL_COLORED_COUNT, coloredTile)
            );
        }
        return fullColoredSet;
    }
}