package ch.epfl.ajul.mcts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyMctsNodeTest {

    private static final short NO_MOVE = (short) 0x3FF;

    // ─── newRoot ─────────────────────────────────────────────────────────────

    @Test
    void newRootGameCountIsOne() {
        var root = MctsNode.newRoot();
        assertEquals(1, root.gameCount());
    }

    @Test
    void newRootTotalPointsIsZero() {
        var root = MctsNode.newRoot();
        assertEquals(0, root.totalPoints());
    }

    @Test
    void newRootAveragePointsIsZero() {
        var root = MctsNode.newRoot();
        // gameCount=1, totalPoints=0 → avg = 0/1 = 0.0
        assertEquals(0.0, root.averagePoints(), 1e-9);
    }

    @Test
    void newRootPkMoveIsNoMove() {
        var root = MctsNode.newRoot();
        assertEquals(NO_MOVE, root.pkMove());
    }

    @Test
    void newRootChildrenIsNull() {
        var root = MctsNode.newRoot();
        assertNull(root.children);
    }

    // ─── newMoveNode ─────────────────────────────────────────────────────────

    @Test
    void newMoveNodeGameCountIsZero() {
        var node = MctsNode.newMoveNode((short) 42);
        assertEquals(0, node.gameCount());
    }

    @Test
    void newMoveNodeTotalPointsIsZero() {
        var node = MctsNode.newMoveNode((short) 42);
        assertEquals(0, node.totalPoints());
    }

    @Test
    void newMoveNodeAveragePointsIsZeroBeforeRegistration() {
        var node = MctsNode.newMoveNode((short) 0);
        assertEquals(0.0, node.averagePoints(), 1e-9);
    }

    @Test
    void newMoveNodePkMovePreservesLow10Bits() {
        short pkMove = (short) 0x1A5;
        var node = MctsNode.newMoveNode(pkMove);
        assertEquals(pkMove, node.pkMove());
    }

    @Test
    void newMoveNodePkMoveMaxValue() {
        short pkMove = NO_MOVE; // 0x3FF
        var node = MctsNode.newMoveNode(pkMove);
        assertEquals(NO_MOVE, node.pkMove());
    }

    @Test
    void newMoveNodePkMoveZero() {
        var node = MctsNode.newMoveNode((short) 0);
        assertEquals((short) 0, node.pkMove());
    }

    @Test
    void newMoveNodeChildrenIsNull() {
        var node = MctsNode.newMoveNode((short) 1);
        assertNull(node.children);
    }

    // ─── registerEvaluation ──────────────────────────────────────────────────

    @Test
    void registerEvaluationIncreasesGameCount() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(10);
        assertEquals(1, node.gameCount());
    }

    @Test
    void registerEvaluationIncreasesTotalPoints() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(15);
        assertEquals(15, node.totalPoints());
    }

    @Test
    void registerEvaluationAccumulatesMultipleCalls() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(10);
        node.registerEvaluation(20);
        node.registerEvaluation(30);
        assertEquals(3, node.gameCount());
        assertEquals(60, node.totalPoints());
    }

    @Test
    void registerEvaluationZeroPoints() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(0);
        assertEquals(1, node.gameCount());
        assertEquals(0, node.totalPoints());
    }

    @Test
    void registerEvaluationNegativePoints() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(-5);
        assertEquals(1, node.gameCount());
        assertEquals(-5, node.totalPoints());
    }

    // ─── averagePoints ───────────────────────────────────────────────────────

    @Test
    void averagePointsAfterOneEvaluation() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(100);
        assertEquals(100.0, node.averagePoints(), 1e-9);
    }

    @Test
    void averagePointsAfterTwoEvaluations() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(10);
        node.registerEvaluation(20);
        assertEquals(15.0, node.averagePoints(), 1e-9);
    }

    @Test
    void averagePointsThreeEvaluations() {
        var node = MctsNode.newMoveNode((short) 0);
        node.registerEvaluation(0);
        node.registerEvaluation(30);
        node.registerEvaluation(60);
        assertEquals(30.0, node.averagePoints(), 1e-9);
    }

    // ─── indexOfChildToExplore : phase séquentielle ──────────────────────────

    @Test
    void sequentialPhaseReturnsFirstChild() {
        // root.gameCount()=1, children.length=3 → nParent(1) ≤ 3 → index 0
        var root = MctsNode.newRoot();
        root.children = new MctsNode[]{
                MctsNode.newMoveNode((short) 1),
                MctsNode.newMoveNode((short) 2),
                MctsNode.newMoveNode((short) 3)
        };
        assertEquals(0, root.indexOfChildToExplore());
    }

    @Test
    void sequentialPhaseReturnsSecondChildAfterOneRegistration() {
        var root = MctsNode.newRoot();
        root.children = new MctsNode[]{
                MctsNode.newMoveNode((short) 1),
                MctsNode.newMoveNode((short) 2),
                MctsNode.newMoveNode((short) 3)
        };
        root.registerEvaluation(0); // gameCount becomes 2
        assertEquals(1, root.indexOfChildToExplore());
    }

    @Test
    void sequentialPhaseReturnsThirdChildAfterTwoRegistrations() {
        var root = MctsNode.newRoot();
        root.children = new MctsNode[]{
                MctsNode.newMoveNode((short) 1),
                MctsNode.newMoveNode((short) 2),
                MctsNode.newMoveNode((short) 3)
        };
        root.registerEvaluation(0); // gameCount=2
        root.registerEvaluation(0); // gameCount=3
        assertEquals(2, root.indexOfChildToExplore());
    }

    @Test
    void sequentialPhaseWorksWithSingleChild() {
        var root = MctsNode.newRoot();
        root.children = new MctsNode[]{MctsNode.newMoveNode((short) 5)};
        // gameCount=1 ≤ 1 → index 0
        assertEquals(0, root.indexOfChildToExplore());
    }

    // ─── indexOfChildToExplore : phase UCB ───────────────────────────────────

    @Test
    void ucbPhasePrefersBetterAverageChildWhenExplorationIsEqual() {
        // Two children, same game count → exploration equal → exploitation decides
        var root = MctsNode.newRoot();
        var c0 = MctsNode.newMoveNode((short) 1);
        var c1 = MctsNode.newMoveNode((short) 2);

        // Give both 1 game; c0 has 100 pts, c1 has 1 pt
        c0.registerEvaluation(100);
        c1.registerEvaluation(1);

        root.children = new MctsNode[]{c0, c1};
        // Advance root past sequential phase: need gameCount > 2
        root.registerEvaluation(0); // gameCount=2 → still sequential
        root.registerEvaluation(0); // gameCount=3 > 2 → UCB
        // Now UCB: c0 avg=100 vs c1 avg=1, exploration terms equal → c0 wins
        assertEquals(0, root.indexOfChildToExplore());
    }

    @Test
    void ucbPhasePreferesUnvisitedChild() {
        // Unvisited child has POSITIVE_INFINITY priority
        var root = MctsNode.newRoot();
        var c0 = MctsNode.newMoveNode((short) 1);
        var c1 = MctsNode.newMoveNode((short) 2); // unvisited

        c0.registerEvaluation(1000);

        root.children = new MctsNode[]{c0, c1};
        root.registerEvaluation(0); // gameCount=2 → sequential → returns 1 (c1)
        // This is the sequential phase returning index 1 (nParent=2 ≤ 2)
        assertEquals(1, root.indexOfChildToExplore());
    }

    @Test
    void pkMoveIsMaintainedAfterRegistrations() {
        short pkMove = (short) 0x1B3;
        var node = MctsNode.newMoveNode(pkMove);
        node.registerEvaluation(50);
        node.registerEvaluation(100);
        assertEquals(pkMove, node.pkMove());
    }
}
