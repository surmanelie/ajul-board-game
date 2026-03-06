package ch.epfl.ajul.intarray;

/**
 * Représente un tableau d'entiers en lecture seule.
 * <p>
 * Cette interface définit les opérations de base : obtenir la taille, accéder à une valeur,
 * obtenir une version immuable, et récupérer une copie des valeurs sous forme de tableau Java.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public interface ReadOnlyIntArray {

    /**
     * Retourne la taille du tableau.
     *
     * @return le nombre d'éléments
     */
    int size();

    /**
     * Retourne l'élément à l'indice {@code i}.
     *
     * @param i l'indice de l'élément
     * @return la valeur à l'indice {@code i}
     * @throws IndexOutOfBoundsException si {@code i} n'est pas un indice valide
     */
    int get(int i);

    /**
     * Retourne une version immuable de ce tableau.
     *
     * @return un {@link ImmutableIntArray} contenant les mêmes valeurs
     */
    ImmutableIntArray immutable();

    /**
     * Retourne une copie des valeurs contenues dans ce tableau.
     *
     * @return un nouveau tableau contenant les mêmes valeurs
     */
    int[] toArray();
}