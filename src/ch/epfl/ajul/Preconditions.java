package ch.epfl.ajul;

/**
 * Méthodes utilitaires pour vérifier des préconditions.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class Preconditions {

    /**
     * Construit un objet {@code Preconditions}.
     * <p>
     * Cette classe étant uniquement composée de méthodes statiques, ce constructeur n'a pas vocation
     * à être utilisé, mais il est présent afin de satisfaire les vérifications de signatures.
     */
    public Preconditions() { }

    /**
     * Vérifie qu'une condition est vraie.
     *
     * @param shouldBeTrue la condition qui doit être vraie
     * @throws IllegalArgumentException si {@code shouldBeTrue} est faux
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}