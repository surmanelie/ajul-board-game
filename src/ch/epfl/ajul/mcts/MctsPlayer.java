package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.RankComputer;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;

import java.util.Arrays;


import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Joueur utilisant l'algorithme Monte Carlo Tree Search (MCTS) pour jouer à Ajul.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class MctsPlayer implements Player {

    private final RandomGeneratorFactory<RandomGenerator> rngFactory;
    private final int iterations;

    /**
     * Construit un joueur MCTS.
     *
     * @param rngFactory la fabrique de générateurs aléatoires
     * @param iterations le nombre d'itérations à effectuer par coup
     */
    public MctsPlayer(RandomGeneratorFactory<RandomGenerator> rngFactory, int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations must be > 0");
        }
        this.rngFactory = rngFactory;
        this.iterations = iterations;
    }

    @Override
    public Move nextMove(ReadOnlyGameState gameState) {
        RandomGenerator simRng = rngFactory.create(gameState.pkTileBag());
        MctsNode root = MctsNode.newRoot();

        int maxMoves = Move.MAX_MOVES;
        short[] validMoves = new short[maxMoves];
        int playersCount = gameState.game().playersCount();
        int[] ranks = new int[playersCount];
        int[] generalizedPoints = new int[playersCount];

        // Tableaux pouvant être agrandis dynamiquement pour mémoriser le chemin exploré lors de la descente
        byte[] pathChildIndices = new byte[32];
        byte[] pathPlayerIndices = new byte[32];

        for (int i = 0; i < iterations; i++) {
            MutableGameState mutState = new MutableGameState(gameState);
            MctsNode currentNode = root;
            int pathLen = 0;

            // 1. Descente dans l'arbre vers un nœud non évalué
            while (true) {
                if (currentNode.gameCount() == 0 || mutState.isGameOver()) {
                    break;
                }

                if (currentNode.children == null) {
                    if (mutState.isRoundOver()) {
                        mutState.endRound();
                        if (!mutState.isGameOver()) {
                            // Remplissage déterministe en fonction du nœud exploré
                            mutState.fillFactories(simRng);
                        }
                    }

                    if (!mutState.isGameOver()) {
                        int movesCount = mutState.uniqueValidMoves(validMoves);
                        currentNode.children = new MctsNode[movesCount];
                        for (int k = 0; k < movesCount; k++) {
                            currentNode.children[k] = MctsNode.newMoveNode(validMoves[k]);
                        }
                    } else {
                        break;
                    }
                }

                int childIndex = currentNode.indexOfChildToExplore();
                MctsNode nextNode = currentNode.children[childIndex];

                if (pathLen == pathChildIndices.length) {
                    pathChildIndices = Arrays.copyOf(pathChildIndices, pathChildIndices.length * 2);
                    pathPlayerIndices = Arrays.copyOf(pathPlayerIndices, pathPlayerIndices.length * 2);
                }

                pathChildIndices[pathLen] = (byte) childIndex;
                pathPlayerIndices[pathLen] = (byte) mutState.currentPlayerId().ordinal();
                pathLen++;

                mutState.registerMove(nextNode.pkMove());
                currentNode = nextNode;
            }

            // 2. Simulation de la fin de la partie
            while (!mutState.isGameOver()) {
                if (mutState.isRoundOver()) {
                    mutState.endRound();
                    if (!mutState.isGameOver()) {
                        mutState.fillFactories(simRng);
                    }
                }

                if (!mutState.isGameOver()) {
                    int validCount = mutState.validMoves(validMoves);
                    int chosenIndex = HeuristicMoveSelector.selectMove(simRng, mutState, validMoves, validCount);
                    mutState.registerMove(validMoves[chosenIndex]);
                }
            }
            mutState.endGame();

            // 3. Calcul et propagation des points
            RankComputer.playersRank(mutState, ranks);
            int maxRank = playersCount - 1;

            for (PlayerId playerId : gameState.game().playerIds()) {
                int points = PkPlayerStates.points(mutState.pkPlayerStates(), playerId);
                int r = ranks[playerId.ordinal()];
                int complementRank = maxRank - r;
                generalizedPoints[playerId.ordinal()] = (complementRank << 8) + points;
            }

            root.registerEvaluation(0);
            MctsNode traverseNode = root;
            for (int k = 0; k < pathLen; k++) {
                int childIndex = Byte.toUnsignedInt(pathChildIndices[k]);
                int playerIndex = Byte.toUnsignedInt(pathPlayerIndices[k]);
                traverseNode = traverseNode.children[childIndex];
                traverseNode.registerEvaluation(generalizedPoints[playerIndex]);
            }
        }

        double maxAvg = Double.NEGATIVE_INFINITY;
        int bestChildIndex = -1;
        if (root.children == null) {
            throw new IllegalStateException("Game is already over or no moves available.");
        }

        for (int i = 0; i < root.children.length; i++) {
            MctsNode child = root.children[i];
            double avg = child.averagePoints();
            if (avg > maxAvg) {
                maxAvg = avg;
                bestChildIndex = i;
            }
        }

        return Move.ofPacked(root.children[bestChildIndex].pkMove());
    }
}
