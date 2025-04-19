/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle.commands;

import dev.dynant.eggBattle.EggBattleManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;

@SuppressWarnings("unused")
@CommandContainer
public class EggBattleCommand {
  private EggBattleManager manager;

  public EggBattleCommand() {
    // No-args constructor required by @CommandContainer
  }

  public EggBattleCommand(EggBattleManager manager) {
    this.manager = manager;
  }

  @Command("eggbattle start")
  @CommandDescription("Start the Egg Battle")
  @Permission("eggbattle.admin")
  public void startCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.startGame(sender);
  }

  @Command("eggbattle pause")
  @CommandDescription("Pause the Egg Battle")
  @Permission("eggbattle.admin")
  public void pauseCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.pauseGame(sender);
  }

  @Command("eggbattle stop")
  @CommandDescription("Stop the Egg Battle")
  @Permission("eggbattle.admin")
  public void endCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.stopGame(sender);
  }

  @Command("eggbattle reset")
  @CommandDescription("Reset the Egg Battle")
  @Permission("eggbattle.admin")
  public void resetCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.resetGame(sender);
  }

  @Command("eggbattle resetscores")
  @CommandDescription("Reset the Egg Battle scores")
  @Permission("eggbattle.admin")
  public void resetScoresCommand(CommandSourceStack sourceStack) {
    CommandSender sender = sourceStack.getSender();
    manager.resetScores(sender);
  }

  @Command("eggbattle resetplayer <player>")
  @CommandDescription("Reset the score for a specific player")
  @Permission("eggbattle.admin")
  public void resetPlayerScoreCommand(
      CommandSourceStack sourceStack,
      @Argument(value = "player", description = "Player to reset score from")
          OfflinePlayer player) {
    CommandSender sender = sourceStack.getSender();
    manager.resetPlayerScore(sender, player);
  }
}
