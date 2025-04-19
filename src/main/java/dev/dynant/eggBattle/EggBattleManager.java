/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle;

import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class EggBattleManager {
  private static final String OBJECTIVE_NAME = "eggScore";
  private static final Component OBJECTIVE_TITLE =
      Component.text()
          .append(Component.text("NV: ", NamedTextColor.LIGHT_PURPLE))
          .append(Component.text("Egg ", NamedTextColor.YELLOW))
          .append(Component.text("Battle", NamedTextColor.AQUA))
          .build();

  private static final Title START_GAME_TITLE =
      Title.title(
          Component.text()
              .append(Component.text("Egg ", NamedTextColor.YELLOW))
              .append(Component.text("Battle ", NamedTextColor.AQUA))
              .append(Component.text("Started!", NamedTextColor.GOLD))
              .build(),
          Component.text("Let's get cracking!", NamedTextColor.GREEN));

  private static final Title PAUSE_GAME_TITLE =
      Title.title(
          Component.text()
              .append(Component.text("Egg ", NamedTextColor.YELLOW))
              .append(Component.text("Battle ", NamedTextColor.AQUA))
              .append(Component.text("Paused!", NamedTextColor.GOLD))
              .build(),
          Component.text("Taking a little egg break!", NamedTextColor.YELLOW));

  private static final Title UNPAUSE_GAME_TITLE =
      Title.title(
          Component.text()
              .append(Component.text("Egg ", NamedTextColor.YELLOW))
              .append(Component.text("Battle ", NamedTextColor.AQUA))
              .append(Component.text("Resumed!", NamedTextColor.GOLD))
              .build(),
          Component.text("Let's get cracking again!", NamedTextColor.YELLOW));

  private static final Title END_GAME_TITLE =
      Title.title(
          Component.text()
              .append(Component.text("Egg ", NamedTextColor.YELLOW))
              .append(Component.text("Battle ", NamedTextColor.AQUA))
              .append(Component.text("Ended!", NamedTextColor.GOLD))
              .build(),
          Component.text("That's all, yolks!", NamedTextColor.RED));

  private static final String GAME_ACTIVE_CONFIG_KEY = "game_active";

  private final EggBattle plugin;
  private final ScoreManager scoreManager;
  private final Scoreboard scoreBoard;
  private Objective objective;

  public EggBattleManager(EggBattle plugin) {
    this.plugin = plugin;
    this.scoreManager = new ScoreManager(plugin, this);

    scoreBoard = Bukkit.getScoreboardManager().getMainScoreboard();
    scoreBoard.clearSlot(DisplaySlot.SIDEBAR);

    if (gameIsActive()) {
      createObjective();
      loadScoresIntoScoreboard();
    }
  }

  public boolean gameIsActive() {
    return plugin.getConfig().getBoolean(GAME_ACTIVE_CONFIG_KEY, false);
  }

  public void setGameState(boolean active) {
    plugin.getConfig().set(GAME_ACTIVE_CONFIG_KEY, active);
    plugin.saveConfig();
  }

  public void createObjective() {
    // Don't create a new objective if it already exists
    Objective exitingObjective = scoreBoard.getObjective(OBJECTIVE_NAME);
    if (exitingObjective != null) exitingObjective.unregister();

    // Register new objective to display on scoreboard
    objective = scoreBoard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, OBJECTIVE_TITLE);
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
  }

  public void loadScoresIntoScoreboard() {
    if (objective == null) return;

    // Clear existing scores from scoreboard
    for (String entry : scoreBoard.getEntries()) {
      objective.getScore(entry).resetScore();
    }

    Map<UUID, Integer> scores = scoreManager.getAllScores();

    // Loop through all scores and set them to the scoreboard
    for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
      OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());

      if (player.getName() != null) {
        objective.getScore(player.getName()).setScore(entry.getValue());
      }
    }
  }

  public void saveScoresToFile() {
    scoreManager.saveScoresToFile();
  }

  public void startGame(CommandSender sender) {
    setGameState(true);
    scoreManager.startSaveTask();

    createObjective();

    for (Player player : Bukkit.getOnlinePlayers()) {
      player.showTitle(START_GAME_TITLE);
    }

    sender.sendMessage(Component.text("Game started!", NamedTextColor.GREEN));
  }

  public void pauseGame(CommandSender sender) {
    if (gameIsActive()) {
      // Pause
      setGameState(false);
      scoreManager.stopSaveTask();

      for (Player player : Bukkit.getOnlinePlayers()) {
        player.showTitle(PAUSE_GAME_TITLE);
      }

      sender.sendMessage(Component.text("Game paused!", NamedTextColor.GREEN));
    } else {
      // Unpause
      setGameState(true);
      scoreManager.startSaveTask();

      for (Player player : Bukkit.getOnlinePlayers()) {
        player.showTitle(UNPAUSE_GAME_TITLE);
      }

      sender.sendMessage(Component.text("Game unpaused!", NamedTextColor.GREEN));
    }
  }

  public void stopGame(CommandSender sender) {
    // Set game active to false in config
    setGameState(false);
    scoreManager.stopSaveTask();
    saveScoresToFile();

    for (Player player : Bukkit.getOnlinePlayers()) {
      player.showTitle(END_GAME_TITLE);
    }

    sender.sendMessage(Component.text("Game stopped!", NamedTextColor.GREEN));
  }

  public void resetGame(CommandSender sender) {
    // Make sure the game is not active (in case we forgot to end the game)
    setGameState(false);
    scoreManager.stopSaveTask();

    resetScores(sender);
    saveScoresToFile();

    // Unregister objective and clear scoreboard
    if (objective != null) objective.unregister();
    if (scoreBoard != null) scoreBoard.clearSlot(DisplaySlot.SIDEBAR);

    sender.sendMessage(Component.text("Game reset!", NamedTextColor.GREEN));
  }

  public void resetScores(CommandSender sender) {
    scoreManager.resetAll();

    saveScoresToFile();
    loadScoresIntoScoreboard();

    sender.sendMessage(Component.text("Scores reset!", NamedTextColor.GREEN));
  }

  public void addScore(Player player, int delta) {
    if (!gameIsActive()) return;
    if (objective == null) return;

    Score current = objective.getScore(player.getName());
    int newScore = current.getScore() + delta;

    if (newScore < 0) return;

    scoreManager.setScore(player.getUniqueId(), newScore);
    objective.getScore(player.getName()).setScore(newScore);
  }

  public void resetPlayerScore(CommandSender sender, OfflinePlayer player) {
    scoreManager.resetPlayer(player.getUniqueId());

    saveScoresToFile();
    loadScoresIntoScoreboard();

    sender.sendMessage(
        Component.text("Score reset for player: " + player.getName(), NamedTextColor.GREEN));
  }
}
