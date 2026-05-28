package ch.epfl.ajul;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Type de tuile du jeu, c'est-à-dire une tuile colorée de A à E ou le
 * marqueur du premier joueur.
 * <p>
 * Les tuiles sont indexées de manière à être cohérentes avec la liste
 * {@link #ALL}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public sealed interface TileKind {

    /**
     * Tuile de couleur A.
     */
    TileKind A = Colored.A;

    /**
     * Tuile de couleur B.
     */
    TileKind B = Colored.B;

    /**
     * Tuile de couleur C.
     */
    TileKind C = Colored.C;

    /**
     * Tuile de couleur D.
     */
    TileKind D = Colored.D;

    /**
     * Tuile de couleur E.
     */
    TileKind E = Colored.E;

    /**
     * Marqueur du premier joueur.
     */
    TileKind FIRST_PLAYER_MARKER = FirstPlayerMarker.FIRST_PLAYER_MARKER;

    /**
     * Liste immuable de tous les types de tuiles, dans l'ordre des indices.
     */
    List<TileKind> ALL = List.of(A, B, C, D, E, FIRST_PLAYER_MARKER);

    /**
     * Nombre total de types de tuiles.
     */
    int COUNT = ALL.size();

    /**
     * Retourne l'indice de ce type de tuile.
     * <p>
     * L'indice correspond à la position de ce type de tuile dans {@link #ALL}.
     *
     * @return l'indice du type de tuile
     */
    int index();

    /**
     * Retourne le nombre total de tuiles de ce type présentes dans la boîte.
     *
     * @return le nombre total de tuiles de ce type
     */
    int tilesCount();

    /**
     * Tuiles colorées, de {@code A} à {@code E}.
     *
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    enum Colored implements TileKind {
        /** Tuile de couleur A. */
        A,
        /** Tuile de couleur B. */
        B,
        /** Tuile de couleur C. */
        C,
        /** Tuile de couleur D. */
        D,
        /** Tuile de couleur E. */
        E;

        /**
         * Liste immuable de toutes les couleurs, dans l'ordre {@code A..E}.
         */
        public static final List<Colored> ALL = List.of(values());

        /**
         * Nombre total de couleurs.
         */
        public static final int COUNT = ALL.size();

        /**
         * Retourne l'indice de cette couleur.
         *
         * @return l'indice de cette couleur
         */
        @Override
        public int index() {
            return ordinal();
        }

        /**
         * Retourne le nombre total de tuiles de cette couleur présentes
         * dans la boîte.
         *
         * @return le nombre total de tuiles de cette couleur
         */
        @Override
        public int tilesCount() {
            return 20;
        }

        /**
         * Mélange le tableau donné en place à l'aide du générateur aléatoire
         * donné, selon l'algorithme de Fisher-Yates.
         *
         * @param tiles le tableau à mélanger
         * @param randomGenerator le générateur aléatoire utilisé pour le mélange
         */
        public static void shuffle(Colored[] tiles, RandomGenerator randomGenerator) {
            for (int i = 0; i < tiles.length - 1; i += 1) {
                int j = randomGenerator.nextInt(i, tiles.length);

                Colored temporaryTile = tiles[i];
                tiles[i] = tiles[j];
                tiles[j] = temporaryTile;
            }
        }
    }

    /**
     * Marqueur du premier joueur.
     *
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    enum FirstPlayerMarker implements TileKind {
        /** Marqueur du premier joueur. */
        FIRST_PLAYER_MARKER;

        /**
         * Retourne l'indice du marqueur du premier joueur.
         *
         * @return l'indice du marqueur
         */
        @Override
        public int index() {
            return 5;
        }

        /**
         * Retourne le nombre total de marqueurs du premier joueur présents
         * dans la boîte.
         *
         * @return le nombre total de marqueurs
         */
        @Override
        public int tilesCount() {
            return 1;
        }
    }
}