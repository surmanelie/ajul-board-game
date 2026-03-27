package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Points;
import ch.epfl.ajul.PointsObserver;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkFloor;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.MutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.random.RandomGenerator;

import static java.util.Objects.requireNonNull;

/**
 * Représente un état mutable d'une partie d'Ajul.
 * <p>
 * Un état mutable contient les mêmes informations qu'un état immuable, mais permet
 * de faire évoluer la partie au fil des coups joués, des fins de manche et de la fin
 * de partie.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class MutableGameState implements ReadOnlyGameState {

    private final Game game;
    private int pkTileBag;
    private final int[] pkTileSourcesArray;
    private final MutableIntArray pkTileSources;
    private int pkUniqueTileSources;
    private final int[] pkPlayerStatesArray;
    private final MutableIntArray pkPlayerStates;
    private PlayerId currentPlayerId;
    private final PointsObserver pointsObserver;

    /**
     * Construit un nouvel état mutable ayant le même contenu que l'état donné, et
     * auquel l'observateur de points donné est attaché.
     *
     * @param initialState l'état initial
     * @param pointsObserver l'observateur de points
     * @throws NullPointerException si l'un des arguments est nul
     */
    public MutableGameState(ReadOnlyGameState initialState, PointsObserver pointsObserver) {
        requireNonNull(initialState);
        requireNonNull(pointsObserver);

        this.game = initialState.game();
        this.pkTileBag = initialState.pkTileBag();

        this.pkTileSourcesArray = initialState.pkTileSources().toArray();
        this.pkTileSources = MutableIntArray.wrapping(pkTileSourcesArray);

        this.pkUniqueTileSources = initialState.pkUniqueTileSources();

        this.pkPlayerStatesArray = initialState.pkPlayerStates().toArray();
        this.pkPlayerStates = MutableIntArray.wrapping(pkPlayerStatesArray);

        this.currentPlayerId = initialState.currentPlayerId();
        this.pointsObserver = pointsObserver;
    }

    /**
     * Construit un nouvel état mutable ayant le même contenu que l'état donné, et
     * auquel un observateur de points vide est attaché.
     *
     * @param initialState l'état initial
     * @throws NullPointerException si l'état initial est nul
     */
    public MutableGameState(ReadOnlyGameState initialState) {
        this(initialState, PointsObserver.EMPTY);
    }

    /**
     * Retourne la configuration de la partie.
     *
     * @return la configuration de la partie
     */
    @Override
    public Game game() {
        return game;
    }

    /**
     * Retourne le contenu empaqueté du sac.
     *
     * @return le contenu empaqueté du sac
     */
    @Override
    public int pkTileBag() {
        return pkTileBag;
    }

    /**
     * Retourne le tableau décrivant le contenu empaqueté des sources de tuiles.
     *
     * @return le tableau des sources de tuiles
     */
    @Override
    public ReadOnlyIntArray pkTileSources() {
        return pkTileSources;
    }

    /**
     * Retourne l'ensemble empaqueté des indices des sources uniques.
     *
     * @return l'ensemble empaqueté des sources uniques
     */
    @Override
    public int pkUniqueTileSources() {
        return pkUniqueTileSources;
    }

    /**
     * Retourne le tableau contenant l'état empaqueté des joueurs.
     *
     * @return le tableau des états de joueurs
     */
    @Override
    public ReadOnlyIntArray pkPlayerStates() {
        return pkPlayerStates;
    }

    /**
     * Retourne l'identité du joueur courant.
     *
     * @return l'identité du joueur courant
     */
    @Override
    public PlayerId currentPlayerId() {
        return currentPlayerId;
    }

    /**
     * Remplit les fabriques au moyen de la technique décrite dans l'énoncé.
     * <p>
     * Si le sac contient strictement plus de tuiles que nécessaire, les tuiles
     * requises sont extraites aléatoirement du sac. Sinon, toutes les tuiles du
     * sac sont d'abord extraites, le sac est rempli avec les tuiles sorties du jeu,
     * puis le complément nécessaire est extrait aléatoirement. Les tuiles extraites
     * sont ensuite mélangées puis réparties dans les fabriques.
     *
     * @param randomGenerator le générateur aléatoire
     * @throws NullPointerException si le générateur est nul
     */
    public void fillFactories(RandomGenerator randomGenerator) {
        requireNonNull(randomGenerator);

        int tilesNeeded = game.factoriesCount() * TileSource.Factory.TILES_PER_FACTORY;
        int extractedTiles = PkTileSet.EMPTY;

        if (PkTileSet.size(pkTileBag) > tilesNeeded) {
            extractedTiles = extractRandomColoredTiles(tilesNeeded, randomGenerator);
        } else {
            int firstExtractionCount = PkTileSet.size(pkTileBag);
            extractedTiles = pkTileBag;
            pkTileBag = PkTileSet.EMPTY;
            pkTileBag = PkTileSet.difference(pkDiscardedTiles(), extractedTiles);

            int secondExtractionCount = Math.min(
                    tilesNeeded - firstExtractionCount,
                    PkTileSet.size(pkTileBag)
            );

            extractedTiles = PkTileSet.union(
                    extractedTiles,
                    extractRandomColoredTiles(secondExtractionCount, randomGenerator)
            );
        }

        TileKind.Colored[] extractedArray = new TileKind.Colored[PkTileSet.size(extractedTiles)];
        PkTileSet.copyColoredInto(extractedTiles, extractedArray);
        shuffle(extractedArray, randomGenerator);

        int nextTileIndex = 0;
        for (TileSource.Factory factory : game.factories()) {
            int pkFactory = PkTileSet.EMPTY;

            for (int i = 0;
                 i < TileSource.Factory.TILES_PER_FACTORY && nextTileIndex < extractedArray.length;
                 i += 1) {
                pkFactory = PkTileSet.add(pkFactory, extractedArray[nextTileIndex]);
                nextTileIndex += 1;
            }

            pkTileSourcesArray[factory.index()] = pkFactory;
        }

        updateUniqueTileSources();
    }

    /**
     * Modifie l'état de la partie pour tenir compte du fait que le joueur courant a
     * joué le coup empaqueté donné.
     *
     * @param pkMove le coup empaqueté
     */
    public void registerMove(short pkMove) {
        TileSource source = PkMove.source(pkMove);
        TileKind.Colored color = PkMove.color(pkMove);
        TileDestination destination = PkMove.destination(pkMove);

        int sourceIndex = source.index();
        int pkSource = pkTileSourcesArray[sourceIndex];
        int takenTiles = PkTileSet.subsetOf(pkSource, color);
        int takenCount = PkTileSet.countOf(takenTiles, color);

        int pkFloor = PkPlayerStates.pkFloor(pkPlayerStates, currentPlayerId);

        if (source == TileSource.CENTER_AREA) {
            pkTileSourcesArray[0] = PkTileSet.difference(pkSource, takenTiles);

            if (PkTileSet.countOf(pkTileSourcesArray[0], TileKind.FIRST_PLAYER_MARKER) > 0) {
                pkTileSourcesArray[0] =
                        PkTileSet.remove(pkTileSourcesArray[0], TileKind.FIRST_PLAYER_MARKER);
                pkFloor = PkFloor.withAddedTiles(
                        pkFloor,
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                );
            }
        } else {
            int remainingTiles = PkTileSet.difference(pkSource, takenTiles);
            pkTileSourcesArray[0] = PkTileSet.union(pkTileSourcesArray[0], remainingTiles);
            pkTileSourcesArray[sourceIndex] = PkTileSet.EMPTY;
        }

        if (destination instanceof TileDestination.Pattern line) {
            int pkPatterns = PkPlayerStates.pkPatterns(pkPlayerStates, currentPlayerId);

            int freeSpace = line.capacity() - PkPatterns.size(pkPatterns, line);
            int tilesToPattern = Math.min(takenCount, freeSpace);
            int tilesToFloor = takenCount - tilesToPattern;

            if (tilesToPattern > 0) {
                pkPatterns = PkPatterns.withAddedTiles(pkPatterns, line, tilesToPattern, color);
                PkPlayerStates.setPkPatterns(pkPlayerStatesArray, currentPlayerId, pkPatterns);
            }

            if (tilesToFloor > 0) {
                pkFloor = PkFloor.withAddedTiles(pkFloor, PkTileSet.of(tilesToFloor, color));
            }
        } else {
            pkFloor = PkFloor.withAddedTiles(pkFloor, takenTiles);
        }

        PkPlayerStates.setPkFloor(pkPlayerStatesArray, currentPlayerId, pkFloor);
        currentPlayerId = nextPlayerId(currentPlayerId);
        updateUniqueTileSources();
    }

    /**
     * Termine la manche en mettant à jour les lignes de motif, les murs, les lignes
     * plancher et les scores de tous les joueurs.
     */
    public void endRound() {
        PlayerId nextStartingPlayer = null;

        for (PlayerId playerId : game.playerIds()) {
            int pkPatterns = PkPlayerStates.pkPatterns(pkPlayerStates, playerId);
            int pkWall = PkPlayerStates.pkWall(pkPlayerStates, playerId);
            int pkFloor = PkPlayerStates.pkFloor(pkPlayerStates, playerId);

            if (PkFloor.containsFirstPlayerMarker(pkFloor)) {
                nextStartingPlayer = playerId;
            }

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (PkPatterns.isFull(pkPatterns, line)) {
                    TileKind.Colored color = PkPatterns.color(pkPatterns, line);
                    pkWall = PkWall.withTileAt(pkWall, line, color);

                    int points = Points.newWallTilePoints(
                            PkWall.hGroupSize(pkWall, line, color),
                            PkWall.vGroupSize(pkWall, line, color)
                    );

                    PkPlayerStates.addPoints(pkPlayerStatesArray, playerId, points);
                    pointsObserver.newWallTile(playerId, line, color, points);

                    pkPatterns = PkPatterns.withEmptyLine(pkPatterns, line);
                }
            }

            PkPlayerStates.setPkPatterns(pkPlayerStatesArray, playerId, pkPatterns);
            PkPlayerStates.setPkWall(pkPlayerStatesArray, playerId, pkWall);

            int penalty = Math.min(
                    Points.totalFloorPenalty(PkFloor.size(pkFloor)),
                    PkPlayerStates.points(pkPlayerStates, playerId)
            );

            if (penalty > 0) {
                PkPlayerStates.addPoints(pkPlayerStatesArray, playerId, -penalty);
                pointsObserver.floor(playerId, penalty);
            }

            PkPlayerStates.setPkFloor(pkPlayerStatesArray, playerId, PkFloor.EMPTY);
        }

        if (nextStartingPlayer != null) {
            currentPlayerId = nextStartingPlayer;
            pkTileSourcesArray[0] = PkTileSet.add(pkTileSourcesArray[0], TileKind.FIRST_PLAYER_MARKER);
        }

        updateUniqueTileSources();
    }

    /**
     * Termine la partie en comptabilisant les points bonus de tous les joueurs.
     */
    public void endGame() {
        for (PlayerId playerId : game.playerIds()) {
            int pkWall = PkPlayerStates.pkWall(pkPlayerStates, playerId);

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (PkWall.isRowFull(pkWall, line)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesArray,
                            playerId,
                            Points.FULL_ROW_BONUS_POINTS
                    );
                    pointsObserver.fullRow(playerId, line, Points.FULL_ROW_BONUS_POINTS);
                }
            }

            for (int column = 0; column < PkWall.WALL_WIDTH; column += 1) {
                if (PkWall.isColumnFull(pkWall, column)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesArray,
                            playerId,
                            Points.FULL_COLUMN_BONUS_POINTS
                    );
                    pointsObserver.fullColumn(playerId, column, Points.FULL_COLUMN_BONUS_POINTS);
                }
            }

            for (TileKind.Colored color : TileKind.Colored.ALL) {
                if (PkWall.isColorFull(pkWall, color)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesArray,
                            playerId,
                            Points.FULL_COLOR_BONUS_POINTS
                    );
                    pointsObserver.fullColor(playerId, color, Points.FULL_COLOR_BONUS_POINTS);
                }
            }
        }
    }

    private int extractRandomColoredTiles(int count, RandomGenerator randomGenerator) {
        TileKind.Colored[] sampledTiles = new TileKind.Colored[count];
        PkTileSet.sampleColoredInto(pkTileBag, sampledTiles, 0, randomGenerator);

        int extractedTiles = PkTileSet.EMPTY;
        for (TileKind.Colored color : sampledTiles) {
            extractedTiles = PkTileSet.add(extractedTiles, color);
            pkTileBag = PkTileSet.remove(pkTileBag, color);
        }

        return extractedTiles;
    }

    private static void shuffle(TileKind.Colored[] tiles, RandomGenerator randomGenerator) {
        for (int i = tiles.length - 1; i > 0; i -= 1) {
            int j = randomGenerator.nextInt(i + 1);

            TileKind.Colored temp = tiles[i];
            tiles[i] = tiles[j];
            tiles[j] = temp;
        }
    }

    private PlayerId nextPlayerId(PlayerId playerId) {
        int nextOrdinal = (playerId.ordinal() + 1) % game.playersCount();
        return PlayerId.ALL.get(nextOrdinal);
    }

    private void updateUniqueTileSources() {
        int pkUniqueSources = PkIntSet32.EMPTY;

        for (int i = 0; i < pkTileSourcesArray.length; i += 1) {
            int pkSource = pkTileSourcesArray[i];
            if (!containsColoredTiles(pkSource)) continue;

            boolean alreadySeen = false;
            for (int j = 0; j < i; j += 1) {
                if (pkTileSourcesArray[j] == pkSource) {
                    alreadySeen = true;
                    break;
                }
            }

            if (!alreadySeen) {
                pkUniqueSources = PkIntSet32.add(pkUniqueSources, i);
            }
        }

        pkUniqueTileSources = pkUniqueSources;
    }

    private static boolean containsColoredTiles(int pkTileSet) {
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            if (PkTileSet.countOf(pkTileSet, color) > 0) return true;
        }
        return false;
    }
}
