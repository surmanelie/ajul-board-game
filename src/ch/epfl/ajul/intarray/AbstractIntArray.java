package ch.epfl.ajul.intarray;

import java.util.Arrays;

/**
 * Implémentation de base partagée par les différents tableaux d'entiers
 * du projet.
 * <p>
 * Cette classe fournit les opérations communes d'un tableau d'entiers en
 * lecture seule, comme la taille, l'accès aux éléments, la copie défensive,
 * et la conversion en chaîne. Elle délègue la politique de mutabilité ou
 * d'immutabilité aux sous-classes.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public abstract class AbstractIntArray implements ReadOnlyIntArray {

    private final int[] array;

    /**
     * Construit un tableau d'entiers à partir du tableau fourni.
     *
     * @param array le tableau d'entiers sous-jacent
     */
    protected AbstractIntArray(int[] array) {
        this.array = array;
    }

    /**
     * Retourne la taille du tableau.
     *
     * @return le nombre d'éléments du tableau
     */
    @Override
    public int size() {
        return array.length;
    }

    /**
     * Retourne l'élément situé à l'indice donné.
     *
     * @param i l'indice de l'élément
     * @return la valeur située à l'indice {@code i}
     * @throws IndexOutOfBoundsException si {@code i} n'est pas un indice valide
     */
    @Override
    public int get(int i) {
        if (i < 0 || i >= array.length) {
            throw new IndexOutOfBoundsException();
        }
        return array[i];
    }

    /**
     * Retourne une version immuable de ce tableau.
     *
     * @return un {@link ImmutableIntArray} contenant les mêmes valeurs
     */
    @Override
    public ImmutableIntArray immutable() {
        return ImmutableIntArray.copyOf(array);
    }

    /**
     * Retourne une copie des valeurs contenues dans ce tableau.
     *
     * @return un nouveau tableau contenant les mêmes valeurs
     */
    @Override
    public int[] toArray() {
        return array.clone();
    }

    /**
     * Retourne une représentation textuelle des valeurs du tableau.
     *
     * @return une chaîne représentant le contenu du tableau
     */
    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}