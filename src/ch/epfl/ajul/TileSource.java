package ch.epfl.ajul;

import java.util.List;

/**
 * Source possible de tuiles lors d'un coup, c'est-à-dire la zone centrale
 * ou une fabrique.
 * <p>
 * Les sources sont indexées de manière à être cohérentes avec la liste
 * {@link #ALL}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public sealed interface TileSource {

    /**
     * Zone centrale.
     */
    TileSource CENTER_AREA = CenterArea.CENTER_AREA;

    /**
     * Première fabrique.
     */
    TileSource FACTORY_1 = Factory.FACTORY_1;

    /**
     * Deuxième fabrique.
     */
    TileSource FACTORY_2 = Factory.FACTORY_2;

    /**
     * Troisième fabrique.
     */
    TileSource FACTORY_3 = Factory.FACTORY_3;

    /**
     * Quatrième fabrique.
     */
    TileSource FACTORY_4 = Factory.FACTORY_4;

    /**
     * Cinquième fabrique.
     */
    TileSource FACTORY_5 = Factory.FACTORY_5;

    /**
     * Sixième fabrique.
     */
    TileSource FACTORY_6 = Factory.FACTORY_6;

    /**
     * Septième fabrique.
     */
    TileSource FACTORY_7 = Factory.FACTORY_7;

    /**
     * Huitième fabrique.
     */
    TileSource FACTORY_8 = Factory.FACTORY_8;

    /**
     * Neuvième fabrique.
     */
    TileSource FACTORY_9 = Factory.FACTORY_9;

    /**
     * Liste immuable de toutes les sources, dans l'ordre des indices.
     */
    List<TileSource> ALL = List.of(
            CENTER_AREA,
            FACTORY_1,
            FACTORY_2,
            FACTORY_3,
            FACTORY_4,
            FACTORY_5,
            FACTORY_6,
            FACTORY_7,
            FACTORY_8,
            FACTORY_9
    );

    /**
     * Nombre total de sources.
     */
    int COUNT = ALL.size();

    /**
     * Retourne l'indice de cette source.
     * <p>
     * L'indice correspond à la position de cette source dans {@link #ALL}.
     *
     * @return l'indice de la source
     */
    int index();

    /**
     * Source correspondant à la zone centrale.
     *
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    enum CenterArea implements TileSource {
        /** Zone centrale. */
        CENTER_AREA;

        /**
         * Retourne l'indice de la zone centrale.
         *
         * @return l'indice de la zone centrale
         */
        @Override
        public int index() {
            return 0;
        }
    }

    /**
     * Sources correspondant aux fabriques.
     *
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    enum Factory implements TileSource {
        /** Première fabrique. */
        FACTORY_1,
        /** Deuxième fabrique. */
        FACTORY_2,
        /** Troisième fabrique. */
        FACTORY_3,
        /** Quatrième fabrique. */
        FACTORY_4,
        /** Cinquième fabrique. */
        FACTORY_5,
        /** Sixième fabrique. */
        FACTORY_6,
        /** Septième fabrique. */
        FACTORY_7,
        /** Huitième fabrique. */
        FACTORY_8,
        /** Neuvième fabrique. */
        FACTORY_9;

        /**
         * Nombre de tuiles contenues dans une fabrique.
         */
        public static final int TILES_PER_FACTORY = 4;

        /**
         * Liste immuable de toutes les fabriques, dans l'ordre
         * {@code FACTORY_1..FACTORY_9}.
         */
        public static final List<Factory> ALL = List.of(values());

        /**
         * Nombre total de fabriques.
         */
        public static final int COUNT = ALL.size();

        /**
         * Retourne l'indice de cette fabrique.
         *
         * @return l'indice de cette fabrique
         */
        @Override
        public int index() {
            return ordinal() + 1;
        }
    }
}