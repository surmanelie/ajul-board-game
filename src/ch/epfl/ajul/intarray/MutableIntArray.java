package ch.epfl.ajul.intarray;

/**
 * Tableau d'entiers mutable.
 * <p>
 * Cette classe encapsule ("wrap") un tableau existant sans en faire de copie. Toute modification
 * du tableau passé à {@link #wrapping(int[])} sera donc visible à travers l'instance créée.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class MutableIntArray extends AbstractIntArray {
    private MutableIntArray(int[] array) {
        super(array);
    }

    /**
     * Crée un {@code MutableIntArray} qui encapsule le tableau donné, sans copie.
     *
     * @param array le tableau à encapsuler
     * @return un tableau mutable encapsulant {@code array}
     */
    public static MutableIntArray wrapping(int[] array) {
        return new MutableIntArray(array);
    }
}