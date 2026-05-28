package ch.epfl.ajul.gamestate.packed;
import ch.epfl.ajul.TileKind;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Méthodes utilitaires pour manipuler la ligne plancher empaquetée d'un joueur.
 * <p>
 * Représentation attendue :
 * <ul>
 *   <li>bits 0..2 : taille de la ligne plancher (0..7),</li>
 *   <li>puis 7 emplacements de 3 bits chacun, contenant directement
 *   {@code tileKind.index()}.</li>
 * </ul>
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class PkFloor {

    private static final int MAX_SIZE = 7;
    private static final int SIZE_MASK = 0b111;
    private static final int BITS_PER_TILE = 3;

    /**
     * Ligne plancher vide.
     */
    public static final int EMPTY = 0;

    /**
     * Construit un manipulateur de ligne plancher empaquetée.
     * <p>
     * Cette classe ne contient que des méthodes statiques ; ce constructeur
     * n'a donc pas vocation à être utilisé.
     */
    public PkFloor() { }

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
     * Retourne la tuile d'index {@code i} de la ligne plancher empaquetée donnée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @param i l'index de la tuile à retourner
     * @return la tuile d'index {@code i}
     * @throws IndexOutOfBoundsException si l'index donné n'est pas valide
     */
    public static TileKind tileAt(int pkFloor, int i) {
        int floorSize = size(pkFloor);
        if (i < 0 || i >= floorSize) {
            throw new IndexOutOfBoundsException();
        }

        int kindIndex = (pkFloor >>> (BITS_PER_TILE * (i + 1))) & SIZE_MASK;
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
        int floorSize = size(pkFloor);
        for (int i = 0; i < floorSize; i += 1) {
            if (tileAt(pkFloor, i) == TileKind.FIRST_PLAYER_MARKER) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne l'ensemble de tuiles empaqueté correspondant au contenu de la
     * ligne plancher empaquetée donnée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return l'ensemble de tuiles empaqueté correspondant
     */
    public static int asPkTileSet(int pkFloor) {
        int packedTileSet = PkTileSet.EMPTY;
        int floorSize = size(pkFloor);

        for (int i = 0; i < floorSize; i += 1) {
            packedTileSet = PkTileSet.add(packedTileSet, tileAt(pkFloor, i));
        }
        return packedTileSet;
    }

    /**
     * Retourne une représentation textuelle de la ligne plancher empaquetée donnée.
     *
     * @param pkFloor la ligne plancher empaquetée
     * @return la représentation textuelle correspondante
     */
    public static String toString(int pkFloor) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        int floorSize = size(pkFloor);

        for (int i = 0; i < floorSize; i += 1) {
            joiner.add(tileAt(pkFloor, i).toString());
        }
        return joiner.toString();
    }

    /**
     * Retourne une ligne plancher empaquetée identique à {@code pkFloor},
     * à laquelle ont été ajoutées les tuiles de {@code pkTileSet}.
     * <p>
     * Si l'ensemble ajouté contient le marqueur de premier joueur et que la
     * ligne plancher le contient déjà, le marqueur n'est pas dupliqué.
     * Si le nombre total de tuiles dépasse la capacité de la ligne plancher,
     * seules les sept premières tuiles selon l'ordre des index sont conservées,
     * avec le marqueur en dernière position s'il est présent.
     *
     * @param pkFloor la ligne plancher empaquetée initiale
     * @param pkTileSet l'ensemble de tuiles empaqueté à ajouter
     * @return la ligne plancher empaquetée après ajout
     */
    public static int withAddedTiles(int pkFloor, int pkTileSet) {
        int initialSize = size(pkFloor);
        ArrayList<TileKind> tiles = new ArrayList<>();
        boolean markerAlreadyPresent = false;
        boolean markerMustBeAdded = false;

        for (int i = 0; i < initialSize; i += 1) {
            TileKind tile = tileAt(pkFloor, i);
            tiles.add(tile);
            if (tile == TileKind.FIRST_PLAYER_MARKER) {
                markerAlreadyPresent = true;
            }
        }

        for (TileKind kind : TileKind.ALL) {
            int count = PkTileSet.countOf(pkTileSet, kind);

            for (int k = 0; k < count; k += 1) {
                if (kind == TileKind.FIRST_PLAYER_MARKER) {
                    if (markerAlreadyPresent) {
                        continue;
                    }
                    markerAlreadyPresent = true;
                    markerMustBeAdded = true;
                }
                tiles.add(kind);
            }
        }

        if (tiles.size() > MAX_SIZE) {
            tiles.subList(MAX_SIZE, tiles.size()).clear();

            if (markerMustBeAdded && !tiles.contains(TileKind.FIRST_PLAYER_MARKER)) {
                tiles.set(MAX_SIZE - 1, TileKind.FIRST_PLAYER_MARKER);
            }
        }

        int packedFloor = 0;
        for (int i = tiles.size() - 1; i >= 0; i -= 1) {
            packedFloor = (packedFloor << BITS_PER_TILE) | tiles.get(i).index();
        }
        packedFloor = (packedFloor << BITS_PER_TILE) | tiles.size();

        return packedFloor;
    }
}