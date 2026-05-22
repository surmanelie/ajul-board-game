package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.PointsObserver;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.mcts.MctsPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ch.epfl.ajul.gui.TileLocation;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Classe principale du programme Ajul.
 * Gère l'initialisation de la fenêtre, l'analyse des arguments, et le fil d'exécution de la partie.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class Main extends Application {

    private static final int MCTS_ITERATIONS = 100_000;

    /**
     * Méthode principale lançant l'application JavaFX.
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Analyse des arguments
        Parameters params = getParameters();
        List<String> unnamed = params.getUnnamed();
        if (unnamed.isEmpty()) {
            unnamed = List.of("Aline", "_Robot");
        } else if (unnamed.size() < 2 || unnamed.size() > 4) {
            throw new Error("Nombre de joueurs invalide. Attendu : entre 2 et 4.");
        }

        List<Game.PlayerDescription> playerDescriptions = new ArrayList<>();
        Map<PlayerId, Boolean> isMctsPlayer = new EnumMap<>(PlayerId.class);

        for (int i = 0; i < unnamed.size(); i++) {
            String arg = unnamed.get(i);
            PlayerId id = PlayerId.ALL.get(i);
            if (arg.startsWith("_")) {
                playerDescriptions.add(new Game.PlayerDescription(id, arg.substring(1), Game.PlayerDescription.PlayerKind.AI));
                isMctsPlayer.put(id, true);
            } else {
                playerDescriptions.add(new Game.PlayerDescription(id, arg, Game.PlayerDescription.PlayerKind.HUMAN));
                isMctsPlayer.put(id, false);
            }
        }

        Game game = new Game(playerDescriptions);

        BoardUI[] boardUIRef = new BoardUI[1];
        TileOverlayUI[] tileOverlayUIRef = new TileOverlayUI[1];

        PointsObserver observer = new PointsObserver() {
            @Override
            public void newWallTile(PlayerId playerId, ch.epfl.ajul.TileDestination.Pattern line, ch.epfl.ajul.TileKind.Colored color, int points) {
                if (tileOverlayUIRef[0] != null) {
                    tileOverlayUIRef[0].showTilePoints(new TileLocation.OnWall(playerId, line, color), points);
                }
            }

            @Override
            public void floor(PlayerId playerId, int penalty) {
            }

            @Override
            public void fullRow(PlayerId playerId, ch.epfl.ajul.TileDestination.Pattern line, int points) {
                if (boardUIRef[0] != null) boardUIRef[0].showBonusPoints(playerId, line);
            }

            @Override
            public void fullColumn(PlayerId playerId, int column, int points) {
                if (boardUIRef[0] != null) boardUIRef[0].showBonusPoints(playerId, column);
            }

            @Override
            public void fullColor(PlayerId playerId, ch.epfl.ajul.TileKind.Colored color, int points) {
                if (boardUIRef[0] != null) boardUIRef[0].showBonusPoints(playerId, color);
            }
        };

        MutableGameState mutState = new MutableGameState(
                ImmutableGameState.initial(game),
                observer
        );

        // Générateur aléatoire
        Map<String, String> namedParams = params.getNamed();
        byte[] seedBytes;
        if (namedParams.containsKey("seed")) {
            seedBytes = namedParams.get("seed").getBytes(StandardCharsets.UTF_8);
        } else {
            seedBytes = new SecureRandom().generateSeed(8);
        }
        RandomGeneratorFactory<RandomGenerator> rngFactory = RandomGeneratorFactory.getDefault();
        RandomGenerator gameRng = rngFactory.create(seedBytes);

        // Composants partagés
        SimpleObjectProperty<ImmutableGameState> gameStateP = new SimpleObjectProperty<>(mutState.immutable());
        Set<Move> validMoves = Collections.synchronizedSet(new HashSet<>());
        BlockingQueue<Move> moveQueue = new SynchronousQueue<>();
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[1];

        // Interfaces graphiques
        Tiles tiles = Tiles.create(game);
        BoardUI boardUI = BoardUI.create(tiles.anchors(), gameStateP, potentialMoves, moveAccepted, moveQueue);
        TileOverlayUI tileOverlayUI = TileOverlayUI.create(gameStateP, tiles, validMoves, potentialMoves, moveAccepted);

        boardUIRef[0] = boardUI;
        tileOverlayUIRef[0] = tileOverlayUI;

        // Graphe de scène
        StackPane root = new StackPane(boardUI.root(), tileOverlayUI.root());
        java.net.URL cssUrl = Main.class.getResource("/ajul.css");
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: ajul.css not found in classpath!");
        }

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ajul");
        primaryStage.setResizable(false);
        primaryStage.show();

        // Fil d'exécution de la partie
        Thread.startVirtualThread(() -> {
            try {
                mutState.fillFactories(gameRng);
                Platform.runLater(() -> gameStateP.set(mutState.immutable()));

                while (!mutState.isGameOver()) {
                    if (mutState.isRoundOver()) {
                        Thread.sleep(1000);
                        mutState.endRound();
                        Platform.runLater(() -> gameStateP.set(mutState.immutable()));
                        if (!mutState.isGameOver()) {
                            Thread.sleep(700);
                            mutState.fillFactories(gameRng);
                            Platform.runLater(() -> gameStateP.set(mutState.immutable()));
                        }
                    }

                    if (!mutState.isGameOver()) {
                        PlayerId currentPlayer = mutState.currentPlayerId();
                        Move move;
                        if (isMctsPlayer.get(currentPlayer)) {
                            MctsPlayer mctsPlayer = new MctsPlayer(rngFactory, MCTS_ITERATIONS);
                            move = mctsPlayer.nextMove(mutState);
                        } else {
                            short[] movesArray = new short[Move.MAX_MOVES];
                            int numMoves = mutState.uniqueValidMoves(movesArray);
                            for (int i = 0; i < numMoves; i++) {
                                validMoves.add(Move.ofPacked(movesArray[i]));
                            }
                            move = moveQueue.take();
                            validMoves.clear();
                        }

                        mutState.registerMove(move.packed());
                        Platform.runLater(() -> gameStateP.set(mutState.immutable()));
                    }
                }
                mutState.endGame();
                Platform.runLater(() -> gameStateP.set(mutState.immutable()));
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        });
    }
}
