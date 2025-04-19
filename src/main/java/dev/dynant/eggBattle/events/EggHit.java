/* Licensed under GNU General Public License v3.0 */
package dev.dynant.eggBattle.events;

import dev.dynant.eggBattle.EggBattle;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class EggHit implements Listener {
  @EventHandler
  public void onEggHit(ProjectileHitEvent event) {
    if (!EggBattle.gameManager.gameIsActive()) return;

    Entity entity = event.getEntity();
    Entity hitEntity = event.getHitEntity();

    if (!(entity instanceof Egg egg)) return;
    if (!(hitEntity instanceof Player target)) return;

    ProjectileSource shooter = egg.getShooter();
    if (!(shooter instanceof Player thrower)) return;

    // Make sure the thrower and target are not the same player
    if (thrower.equals(target)) return;

    // Update scores for thrower and target
    EggBattle.gameManager.addScore(thrower, 1);
    EggBattle.gameManager.addScore(target, -1);

    // Spawn particles near target on hit
    target
        .getWorld()
        .spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);

    // Player sound for the thrower when hitting
    thrower.playSound(thrower.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.7f, 1.5f);
    // Play sound for the target player when hit
    target.playSound(target.getLocation(), Sound.ENTITY_TURTLE_EGG_CRACK, 0.4f, 1.5f);

    // Apply knock back to the target player
    double knockBackStrength = 0.25;
    target.setVelocity(
        target
            .getLocation()
            .subtract(thrower.getLocation())
            .toVector()
            .normalize()
            .multiply(knockBackStrength));
  }
}
