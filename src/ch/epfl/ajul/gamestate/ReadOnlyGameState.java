package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Preconditions;
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
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.List;

/**
 * Représente un état de partie d'Ajul en lecture seule.
 * <p>
 * L'état est composé de la configuration de la partie, du contenu du sac,
 * du contenu des sources de tuiles, de l'ensemble des sources uniques,
 * de l'état des joueurs et de l'identité du joueur courant.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public interface ReadOnlyGameState {

    /**
     * Retourne la configuration de la partie.
     *
     * @return la configuration de la partie
     */
    Game game();

    /**
     * Retourne le contenu du sac duquel les tuiles sont extraites pour remplir
     * les fabriques, sous la forme d'un ensemble de tuiles empaqueté.
     *
     * @return le contenu empaqueté du sac
     */
    int pkTileBag();

    /**
     * Retourne un tableau décrivant le contenu des sources de tuiles.
     * <p>
     * L'élément d'index {@code i} de ce tableau est l'ensemble de tuiles
     * empaqueté correspondant à la source d'index {@code i}.
     *
     * @return le tableau décrivant le contenu des sources de tuiles
     */
    ReadOnlyIntArray pkTileSources();

    /**
     * Retourne l'ensemble empaqueté des indices des sources uniques.
     *
     * @return l'ensemble empaqueté des indices des sources uniques
     */
    int pkUniqueTileSources();

    /**
     * Retourne le tableau contenant les états empaquetés des joueurs.
     *
     * @return le tableau contenant les états empaquetés des joueurs
     */
    ReadOnlyIntArray pkPlayerStates();

    /**
     * Retourne l'identité du joueur courant.
     *
     * @return l'identité du joueur courant
     */
    PlayerId currentPlayerId();

    /**
     * Retourne une version immuable de cet état.
     *
     * @return une version immuable de cet état
     */
    default ImmutableGameState immutable() {
        return new ImmutableGameState(
                game(),
                pkTileBag(),
                pkTileSources().immutable(),
                pkUniqueTileSources(),
                pkPlayerStates().immutable(),
                currentPlayerId()
        );
    }

    /**
     * Retourne la liste des identités des joueurs de la partie.
     *
     * @return la liste des identités des joueurs
     */
    default List<PlayerId> playerIds() {
        return game().playerIds();
    }

    /**
     * Retourne vrai ssi la manche est terminée, c'est-à-dire si aucune source
     * ne contient de tuile colorée.
     *
     * @return vrai ssi la manche est terminée
     */
    default boolean isRoundOver() {
        ReadOnlyIntArray tileSources = pkTileSources();

        for (int sourceIndex = 0; sourceIndex < tileSources.size(); sourceIndex += 1) {
            if (coloredCount(tileSources.get(sourceIndex)) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retourne vrai ssi la partie est terminée.
     * <p>
     * La partie est terminée si la manche est terminée et qu'au moins un mur
     * contient une ligne complète.
     *
     * @return vrai ssi la partie est terminée
     */
    default boolean isGameOver() {
        if (!isRoundOver()) {
            return false;
        }

        ReadOnlyIntArray playerStates = pkPlayerStates();
        for (PlayerId playerId : playerIds()) {
            if (PkWall.hasFullRow(PkPlayerStates.pkWall(playerStates, playerId))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne l'ensemble empaqueté des tuiles sorties du jeu.
     *
     * @return l'ensemble empaqueté des tuiles sorties du jeu
     */
    default int pkDiscardedTiles() {
        int remainingTiles = PkTileSet.FULL;

        remainingTiles = PkTileSet.difference(remainingTiles, pkTileBag());

        ReadOnlyIntArray tileSources = pkTileSources();
        for (int sourceIndex = 0; sourceIndex < tileSources.size(); sourceIndex += 1) {
            remainingTiles = PkTileSet.difference(
                    remainingTiles,
                    tileSources.get(sourceIndex)
            );
        }

        ReadOnlyIntArray playerStates = pkPlayerStates();
        for (PlayerId playerId : playerIds()) {
            remainingTiles = PkTileSet.difference(
                    remainingTiles,
                    PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(playerStates, playerId))
            );
            remainingTiles = PkTileSet.difference(
                    remainingTiles,
                    PkFloor.asPkTileSet(PkPlayerStates.pkFloor(playerStates, playerId))
            );
            remainingTiles = PkTileSet.difference(
                    remainingTiles,
                    PkWall.asPkTileSet(PkPlayerStates.pkWall(playerStates, playerId))
            );
        }

        return remainingTiles;
    }

    /**
     * Écrit dans {@code destination} tous les coups empaquetés jouables par le
     * joueur courant, puis retourne leur nombre.
     *
     * @param destination le tableau recevant les coups empaquetés
     * @return le nombre de coups écrits dans {@code destination}
     */
    default int validMoves(short[] destination) {
        return validMovesInto(destination, false);
    }

    /**
     * Écrit dans {@code destination} tous les coups empaquetés jouables par le
     * joueur courant en ne considérant que les sources uniques, puis retourne
     * leur nombre.
     *
     * @param destination le tableau recevant les coups empaquetés
     * @return le nombre de coups écrits dans {@code destination}
     */
    default int uniqueValidMoves(short[] destination) {
        return validMovesInto(destination, true);
    }

    /**
     * Écrit dans {@code destination} les coups valides, en ne considérant que
     * les sources uniques si {@code uniqueOnly} vaut vrai.
     *
     * @param destination le tableau recevant les coups empaquetés
     * @param uniqueOnly vrai ssi seules les sources uniques doivent être considérées
     * @return le nombre de coups écrits dans {@code destination}
     */
    private int validMovesInto(short[] destination, boolean uniqueOnly) {
        Preconditions.checkArgument(destination.length >= Move.MAX_MOVES);

        ReadOnlyIntArray tileSources = pkTileSources();
        PlayerId currentPlayer = currentPlayerId();
        ReadOnlyIntArray playerStates = pkPlayerStates();

        int pkPatterns = PkPlayerStates.pkPatterns(playerStates, currentPlayer);
        int pkWall = PkPlayerStates.pkWall(playerStates, currentPlayer);
        int uniqueSources = pkUniqueTileSources();

        int moveCount = 0;

        for (int sourceIndex = 0; sourceIndex < tileSources.size(); sourceIndex += 1) {
            if (uniqueOnly && !PkIntSet32.contains(uniqueSources, sourceIndex)) {
                continue;
            }

            int pkSource = tileSources.get(sourceIndex);
            TileSource source = TileSource.ALL.get(sourceIndex);

            for (TileKind.Colored color : TileKind.Colored.ALL) {
                if (PkTileSet.countOf(pkSource, color) == 0) {
                    continue;
                }

                for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                    if (PkPatterns.isFull(pkPatterns, line)) {
                        continue;
                    }
                    if (PkWall.hasTileAt(pkWall, line, color)) {
                        continue;
                    }
                    if (!PkPatterns.canContain(pkPatterns, line, color)) {
                        continue;
                    }

                    destination[moveCount] = PkMove.pack(source, color, line);
                    moveCount += 1;
                }

                destination[moveCount] =
                        PkMove.pack(source, color, TileDestination.FLOOR);
                moveCount += 1;
            }
        }

        return moveCount;
    }

    /**
     * Retourne le nombre total de tuiles colorées contenues dans l'ensemble
     * empaqueté donné.
     *
     * @param pkTileSet l'ensemble empaqueté
     * @return le nombre de tuiles colorées
     */
    private static int coloredCount(int pkTileSet) {
        int coloredTileCount = 0;

        for (TileKind.Colored color : TileKind.Colored.ALL) {
            coloredTileCount += PkTileSet.countOf(pkTileSet, color);
        }

        return coloredTileCount;
    }
}