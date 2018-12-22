package net.neferett.Survivor.Listeners;

import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Lang;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class GlobalListener
implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (Survivor.getGameState() == Survivor.GameState.CONFIG) {
            return;
        }
        if (Survivor.getGameState() != Survivor.GameState.LOBBY && Survivor.getGameState() != Survivor.GameState.GAME) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Lang.get("KICK_RESTART"));
        }
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < 20) {
            event.setCancelled(true);
        } else {
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (Survivor.getGameState() != Survivor.GameState.CONFIG) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (Survivor.getGameState() != Survivor.GameState.CONFIG && (Survivor.getGameState() != Survivor.GameState.GAME || event.getBlock().getType() != Material.CAKE_BLOCK)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (Survivor.getGameState() != Survivor.GameState.CONFIG) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (Survivor.getGameState() != Survivor.GameState.CONFIG && event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (Survivor.getGameState() != Survivor.GameState.CONFIG && event.getEntity().getType() == EntityType.ITEM_FRAME) {
            event.setCancelled(true);
        }
    }
}

