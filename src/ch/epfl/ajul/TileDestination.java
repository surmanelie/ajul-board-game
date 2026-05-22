package ch.epfl.ajul;

import java.util.List;

/**
 * Destination possible pour des tuiles lors d'un coup : une ligne de motif (pattern line) ou le plancher (floor).
 * <p>
 * Les destinations sont indexées de manière à être cohérentes avec la liste {@link #ALL}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public sealed interface TileDestination permits TileDestination.Pattern, TileDestination.Floor {

    /**
     * Retourne l'indice de cette destination.
     * <p>
     * L'indice correspond à la position de cette destination dans {@link #ALL}.
     *
     * @return l'indice de la destination
     */
    int index();

    /**
     * Retourne la capacité (nombre maximal de tuiles) de cette destination.
     *
     * @return la capacité de la destination
     */
    int capacity();

    /**
     * Première ligne de motif (capacité 1).
     */
    TileDestination PATTERN_1 = Pattern.PATTERN_1;

    /**
     * Deuxième ligne de motif (capacité 2).
     */
    TileDestination PATTERN_2 = Pattern.PATTERN_2;

    /**
     * Troisième ligne de motif (capacité 3).
     */
    TileDestination PATTERN_3 = Pattern.PATTERN_3;

    /**
     * Quatrième ligne de motif (capacité 4).
     */
    TileDestination PATTERN_4 = Pattern.PATTERN_4;

    /**
     * Cinquième ligne de motif (capacité 5).
     */
    TileDestination PATTERN_5 = Pattern.PATTERN_5;

    /**
     * Plancher (floor), destination de pénalité.
     */
    TileDestination FLOOR = Floor.FLOOR;

    /**
     * Liste immuable de toutes les destinations, dans l'ordre des indices.
     */
    List<TileDestination> ALL = List.of(PATTERN_1, PATTERN_2, PATTERN_3, PATTERN_4, PATTERN_5, FLOOR);

    /**
     * Nombre total de destinations.
     */
    int COUNT = ALL.size();

    /**
     * Destinations correspondant aux lignes de motif (pattern lines).
     *
     * @author Danny Levy (394098)
     * @author Elie Menashe Reuben Surman (410685)
     */
    enum Pattern implements TileDestination {
        /** Première ligne de motif (capacité 1). */
        PATTERN_1,
        /** Deuxième ligne de motif (capacité 2). */
        PATTERN_2,
        /** Troisième ligne de motif (capacité 3). */
        PATTERN_3,
        /** Quatrième ligne de motif (capacité 4). */
        PATTERN_4,
        /** Cinquième ligne de motif (capacité 5). */
        PATTERN_5;

        /**
         * Liste immuable de toutes les lignes de motif, dans l'ordre {@code PATTERN_1..PATTERN_5}.
         */
        public static final List<Pattern> ALL = List.of(values());

        /**
         * Nombre total de lignes de motif.
         */
        public static final int COUNT = ALL.size();

        /**
         * {@inheritDoc}
         */
        @Override
        public int index() {
            return ordinal();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int capacity() {
            return ordinal() + 1;
        }
    }

    /**
     * Destination correspondant au plancher (floor).
     *
     * @author Danny Levy (394098)
     * @author Elie Menashe Reuben Surman (410685)
     */
    enum Floor implements TileDestination {
        /** Plancher (floor), destination de pénalité. */
        FLOOR;

        /**
         * Liste immuable contenant l'unique valeur {@link #FLOOR}.
         */
        public static final List<Floor> ALL = List.of(values());

        /**
         * Nombre total de destinations de type floor.
         */
        public static final int COUNT = ALL.size();

        /**
         * {@inheritDoc}
         */
        @Override
        public int index() {
            return 5;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int capacity() {
            return 7;
        }
    }
}