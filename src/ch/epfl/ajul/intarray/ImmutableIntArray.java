package ch.epfl.ajul.intarray;

/**
 * Tableau d'entiers immuable.
 * <p>
 * Une instance de cette classe ne peut pas être modifiée après sa création. La méthode
 * {@link #copyOf(int[])} effectue une copie du tableau fourni afin de garantir l'immutabilité.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class ImmutableIntArray extends AbstractIntArray {
    private ImmutableIntArray(int[] array) {
        super(array);
    }

    /**
     * Crée un {@code ImmutableIntArray} contenant les mêmes valeurs que le tableau donné.
     *
     * @param array le tableau source
     * @return un tableau immuable contenant une copie des valeurs de {@code array}
     */
    public static ImmutableIntArray copyOf(int[] array) {
        return new ImmutableIntArray(array.clone());
    }

    /**
     * Retourne une version immuable de ce tableau.
     * <p>
     * Comme ce tableau est déjà immuable, la méthode retourne {@code this}.
     *
     * @return {@code this}
     */
    @Override
    public ImmutableIntArray immutable() {
        return this;
    }
}