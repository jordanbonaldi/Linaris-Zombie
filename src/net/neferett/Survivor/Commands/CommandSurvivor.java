package net.neferett.Survivor.Commands;

import net.neferett.Survivor.Managers.FenceManager;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Timers.GameTimer;
import net.neferett.Survivor.Utils.Data;
import net.neferett.Survivor.Utils.FileUtils;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Gate;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

public class CommandSurvivor implements CommandExecutor,Listener {
    private List<UUID> m_fenceAdd = new LinkedList<UUID>();

    public CommandSurvivor() {
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)Survivor.getInstance());
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player !");
            return true;
        }
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You must be an admin !");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setlobby")) {
                Location l = player.getLocation();
                Data.getInstance().setLobby(l);
                l.getWorld().setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                player.sendMessage(ChatColor.GREEN + "Lobby define !");
                return true;
            }
            if (args[0].equalsIgnoreCase("lobby")) {
                player.teleport(Data.getInstance().getLobby());
                return true;
            }
            if (args[0].equalsIgnoreCase("gameworld")) {
                player.teleport(Survivor.getWorldGame().getSpawnLocation());
                return true;
            }
            if (args[0].equalsIgnoreCase("start")) {
                if (Survivor.getGameState() == Survivor.GameState.CONFIG) {
                    Survivor.getInstance().getConfig().set("config-mode", false);
                    Survivor.getInstance().saveConfig();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer(ChatColor.RED + "Config mode disable, please start the server");
                    }
                    Bukkit.unloadWorld((World)Survivor.getWorldGame(), (boolean)true);
                    File srcDir = new File("world_in_progress");
                    File destDir = new File("world_game");
                    FileUtils.deleteDirectory(destDir);
                    FileUtils.copyDirectory(srcDir, destDir);
                    Bukkit.shutdown();
                    return true;
                }
                if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
                    GameTimer.getInstance().setTime(2);
                    return true;
                }
            } 
        } 
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !this.m_fenceAdd.contains(player.getUniqueId())) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block.getType() != Material.FENCE_GATE) {
            return;
        }
        FenceManager.getInstance().addFence(block.getLocation(), (Gate)block.getState().getData());
        this.m_fenceAdd.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Barriere ajout\u00e9 !");
    }
}

