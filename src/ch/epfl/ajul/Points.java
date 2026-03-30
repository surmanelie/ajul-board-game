package ch.epfl.ajul;

/**
 * Constantes et méthodes statiques permettant de calculer les points obtenus
 * par les joueurs lors d'une partie d'Ajul.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class Points {

    /**
     * Bonus obtenu pour chaque ligne complète du mur.
     */
    public static final int FULL_ROW_BONUS_POINTS = 2;

    /**
     * Bonus obtenu pour chaque colonne complète du mur.
     */
    public static final int FULL_COLUMN_BONUS_POINTS = 7;

    /**
     * Bonus obtenu pour chaque couleur complète du mur.
     */
    public static final int FULL_COLOR_BONUS_POINTS = 10;

    private static final int FLOOR_PENALTY = 0x3322211;
    private static final int TOTAL_FLOOR_PENALTY = 0xEB864210;
    private static final int NIBBLE_MASK = 0xF;

    /**
     * Construit un calculateur de points.
     * <p>
     * Cette classe ne contient que des méthodes statiques ; ce constructeur
     * n'a donc pas vocation à être utilisé.
     */
    public Points() { }

    /**
     * Retourne le nombre de points dus à l'ajout d'une tuile au mur
     * appartenant à un groupe horizontal de taille {@code hGroupSize}
     * et à un groupe vertical de taille {@code vGroupSize}.
     *
     * @param hGroupSize la taille du groupe horizontal
     * @param vGroupSize la taille du groupe vertical
     * @return le nombre de points obtenus
     */
    public static int newWallTilePoints(int hGroupSize, int vGroupSize) {
        assert hGroupSize >= 1;
        assert vGroupSize >= 1;

        if (vGroupSize == 1) {
            return hGroupSize;
        }
        if (hGroupSize == 1) {
            return vGroupSize;
        }
        return hGroupSize + vGroupSize;
    }

    /**
     * Retourne la pénalité associée à la tuile d'index {@code tileIndex}
     * dans la ligne plancher.
     *
     * @param tileIndex l'index de la tuile dans la ligne plancher
     * @return la pénalité associée à cette tuile
     */
    public static int floorPenalty(int tileIndex) {
        assert 0 <= tileIndex && tileIndex < 7;
        return (FLOOR_PENALTY >>> (4 * tileIndex)) & NIBBLE_MASK;
    }

    /**
     * Retourne la pénalité totale associée à une ligne plancher contenant
     * {@code tilesCount} tuiles.
     *
     * @param tilesCount le nombre de tuiles dans la ligne plancher
     * @return la pénalité totale correspondante
     */
    public static int totalFloorPenalty(int tilesCount) {
        assert 0 <= tilesCount && tilesCount <= 7;
        return (TOTAL_FLOOR_PENALTY >>> (4 * tilesCount)) & NIBBLE_MASK;
    }
}