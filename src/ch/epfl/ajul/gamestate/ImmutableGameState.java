package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;

import static java.util.Objects.requireNonNull;

/**
 * État immuable d'une partie d'Ajul.
 * <p>
 * Un état de jeu est composé :
 * <ul>
 *   <li>de la configuration de la partie ({@link Game}),</li>
 *   <li>du contenu du sac, sous la forme d'un ensemble de tuiles empaqueté,</li>
 *   <li>du contenu des sources, sous la forme d'un tableau d'ensembles de
 *   tuiles empaquetés,</li>
 *   <li>de l'ensemble des sources uniques, sous la forme d'un ensemble
 *   empaqueté d'indices,</li>
 *   <li>de l'état de chaque joueur, sous la forme d'un tableau empaqueté,</li>
 *   <li>de l'identité du joueur courant ({@link PlayerId}).</li>
 * </ul>
 * <p>
 * Cet enregistrement implémente {@link ReadOnlyGameState}. Les méthodes
 * d'accès correspondantes sont générées automatiquement par le record.
 *
 * @param game la configuration de la partie
 * @param pkTileBag le contenu du sac, sous forme d'ensemble de tuiles empaqueté
 * @param pkTileSources le contenu des sources, sous forme de tableau d'ensembles empaquetés
 * @param pkUniqueTileSources l'ensemble empaqueté des indices des sources uniques
 * @param pkPlayerStates l'état empaqueté des joueurs
 * @param currentPlayerId l'identité du joueur courant
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public record ImmutableGameState(
        Game game,
        int pkTileBag,
        ImmutableIntArray pkTileSources,
        int pkUniqueTileSources,
        ImmutableIntArray pkPlayerStates,
        PlayerId currentPlayerId
) implements ReadOnlyGameState {

    /**
     * Construit un état immuable à partir des valeurs données.
     *
     * @throws NullPointerException si {@code game}, {@code pkTileSources},
     * {@code pkPlayerStates} ou {@code currentPlayerId} est {@code null}
     */
    public ImmutableGameState {
        requireNonNull(game);
        requireNonNull(pkTileSources);
        requireNonNull(pkPlayerStates);
        requireNonNull(currentPlayerId);
    }

    /**
     * Retourne l'état initial d'une partie pour la configuration donnée.
     * <p>
     * Dans l'état initial :
     * <ul>
     *   <li>le sac contient toutes les tuiles colorées, à raison de 20 de
     *   chaque couleur,</li>
     *   <li>la zone centrale, c'est-à-dire la source d'index 0, contient le
     *   marqueur de premier joueur,</li>
     *   <li>toutes les fabriques sont vides,</li>
     *   <li>l'ensemble des sources uniques est vide,</li>
     *   <li>tous les joueurs ont des lignes de motif vides, une ligne plancher
     *   vide, un mur vide et 0 point,</li>
     *   <li>le joueur courant est le premier joueur de la partie.</li>
     * </ul>
     *
     * @param game la configuration de la partie
     * @return l'état initial immuable correspondant
     * @throws NullPointerException si {@code game} est {@code null}
     */
    public static ImmutableGameState initial(Game game) {
        requireNonNull(game);

        int initialPkTileBag = PkTileSet.FULL_COLORED;

        int[] initialSourcesArray = new int[game.tileSourcesCount()];
        initialSourcesArray[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        ImmutableIntArray initialPkTileSources = ImmutableIntArray.copyOf(initialSourcesArray);

        int initialPkUniqueTileSources = 0;
        ImmutableIntArray initialPkPlayerStates = PkPlayerStates.initial(game);
        PlayerId initialCurrentPlayerId = game.playerIds().get(0);

        return new ImmutableGameState(
                game,
                initialPkTileBag,
                initialPkTileSources,
                initialPkUniqueTileSources,
                initialPkPlayerStates,
                initialCurrentPlayerId
        );
    }

    /**
     * Retourne cet état.
     * <p>
     * Cet état étant déjà immuable, cette méthode retourne simplement
     * {@code this}.
     *
     * @return {@code this}
     */
    @Override
    public ImmutableGameState immutable() {
        return this;
    }
}