package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

/**
 * Méthodes statiques permettant de manipuler le contenu empaqueté du mur d'un joueur.
 * <p>
 * Le mur est représenté comme un ensemble empaqueté d'indices de cases occupées.
 * La case d'index {@code i} est occupée si et seulement si le bit d'index {@code i}
 * de l'entier représentant le mur vaut 1.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkWall {

    /** Mur vide. */
    public static final int EMPTY = PkIntSet32.EMPTY;

    /** Largeur du mur. */
    public static final int WALL_WIDTH = 5;

    /** Hauteur du mur. */
    public static final int WALL_HEIGHT = 5;

    private static final int ROW0_MASK = 0b00000_00000_00000_00000_11111;
    private static final int COL0_MASK = 0b00001_00001_00001_00001_00001;

    private static final int[] ROW_MASKS = new int[WALL_HEIGHT];
    private static final int[] COLUMN_MASKS = new int[WALL_WIDTH];

    private static final int COLOR_A_MASK = 0b10000_01000_00100_00010_00001;
    private static final int COLOR_B_MASK = 0b00001_10000_01000_00100_00010;
    private static final int COLOR_C_MASK = 0b00010_00001_10000_01000_00100;
    private static final int COLOR_D_MASK = 0b00100_00010_00001_10000_01000;
    private static final int COLOR_E_MASK = 0b01000_00100_00010_00001_10000;

    private static final int[] COLOR_MASKS = {
            COLOR_A_MASK, COLOR_B_MASK, COLOR_C_MASK, COLOR_D_MASK, COLOR_E_MASK
    };

    static {
        for (int i = 0; i < WALL_HEIGHT; i++) {
            ROW_MASKS[i] = ROW0_MASK << (i * WALL_WIDTH);
        }
        for (int i = 0; i < WALL_WIDTH; i++) {
            COLUMN_MASKS[i] = COL0_MASK << i;
        }
    }

    /**
     * Construit un objet {@code PkWall}.
     * <p>
     * Cette classe n'étant composée que de méthodes statiques, ce constructeur n'a pas vocation
     * à être utilisé.
     */
    public PkWall() { }

    public static int indexOf(TileDestination.Pattern line, TileKind.Colored color) {
        assert line != null;
        assert color != null;
        return line.index() * WALL_WIDTH + column(line, color);
    }

    public static int column(TileDestination.Pattern line, TileKind.Colored color) {
        assert line != null;
        assert color != null;
        return Math.floorMod(color.index() + line.index(), WALL_WIDTH);
    }

    public static TileKind.Colored colorAt(TileDestination.Pattern line, int column) {
        assert line != null;
        assert 0 <= column && column < WALL_WIDTH;
        int colorIndex = Math.floorMod(column - line.index(), WALL_WIDTH);
        return TileKind.Colored.ALL.get(colorIndex);
    }

    public static int withTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert line != null;
        assert color != null;
        return PkIntSet32.add(pkWall, indexOf(line, color));
    }

    public static boolean hasTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert line != null;
        assert color != null;
        return PkIntSet32.contains(pkWall, indexOf(line, color));
    }

    public static int hGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert line != null;
        assert color != null;

        int col = column(line, color);
        int row = line.index();

        int size = 1;

        for (int c = col - 1; c >= 0; --c) {
            int index = row * WALL_WIDTH + c;
            if (!PkIntSet32.contains(pkWall, index)) break;
            size += 1;
        }

        for (int c = col + 1; c < WALL_WIDTH; ++c) {
            int index = row * WALL_WIDTH + c;
            if (!PkIntSet32.contains(pkWall, index)) break;
            size += 1;
        }

        return size;
    }

    public static int vGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert line != null;
        assert color != null;

        int col = column(line, color);
        int row = line.index();

        int size = 1;

        for (int r = row - 1; r >= 0; --r) {
            int index = r * WALL_WIDTH + col;
            if (!PkIntSet32.contains(pkWall, index)) break;
            size += 1;
        }

        for (int r = row + 1; r < WALL_HEIGHT; ++r) {
            int index = r * WALL_WIDTH + col;
            if (!PkIntSet32.contains(pkWall, index)) break;
            size += 1;
        }

        return size;
    }

    public static boolean hasFullRow(int pkWall) {
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            if (isRowFull(pkWall, line)) return true;
        }
        return false;
    }

    public static boolean isRowFull(int pkWall, TileDestination.Pattern line) {
        assert line != null;
        int mask = ROW_MASKS[line.index()];
        return (pkWall & mask) == mask;
    }

    public static boolean isColumnFull(int pkWall, int column) {
        assert 0 <= column && column < WALL_WIDTH;
        int mask = COLUMN_MASKS[column];
        return (pkWall & mask) == mask;
    }

    public static boolean isColorFull(int pkWall, TileKind.Colored color) {
        assert color != null;
        int mask = COLOR_MASKS[color.index()];
        return (pkWall & mask) == mask;
    }

    public static int asPkTileSet(int pkWall) {
        int pkTileSet = PkTileSet.EMPTY;

        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int count = Integer.bitCount(pkWall & COLOR_MASKS[color.index()]);
            pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(count, color));
        }

        return pkTileSet;
    }

    public static String toString(int pkWall) {
        StringBuilder b = new StringBuilder();
        b.append('[');

        for (int row = 0; row < WALL_HEIGHT; row++) {
            if (row > 0) b.append(", ");

            TileDestination.Pattern line = TileDestination.Pattern.ALL.get(row);

            for (int col = 0; col < WALL_WIDTH; col++) {
                TileKind.Colored color = colorAt(line, col);
                char letter = (char) ('a' + color.index());

                if (PkIntSet32.contains(pkWall, row * WALL_WIDTH + col)) {
                    letter = Character.toUpperCase(letter);
                }

                b.append(letter);
            }
        }

        b.append(']');
        return b.toString();
    }
}