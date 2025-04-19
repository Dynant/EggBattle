/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle;

import dev.dynant.eggBattle.commands.EggBattleCommand;
import dev.dynant.eggBattle.events.EggHit;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

public final class EggBattle extends JavaPlugin {

  public static EggBattle plugin;
  public static EggBattleManager gameManager;

  @Override
  public void onEnable() {
    plugin = this;

    // Initialize managers
    gameManager = new EggBattleManager(this);

    saveDefaultConfig();

    // Register commands
    try {
      PaperCommandManager<CommandSourceStack> commandManager =
          PaperCommandManager.builder()
              .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
              .buildOnEnable(this);

      AnnotationParser<CommandSourceStack> annotationParser =
          new AnnotationParser(commandManager, CommandSourceStack.class);

      annotationParser.parse(new EggBattleCommand(gameManager));

    } catch (Exception exc) {
      getLogger().warning("Failed to parse command containers. Commands will not work!");
      exc.printStackTrace();
    }

    // Register events
    getServer().getPluginManager().registerEvents(new EggHit(), this);

    getLogger().info("EggBattle Enabled!");
  }

  @Override
  public void onDisable() {
    gameManager.saveScoresToFile();

    // Plugin shutdown logic
    getLogger().info("EggBattle Disabled!");
  }
}
