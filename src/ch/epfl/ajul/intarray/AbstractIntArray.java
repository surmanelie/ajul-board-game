package ch.epfl.ajul.intarray;

import java.util.Arrays;

/**
 * Implémentation de base partagée par les différents tableaux d'entiers du projet.
 * <p>
 * Cette classe fournit les opérations communes d'un tableau d'entiers en lecture seule
 * (taille, accès, copie défensive, conversion en chaîne) et délègue la politique
 * de mutabilité/immutabilité aux sous-classes.
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
     * @return le nombre d'éléments
     */
    @Override
    public int size() {
        return array.length;
    }

    /**
     * Retourne l'élément à l'indice {@code i}.
     *
     * @param i l'indice de l'élément
     * @return la valeur à l'indice {@code i}
     * @throws IndexOutOfBoundsException si {@code i} n'est pas un indice valide
     */
    @Override
    public int get(int i) {
        if (i < 0 || i >= array.length) throw new IndexOutOfBoundsException();
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
     * Retourne une copie des valeurs contenues dans ce tableau (copie défensive).
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