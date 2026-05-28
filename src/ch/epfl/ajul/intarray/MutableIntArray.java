package ch.epfl.ajul.intarray;

/**
 * Tableau d'entiers mutable.
 * <p>
 * Cette classe encapsule un tableau existant sans en faire de copie. Toute
 * modification du tableau passé à {@link #wrapping(int[])} est donc visible
 * à travers l'instance créée.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class MutableIntArray extends AbstractIntArray {

    /**
     * Construit un tableau mutable à partir du tableau donné.
     *
     * @param array le tableau sous-jacent
     */
    private MutableIntArray(int[] array) {
        super(array);
    }

    /**
     * Retourne un tableau mutable encapsulant le tableau donné, sans copie.
     *
     * @param array le tableau à encapsuler
     * @return un tableau mutable encapsulant {@code array}
     */
    public static MutableIntArray wrapping(int[] array) {
        return new MutableIntArray(array);
    }
}