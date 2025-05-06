/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle;

import java.io.File;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ParticipantManager {
  private static final String PARTICIPANTS_CONFIG_NAME = "participants.yml";

  private final EggBattle plugin;
  private final File participantsFile;
  private final FileConfiguration participantsConfig;
  private static final Map<UUID, OfflinePlayer> participants = new HashMap<>();

  public ParticipantManager(EggBattle plugin) {
    this.plugin = plugin;

    this.participantsFile = new File(plugin.getDataFolder(), PARTICIPANTS_CONFIG_NAME);
    this.participantsConfig = YamlConfiguration.loadConfiguration(participantsFile);

    loadParticipantsFromFile();
  }

  public void loadParticipantsFromFile() {
    List<String> idList = participantsConfig.getStringList("participants");

    // Loop over participants and add them to the map
    for (String id : idList) {
      try {
        UUID uuid = UUID.fromString(id);
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        participants.put(uuid, player);
      } catch (IllegalArgumentException e) {
        plugin.getLogger().warning("Invalid UUID found in participants.yml: " + id);
      }
    }
  }

  public void saveParticipantsToFile() {
    // Save all participants
    participantsConfig.set(
        "participants", participants.keySet().stream().map(UUID::toString).toList());

    try {
      participantsConfig.save(participantsFile);
    } catch (Exception e) {
      plugin.getLogger().severe("Failed to save participants to file: " + e.getMessage());
    }
  }

  public void addParticipant(Player player) {
    participants.put(player.getUniqueId(), player);
    saveParticipantsToFile();
  }

  public void removeParticipant(Player player) {
    participants.remove(player.getUniqueId());
    saveParticipantsToFile();
  }

  public void removeAllParticipants() {
    participants.clear();
    saveParticipantsToFile();
  }

  public boolean isParticipant(UUID uuid) {
    return participants.containsKey(uuid);
  }
}
