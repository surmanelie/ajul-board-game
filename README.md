# Ajul

Jeu de plateau de pose de tuiles (dans l'esprit du jeu *Azul*), écrit en Java avec une interface graphique JavaFX et un joueur contrôlé par intelligence artificielle.

Les joueurs piochent des tuiles colorées, les placent sur des lignes de préparation, puis les transfèrent sur un mur en fin de manche pour marquer des points selon des règles d'alignement et de bonus (lignes, colonnes, couleurs complètes). La partie peut se jouer à plusieurs, avec un mélange de joueurs humains et de joueurs IA.

Projet réalisé dans le cadre du cours de pratique de la programmation orientée objet (POO) à l'EPFL, en binôme.

**Auteurs**
- Danny Levy
- Elie Menasche Reuben Surman

## Notions de POO et concepts mis en œuvre

- **Immutabilité et séparation des responsabilités** : l'état de la partie existe sous plusieurs formes selon le besoin — une version immuable (`ImmutableGameState`), une version modifiable (`MutableGameState`) et une version en lecture seule exposée à l'IA (`ReadOnlyGameState`), pour garantir qu'aucune partie du code ne modifie l'état par erreur.
- **Interfaces scellées (`sealed interface`) et records** (Java 17+) pour modéliser les types de tuiles et les descriptions de joueurs de façon sûre et concise.
- **Encapsulation bas niveau** : le paquetage `gamestate.packed` représente l'état du jeu sous forme de valeurs compactées en bits (`long`, `int`) plutôt qu'en objets classiques, pour des raisons de performance.
- **Pattern Observer** : `PointsObserver` permet de réagir aux changements de score sans coupler la logique de jeu à l'affichage.
- **Programmation événementielle avec JavaFX** pour la couche graphique (`BoardUI`, `TileAnimator`, `TileOverlayUI`).
- **Intelligence artificielle par Monte Carlo Tree Search (MCTS)** : `MctsPlayer` simule un grand nombre de parties aléatoires depuis l'état courant pour choisir le coup le plus prometteur.
- **Programmation concurrente** : la boucle de jeu communique avec l'interface graphique via une file bloquante (`BlockingQueue` / `SynchronousQueue`) pour séparer le thread de logique du thread d'interface.
- **Tests unitaires** organisés par composant (`gamestate`, `mcts`, `intarray`, etc.), validant indépendamment chaque brique du système.

## Stack technique

- Java 17+ (records, sealed interfaces, pattern matching)
- JavaFX 21 pour l'interface graphique
- JUnit 5 pour les tests

## Structure du projet

```
src/ch/epfl/ajul/
  Game.java, Player.java, PlayerId.java       Modèle de base du jeu et des joueurs
  Points.java, PointsObserver.java            Calcul et observation des points
  TileKind.java, TileSource.java,
  TileDestination.java, Preconditions.java,
  RankComputer.java                           Règles du jeu et utilitaires

  gamestate/                                  État de la partie
    ImmutableGameState.java
    MutableGameState.java
    ReadOnlyGameState.java
    Move.java
    packed/                                   Représentation compacte en bits de l'état

  gui/                                        Interface graphique JavaFX
    Main.java, BoardUI.java, TileAnimator.java,
    TileOverlayUI.java, RelocationTransition.java, Tiles.java

  intarray/                                   Tableaux d'entiers immuables/modifiables

  mcts/                                       Joueur IA (Monte Carlo Tree Search)
    MctsPlayer.java, MctsNode.java,
    HeuristicMoveSelector.java

test/                                         Tests unitaires (JUnit 5)
lib/                                          Bibliothèques JavaFX
```

## Lancer le projet

Le projet est configuré comme un projet IntelliJ IDEA (module Java avec `module-info.java`).

1. Ouvrir le dossier dans IntelliJ IDEA.
2. Vérifier qu'un JDK 17 ou supérieur est configuré pour le projet.
3. Les bibliothèques JavaFX nécessaires sont fournies dans `lib/`.
4. Lancer la classe `ch.epfl.ajul.gui.Main`.

## Tests

Les tests sont écrits avec JUnit 5 et se trouvent dans le dossier `test/`, organisés selon la même structure de paquetages que le code source.
