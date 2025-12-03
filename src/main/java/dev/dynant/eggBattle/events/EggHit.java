/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle.events;

import dev.dynant.eggBattle.EggBattle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EggHit implements Listener {
  @EventHandler
  public void onEggHit(ProjectileHitEvent event) {
    if (!EggBattle.gameManager.gameIsActive()) return;
    if (!(event.getEntity() instanceof Projectile projectile)) return;
    if (!(event.getHitEntity() instanceof Player target)) return;
    if (!(projectile.getShooter() instanceof Player thrower)) return;

    // Validate projectile type matches config, default is EGG
    String configType =
        EggBattle.plugin.getConfig().getString("projectile_type", "EGG").toUpperCase();
    if (!event.getEntity().getType().name().equals(configType)) return;

    // Check if the thrower and target are the same player
    if (thrower.equals(target)) return;

    // Check if the thrower and target are in the game
    if (!EggBattle.gameManager.isPlayerInGame(thrower.getUniqueId())) return;
    if (!EggBattle.gameManager.isPlayerInGame(target.getUniqueId())) return;

    // Update scores
    EggBattle.gameManager.addScore(thrower, 1);
    EggBattle.gameManager.addScore(target, -1);

    // Apply effects
    spawnParticle(target);
    playSound(thrower, "effects.thrower_sound", "entity.chicken.egg", 0.7f, 1.5f);
    playSound(target, "effects.target_sound", "entity.turtle.egg_crack", 0.4f, 1.5f);

    // Apply knockback
    double knockBackStrength = 0.25;
    target.setVelocity(
        target
            .getLocation()
            .subtract(thrower.getLocation())
            .toVector()
            .normalize()
            .multiply(knockBackStrength));
  }

  private void spawnParticle(Player target) {
    String particleName = EggBattle.plugin.getConfig().getString("effects.particle", "CRIT");
    try {
      Particle particle = Particle.valueOf(particleName);
      target
          .getWorld()
          .spawnParticle(particle, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
    } catch (IllegalArgumentException e) {
      target
          .getWorld()
          .spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
    }
  }

  private void playSound(
      Player player, String configPath, String defaultSound, float volume, float pitch) {
    String soundName = EggBattle.plugin.getConfig().getString(configPath, defaultSound);
    try {
      Key key = Key.key("minecraft:" + soundName);
      Sound sound = Sound.sound(key, Source.PLAYER, volume, pitch);
      var loc = player.getLocation();
      player.playSound(sound, loc.x(), loc.y(), loc.z());
    } catch (Exception ignored) {
      EggBattle.plugin.getLogger().warning("Invalid sound: " + soundName);
      // Silently fail for invalid sounds
    }
  }
}
