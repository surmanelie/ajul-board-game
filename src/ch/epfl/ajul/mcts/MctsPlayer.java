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
     * @throws IllegalArgumentException si iterations est inférieur ou égal à zéro
     */
    public MctsPlayer(RandomGeneratorFactory<RandomGenerator> rngFactory, int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException();
        }
        this.rngFactory = rngFactory;
        this.iterations = iterations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Move nextMove(ReadOnlyGameState gameState) {
        MctsNode root = MctsNode.newRoot();

        int maxMoves = Move.MAX_MOVES;
        short[] validMoves = new short[maxMoves];
        int playersCount = gameState.game().playersCount();
        int[] ranks = new int[playersCount];
        int[] generalizedPoints = new int[playersCount];

        // Tableaux redimensionnables pour mémoriser le chemin lors de la descente
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

                // La transition de manche est faite à chaque visite, que les enfants
                // existent déjà ou non, afin d'éviter de jouer des coups sur un état
                // en fin de manche.
                if (mutState.isRoundOver()) {
                    mutState.endRound();
                    if (mutState.isGameOver()) break;
                    // Graine déterministe par nœud : même nœud → même remplissage
                    mutState.fillFactories(rngFactory.create(currentNode.pkMove()));
                }

                if (currentNode.children == null) {
                    int movesCount = mutState.uniqueValidMoves(validMoves);
                    currentNode.children = new MctsNode[movesCount];
                    for (int k = 0; k < movesCount; k++) {
                        currentNode.children[k] = MctsNode.newMoveNode(validMoves[k]);
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

            // 2. Simulation de la fin de la partie avec un générateur propre à cette itération
            // La graine est le gameCount du nœud évalué : varie au fil du temps et est déterministe
            RandomGenerator simRng = rngFactory.create(currentNode.gameCount());

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

            // 3. Calcul des points généralisés : Pj = complement_rank × 256 + points_effectifs
            RankComputer.playersRank(mutState, ranks);
            int maxRank = playersCount - 1;

            for (PlayerId playerId : gameState.game().playerIds()) {
                int points = PkPlayerStates.points(mutState.pkPlayerStates(), playerId);
                int r = ranks[playerId.ordinal()];
                int complementRank = maxRank - r;
                generalizedPoints[playerId.ordinal()] = (complementRank << 8) + points;
            }

            // 4. Propagation : la racine incrémente son compteur sans points
            root.registerEvaluation(0);
            MctsNode traverseNode = root;
            for (int k = 0; k < pathLen; k++) {
                int childIndex = Byte.toUnsignedInt(pathChildIndices[k]);
                int playerIndex = Byte.toUnsignedInt(pathPlayerIndices[k]);
                traverseNode = traverseNode.children[childIndex];
                traverseNode.registerEvaluation(generalizedPoints[playerIndex]);
            }
        }

        if (root.children == null) {
            throw new IllegalStateException("Game is already over or no moves available.");
        }

        double maxAvg = Double.NEGATIVE_INFINITY;
        int bestChildIndex = -1;
        for (int i = 0; i < root.children.length; i++) {
            double avg = root.children[i].averagePoints();
            if (avg > maxAvg) {
                maxAvg = avg;
                bestChildIndex = i;
            }
        }

        return Move.ofPacked(root.children[bestChildIndex].pkMove());
    }
}
