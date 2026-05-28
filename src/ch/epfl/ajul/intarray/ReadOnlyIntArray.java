package ch.epfl.ajul.intarray;

/**
 * Représente un tableau d'entiers en lecture seule.
 * <p>
 * Cette interface définit les opérations de base permettant d'obtenir la
 * taille du tableau, d'accéder à une valeur, d'obtenir une version immuable,
 * et de récupérer une copie des valeurs sous la forme d'un tableau Java.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public interface ReadOnlyIntArray {

    /**
     * Retourne la taille du tableau.
     *
     * @return le nombre d'éléments du tableau
     */
    int size();

    /**
     * Retourne l'élément situé à l'indice donné.
     *
     * @param i l'indice de l'élément
     * @return la valeur située à l'indice {@code i}
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