package ch.epfl.ajul;

import java.util.List;

/**
 * Identifiant d'un joueur, de {@code P1} à {@code P4}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public enum PlayerId {
    P1,
    P2,
    P3,
    P4;

    /**
     * Liste immuable contenant tous les identifiants de joueurs, dans l'ordre
     * {@code P1..P4}.
     */
    public static final List<PlayerId> ALL = List.of(values());
}