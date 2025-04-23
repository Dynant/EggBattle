/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle.commands;

import dev.dynant.eggBattle.EggBattle;
import dev.dynant.eggBattle.EggBattleManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;

@SuppressWarnings("unused")
@CommandContainer
@CommandDescription("Main command for the Egg Battle plugin")
public class EggBattleCommand {
  public static EggBattleManager manager = EggBattle.gameManager;

  @Command("eggbattle start")
  @Permission("eggbattle.admin")
  public void startCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.startGame(sender);
  }

  @Command("eggbattle stop")
  @Permission("eggbattle.admin")
  public void endCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.stopGame(sender);
  }

  @Command("eggbattle setup")
  @Permission("eggbattle.admin")
  public void setupCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.setupGame(sender);
  }

  @Command("eggbattle reset")
  @Permission("eggbattle.admin")
  public void resetCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.resetGame(sender);
  }

  @Command("eggbattle broadcast_explanation")
  @Permission("eggbattle.admin")
  public void broadcastExplanationCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.broadcastExplanation();
  }

  @Command("eggbattle broadcast_end")
  @Permission("eggbattle.admin")
  public void broadcastEndCommand(CommandSourceStack sourceStack) {
    manager.broadcastEnd();
  }

  @Command("eggbattle resetscores")
  @Permission("eggbattle.admin")
  public void resetScoresCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.resetScores(sender);
  }

  @Command("eggbattle resetplayer <player>")
  @Permission("eggbattle.admin")
  public void resetPlayerScoreCommand(
      CommandSourceStack sourceStack,
      @Argument(value = "player", description = "Player to reset score from")
          OfflinePlayer player) {
    CommandSender sender = sourceStack.getSender();
    manager.resetPlayerScore(sender, player);
  }

  @Command("eggbattle status")
  @Permission("eggbattle.admin")
  public void statusCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.showStatus(sender);
  }

  @Command("eggbattle top")
  @Permission("eggbattle.admin")
  public void topCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.getTopPlayers(sender);
  }

  @Command("eggbattle join")
  public void joinCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    if (!(sender instanceof Player player)) return;

    manager.addPlayer(player);
  }

  @Command("eggbattle leave")
  public void leaveCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    if (!(sender instanceof Player player)) return;

    manager.removePlayer(player);
  }
}
