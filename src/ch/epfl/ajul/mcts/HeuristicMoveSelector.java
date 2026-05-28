package ch.epfl.ajul.mcts;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.random.RandomGenerator;

/**
 * Sélectionneur de coups heuristique utilisé par l'algorithme MCTS pour
 * jouer de manière quasi-aléatoire lors des simulations.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class HeuristicMoveSelector {

    /**
     * Empêche l'instanciation de cette classe utilitaire.
     */
    private HeuristicMoveSelector() {
    }

    /**
     * Sélectionne un coup à jouer selon une heuristique qui privilégie
     * le remplissage exact des lignes de motif, ou à défaut le remplissage
     * partiel, avant de jouer sur la ligne plancher.
     *
     * @param rng le générateur aléatoire
     * @param gameState l'état de la partie
     * @param pkMoves un tableau contenant les coups empaquetés parmi lesquels choisir
     * @param validMovesCount le nombre de coups valides présents au début du tableau
     * @return l'index du coup choisi dans le tableau
     */
    public static int selectMove(
            RandomGenerator rng,
            ReadOnlyGameState gameState,
            short[] pkMoves,
            int validMovesCount
    ) {
        ReservoirSampler exactFill = new ReservoirSampler();
        ReservoirSampler partialFill = new ReservoirSampler();
        ReservoirSampler others = new ReservoirSampler();

        ReadOnlyIntArray pkPlayerStates = gameState.pkPlayerStates();
        int pkPatterns = PkPlayerStates.pkPatterns(pkPlayerStates, gameState.currentPlayerId());
        ReadOnlyIntArray tileSources = gameState.pkTileSources();

        for (int i = 0; i < validMovesCount; i++) {
            short pkMove = pkMoves[i];
            TileDestination destination = PkMove.destination(pkMove);

            if (destination instanceof TileDestination.Pattern patternLine) {
                TileSource source = PkMove.source(pkMove);
                TileKind.Colored color = PkMove.color(pkMove);

                int pkSource = tileSources.get(source.index());
                int tilesAvailable = PkTileSet.countOf(pkSource, color);

                int currentTiles = PkPatterns.size(pkPatterns, patternLine);
                int missingTiles = patternLine.capacity() - currentTiles;

                if (tilesAvailable >= missingTiles) {
                    // La ligne sera totalement remplie (tuiles exactes ou overflow)
                    exactFill.add(i, rng);
                } else {
                    // tilesAvailable < missingTiles : remplissage partiel
                    partialFill.add(i, rng);
                }
            } else {
                others.add(i, rng);
            }
        }

        if (!exactFill.isEmpty()) {
            return exactFill.sampledIndex;
        } else if (!partialFill.isEmpty()) {
            return partialFill.sampledIndex;
        } else {
            return others.sampledIndex;
        }
    }

    /**
     * Échantillonneur par réservoir (algorithme de Vitter), avec un réservoir
     * de capacité 1.
     */
    private static final class ReservoirSampler {
        int sampledIndex = -1;
        int count = 0;

        void add(int index, RandomGenerator rng) {
            count++;
            if (rng.nextInt(count) == 0) {
                sampledIndex = index;
            }
        }

        boolean isEmpty() {
            return count == 0;
        }
    }
}
