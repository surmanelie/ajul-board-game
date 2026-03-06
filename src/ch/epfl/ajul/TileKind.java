package ch.epfl.ajul;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Type de tuile du jeu : une tuile colorée (A à E) ou le marqueur du premier joueur.
 * <p>
 * Les tuiles sont indexées de manière à être cohérentes avec la liste {@link #ALL}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
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
     * @return l'indice de la tuile
     */
    int index();

    /**
     * Retourne le nombre total de tuiles de ce type présentes dans la boîte.
     *
     * @return le nombre total de tuiles de ce type
     */
    int tilesCount();

    /**
     * Tuiles colorées (A à E).
     *
     * @author Danny Levy (394098)
     * @author Elie Menashe Reuben Surman (410685)
     */
    enum Colored implements TileKind {
        A, B, C, D, E;

        /**
         * Liste immuable de toutes les couleurs, dans l'ordre {@code A..E}.
         */
        public static final List<Colored> ALL = List.of(values());

        /**
         * Nombre total de couleurs.
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
        public int tilesCount() {
            return 20;
        }

        /**
         * Mélange le tableau {@code tiles} en place, à l'aide du générateur aléatoire donné,
         * selon l'algorithme de Fisher–Yates.
         *
         * @param tiles le tableau à mélanger (modifié en place)
         * @param randomGenerator le générateur aléatoire utilisé pour le mélange
         */
        public static void shuffle(Colored[] tiles, RandomGenerator randomGenerator) {
            for (int i = 0; i < tiles.length - 1; i++) {
                int j = randomGenerator.nextInt(i, tiles.length);
                Colored temp = tiles[i];
                tiles[i] = tiles[j];
                tiles[j] = temp;
            }
        }
    }

    /**
     * Marqueur du premier joueur.
     *
     * @author Danny Levy (394098)
     * @author Elie Menashe Reuben Surman (410685)
     */
    enum FirstPlayerMarker implements TileKind {
        FIRST_PLAYER_MARKER;

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
        public int tilesCount() {
            return 1;
        }
    }
}