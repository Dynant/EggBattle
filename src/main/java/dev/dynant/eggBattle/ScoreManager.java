/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreManager {
  private static final String SCORES_CONFIG_NAME = "scores.yml";
  private static final long SAVE_INTERVAL = 20L * 60; // 60 seconds

  private final EggBattle plugin;
  private final File scoresFile;
  private final FileConfiguration scoresConfig;
  private BukkitRunnable saveTask;

  private final Map<UUID, Integer> scores = new HashMap<>();

  public ScoreManager(EggBattle plugin, boolean gameIsActive) {
    this.plugin = plugin;

    this.scoresFile = new File(plugin.getDataFolder(), SCORES_CONFIG_NAME);
    this.scoresConfig = YamlConfiguration.loadConfiguration(scoresFile);

    loadScoresFromFile();

    if (gameIsActive) {
      startSaveTask();
    }
  }

  public void startSaveTask() {
    // Cancel existing task if it exists
    if (saveTask != null) saveTask.cancel();

    // Save scores every 60 seconds
    saveTask =
        new BukkitRunnable() {
          @Override
          public void run() {
            saveScoresToFile();
          }
        };
    saveTask.runTaskTimer(plugin, SAVE_INTERVAL, SAVE_INTERVAL);
  }

  public void stopSaveTask() {
    if (saveTask != null) {
      saveTask.cancel();
      saveTask = null;
    }
  }

  private void loadScoresFromFile() {
    for (String key : scoresConfig.getKeys(false)) {
      try {
        UUID uuid = UUID.fromString(key);
        int score = scoresConfig.getInt(key);

        scores.put(uuid, score);

      } catch (IllegalArgumentException e) {
        plugin.getLogger().warning("Invalid UUID found in scores.yml: " + key);
      }
    }
  }

  public void saveScoresToFile() {
    plugin.getLogger().info("Saving scores to " + SCORES_CONFIG_NAME);

    // Cleanup existing scores
    for (String key : scoresConfig.getKeys(false)) {
      scoresConfig.set(key, null);
    }

    for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
      scoresConfig.set(entry.getKey().toString(), entry.getValue());
    }

    try {
      scoresConfig.save(scoresFile);
    } catch (IOException e) {
      plugin.getLogger().severe("Failed to save scores to file: " + e.getMessage());
    }
  }

  public void setScore(UUID uuid, int score) {
    scores.put(uuid, score);
  }

  public Map<UUID, Integer> getAllScores() {
    return scores;
  }

  public void resetAll() {
    scores.clear();
    saveScoresToFile();
  }

  public void resetPlayer(UUID uuid) {
    scores.remove(uuid);
    saveScoresToFile();
  }
}
