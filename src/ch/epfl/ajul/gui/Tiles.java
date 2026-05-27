package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Contient les nœuds JavaFX représentant la totalité des ancres et des tuiles.
 *
 * @param anchors les ancres, associées à leur emplacement
 * @param tiles   les tuiles, regroupées par sorte
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public record Tiles(
        Map<TileLocation, Node> anchors,
        Map<TileKind, List<Node>> tiles
) {
    /** Le paramètre largeur de toutes les tuiles. */
    public static final int TILE_WIDTH = 30;
    /** Le paramètre hauteur de toutes les tuiles. */
    public static final int TILE_HEIGHT = 30;

    /**
     * Crée et retourne une instance de Tiles contenant les ancres et tuiles
     * correspondant à la configuration de la partie.
     *
     * @param game la configuration de la partie
     * @return un objet Tiles contenant tous les noeuds graphiques nécessaires
     */
    public static Tiles create(Game game) {
        Map<TileLocation, Node> anchorsMap = new HashMap<>();
        Map<TileKind, List<Node>> tilesMap = new HashMap<>();

        Stream.Builder<TileLocation> locBuilder = Stream.builder();
        int factoryCount = game.factoriesCount();

        // Emplacements OnSource
        int centralPoints = 3 * factoryCount + 1;
        for (int i = 0; i < centralPoints; i++) {
            locBuilder.add(new TileLocation.OnSource(TileSource.CENTER_AREA, i));
        }
        for (TileSource.Factory factory : game.factories()) {
            for (int i = 0; i < TileSource.Factory.TILES_PER_FACTORY; i++) {
                locBuilder.add(new TileLocation.OnSource(factory, i));
            }
        }

        // Emplacements pour les plateaux de joueurs
        for (PlayerId playerId : game.playerIds()) {
            // Lignes de motif
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                for (int i = 0; i < line.capacity(); i++) {
                    locBuilder.add(new TileLocation.OnPattern(playerId, line, i));
                }
            }
            // Mur (25 cases)
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                for (int col = 0; col < 5; col++) {
                    TileKind.Colored color = PkWall.colorAt(line, col);
                    locBuilder.add(new TileLocation.OnWall(playerId, line, color));
                }
            }
            // Ligne plancher (7 emplacements)
            for (int i = 0; i < 7; i++) {
                locBuilder.add(new TileLocation.OnFloor(playerId, i));
            }
        }

        // Emplacements hors du plateau : 20 par couleur + 1 pour le marqueur
        for (TileKind.Colored colored : TileKind.Colored.ALL) {
            for (int i = 0; i < 20; i++) {
                locBuilder.add(new TileLocation.OffBoard(colored, i));
            }
        }
        locBuilder.add(new TileLocation.OffBoard(TileKind.FIRST_PLAYER_MARKER, 0));

        // Génération et configuration des ancres (visibles ou invisibles selon leur classe)
        locBuilder.build().forEach(loc -> {
            Rectangle rect = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
            rect.getStyleClass().addAll("tile", "anchor");

            if (loc instanceof TileLocation.OnWall onWall) {
                rect.getStyleClass().add(onWall.color().name());
            }

            setLocation(rect, loc);
            anchorsMap.put(loc, rect);
        });

        // Génération des vraies tuiles (sans emplacement assigné au départ)
        List<TileKind> allKinds = new ArrayList<>(TileKind.Colored.ALL);
        allKinds.add(TileKind.FIRST_PLAYER_MARKER);

        for (TileKind kind : allKinds) {
            int count = (kind instanceof TileKind.Colored) ? 20 : 1;
            List<Node> list = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                Node tileNode;
                if (kind instanceof TileKind.Colored color) {
                    Rectangle rect = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
                    rect.getStyleClass().addAll("tile", color.name());
                    tileNode = rect;
                } else {
                    StackPane stack = new StackPane();
                    stack.setId("first-player-marker");
                    Rectangle rect = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
                    rect.getStyleClass().add("tile");
                    Text text = new Text("1");
                    stack.getChildren().addAll(rect, text);
                    tileNode = stack;
                }
                list.add(tileNode);
            }
            tilesMap.put(kind, List.copyOf(list));
        }

        return new Tiles(Map.copyOf(anchorsMap), Map.copyOf(tilesMap));
    }

    /**
     * Lit l'emplacement logique stocké dans les métadonnées (UserData) du nœud.
     *
     * @param node le nœud JavaFX (tuile ou ancre)
     * @return son emplacement, ou null si non défini
     */
    public static TileLocation location(Node node) {
        return (TileLocation) node.getUserData();
    }

    /**
     * Modifie l'emplacement logique stocké dans les métadonnées du nœud.
     *
     * @param node     le nœud JavaFX
     * @param location le nouvel emplacement
     */
    public static void setLocation(Node node, TileLocation location) {
        node.setUserData(location);
    }
}


