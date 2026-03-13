package ch.epfl.ajul.gamestate.packed;

/**
 * Méthodes statiques permettant de manipuler un ensemble d'entiers compris entre 0 et 31,
 * empaqueté dans un entier de type {@code int}.
 * <p>
 * Dans cette représentation, le bit d'index {@code i} correspond à l'entier {@code i}.
 * Si ce bit vaut 1, alors {@code i} appartient à l'ensemble.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class PkIntSet32 {

    /**
     * Ensemble vide.
     */
    public static final int EMPTY = 0;


    /**
     * Retourne vrai si et seulement si l'ensemble empaqueté contient l'entier {@code i}.
     *
     * @param pkIntSet32 l'ensemble empaqueté
     * @param i l'entier à tester
     * @return vrai si et seulement si {@code i} appartient à l'ensemble
     */
    public static boolean contains(int pkIntSet32, int i) {
        assert 0 <= i && i < Integer.SIZE;
        return ((pkIntSet32 >>> i) & 1) == 1;
    }

    /**
     * Retourne vrai si et seulement si l'ensemble empaqueté {@code pkIntSet32a}
     * contient tous les éléments de l'ensemble empaqueté {@code pkIntSet32b}.
     *
     * @param pkIntSet32a le premier ensemble empaqueté
     * @param pkIntSet32b le second ensemble empaqueté
     * @return vrai si et seulement si {@code pkIntSet32a} contient tous les éléments de {@code pkIntSet32b}
     */
    public static boolean containsAll(int pkIntSet32a, int pkIntSet32b) {
        return (pkIntSet32a & pkIntSet32b) == pkIntSet32b;
    }

    /**
     * Retourne un ensemble empaqueté identique à {@code pkIntSet32}, mais contenant l'entier {@code i}.
     *
     * @param pkIntSet32 l'ensemble empaqueté
     * @param i l'entier à ajouter
     * @return l'ensemble empaqueté après ajout de {@code i}
     */
    public static int add(int pkIntSet32, int i) {
        assert 0 <= i && i < Integer.SIZE;
        return pkIntSet32 | (1 << i);
    }

    /**
     * Retourne un ensemble empaqueté identique à {@code pkIntSet32}, mais ne contenant pas l'entier {@code i}.
     *
     * @param pkIntSet32 l'ensemble empaqueté
     * @param i l'entier à retirer
     * @return l'ensemble empaqueté après retrait de {@code i}
     */
    public static int remove(int pkIntSet32, int i) {
        assert 0 <= i && i < Integer.SIZE;
        return pkIntSet32 & ~(1 << i);
    }
}