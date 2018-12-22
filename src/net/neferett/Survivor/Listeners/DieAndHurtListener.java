package net.neferett.Survivor.Listeners;

import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.Utils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public class DieAndHurtListener
implements Listener {
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
        if (this.isDieOrHurt(player)) {
            event.setCancelled(true);
        }
        if (tplayer.getPlayerState() != PlPlayer.PlayerState.DIE) {
            return;
        }
        try {
            ItemStack is = event.getItem();
            if (is == null) {
                return;
            }
            if (is.getType() == Material.COMPASS) {
                player.closeInventory();
                this.openInvSpectatorTp(tplayer);
            } else if (is.getType() == Material.BED) {
                Utils.tpToLobby(player);
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        PlPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
        if (tplayer.getPlayerState() != PlPlayer.PlayerState.DIE) {
            return;
        }
        try {
            ItemStack is = event.getCurrentItem();
            if (is.getType() == Material.COMPASS) {
                player.closeInventory();
                this.openInvSpectatorTp(tplayer);
            } else if (is.getType() == Material.BED) {
                Utils.tpToLobby(player);
            } else if (is.getType() == Material.SKULL_ITEM) {
                SkullMeta meta = (SkullMeta)is.getItemMeta();
                Player to = Bukkit.getPlayerExact((String)meta.getOwner());
                if (to != null) {
                    player.teleport((Entity)to);
                }
                player.closeInventory();
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void openInvSpectatorTp(PlPlayer tplayer) {
        final Player player = tplayer.getPlayer();
        final Inventory inv = Bukkit.createInventory((InventoryHolder)player, (int)45, (String)Lang.get("INV_SPECTATOR_TP"));
        int i = 0;
        for (PlPlayer a_tplayer : PlayersManager.getInstance().getGamers()) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM);
            skull.setDurability((short) 3);
            SkullMeta meta = (SkullMeta)skull.getItemMeta();
            meta.setDisplayName(a_tplayer.getPlayer().getName());
            meta.setOwner(a_tplayer.getPlayer().getName());
            skull.setItemMeta((ItemMeta)meta);
            inv.setItem(i++, skull);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)Survivor.getInstance(), new Runnable(){

            @Override
            public void run() {
                player.openInventory(inv);
            }
        }, 10);
    }

    public boolean isDie(Player player) {
        if (PlayersManager.getInstance().getPlayer(player).getPlayerState() == PlPlayer.PlayerState.DIE) {
            return true;
        }
        return false;
    }

    public boolean isHurt(Player player) {
        PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);
        if (plPlayer.getPlayerState() != PlPlayer.PlayerState.HURT && plPlayer.getPlayerState() != PlPlayer.PlayerState.CURE) {
            return false;
        }
        return true;
    }

    public boolean isDieOrHurt(Player player) {
        if (!this.isDie(player) && !this.isHurt(player)) {
            return false;
        }
        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!this.isHurt(event.getPlayer())) {
            return;
        }
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            Location loc = event.getFrom();
            loc.setPitch(event.getTo().getPitch());
            loc.setYaw(event.getTo().getYaw());
            event.getPlayer().teleport(loc);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && this.isDie((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (this.isDieOrHurt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (this.isDieOrHurt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        if (this.isDieOrHurt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.isDieOrHurt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.isDieOrHurt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (this.isDieOrHurt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && this.isDieOrHurt((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && this.isDieOrHurt((Player)event.getDamager())) {
            event.setCancelled(true);
        }
    }

}

