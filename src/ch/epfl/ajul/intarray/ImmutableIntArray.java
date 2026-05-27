package ch.epfl.ajul.intarray;

/**
 * Tableau d'entiers immuable.
 * <p>
 * Une instance de cette classe ne peut pas être modifiée après sa création.
 * La méthode {@link #copyOf(int[])} effectue une copie du tableau fourni afin
 * de garantir l'immutabilité.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class ImmutableIntArray extends AbstractIntArray {

    /**
     * Construit un tableau immuable à partir du tableau donné.
     *
     * @param array le tableau sous-jacent
     */
    private ImmutableIntArray(int[] array) {
        super(array);
    }

    /**
     * Retourne un tableau immuable contenant les mêmes valeurs que le tableau donné.
     *
     * @param array le tableau source
     * @return un tableau immuable contenant une copie des valeurs de {@code array}
     */
    public static ImmutableIntArray copyOf(int[] array) {
        return new ImmutableIntArray(array.clone());
    }

    /**
     * Retourne ce tableau.
     * <p>
     * Comme ce tableau est déjà immuable, cette méthode retourne simplement
     * {@code this}.
     *
     * @return {@code this}
     */
    @Override
    public ImmutableIntArray immutable() {
        return this;
    }
}