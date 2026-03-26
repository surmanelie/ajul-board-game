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
 * L'état est composé de la configuration (Game), du sac, des sources, de l'état des joueurs
 * et du joueur courant. Certaines informations (p.ex. tuiles sorties du jeu) sont calculées
 * à la demande.
 */
public interface ReadOnlyGameState {

    // --- Méthodes abstraites (stockage) ---

    Game game();

    int pkTileBag();

    ReadOnlyIntArray pkTileSources();

    int pkUniqueTileSources();

    ReadOnlyIntArray pkPlayerStates();

    PlayerId currentPlayerId();

    // --- Méthodes par défaut (calculées) ---

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

    // --- Code commun ---

    private int validMovesInto(short[] destination, boolean uniqueOnly) {
        Preconditions.checkArgument(destination.length >= Move.MAX_MOVES);

        var sources = pkTileSources();
        var current = currentPlayerId();
        var ps = pkPlayerStates();
        int pkPatterns = PkPlayerStates.pkPatterns(ps, current);

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