package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Gère la représentation graphique des tuiles à l'écran.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class TileOverlayUI {

    private final Pane root;
    private final Tiles tiles;

    private TileOverlayUI(Pane root, Tiles tiles) {
        this.root = root;
        this.tiles = tiles;
    }

    /**
     * Retourne le nœud JavaFX à la racine du graphe de scène.
     *
     * @return le nœud racine
     */
    public Node root() {
        return root;
    }

    /**
     * Affiche un nombre de points au-dessus d'un emplacement du mur.
     *
     * @param location l'emplacement sur le mur
     * @param points   le nombre de points à afficher
     */
    public void showTilePoints(TileLocation.OnWall location, int points) {
        Text text = new Text(String.valueOf(points));
        text.getStyleClass().add("tile-points");
        text.setViewOrder(-2);

        Platform.runLater(() -> {
            root.getChildren().add(text);
            text.applyCss();
            Bounds bounds = text.getBoundsInLocal();
            
            Node anchor = tiles.anchors().get(location);
            if (anchor != null) {
                Point2D anchorPos = anchor.localToScene(Point2D.ZERO);
                Point2D rootPos = root.sceneToLocal(anchorPos);
                
                double centerX = rootPos.getX() + Tiles.TILE_WIDTH / 2.0 - bounds.getWidth() / 2.0;
                double centerY = rootPos.getY() + Tiles.TILE_HEIGHT / 2.0 - bounds.getHeight() / 2.0;
                
                text.relocate(centerX, centerY);
            }
        });
    }

    /**
     * Crée et retourne l'interface de superposition des tuiles.
     *
     * @param gameStateO     l'état observable de la partie
     * @param tiles          les nœuds des tuiles et ancres
     * @param validMoves     les coups valides totaux
     * @param potentialMoves le sous-ensemble de coups potentiels lors d'un déplacement
     * @param moveAccepted   tableau muté pour valider un coup après relâchement
     * @return l'instance de TileOverlayUI
     */
    public static TileOverlayUI create(
            ObservableValue<ImmutableGameState> gameStateO,
            Tiles tiles,
            Set<Move> validMoves,
            Set<Move> potentialMoves,
            boolean[] moveAccepted) {

        Pane root = new Pane();
        root.setId("tile-overlay");

        for (TileKind kind : TileKind.ALL) {
            List<Node> kindNodes = tiles.tiles().getOrDefault(kind, List.of());
            for (int i = 0; i < kindNodes.size(); i++) {
                Node node = kindNodes.get(i);
                TileLocation.OffBoard initialLoc = new TileLocation.OffBoard(kind, i);
                node.setUserData(initialLoc);
                node.relocate(-30, -30);
                node.setViewOrder(0);
                root.getChildren().add(node);

                if (kind instanceof TileKind.Colored colored) {
                    node.setOnMousePressed(e -> e.setDragDetect(true));

                    node.setOnDragDetected(e -> {
                        TileLocation loc = (TileLocation) node.getUserData();
                        if (!(loc instanceof TileLocation.OnSource onSource)) {
                            return;
                        }

                        potentialMoves.clear();
                        if (!validMoves.isEmpty()) {
                            for (Move m : validMoves) {
                                if (m.source().equals(onSource.source()) && m.tileColor().equals(colored)) {
                                    potentialMoves.add(m);
                                }
                            }
                        }

                        if (potentialMoves.isEmpty()) {
                            return;
                        }

                        node.startFullDrag();
                        root.setMouseTransparent(true);

                        List<Node> dragNodes = new ArrayList<>();
                        for (Node n : tiles.tiles().getOrDefault(colored, List.of())) {
                            if (n.getUserData() instanceof TileLocation.OnSource s && s.source().equals(onSource.source())) {
                                dragNodes.add(n);
                            }
                        }

                        for (Node n : dragNodes) {
                            n.setViewOrder(-1);
                        }

                        EventHandler<MouseEvent> dragHandler = dragEvent -> {
                            double dx = dragEvent.getSceneX() - e.getSceneX();
                            double dy = dragEvent.getSceneY() - e.getSceneY();
                            for (Node n : dragNodes) {
                                n.setTranslateX(dx);
                                n.setTranslateY(dy);
                            }
                        };

                        EventHandler<MouseEvent> releaseHandler = new EventHandler<>() {
                            @Override
                            public void handle(MouseEvent releaseEvent) {
                                root.setMouseTransparent(false);
                                potentialMoves.clear();
                                boolean accepted = moveAccepted[0];
                                moveAccepted[0] = false;

                                node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
                                node.removeEventHandler(MouseEvent.MOUSE_RELEASED, this);

                                if (accepted) {
                                    for (Node n : dragNodes) {
                                        n.setViewOrder(0);
                                        n.relocate(n.getLayoutX() + n.getTranslateX(), n.getLayoutY() + n.getTranslateY());
                                        n.setTranslateX(0);
                                        n.setTranslateY(0);
                                    }
                                } else {
                                    ParallelTransition resetPt = new ParallelTransition();
                                    for (Node n : dragNodes) {
                                        TranslateTransition tt = new TranslateTransition(Duration.millis(125), n);
                                        tt.setToX(0);
                                        tt.setToY(0);
                                        resetPt.getChildren().add(tt);
                                    }
                                    resetPt.setOnFinished(evt -> {
                                        for (Node n : dragNodes) {
                                            n.setViewOrder(0);
                                        }
                                    });
                                    resetPt.play();
                                }
                            }
                        };

                        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
                        node.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
                    });
                }
            }
        }

        Function<TileLocation, Point2D> layoutFunc = loc -> {
            if (loc instanceof TileLocation.OffBoard) {
                return new Point2D(-30, -30);
            }
            Node anchor = tiles.anchors().get(loc);
            if (anchor == null) {
                return new Point2D(-30, -30);
            }
            Point2D anchorPos = anchor.localToScene(Point2D.ZERO);
            return root.sceneToLocal(anchorPos);
        };

        Platform.runLater(() -> {
            gameStateO.subscribe(newState -> {
                Animation anim = TileAnimator.animateTiles(layoutFunc, tiles.tiles(), newState);
                anim.play();
            });
        });

        return new TileOverlayUI(root, tiles);
    }
}
