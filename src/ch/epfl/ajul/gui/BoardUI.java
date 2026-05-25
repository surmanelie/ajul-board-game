package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Points;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Gère la partie de l'interface graphique représentant le plateau de jeu.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class BoardUI {

    private final Node root;
    private final Map<BonusKey, Node> bonusNodes;

    private BoardUI(Node root, Map<BonusKey, Node> bonusNodes) {
        this.root = root;
        this.bonusNodes = bonusNodes;
    }

    /**
     * Retourne le nœud JavaFX à la racine du graphe de scène représentant le plateau.
     *
     * @return la racine du plateau de jeu
     */
    public Node root() {
        return root;
    }

    /**
     * Rend visibles les points bonus associés à la ligne, colonne ou couleur donnée pour un joueur.
     *
     * @param playerId l'identité du joueur concerné
     * @param bonusKey la clef du bonus (Pattern, Integer, ou Colored)
     */
    public void showBonusPoints(PlayerId playerId, Object bonusKey) {
        Node node = bonusNodes.get(new BonusKey(playerId, bonusKey));
        if (node != null) {
            node.setVisible(true);
        }
    }

    private record BonusKey(PlayerId playerId, Object key) {}

    /**
     * Méthode fabrique pour construire et initialiser le plateau de jeu graphique.
     *
     * @param anchors        table associant les emplacements (TileLocation) à leur ancre (Node) correspondante
     * @param gameStateP     la valeur observable du GameState immuable
     * @param potentialMoves l'ensemble des coups valides pour une interaction en cours
     * @param moveAccepted   tableau muté pour valider un coup après relachement (indice 0 positionné à true)
     * @param moveQueue      file bloquante où le coup joué par l'utilisateur est introduit
     * @return l'instance de la classe contenant la racine de la scène et la table des textes de bonus
     */
    public static BoardUI create(
            Map<TileLocation, Node> anchors,
            ObservableValue<ImmutableGameState> gameStateP,
            Set<Move> potentialMoves,
            boolean[] moveAccepted,
            BlockingQueue<Move> moveQueue
    ) {
        Map<BonusKey, Node> bonusNodes = new HashMap<>();
        Game game = gameStateP.getValue().game();

        HBox boardBox = new HBox();
        boardBox.setId("board");

        GridPane sourcesBox = new GridPane();
        sourcesBox.setId("tile-sources");

        GridPane factoriesGrid = new GridPane();
        factoriesGrid.setId("factories");
        int fCol = 0;
        int fRow = 0;
        for (TileSource.Factory factory : game.factories()) {
            GridPane fGrid = new GridPane();
            fGrid.getStyleClass().addAll("factory", "tile-source");
            for (int i = 0; i < 4; i++) {
                Node anchor = anchors.get(new TileLocation.OnSource(factory, i));
                fGrid.add(anchor, i % 2, i / 2);
            }
            factoriesGrid.add(fGrid, fCol, fRow);
            fCol++;
            if (fCol == 2) {
                fCol = 0;
                fRow++;
            }
        }

        GridPane centerGrid = new GridPane();
        centerGrid.setId("center-area");
        centerGrid.getStyleClass().add("tile-source");
        int centerPoints = 3 * game.factoriesCount() + 1;
        for (int i = 0; i < centerPoints; i++) {
            Node anchor = anchors.get(new TileLocation.OnSource(TileSource.CENTER_AREA, i));
            centerGrid.add(anchor, i % 8, i / 8);
        }

        sourcesBox.add(factoriesGrid, 0, 0, 2, 1);
        sourcesBox.add(centerGrid, 0, 1, 2, 1);

        VBox playerBoardsBox = new VBox();
        playerBoardsBox.setId("player-boards");

        for (PlayerId pId : game.playerIds()) {
            StackPane playerStack = new StackPane();
            playerStack.getStyleClass().add("player-board");

            // Classe CSS dynamique : current-player
            gameStateP.map(ImmutableGameState::currentPlayerId).addListener((obs, oldCur, newCur) -> {
                if (newCur.equals(pId)) {
                    if (!playerStack.getStyleClass().contains("current-player")) {
                        playerStack.getStyleClass().add("current-player");
                    }
                } else {
                    playerStack.getStyleClass().remove("current-player");
                }
            });
            if (gameStateP.getValue().currentPlayerId().equals(pId)) {
                playerStack.getStyleClass().add("current-player");
            }

            VBox playerVBox = new VBox();

            // Nom et score (dynamiques)
            Text infoText = new Text();
            infoText.getStyleClass().add("player-info");
            Game.PlayerDescription pDesc = game.playerDescriptions().get(pId.ordinal());
            infoText.textProperty().bind(Bindings.format("%s\n%s",
                    pDesc.name(),
                    gameStateP.map(gs -> PkPlayerStates.points(gs.pkPlayerStates(), pId))
            ));
            playerVBox.getChildren().add(infoText);

            // Grille pour les lignes de motif, le mur de cases et les bonus
            GridPane boardGrid = new GridPane();
            boardGrid.getStyleClass().add("lines-and-wall");

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                int rowIndex = line.index();

                // Ligne de motif
                HBox patternHBox = new HBox();
                patternHBox.getStyleClass().addAll("tile-destination", "tile-group");
                for (int i = 0; i < line.capacity(); i++) {
                    Node anchor = anchors.get(new TileLocation.OnPattern(pId, line, i));
                    patternHBox.getChildren().add(anchor);
                }
                
                setupDestinationInteraction(patternHBox, line, potentialMoves, moveAccepted, moveQueue);

                GridPane.setHalignment(patternHBox, HPos.RIGHT);
                GridPane.setFillWidth(patternHBox, false);
                boardGrid.add(patternHBox, 0, rowIndex);

                // Bonus de couleur (+10) dans la colonne 1
                TileKind.Colored colorBonusKey = TileKind.Colored.ALL.get(rowIndex);
                Text colorBonusText = new Text("+10");
                colorBonusText.setVisible(false);
                colorBonusText.getStyleClass().addAll("bonus", "color-bonus");
                boardGrid.add(colorBonusText, 1, rowIndex);
                bonusNodes.put(new BonusKey(pId, colorBonusKey), colorBonusText);

                // Mur dans les colonnes 2 à 6
                for (int col = 0; col < 5; col++) {
                    TileKind.Colored wallColor = PkWall.colorAt(line, col);
                    Node wallNode = anchors.get(new TileLocation.OnWall(pId, line, wallColor));
                    if (!wallNode.getStyleClass().contains("wall-background")) {
                        wallNode.getStyleClass().add("wall-background");
                    }
                    boardGrid.add(wallNode, col + 2, rowIndex);
                }

                // Bonus de ligne (+2) dans la colonne 7
                Text lineBonusText = new Text("+2");
                lineBonusText.setVisible(false);
                lineBonusText.getStyleClass().addAll("bonus", "line-bonus");
                boardGrid.add(lineBonusText, 7, rowIndex);
                bonusNodes.put(new BonusKey(pId, line), lineBonusText);
            }

            // Bonus de colonne (+7) à la bascule de la grille (ligne 5)
            for (int col = 0; col < 5; col++) {
                Text colBonusText = new Text("+7");
                colBonusText.setVisible(false);
                colBonusText.getStyleClass().addAll("bonus", "column-bonus");
                boardGrid.add(colBonusText, col + 2, 5);
                bonusNodes.put(new BonusKey(pId, col), colBonusText);
            }

            playerVBox.getChildren().add(boardGrid);

            // Plancher
            HBox floorHBox = new HBox();
            floorHBox.getStyleClass().addAll("tile-destination", "tile-group");
            
            for (int i = 0; i < 7; i++) {
                VBox columnBox = new VBox();
                Node anchor = anchors.get(new TileLocation.OnFloor(pId, i));

                Text penaltyText = new Text(String.valueOf(-Points.floorPenalty(i)));
                StackPane penPane = new StackPane(penaltyText);
                
                columnBox.getChildren().addAll(anchor, penPane);
                floorHBox.getChildren().add(columnBox);
            }

            setupDestinationInteraction(floorHBox, TileDestination.FLOOR, potentialMoves, moveAccepted, moveQueue);

            playerVBox.getChildren().add(floorHBox);

            playerStack.getChildren().add(playerVBox);
            playerBoardsBox.getChildren().add(playerStack);
        }

        boardBox.getChildren().addAll(sourcesBox, playerBoardsBox);

        return new BoardUI(boardBox, bonusNodes);
    }

    private static void setupDestinationInteraction(
            Node destNode,
            TileDestination dest,
            Set<Move> potentialMoves,
            boolean[] moveAccepted,
            BlockingQueue<Move> moveQueue
    ) {
        destNode.setOnMouseDragEntered(e -> {
            boolean accepting = potentialMoves.stream().anyMatch(m -> m.destination().equals(dest));
            if (accepting && !destNode.getStyleClass().contains("accepting")) {
                destNode.getStyleClass().add("accepting");
            }
        });

        destNode.setOnMouseDragExited(e -> {
            destNode.getStyleClass().remove("accepting");
        });

        destNode.setOnMouseDragReleased(e -> {
            destNode.getStyleClass().remove("accepting");
            for (Move move : potentialMoves) {
                if (move.destination().equals(dest)) {
                    moveAccepted[0] = true;
                    try {
                        moveQueue.put(move);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }
            }
        });
    }
}
