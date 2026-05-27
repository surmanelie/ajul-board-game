package ch.epfl.ajul.mcts;

/**
 * Représente un nœud de l'arbre de jeu construit par l'algorithme MCTS.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class MctsNode {

    private static final int C_FACTOR = 80;
    private static final int NO_MOVE = 0x3FF; // Tous les bits à 1 sur 10 bits

    private int pkMoveAndGameCount;
    private int totalPoints;
    /** Fils de ce nœud, ou {@code null} si le nœud n'a pas encore été développé. */
    MctsNode[] children;

    /**
     * Construit un nœud MCTS.
     *
     * @param pkMoveAndGameCount l'entier combinant le coup empaqueté et le compteur de parties
     * @param totalPoints la somme des points obtenus
     */
    private MctsNode(int pkMoveAndGameCount, int totalPoints) {
        this.pkMoveAndGameCount = pkMoveAndGameCount;
        this.totalPoints = totalPoints;
        this.children = null;
    }

    /**
     * Crée et retourne un nouveau nœud servant de racine à l'arbre.
     *
     * @return le nœud racine
     */
    public static MctsNode newRoot() {
        // Le compteur est initialisé à 1 pour éviter une division par zéro lors de l'exploration.
        return new MctsNode((1 << 10) | NO_MOVE, 0);
    }

    /**
     * Crée et retourne un nouveau nœud correspondant à un coup donné.
     *
     * @param pkMove le coup empaqueté lié à ce nœud
     * @return le nouveu nœud
     */
    public static MctsNode newMoveNode(short pkMove) {
        return new MctsNode(pkMove & 0x3FF, 0);
    }

    /**
     * Retourne le coup empaqueté de l'arête menant au nœud.
     *
     * @return le coup empaqueté
     */
    public short pkMove() {
        return (short) (pkMoveAndGameCount & 0x3FF);
    }

    /**
     * Retourne le compteur de parties simulées du nœud.
     *
     * @return le nombre de parties simulées
     */
    public int gameCount() {
        return pkMoveAndGameCount >>> 10;
    }

    /**
     * Retourne le nombre total de points.
     *
     * @return le nombre total de points
     */
    public int totalPoints() {
        return totalPoints;
    }

    /**
     * Retourne le nombre moyen de points.
     *
     * @return le nombre moyen de points
     */
    public double averagePoints() {
        int n = gameCount();
        return n == 0 ? 0.0 : (double) totalPoints / n;
    }

    /**
     * Ajoute le nombre de points au total et incrémente le compteur de parties simulées.
     *
     * @param points les points à ajouter
     */
    public void registerEvaluation(int points) {
        totalPoints += points;
        pkMoveAndGameCount += (1 << 10);
    }

    /**
     * Retourne l'index du fils du nœud à explorer, qui est celui dont la priorité
     * est maximale.
     *
     * @return l'index du fils à explorer
     */
    public int indexOfChildToExplore() {
        int nParent = gameCount();

        // Optimisation : exploration séquentielle initiale
        if (nParent <= children.length) {
            return nParent - 1;
        }

        double maxPriority = Double.NEGATIVE_INFINITY;
        int bestChildIndex = -1;

        double lnNParent = Math.log(nParent);

        for (int i = 0; i < children.length; i++) {
            MctsNode child = children[i];
            int nChild = child.gameCount();

            double priority;
            if (nChild == 0) {
                priority = Double.POSITIVE_INFINITY;
            } else {
                priority = ((double) child.totalPoints / nChild)
                        + C_FACTOR * Math.sqrt(2.0 * lnNParent / nChild);
            }

            if (priority > maxPriority) {
                maxPriority = priority;
                bestChildIndex = i;
            }
        }

        return bestChildIndex;
    }
}
