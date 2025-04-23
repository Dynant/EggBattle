/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle;

import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.Nullable;

public class EggBattleManager {
  private static final String OBJECTIVE_NAME = "eggScore";
  private static final String PARTICIPANT_TEAM_NAME = "eggBattleParticipant";
  private static final Component PARTICIPANT_TEAM_PREFIX =
      Component.text("[Egg] ", NamedTextColor.GOLD);
  private static final Component OBJECTIVE_TITLE =
      Component.text()
          .append(Component.text("NV: ", NamedTextColor.LIGHT_PURPLE))
          .append(Component.text("Egg ", NamedTextColor.YELLOW))
          .append(Component.text("Battle", NamedTextColor.AQUA))
          .build();

  private static final String GAME_ACTIVE_CONFIG_KEY = "game_active";

  private final EggBattle plugin;
  private final ScoreManager scoreManager;
  private final ParticipantManager participantManager;

  private final Scoreboard scoreBoard;
  private Objective objective;
  private Team participantTeam;

  public EggBattleManager(EggBattle plugin) {
    this.plugin = plugin;
    this.scoreManager = new ScoreManager(plugin, this.gameIsActive());
    this.participantManager = new ParticipantManager(plugin);

    scoreBoard = Bukkit.getScoreboardManager().getMainScoreboard();

    setupGame(null);
  }

  public boolean gameIsActive() {
    return plugin.getConfig().getBoolean(GAME_ACTIVE_CONFIG_KEY, false);
  }

  public void setGameState(boolean active) {
    plugin.getConfig().set(GAME_ACTIVE_CONFIG_KEY, active);
    plugin.saveConfig();
  }

  // Setup game will create the objective and participant team
  public void setupGame(@Nullable CommandSender sender) {
    getOrCreateObjective();
    getOrCreateParticipantTeam();

    // When game is active, load scores into scoreboard
    if (gameIsActive()) loadScoresIntoScoreboard();

    if (sender != null) {
      sender.sendMessage(Component.text("Game setup!", NamedTextColor.GREEN));
    }
  }

  // Reset game will clear objectives, scoreboard, scores and participants
  public void resetGame(CommandSender sender) {
    setGameState(false);
    scoreManager.stopSaveTask();

    scoreManager.resetAll();
    participantManager.removeAllParticipants();

    if (scoreBoard != null) scoreBoard.clearSlot(DisplaySlot.PLAYER_LIST);

    if (objective != null) {
      objective.unregister();
      objective = null;
    }

    if (participantTeam != null) {
      participantTeam.unregister();
      participantTeam = null;
    }

    sender.sendMessage(Component.text("Game reset!", NamedTextColor.GREEN));
  }

  // Get or create objective to display on scoreboard
  public void getOrCreateObjective() {
    objective = scoreBoard.getObjective(OBJECTIVE_NAME);

    if (objective != null) return;
    // Register new objective to display on scoreboard
    objective = scoreBoard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, OBJECTIVE_TITLE);
    objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
  }

  // Get or create participant team to show if player is in game
  private Team getOrCreateParticipantTeam() {
    participantTeam = scoreBoard.getTeam(PARTICIPANT_TEAM_NAME);

    if (participantTeam == null) {
      participantTeam = scoreBoard.registerNewTeam(PARTICIPANT_TEAM_NAME);
      participantTeam.prefix(PARTICIPANT_TEAM_PREFIX);
    }

    return participantTeam;
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
    // Check if game is setup already
    if (scoreBoard == null || objective == null || participantTeam == null) {
      sender.sendMessage(
          Component.text("Game not setup! run ", NamedTextColor.RED)
              .append(Component.text("/eggbattle setup", NamedTextColor.YELLOW)));
      return;
    }

    setGameState(true);
    scoreManager.startSaveTask();

    sender.sendMessage(Component.text("Game started!", NamedTextColor.GREEN));
  }

  public void stopGame(CommandSender sender) {
    // Set game active to false in config
    setGameState(false);
    scoreManager.stopSaveTask();
    saveScoresToFile();

    sender.sendMessage(Component.text("Game stopped!", NamedTextColor.GREEN));
  }

  public void addScore(Player player, int delta) {
    if (!gameIsActive()) return;
    if (objective == null) return;

    Score current = objective.getScore(player.getName());
    int newScore = current.getScore() + delta;

    scoreManager.setScore(player.getUniqueId(), newScore);
    objective.getScore(player.getName()).setScore(newScore);
  }

  public void resetScores(CommandSender sender) {
    scoreManager.resetAll();
    loadScoresIntoScoreboard();

    sender.sendMessage(Component.text("Scores reset!", NamedTextColor.GREEN));
  }

  public void resetPlayerScore(CommandSender sender, OfflinePlayer player) {
    scoreManager.resetPlayer(player.getUniqueId());
    loadScoresIntoScoreboard();

    sender.sendMessage(
        Component.text("Score reset for player: " + player.getName(), NamedTextColor.GREEN));
  }

  public void addPlayer(Player player) {
    if (!gameIsActive()) return;

    // Check if player is already in the game
    if (isPlayerInGame(player.getUniqueId())) {
      player.sendMessage(getMessage("already_joined"));
      return;
    }

    participantManager.addParticipant(player);
    getOrCreateParticipantTeam().addEntry(player.getName());

    player.sendMessage(getMessage("player_join"));
  }

  public void removePlayer(Player player) {
    // Check if player is in the game
    if (!isPlayerInGame(player.getUniqueId())) {
      player.sendMessage(getMessage("not_joined"));
      return;
    }

    participantManager.removeParticipant(player);
    Team team = scoreBoard.getTeam(PARTICIPANT_TEAM_NAME);
    if (team != null) team.removeEntry(player.getName());

    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    player.sendMessage(getMessage("player_leave"));
  }

  public void showStatus(CommandSender sender) {
    if (gameIsActive()) {
      sender.sendMessage(Component.text("Game is active", NamedTextColor.GREEN));
    } else {
      sender.sendMessage(Component.text("Game is not active!", NamedTextColor.RED));
    }
  }

  private Component getMessage(String path) {
    String message = plugin.getConfig().getString("messages." + path, "");
    return MiniMessage.miniMessage().deserialize(message);
  }

  public void getTopPlayers(CommandSender sender) {
    Map<UUID, Integer> scores = scoreManager.getAllScores();
    List<Map.Entry<UUID, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
    sortedScores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

    Component message = Component.text("==== Top 10 Players ====\n");

    for (int i = 0; i < Math.min(sortedScores.size(), 10); i++) {
      Map.Entry<UUID, Integer> entry = sortedScores.get(i);
      OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());

      String playerName = player.getName();
      if (playerName == null) playerName = "Unknown";

      message =
          message
              .append(Component.text((i + 1) + ". ", NamedTextColor.GOLD))
              .append(Component.text(playerName, NamedTextColor.GREEN))
              .append(Component.text(": ", NamedTextColor.WHITE))
              .append(Component.text(entry.getValue(), NamedTextColor.AQUA))
              .append(Component.text("\n", NamedTextColor.WHITE));
    }

    sender.sendMessage(message);
  }

  public boolean isPlayerInGame(UUID player) {
    return participantManager.isParticipant(player);
  }

  public void broadcastExplanation() {
    plugin.getServer().broadcast(getMessage("game_explanation"));
  }

  public void broadcastEnd() {
    plugin.getServer().broadcast(getMessage("game_end"));
  }
}
