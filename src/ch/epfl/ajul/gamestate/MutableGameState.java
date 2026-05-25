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
 * Un état mutable contient les mêmes informations qu'un état immuable, mais
 * permet de faire évoluer la partie au fil des coups joués, des fins de manche
 * et de la fin de partie.
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
     * Construit un nouvel état mutable ayant le même contenu que l'état donné,
     * et auquel l'observateur de points donné est attaché.
     *
     * @param initialState l'état initial
     * @param pointsObserver l'observateur de points
     * @throws NullPointerException si l'un des arguments est nul
     */
    public MutableGameState(
            ReadOnlyGameState initialState,
            PointsObserver pointsObserver
    ) {
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
     * Construit un nouvel état mutable ayant le même contenu que l'état donné,
     * et auquel un observateur de points vide est attaché.
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
     * sac sont d'abord extraites, le sac est rempli avec les tuiles sorties du
     * jeu, puis le complément nécessaire est extrait aléatoirement. Les tuiles
     * extraites sont ensuite mélangées puis réparties dans les fabriques.
     *
     * @param randomGenerator le générateur aléatoire
     * @throws NullPointerException si le générateur est nul
     */
    public void fillFactories(RandomGenerator randomGenerator) {
        requireNonNull(randomGenerator);

        int tilesNeeded =
                game.factoriesCount() * TileSource.Factory.TILES_PER_FACTORY;
        int bagTileCount = PkTileSet.size(pkTileBag);

        TileKind.Colored[] extractedTiles;

        if (bagTileCount > tilesNeeded) {
            extractedTiles = new TileKind.Colored[tilesNeeded];
            PkTileSet.sampleColoredInto(
                    pkTileBag,
                    extractedTiles,
                    0,
                    randomGenerator
            );
            removeTilesFromBag(extractedTiles, 0, extractedTiles.length);
        } else {
            int cachedDiscardedTiles = pkDiscardedTiles();
            int discardedTileCount = PkTileSet.size(cachedDiscardedTiles);
            int extractedTileCount = Math.min(
                    tilesNeeded,
                    bagTileCount + discardedTileCount
            );
            extractedTiles = new TileKind.Colored[extractedTileCount];

            int offset = 0;

            if (bagTileCount > 0) {
                offset = PkTileSet.copyColoredInto(pkTileBag, extractedTiles);
                pkTileBag = PkTileSet.EMPTY;
            }

            pkTileBag = cachedDiscardedTiles;
            if (offset < extractedTileCount) {
                int remainingTileCount = extractedTileCount - offset;
                int refilledBagTileCount = PkTileSet.size(pkTileBag);

                if (refilledBagTileCount > remainingTileCount) {
                    PkTileSet.sampleColoredInto(
                            pkTileBag,
                            extractedTiles,
                            offset,
                            randomGenerator
                    );
                    removeTilesFromBag(
                            extractedTiles,
                            offset,
                            extractedTileCount
                    );
                } else {
                    TileKind.Colored[] bagTiles =
                            new TileKind.Colored[refilledBagTileCount];
                    PkTileSet.copyColoredInto(pkTileBag, bagTiles);
                    System.arraycopy(
                            bagTiles,
                            0,
                            extractedTiles,
                            offset,
                            bagTiles.length
                    );
                    pkTileBag = PkTileSet.EMPTY;
                }
            }
        }

        shuffle(extractedTiles, randomGenerator);

        int nextTileIndex = 0;
        for (TileSource.Factory factory : game.factories()) {
            int pkFactory = PkTileSet.EMPTY;

            for (int i = 0;
                 i < TileSource.Factory.TILES_PER_FACTORY
                         && nextTileIndex < extractedTiles.length;
                 i += 1) {
                pkFactory = PkTileSet.add(
                        pkFactory,
                        extractedTiles[nextTileIndex]
                );
                nextTileIndex += 1;
            }

            pkTileSourcesArray[factory.index()] = pkFactory;
        }

        updateUniqueTileSources();
    }

    /**
     * Modifie l'état de la partie pour tenir compte du fait que le joueur
     * courant a joué le coup empaqueté donné.
     *
     * @param pkMove le coup empaqueté
     */
    public void registerMove(short pkMove) {
        TileSource source = PkMove.source(pkMove);
        TileKind.Colored color = PkMove.color(pkMove);
        TileDestination destination = PkMove.destination(pkMove);

        int sourceIndex = source.index();
        int oldCenter = pkTileSourcesArray[0];
        int oldSource = pkTileSourcesArray[sourceIndex];

        int takenTiles = PkTileSet.subsetOf(oldSource, color);
        int takenTileCount = PkTileSet.countOf(takenTiles, color);

        int pkFloor = PkPlayerStates.pkFloor(pkPlayerStates, currentPlayerId);

        if (source == TileSource.CENTER_AREA) {
            pkTileSourcesArray[0] = PkTileSet.difference(oldSource, takenTiles);

            if (PkTileSet.countOf(
                    pkTileSourcesArray[0],
                    TileKind.FIRST_PLAYER_MARKER
            ) > 0) {
                pkTileSourcesArray[0] = PkTileSet.remove(
                        pkTileSourcesArray[0],
                        TileKind.FIRST_PLAYER_MARKER
                );
                pkFloor = PkFloor.withAddedTiles(
                        pkFloor,
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                );
            }
        } else {
            int remainingTiles = PkTileSet.difference(oldSource, takenTiles);
            pkTileSourcesArray[0] =
                    PkTileSet.union(pkTileSourcesArray[0], remainingTiles);
            pkTileSourcesArray[sourceIndex] = PkTileSet.EMPTY;
        }

        if (destination instanceof TileDestination.Pattern line) {
            int pkPatterns =
                    PkPlayerStates.pkPatterns(pkPlayerStates, currentPlayerId);

            int freeSpace = line.capacity() - PkPatterns.size(pkPatterns, line);
            int tilesToPattern = Math.min(takenTileCount, freeSpace);
            int tilesToFloor = takenTileCount - tilesToPattern;

            if (tilesToPattern > 0) {
                pkPatterns = PkPatterns.withAddedTiles(
                        pkPatterns,
                        line,
                        tilesToPattern,
                        color
                );
                PkPlayerStates.setPkPatterns(
                        pkPlayerStatesArray,
                        currentPlayerId,
                        pkPatterns
                );
            }

            if (tilesToFloor > 0) {
                pkFloor = PkFloor.withAddedTiles(
                        pkFloor,
                        PkTileSet.of(tilesToFloor, color)
                );
            }
        } else {
            pkFloor = PkFloor.withAddedTiles(pkFloor, takenTiles);
        }

        PkPlayerStates.setPkFloor(pkPlayerStatesArray, currentPlayerId, pkFloor);
        updateUniqueTileSources();
        currentPlayerId = nextPlayerId(currentPlayerId);
    }

    /**
     * Termine la manche en mettant à jour les lignes de motif, les murs,
     * les lignes plancher et les scores de tous les joueurs.
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
                    TileKind.Colored wallTileColor =
                            PkPatterns.color(pkPatterns, line);
                    pkWall = PkWall.withTileAt(pkWall, line, wallTileColor);

                    int gainedPoints = Points.newWallTilePoints(
                            PkWall.hGroupSize(pkWall, line, wallTileColor),
                            PkWall.vGroupSize(pkWall, line, wallTileColor)
                    );

                    PkPlayerStates.addPoints(
                            pkPlayerStatesArray,
                            playerId,
                            gainedPoints
                    );
                    pointsObserver.newWallTile(
                            playerId,
                            line,
                            wallTileColor,
                            gainedPoints
                    );

                    pkPatterns = PkPatterns.withEmptyLine(pkPatterns, line);
                }
            }

            PkPlayerStates.setPkPatterns(pkPlayerStatesArray, playerId, pkPatterns);
            PkPlayerStates.setPkWall(pkPlayerStatesArray, playerId, pkWall);

            int floorPenalty = Math.min(
                    Points.totalFloorPenalty(PkFloor.size(pkFloor)),
                    PkPlayerStates.points(pkPlayerStates, playerId)
            );

            if (floorPenalty > 0) {
                PkPlayerStates.addPoints(
                        pkPlayerStatesArray,
                        playerId,
                        -floorPenalty
                );
                pointsObserver.floor(playerId, floorPenalty);
            }

            PkPlayerStates.setPkFloor(
                    pkPlayerStatesArray,
                    playerId,
                    PkFloor.EMPTY
            );
        }

        if (nextStartingPlayer != null) {
            currentPlayerId = nextStartingPlayer;
            pkTileSourcesArray[0] = PkTileSet.add(
                    pkTileSourcesArray[0],
                    TileKind.FIRST_PLAYER_MARKER
            );
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
                    pointsObserver.fullRow(
                            playerId,
                            line,
                            Points.FULL_ROW_BONUS_POINTS
                    );
                }
            }

            for (int column = 0; column < PkWall.WALL_WIDTH; column += 1) {
                if (PkWall.isColumnFull(pkWall, column)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesArray,
                            playerId,
                            Points.FULL_COLUMN_BONUS_POINTS
                    );
                    pointsObserver.fullColumn(
                            playerId,
                            column,
                            Points.FULL_COLUMN_BONUS_POINTS
                    );
                }
            }

            for (TileKind.Colored color : TileKind.Colored.ALL) {
                if (PkWall.isColorFull(pkWall, color)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesArray,
                            playerId,
                            Points.FULL_COLOR_BONUS_POINTS
                    );
                    pointsObserver.fullColor(
                            playerId,
                            color,
                            Points.FULL_COLOR_BONUS_POINTS
                    );
                }
            }
        }
    }

    /**
     * Retire du sac les tuiles du sous-tableau donné.
     *
     * @param tiles le tableau contenant les tuiles à retirer
     * @param fromIndex l'indice de début (inclus)
     * @param toIndex l'indice de fin (exclu)
     */
    private void removeTilesFromBag(
            TileKind.Colored[] tiles,
            int fromIndex,
            int toIndex
    ) {
        for (int i = fromIndex; i < toIndex; i += 1) {
            pkTileBag = PkTileSet.remove(pkTileBag, tiles[i]);
        }
    }

    /**
     * Mélange le tableau donné au moyen de l'algorithme de Fisher-Yates.
     *
     * @param tiles les tuiles à mélanger
     * @param randomGenerator le générateur aléatoire utilisé
     */
    private static void shuffle(
            TileKind.Colored[] tiles,
            RandomGenerator randomGenerator
    ) {
        for (int i = tiles.length - 1; i > 0; i -= 1) {
            int j = randomGenerator.nextInt(i + 1);

            TileKind.Colored temporaryTile = tiles[i];
            tiles[i] = tiles[j];
            tiles[j] = temporaryTile;
        }
    }

    /**
     * Retourne l'identité du joueur suivant.
     *
     * @param playerId l'identité du joueur courant
     * @return l'identité du joueur suivant
     */
    private PlayerId nextPlayerId(PlayerId playerId) {
        int nextOrdinal = (playerId.ordinal() + 1) % game.playersCount();
        return PlayerId.ALL.get(nextOrdinal);
    }

    /**
     * Met à jour l'ensemble empaqueté des indices des sources uniques.
     */
    private void updateUniqueTileSources() {
        int pkUniqueSources = PkIntSet32.EMPTY;

        for (int sourceIndex = 0;
             sourceIndex < pkTileSourcesArray.length;
             sourceIndex += 1) {
            int pkSource = pkTileSourcesArray[sourceIndex];
            if (!containsColoredTiles(pkSource)) {
                continue;
            }

            boolean alreadySeen = false;
            for (int previousIndex = 0;
                 previousIndex < sourceIndex;
                 previousIndex += 1) {
                if (pkTileSourcesArray[previousIndex] == pkSource) {
                    alreadySeen = true;
                    break;
                }
            }

            if (!alreadySeen) {
                pkUniqueSources = PkIntSet32.add(pkUniqueSources, sourceIndex);
            }
        }

        pkUniqueTileSources = pkUniqueSources;
    }

    /**
     * Retourne vrai ssi l'ensemble empaqueté donné contient au moins une tuile
     * colorée.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return vrai ssi l'ensemble contient au moins une tuile colorée
     */
    private static boolean containsColoredTiles(int pkTileSet) {
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            if (PkTileSet.countOf(pkTileSet, color) > 0) {
                return true;
            }
        }
        return false;
    }
}