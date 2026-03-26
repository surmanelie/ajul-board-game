package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.StringJoiner;

/**
 * Méthodes utilitaires pour manipuler le contenu de la ligne plancher d'un joueur,
 * empaqueté dans un {@code int}.
 * <p>
 * Représentation attendue :
 * <ul>
 *   <li>bits 0..2 : taille de la ligne plancher (0..7)</li>
 *   <li>ensuite, 7 emplacements de 3 bits chacun, contenant directement {@code tileKind.index()}</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkFloor {

    private static final int MAX_SIZE = 7;
    private static final int SIZE_MASK = 0b111;

    /**
     * Construit un manipulateur de ligne plancher empaquetée.
     * <p>
     * Cette classe ne contient que des méthodes statiques ; ce constructeur n'a donc
     * pas vocation à être utilisé.
     */
    public PkFloor() { }

    /**
     * Ligne plancher vide.
     */
    public static final int EMPTY = 0;

    /**
     * Retourne le nombre de tuiles contenues dans la ligne plancher empaquetée donnée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return le nombre de tuiles qu'elle contient
     */
    public static int size(int pkFloor) {
        return pkFloor & SIZE_MASK;
    }

    /**
     * Retourne la tuile d'index {@code i} dans la ligne plancher empaquetée donnée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @param i l'index de la tuile à retourner
     * @return la tuile d'index {@code i}
     * @throws IndexOutOfBoundsException si l'index donné n'est pas valide
     */
    public static TileKind tileAt(int pkFloor, int i) {
        int s = size(pkFloor);
        if (i < 0 || i >= s) throw new IndexOutOfBoundsException();
        int kindIndex = (pkFloor >>> (3 * (i + 1))) & 0b111;
        return TileKind.ALL.get(kindIndex);
    }

    /**
     * Retourne vrai ssi la ligne plancher empaquetée donnée contient le marqueur
     * de premier joueur.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return vrai ssi elle contient le marqueur de premier joueur
     */
    public static boolean containsFirstPlayerMarker(int pkFloor) {
        int s = size(pkFloor);
        for (int i = 0; i < s; i += 1) {
            if (tileAt(pkFloor, i) == TileKind.FIRST_PLAYER_MARKER) return true;
        }
        return false;
    }

    /**
     * Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles de la
     * ligne plancher empaquetée donnée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkFloor) {
        int acc = PkTileSet.EMPTY;
        int s = size(pkFloor);
        for (int i = 0; i < s; i += 1) {
            acc = PkTileSet.add(acc, tileAt(pkFloor, i));
        }
        return acc;
    }

    /**
     * Retourne une représentation textuelle de la ligne plancher empaquetée donnée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return la représentation textuelle correspondante
     */
    public static String toString(int pkFloor) {
        var j = new StringJoiner(", ", "[", "]");
        int s = size(pkFloor);
        for (int i = 0; i < s; i += 1) {
            j.add(tileAt(pkFloor, i).toString());
        }
        return j.toString();
    }

    /**
     * Retourne une ligne plancher empaquetée identique à {@code pkFloor}, mais à laquelle
     * ont été ajoutées les tuiles de {@code pkTileSet}.
     * <p>
     * Si l'ensemble ajouté contient le marqueur de premier joueur et que la ligne plancher
     * le contient déjà, le marqueur n'est pas dupliqué. Si le nombre total de tuiles dépasse
     * la capacité de la ligne plancher, seules les sept premières tuiles selon l'ordre des
     * index sont conservées, avec le marqueur en dernière position s'il est présent.
     *
     * @param pkFloor la ligne plancher empaquetée initiale
     * @param pkTileSet l'ensemble de tuiles empaqueté à ajouter
     * @return la ligne plancher empaquetée après ajout
     */
    public static int withAddedTiles(int pkFloor, int pkTileSet) {
        int s0 = size(pkFloor);
        var tiles = new ArrayList<TileKind>();
        boolean markerAlreadyPresent = false;

        for (int i = 0; i < s0; i += 1) {
            TileKind t = tileAt(pkFloor, i);
            tiles.add(t);
            if (t == TileKind.FIRST_PLAYER_MARKER) markerAlreadyPresent = true;
        }

        for (var kind : TileKind.ALL) {
            int count = PkTileSet.countOf(pkTileSet, kind);
            for (int k = 0; k < count; k += 1) {
                if (kind == TileKind.FIRST_PLAYER_MARKER) {
                    if (markerAlreadyPresent) continue;
                    markerAlreadyPresent = true;
                }
                tiles.add(kind);
            }
        }

        boolean containsMarker = markerAlreadyPresent;

        tiles.sort(Comparator.comparingInt(TileKind::index));

        int newSize = Math.min(MAX_SIZE, tiles.size());
        var kept = tiles.subList(0, newSize);

        if (tiles.size() > MAX_SIZE && containsMarker) {
            kept.set(MAX_SIZE - 1, TileKind.FIRST_PLAYER_MARKER);
            newSize = MAX_SIZE;
        }

        int packed = 0;
        for (int i = newSize - 1; i >= 0; i -= 1) {
            packed = (packed << 3) | kept.get(i).index();
        }
        packed = (packed << 3) | newSize;

        return packed;
    }
}
