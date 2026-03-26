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
 *   <li>du contenu du sac (ensemble de tuiles empaqueté),</li>
 *   <li>du contenu des sources (tableau d'ensembles de tuiles empaquetés),</li>
 *   <li>de l'ensemble des sources uniques (ensemble d'indices empaqueté),</li>
 *   <li>de l'état de chaque joueur (tableau empaqueté),</li>
 *   <li>de l'identité du joueur courant ({@link PlayerId}).</li>
 * </ul>
 * <p>
 * Cet enregistrement implémente {@link ReadOnlyGameState}. Les méthodes de lecture
 * correspondantes sont celles générées automatiquement par le record.
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
     * Construit un état immuable en vérifiant que les paramètres pouvant être {@code null}
     * ne le sont pas.
     *
     * @throws NullPointerException si {@code game}, {@code pkTileSources}, {@code pkPlayerStates}
     *                              ou {@code currentPlayerId} est {@code null}
     */
    public ImmutableGameState {
        requireNonNull(game);
        requireNonNull(pkTileSources);
        requireNonNull(pkPlayerStates);
        requireNonNull(currentPlayerId);
    }

    /**
     * Retourne l'état initial d'une partie, pour la configuration donnée.
     * <p>
     * Dans l'état initial :
     * <ul>
     *   <li>le sac contient toutes les tuiles colorées (20 de chaque couleur),</li>
     *   <li>la zone centrale (source d'index 0) contient le marqueur de premier joueur,</li>
     *   <li>toutes les fabriques sont vides,</li>
     *   <li>l'ensemble des sources uniques est vide,</li>
     *   <li>tous les joueurs ont des lignes de motif vides, un plancher vide, un mur vide et 0 point,</li>
     *   <li>le joueur courant est le premier joueur de la partie.</li>
     * </ul>
     *
     * @param game la configuration de la partie
     * @return l'état initial immuable
     * @throws NullPointerException si {@code game} est {@code null}
     */
    public static ImmutableGameState initial(Game game) {
        requireNonNull(game);

        int pkTileBag = PkTileSet.FULL_COLORED;

        int[] sources = new int[game.tileSourcesCount()];
        sources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        ImmutableIntArray pkTileSources = ImmutableIntArray.copyOf(sources);

        int pkUniqueTileSources = 0;

        ImmutableIntArray pkPlayerStates = PkPlayerStates.initial(game);

        PlayerId currentPlayerId = game.playerIds().get(0);

        return new ImmutableGameState(
                game,
                pkTileBag,
                pkTileSources,
                pkUniqueTileSources,
                pkPlayerStates,
                currentPlayerId
        );
    }

    /**
     * Cet état étant déjà immuable, retourne {@code this}.
     *
     * @return {@code this}
     */
    @Override
    public ImmutableGameState immutable() {
        return this;
    }
}