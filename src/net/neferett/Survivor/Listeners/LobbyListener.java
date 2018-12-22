package net.neferett.Survivor.Listeners;

import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Timers.GameTimer;
import net.neferett.Survivor.Utils.Data;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.Utils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyListener
implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);
        Utils.resetPlayer(player);
        if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
            player.teleport(Data.getInstance().getLobby());
            if (Bukkit.getOnlinePlayers().size() >= Survivor.getInt("player-min")) {
                if (GameTimer.getInstance().getTime() > (long)Survivor.getInt("lobby-time")) {
                    GameTimer.getInstance().setTime(Survivor.getInt("lobby-time"));
                }
            } else {
                GameTimer.getInstance().setTime(9999);
            }
        } else {
            player.teleport(Data.getInstance().getSpawn());
            if (GameTimer.isRound()) {
                plPlayer.setPlayerState(PlPlayer.PlayerState.DIE);
            }
        }
        Utils.m_playersJustConnected.add(player);
        Utils.setInventory(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayersManager.getInstance().removePlayer(event.getPlayer());
        if (Survivor.getGameState() != Survivor.GameState.LOBBY) {
            if (Bukkit.getOnlinePlayers().size() - 1 <= 0) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)"stop");
            }
            return;
        }
        if (Bukkit.getOnlinePlayers().size() - 1 >= Survivor.getInt("player-min")) {
            if (GameTimer.getInstance().getTime() > (long)Survivor.getInt("lobby-time")) {
                GameTimer.getInstance().setTime(Survivor.getInt("lobby-time"));
            }
        } else {
            GameTimer.getInstance().setTime(9999);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = player.getItemInHand();
        if (inHand == null || !inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName()) {
            return;
        }
        if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
        	
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
            Player player = (Player)event.getWhoClicked();
            Inventory inv = event.getInventory();
            event.setCancelled(true);
            player.closeInventory();
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (Survivor.getGameState() != Survivor.GameState.GAME) {
            if (event.getFoodLevel() < 20) {
                event.setFoodLevel(20);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (Survivor.getGameState() != Survivor.GameState.GAME) {
            event.setCancelled(true);
        }
    }
}

