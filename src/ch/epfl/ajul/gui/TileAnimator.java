package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkFloor;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Function;

/**
 * Gère l'animation du déplacement des tuiles.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class TileAnimator {

    private TileAnimator() {}

    private record TileLocationPartition(
            List<TileLocation.OnWall> wall,
            List<TileLocation.OnPattern> pattern,
            List<TileLocation.OnFloor> floor,
            List<TileLocation.OnSource> source,
            List<TileLocation.OffBoard> offBoard
    ) {
        public TileLocationPartition() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    /**
     * Calcule et retourne l'animation déplaçant les tuiles vers leurs emplacements cibles.
     *
     * @param layoutFunc fonction retournant la position (Point2D) associée à un emplacement
     * @param tiles      table associant les nœuds représentant les tuiles à leur sorte
     * @param gameState  l'état actuel de la partie
     * @return l'animation déplaçant les tuiles mal placées vers leur emplacement attendu
     */
    public static Animation animateTiles(
            Function<TileLocation, Point2D> layoutFunc,
            Map<TileKind, List<Node>> tiles,
            ReadOnlyGameState gameState) {

        ParallelTransition parallelTransition = new ParallelTransition();

        for (TileKind kind : TileKind.ALL) {
            List<Node> kindNodes = tiles.getOrDefault(kind, List.of());
            if (kindNodes.isEmpty()) {
                continue;
            }

            TileLocationPartition demand = new TileLocationPartition();
            int expectedOnBoard = 0;

            // 1. Demande du Mur, Motif, et Plancher
            for (PlayerId pId : gameState.playerIds()) {
                if (kind instanceof TileKind.Colored colored) {
                    // Mur
                    int pkWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), pId);
                    for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                        if (PkWall.hasTileAt(pkWall, line, colored)) {
                            demand.wall().add(new TileLocation.OnWall(pId, line, colored));
                            expectedOnBoard++;
                        }
                    }

                    // Motif
                    int pkPatterns = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), pId);
                    for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                        if (PkPatterns.color(pkPatterns, line) == colored) {
                            int size = PkPatterns.size(pkPatterns, line);
                            for (int i = 0; i < size; i++) {
                                demand.pattern().add(new TileLocation.OnPattern(pId, line, i));
                                expectedOnBoard++;
                            }
                        }
                    }
                }

                // Plancher
                int pkFloor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), pId);
                int floorSize = PkFloor.size(pkFloor);
                for (int i = 0; i < floorSize; i++) {
                    if (PkFloor.tileAt(pkFloor, i) == kind) {
                        demand.floor().add(new TileLocation.OnFloor(pId, i));
                        expectedOnBoard++;
                    }
                }
            }

            // 2. Demande des Sources
            for (int sourceIndex = 0; sourceIndex < gameState.pkTileSources().size(); sourceIndex++) {
                int pkSource = gameState.pkTileSources().get(sourceIndex);
                TileSource source = TileSource.ALL.get(sourceIndex);
                int startIndex = 0;
                for (TileKind k : TileKind.ALL) {
                    int count = PkTileSet.countOf(pkSource, k);
                    if (k == kind) {
                        for (int i = 0; i < count; i++) {
                            demand.source().add(new TileLocation.OnSource(source, startIndex + i));
                            expectedOnBoard++;
                        }
                        break;
                    }
                    startIndex += count;
                }
            }

            // 3. Demande hors du plateau
            int totalTiles = kindNodes.size();
            int remaining = totalTiles - expectedOnBoard;
            if (remaining < 0) {
                throw new IllegalStateException("Le nombre de tuiles attendues (" + expectedOnBoard + ") dépasse le nombre total de tuiles (" + totalTiles + ") de la sorte " + kind);
            }
            for (int i = 0; i < remaining; i++) {
                demand.offBoard().add(new TileLocation.OffBoard(kind, i));
            }

            // 4. Calcul de l'offre et soustraction de la demande satisfaite
            TileLocationPartition supply = new TileLocationPartition();
            Map<TileLocation, Node> supplyNodes = new HashMap<>();

            for (Node node : kindNodes) {
                TileLocation loc = (TileLocation) node.getUserData();
                boolean removed = false;
                switch (loc) {
                    case TileLocation.OnWall w -> removed = demand.wall().remove(w);
                    case TileLocation.OnPattern p -> removed = demand.pattern().remove(p);
                    case TileLocation.OnFloor f -> removed = demand.floor().remove(f);
                    case TileLocation.OnSource s -> removed = demand.source().remove(s);
                    case TileLocation.OffBoard o -> removed = demand.offBoard().remove(o);
                }
                if (!removed) {
                    switch (loc) {
                        case TileLocation.OnWall w -> supply.wall().add(w);
                        case TileLocation.OnPattern p -> supply.pattern().add(p);
                        case TileLocation.OnFloor f -> supply.floor().add(f);
                        case TileLocation.OnSource s -> supply.source().add(s);
                        case TileLocation.OffBoard o -> supply.offBoard().add(o);
                    }
                    supplyNodes.put(loc, node);
                }
            }

            // 5. Tris requis pour l'appariement
            demand.wall().sort(Comparator.comparing(TileLocation.OnWall::playerId).thenComparing(TileLocation.OnWall::line).thenComparing(TileLocation.OnWall::color));
            demand.pattern().sort(Comparator.comparing(TileLocation.OnPattern::index));
            demand.floor().sort(Comparator.comparing(TileLocation.OnFloor::index));
            demand.source().sort(Comparator.<TileLocation.OnSource, Integer>comparing(s -> s.source().index()).thenComparing(TileLocation.OnSource::index));
            demand.offBoard().sort(Comparator.comparing(TileLocation.OffBoard::index));

            supply.source().sort(Comparator.<TileLocation.OnSource, Integer>comparing(s -> s.source().index()).reversed().thenComparing(TileLocation.OnSource::index));

            // 6. Appariement
            // 6.1 Demande mur -> Offre motif (plus à droite)
            Iterator<TileLocation.OnWall> wDemIt = demand.wall().iterator();
            while (wDemIt.hasNext()) {
                TileLocation.OnWall wDem = wDemIt.next();
                TileLocation.OnPattern bestSupply = null;
                for (TileLocation.OnPattern pSup : supply.pattern()) {
                    if (pSup.playerId().equals(wDem.playerId()) && pSup.line().equals(wDem.line())) {
                        if (bestSupply == null || pSup.index() > bestSupply.index()) {
                            bestSupply = pSup;
                        }
                    }
                }
                if (bestSupply != null) {
                    supply.pattern().remove(bestSupply);
                    match(bestSupply, wDem, supplyNodes, parallelTransition, layoutFunc);
                    wDemIt.remove();
                }
            }

            // 6.2 Demande plancher -> Offre source
            Iterator<TileLocation.OnFloor> fDemIt = demand.floor().iterator();
            while (fDemIt.hasNext()) {
                if (!supply.source().isEmpty()) {
                    TileLocation.OnFloor fDem = fDemIt.next();
                    TileLocation.OnSource sSup = supply.source().remove(0);
                    match(sSup, fDem, supplyNodes, parallelTransition, layoutFunc);
                    fDemIt.remove();
                } else {
                    break;
                }
            }

            // 6.3 Demande motif -> Offre source
            Iterator<TileLocation.OnPattern> pDemIt = demand.pattern().iterator();
            while (pDemIt.hasNext()) {
                if (!supply.source().isEmpty()) {
                    TileLocation.OnPattern pDem = pDemIt.next();
                    TileLocation.OnSource sSup = supply.source().remove(0);
                    match(sSup, pDem, supplyNodes, parallelTransition, layoutFunc);
                    pDemIt.remove();
                } else {
                    break;
                }
            }

            // 6.4 Demande hors plateau -> Offre plancher
            Iterator<TileLocation.OffBoard> oDemIt = demand.offBoard().iterator();
            while (oDemIt.hasNext()) {
                if (!supply.floor().isEmpty()) {
                    TileLocation.OffBoard oDem = oDemIt.next();
                    TileLocation.OnFloor fSup = supply.floor().remove(0);
                    match(fSup, oDem, supplyNodes, parallelTransition, layoutFunc);
                    oDemIt.remove();
                } else {
                    break;
                }
            }

            // 6.5 Demande hors plateau -> Offre source
            while (oDemIt.hasNext()) {
                if (!supply.source().isEmpty()) {
                    TileLocation.OffBoard oDem = oDemIt.next();
                    TileLocation.OnSource sSup = supply.source().remove(0);
                    match(sSup, oDem, supplyNodes, parallelTransition, layoutFunc);
                    oDemIt.remove();
                } else {
                    break;
                }
            }

            // 6.6 Reste de l'offre -> Reste de la demande
            List<TileLocation> remainingSupply = new ArrayList<>();
            remainingSupply.addAll(supply.wall());
            remainingSupply.addAll(supply.pattern());
            remainingSupply.addAll(supply.floor());
            remainingSupply.addAll(supply.source());
            remainingSupply.addAll(supply.offBoard());

            List<TileLocation> remainingDemand = new ArrayList<>();
            remainingDemand.addAll(demand.wall());
            remainingDemand.addAll(demand.pattern());
            remainingDemand.addAll(demand.floor());
            remainingDemand.addAll(demand.source());
            remainingDemand.addAll(demand.offBoard());

            if (remainingSupply.size() != remainingDemand.size()) {
                throw new IllegalStateException("Déséquilibre lors du dernier appariement : " + remainingSupply.size() + " offre(s) pour " + remainingDemand.size() + " demande(s).");
            }

            for (int i = 0; i < remainingSupply.size(); i++) {
                match(remainingSupply.get(i), remainingDemand.get(i), supplyNodes, parallelTransition, layoutFunc);
            }
        }

        return parallelTransition;
    }

    private static void match(
            TileLocation supplyLoc,
            TileLocation demandLoc,
            Map<TileLocation, Node> supplyNodes,
            ParallelTransition pt,
            Function<TileLocation, Point2D> layoutFunc) {
        Node node = supplyNodes.remove(supplyLoc);
        if (node == null) {
            throw new IllegalStateException("Aucun nœud n'a été trouvé pour l'emplacement d'offre : " + supplyLoc);
        }
        node.setUserData(demandLoc);
        Point2D endPos = layoutFunc.apply(demandLoc);
        RelocationTransition rt = new RelocationTransition(node, endPos, Duration.millis(500));
        pt.getChildren().add(rt);
    }
}
