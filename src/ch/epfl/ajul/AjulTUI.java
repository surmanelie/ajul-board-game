package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkFloor;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.gamestate.packed.PkWall;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;

/**
 * Interface textuelle minimale permettant de jouer à Ajul.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class AjulTUI {

    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Constructeur privé de classe utilitaire.
     */
    private AjulTUI() { }

    /**
     * Affiche l'état courant de la partie.
     *
     * @param gameState l'état de partie à afficher
     */
    static void printState(ReadOnlyGameState gameState) {
        System.out.println();
        System.out.println("=== ÉTAT DE LA PARTIE ===");
        System.out.println();

        System.out.println("Joueur courant : " + gameState.currentPlayerId());
        System.out.println("Sac            : " + PkTileSet.toString(gameState.pkTileBag()));
        System.out.println(
                "Sources uniques: "
                        + uniqueSourcesToString(gameState.pkUniqueTileSources())
        );
        System.out.println();

        System.out.println("Fabriques / centre :");
        for (TileSource source : gameState.game().tileSources()) {
            int pkSource = gameState.pkTileSources().get(source.index());
            String label = source == TileSource.CENTER_AREA
                    ? "[0] Centre"
                    : "[" + source.index() + "] Fabrique " + source.index();

            System.out.println("  " + label + " : " + PkTileSet.toString(pkSource));
        }

        System.out.println();
        System.out.println("Joueurs :");

        for (Game.PlayerDescription description : gameState.game().playerDescriptions()) {
            PlayerId playerId = description.id();

            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            int pkPatterns = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), playerId);
            int pkWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
            int pkFloor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), playerId);

            System.out.println("- " + description.name() + " (" + playerId + ")");
            System.out.println("    Points   : " + points);
            System.out.println("    Patterns : " + PkPatterns.toString(pkPatterns));
            System.out.println("    Wall     : " + PkWall.toString(pkWall));
            System.out.println("    Floor    : " + PkFloor.toString(pkFloor));
        }

        System.out.println();
    }

    /**
     * Demande au joueur donné de saisir son prochain coup valide.
     *
     * @param playerName le nom du joueur
     * @param gameState l'état courant de la partie
     * @return le coup choisi
     */
    static Move queryNextMove(String playerName, ReadOnlyGameState gameState) {
        short[] validMoves = new short[Move.MAX_MOVES];
        int moveCount = gameState.validMoves(validMoves);

        while (true) {
            System.out.println("Quel coup désirez-vous jouer, " + playerName + " ?");
            System.out.println("Format attendu : source-couleur-destination, par ex. 4B2");
            System.out.println(
                    "0 = centre, 1..9 = fabriques ; 0 = floor, 1..5 = lignes de motif"
            );
            System.out.print("> ");

            String input = SCANNER.nextLine().trim().toUpperCase();

            if (input.length() != 3) {
                System.out.println("Entrée invalide : il faut exactement 3 caractères.");
                continue;
            }

            char sourceChar = input.charAt(0);
            char colorChar = input.charAt(1);
            char destinationChar = input.charAt(2);

            if (!Character.isDigit(sourceChar) || !Character.isDigit(destinationChar)) {
                System.out.println(
                        "Entrée invalide : la source et la destination doivent être des chiffres."
                );
                continue;
            }

            int sourceIndex = sourceChar - '0';
            int destinationIndex = destinationChar - '0';

            if (sourceIndex < 0 || sourceIndex >= gameState.game().tileSourcesCount()) {
                System.out.println("Source invalide.");
                continue;
            }

            TileKind.Colored color = switch (colorChar) {
                case 'A' -> TileKind.Colored.A;
                case 'B' -> TileKind.Colored.B;
                case 'C' -> TileKind.Colored.C;
                case 'D' -> TileKind.Colored.D;
                case 'E' -> TileKind.Colored.E;
                default -> null;
            };

            if (color == null) {
                System.out.println("Couleur invalide.");
                continue;
            }

            TileDestination destination;
            if (destinationIndex == 0) {
                destination = TileDestination.FLOOR;
            } else if (1 <= destinationIndex
                    && destinationIndex <= TileDestination.Pattern.COUNT) {
                destination = TileDestination.Pattern.ALL.get(destinationIndex - 1);
            } else {
                System.out.println("Destination invalide.");
                continue;
            }

            TileSource source = TileSource.ALL.get(sourceIndex);
            Move move = new Move(source, color, destination);
            short packedMove = move.packed();

            boolean isValid = false;
            for (int i = 0; i < moveCount; i += 1) {
                if (validMoves[i] == packedMove) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                System.out.println("Coup invalide dans l'état actuel.");
                System.out.println("Coups valides :");
                for (int i = 0; i < moveCount; i += 1) {
                    System.out.println("  " + compactString(Move.ofPacked(validMoves[i])));
                }
                continue;
            }

            return move;
        }
    }

    /**
     * Retourne une représentation compacte du coup donné.
     *
     * @param move le coup
     * @return la représentation compacte correspondante
     */
    private static String compactString(Move move) {
        int sourceIndex = move.source().index();
        char colorLetter = move.tileColor().name().charAt(0);
        int destinationIndex = move.destination() == TileDestination.FLOOR
                ? 0
                : ((TileDestination.Pattern) move.destination()).index() + 1;

        return "" + sourceIndex + colorLetter + destinationIndex;
    }

    /**
     * Lance l'interface textuelle du jeu.
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        RandomGenerator randomGenerator = RandomGeneratorFactory.getDefault().create();

        System.out.println("=== AJUL TUI ===");
        int playersCount = queryPlayersCount();

        List<Game.PlayerDescription> playerDescriptions = new ArrayList<>();
        for (int i = 0; i < playersCount; i += 1) {
            PlayerId playerId = PlayerId.ALL.get(i);

            System.out.print("Nom du joueur " + playerId + " : ");
            String playerName = SCANNER.nextLine().trim();
            if (playerName.isEmpty()) {
                playerName = playerId.name();
            }

            playerDescriptions.add(
                    new Game.PlayerDescription(playerId, playerName, HUMAN)
            );
        }

        Game game = new Game(playerDescriptions);
        MutableGameState gameState =
                new MutableGameState(ImmutableGameState.initial(game));

        gameState.fillFactories(randomGenerator);

        while (!gameState.isGameOver()) {
            printState(gameState);

            String playerName = game.playerDescriptions()
                    .get(gameState.currentPlayerId().ordinal())
                    .name();

            Move move = queryNextMove(playerName, gameState);
            gameState.registerMove(move.packed());

            if (gameState.isRoundOver()) {
                System.out.println();
                System.out.println("=== FIN DE MANCHE ===");
                gameState.endRound();

                if (!gameState.isGameOver()) {
                    gameState.fillFactories(randomGenerator);
                }
            }
        }

        gameState.endGame();

        System.out.println();
        System.out.println("=== PARTIE TERMINÉE ===");
        printState(gameState);

        System.out.println("Scores finaux :");
        for (Game.PlayerDescription description : game.playerDescriptions()) {
            PlayerId playerId = description.id();
            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            System.out.println("  " + description.name() + " : " + points + " points");
        }
    }

    /**
     * Demande le nombre de joueurs jusqu'à obtenir une valeur valide.
     *
     * @return le nombre de joueurs
     */
    private static int queryPlayersCount() {
        while (true) {
            System.out.print("Nombre de joueurs (2 à 4) : ");
            String input = SCANNER.nextLine().trim();

            try {
                int playerCount = Integer.parseInt(input);
                if (2 <= playerCount && playerCount <= 4) {
                    return playerCount;
                }
            } catch (NumberFormatException e) {
                // Rien à faire, on redemande.
            }

            System.out.println("Valeur invalide.");
        }
    }

    /**
     * Retourne une représentation textuelle de l'ensemble des sources uniques.
     *
     * @param pkUniqueSources l'ensemble empaqueté des sources uniques
     * @return la représentation textuelle correspondante
     */
    private static String uniqueSourcesToString(int pkUniqueSources) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');

        boolean first = true;
        for (int i = 0; i < 32; i += 1) {
            if (((pkUniqueSources >>> i) & 1) != 0) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(i);
                first = false;
            }
        }

        builder.append(']');
        return builder.toString();
    }
}