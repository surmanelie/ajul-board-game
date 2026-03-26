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
 * L'état est composé de la configuration de la partie, du contenu du sac, du contenu
 * des sources de tuiles, de l'ensemble des sources uniques, de l'état des joueurs
 * et de l'identité du joueur courant.
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
     * Retourne le contenu du sac duquel les tuiles sont extraites pour remplir les fabriques,
     * sous la forme d'un ensemble de tuiles empaqueté.
     *
     * @return le contenu empaqueté du sac
     */
    int pkTileBag();

    /**
     * Retourne un tableau décrivant le contenu des sources de tuiles, l'élément à l'index
     * {@code i} de ce tableau étant l'ensemble de tuiles empaqueté correspondant à la source
     * d'index {@code i}.
     *
     * @return le tableau décrivant le contenu des sources de tuiles
     */
    ReadOnlyIntArray pkTileSources();

    /**
     * Retourne l'ensemble empaqueté des index des sources uniques.
     *
     * @return l'ensemble empaqueté des sources uniques
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
     */
    default List<PlayerId> playerIds() {
        return game().playerIds();
    }

    /**
     * Retourne vrai ssi la manche est terminée, c.-à-d. qu'aucune source ne contient
     * de tuile colorée.
     */
    default boolean isRoundOver() {
        var sources = pkTileSources();
        for (int i = 0; i < sources.size(); i += 1) {
            if (coloredCount(sources.get(i)) > 0) return false;
        }
        return true;
    }

    /**
     * Retourne vrai ssi la partie est terminée (manche terminée et au moins un mur a une ligne pleine).
     */
    default boolean isGameOver() {
        if (!isRoundOver()) return false;

        var ps = pkPlayerStates();
        for (var pid : playerIds()) {
            if (PkWall.hasFullRow(PkPlayerStates.pkWall(ps, pid))) return true;
        }
        return false;
    }

    /**
     * Retourne l'ensemble des tuiles sorties du jeu.
     */
    default int pkDiscardedTiles() {
        int remaining = PkTileSet.FULL;

        remaining = PkTileSet.difference(remaining, pkTileBag());

        var sources = pkTileSources();
        for (int i = 0; i < sources.size(); i += 1) {
            remaining = PkTileSet.difference(remaining, sources.get(i));
        }

        var ps = pkPlayerStates();
        for (var pid : playerIds()) {
            remaining = PkTileSet.difference(remaining, PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(ps, pid)));
            remaining = PkTileSet.difference(remaining, PkFloor.asPkTileSet(PkPlayerStates.pkFloor(ps, pid)));
            remaining = PkTileSet.difference(remaining, PkWall.asPkTileSet(PkPlayerStates.pkWall(ps, pid)));
        }

        return remaining;
    }

    /**
     * Écrit dans destination tous les coups empaquetés jouables par le joueur courant, et retourne leur nombre.
     */
    default int validMoves(short[] destination) {
        return validMovesInto(destination, false);
    }

    /**
     * Identique à validMoves mais ne considère que les sources uniques.
     */
    default int uniqueValidMoves(short[] destination) {
        return validMovesInto(destination, true);
    }



    private int validMovesInto(short[] destination, boolean uniqueOnly) {
        Preconditions.checkArgument(destination.length >= Move.MAX_MOVES);

        var sources = pkTileSources();
        var current = currentPlayerId();
        var ps = pkPlayerStates();

        int pkPatterns = PkPlayerStates.pkPatterns(ps, current);
        int pkWall = PkPlayerStates.pkWall(ps, current);

        int uniqueSources = pkUniqueTileSources();

        int count = 0;
        for (int srcIndex = 0; srcIndex < sources.size(); srcIndex += 1) {
            if (uniqueOnly && !PkIntSet32.contains(uniqueSources, srcIndex)) continue;

            int pkSource = sources.get(srcIndex);
            var source = TileSource.ALL.get(srcIndex);

            for (var color : TileKind.Colored.ALL) {
                if (PkTileSet.countOf(pkSource, color) == 0) continue;

                for (var line : TileDestination.Pattern.ALL) {
                    if (PkPatterns.isFull(pkPatterns, line)) continue;
                    if (PkWall.hasTileAt(pkWall, line, color)) continue;
                    if (!PkPatterns.canContain(pkPatterns, line, color)) continue;

                    destination[count++] = PkMove.pack(source, color, line);
                }

                destination[count++] = PkMove.pack(source, color, TileDestination.FLOOR);
            }
        }
        return count;
    }

    private static int coloredCount(int pkTileSet) {
        int s = 0;
        for (var c : TileKind.Colored.ALL) {
            s += PkTileSet.countOf(pkTileSet, c);
        }
        return s;
    }
}