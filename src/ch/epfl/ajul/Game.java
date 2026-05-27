package ch.epfl.ajul;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Configuration immuable d'une partie d'Ajul.
 *
 * @author Danny Levy (394098)
 * @author Elie Menasche Reuben Surman (410685)
 */
public final class Game {

    /**
     * Description immuable d'un joueur.
     *
     * @param id l'identité du joueur
     * @param name le nom du joueur
     * @param kind la sorte du joueur
     *
     * @author Danny Levy (394098)
     * @author Elie Menasche Reuben Surman (410685)
     */
    public record PlayerDescription(PlayerId id, String name, PlayerKind kind) {

        /**
         * Sorte de joueur.
         *
         * @author Danny Levy (394098)
         * @author Elie Menasche Reuben Surman (410685)
         */
        public enum PlayerKind {
            /** Joueur humain. */
            HUMAN,
            /** Joueur contrôlé par l’ordinateur. */
            AI
        }

        /**
         * Construit une description de joueur.
         *
         * @throws NullPointerException si l'un des paramètres est {@code null}
         */
        public PlayerDescription {
            requireNonNull(id);
            requireNonNull(name);
            requireNonNull(kind);
        }
    }

    private final List<PlayerDescription> playerDescriptions;

    /**
     * Construit une configuration à partir des descriptions des joueurs.
     *
     * @param playerDescriptions la liste des descriptions des joueurs
     * @throws NullPointerException si la liste ou l'une de ses entrées est {@code null}
     * @throws IllegalArgumentException si le nombre de joueurs n'est pas entre 2 et 4,
     * ou si l'ordre des identités n'est pas {@code P1, P2, ...}
     */
    public Game(List<PlayerDescription> playerDescriptions) {
        requireNonNull(playerDescriptions);
        this.playerDescriptions = List.copyOf(playerDescriptions);

        int playerCount = this.playerDescriptions.size();
        Preconditions.checkArgument(2 <= playerCount && playerCount <= 4);

        for (int i = 0; i < playerCount; i += 1) {
            PlayerId expectedPlayerId = PlayerId.ALL.get(i);
            Preconditions.checkArgument(
                    this.playerDescriptions.get(i).id().equals(expectedPlayerId)
            );
        }
    }

    /**
     * Retourne la taille maximale de la zone centrale, marqueur de premier
     * joueur inclus.
     *
     * @return la taille maximale de la zone centrale
     */
    public int centralAreaMaxSize() {
        int factoryCount = factoriesCount();
        return 3 * factoryCount + 1;
    }

    /**
     * Retourne la liste des fabriques utilisées.
     *
     * @return la liste des fabriques utilisées
     */
    public List<TileSource.Factory> factories() {
        return TileSource.Factory.ALL.subList(0, factoriesCount());
    }

    /**
     * Retourne le nombre de fabriques utilisées.
     *
     * @return le nombre de fabriques utilisées
     */
    public int factoriesCount() {
        return 2 * playersCount() + 1;
    }

    /**
     * Retourne la liste des descriptions des joueurs.
     *
     * @return la liste des descriptions des joueurs
     */
    public List<PlayerDescription> playerDescriptions() {
        return playerDescriptions;
    }

    /**
     * Retourne la liste des identités des joueurs.
     *
     * @return la liste des identités des joueurs
     */
    public List<PlayerId> playerIds() {
        return PlayerId.ALL.subList(0, playersCount());
    }

    /**
     * Retourne le nombre de joueurs.
     *
     * @return le nombre de joueurs
     */
    public int playersCount() {
        return playerDescriptions.size();
    }

    /**
     * Retourne la liste des sources de tuiles utilisées.
     *
     * @return la liste des sources de tuiles utilisées
     */
    public List<TileSource> tileSources() {
        return TileSource.ALL.subList(0, tileSourcesCount());
    }

    /**
     * Retourne le nombre de sources de tuiles utilisées.
     *
     * @return le nombre de sources de tuiles utilisées
     */
    public int tileSourcesCount() {
        return 1 + factoriesCount();
    }
}