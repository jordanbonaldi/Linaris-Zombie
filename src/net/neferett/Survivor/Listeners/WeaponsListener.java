package net.neferett.Survivor.Listeners;

import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Managers.WeaponsManager;
import net.neferett.Survivor.Utils.Utils;
import net.neferett.Survivor.Weapons.GunBullet;
import net.neferett.Survivor.Weapons.GunInstance;
import net.neferett.Survivor.Weapons.Templates.ActivableWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.LaunchWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.Metadatable;

public class WeaponsListener
implements Listener {
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        if (event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.WORKBENCH || event.getClickedBlock().getType() == Material.CAKE_BLOCK || event.getClickedBlock().getType() == Material.DIAMOND_ORE || event.getClickedBlock().getType() == Material.ENDER_CHEST)) {
            return;
        }
        Player player = event.getPlayer();
        PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);
        if (plPlayer.getPlayerState() == PlPlayer.PlayerState.DIE || plPlayer.getPlayerState() == PlPlayer.PlayerState.HURT) {
            return;
        }
        WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(event.getItem().getType());
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (weapon instanceof GunWeaponTmpl) {
                int slot = player.getInventory().getHeldItemSlot();
                GunInstance gun = plPlayer.getGun(slot);
                if (gun.shot()) {
                    gun.getBullet().decrementBullet(1);
                    if (gun.getBullet().getBullet() <= 0 && gun.getBullet().getCharger() <= 0 && !gun.getGun().isMainWeapon()) {
                        player.getInventory().setItem(slot, null);
                        plPlayer.deleteGun(slot);
                    } else {
                        plPlayer.updateBullet(slot);
                    }
                }
            } else if (weapon instanceof LaunchWeaponTmpl) {
                LaunchWeaponTmpl launch = (LaunchWeaponTmpl)weapon;
                launch.onRightClick(event);
            } else if (weapon instanceof ActivableWeaponTmpl) {
                ActivableWeaponTmpl activable = (ActivableWeaponTmpl)weapon;
                activable.onRightClick(event);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        try {
            if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile)event.getDamager();
                if (projectile.hasMetadata("weapon")) {
                    GunInstance weapon = (GunInstance)Utils.getMetadata((Metadatable)projectile, "weapon");
                    if (weapon.getEntity().getType() == event.getEntityType()) {
                        event.setCancelled(true);
                        return;
                    }
                    int rate = 1;
                    if (event.getEntityType() == EntityType.ZOMBIE) {
                        double distance;
                        Zombie zombie = (Zombie)event.getEntity();
                        if (zombie.getEquipment().getChestplate().getType() == Material.CHAINMAIL_CHESTPLATE) {
                            Location loc = zombie.getLocation();
                            TNTPrimed tnt = (TNTPrimed)loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                            tnt.setFuseTicks(0);
                            rate = 100;
                        }
                        if ((distance = Math.abs(zombie.getEyeLocation().getY() - projectile.getLocation().getY())) < 0.3 && !zombie.isVillager()) {
                            EntityEquipment stuff = zombie.getEquipment();
                            stuff.setHelmet(new ItemStack(Material.AIR));
                            zombie.setVillager(true);
                            rate = 2;
                        }
                    }
                    event.setDamage(weapon.getGunLevel().getDamage() * (double)rate);
                }
            } else if (event.getDamager() instanceof TNTPrimed && event.getDamager().hasMetadata("oneshot")) {
                event.setDamage(100.0);
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);
        if (plPlayer.isRechargement()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);
        WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(event.getItemDrop().getItemStack().getType());
        if (weapon instanceof GunWeaponTmpl) {
            if (plPlayer.isRechargement()) {
                return;
            }
            GunInstance gun = plPlayer.getGun(player.getInventory().getHeldItemSlot());
            if (gun.getBullet().getCharger() != 0) {
                plPlayer.setRechargement(event.getItemDrop().getItemStack());
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.PRIMED_TNT) {
            event.blockList().clear();
        }
    }
}

