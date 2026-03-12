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

    public PkFloor() { }

    public static final int EMPTY = 0;

    public static int size(int pkFloor) {
        return pkFloor & SIZE_MASK;
    }

    public static TileKind tileAt(int pkFloor, int i) {
        int s = size(pkFloor);
        if (i < 0 || i >= s) throw new IndexOutOfBoundsException();
        int kindIndex = (pkFloor >>> (3 * (i + 1))) & 0b111;
        return TileKind.ALL.get(kindIndex);
    }

    public static boolean containsFirstPlayerMarker(int pkFloor) {
        int s = size(pkFloor);
        for (int i = 0; i < s; i += 1) {
            if (tileAt(pkFloor, i) == TileKind.FIRST_PLAYER_MARKER) return true;
        }
        return false;
    }

    public static int asPkTileSet(int pkFloor) {
        int acc = PkTileSet.EMPTY;
        int s = size(pkFloor);
        for (int i = 0; i < s; i += 1) {
            acc = PkTileSet.add(acc, tileAt(pkFloor, i));
        }
        return acc;
    }

    public static String toString(int pkFloor) {
        var j = new StringJoiner(", ", "[", "]");
        int s = size(pkFloor);
        for (int i = 0; i < s; i += 1) {
            j.add(tileAt(pkFloor, i).toString());
        }
        return j.toString();
    }

    public static int withAddedTiles(int pkFloor, int pkTileSet) {
        // 1) Récupérer les tuiles déjà présentes sur le floor
        int s0 = size(pkFloor);
        var tiles = new ArrayList<TileKind>();
        boolean markerAlreadyPresent = false;

        for (int i = 0; i < s0; i += 1) {
            TileKind t = tileAt(pkFloor, i);
            tiles.add(t);
            if (t == TileKind.FIRST_PLAYER_MARKER) markerAlreadyPresent = true;
        }

        // 2) Ajouter les tuiles contenues dans pkTileSet (avec multiplicités),
        //    en garantissant qu'on ne duplique jamais le marqueur
        for (var kind : TileKind.ALL) {
            int count = PkTileSet.countOf(pkTileSet, kind);
            for (int k = 0; k < count; k += 1) {
                if (kind == TileKind.FIRST_PLAYER_MARKER) {
                    if (markerAlreadyPresent) continue; // pas de duplication du marqueur
                    markerAlreadyPresent = true;
                }
                tiles.add(kind);
            }
        }

        boolean containsMarker = markerAlreadyPresent;

        // 3) Trier par index (A..E..MARKER)
        tiles.sort(Comparator.comparingInt(TileKind::index));

        // 4) Appliquer la capacité max = 7
        int newSize = Math.min(MAX_SIZE, tiles.size());
        var kept = tiles.subList(0, newSize);

        // 5) Si overflow et que le marker est présent, il doit être présent en dernière case
        if (tiles.size() > MAX_SIZE && containsMarker) {
            kept.set(MAX_SIZE - 1, TileKind.FIRST_PLAYER_MARKER);
            newSize = MAX_SIZE;
        }

        // 6) Empaqueter comme les tests officiels :
        int packed = 0;
        for (int i = newSize - 1; i >= 0; i -= 1) {
            packed = (packed << 3) | kept.get(i).index();
        }
        packed = (packed << 3) | newSize;

        return packed;
    }

}