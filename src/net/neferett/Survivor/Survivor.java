
package net.neferett.Survivor;

import net.neferett.Survivor.Commands.CommandSurvivor;
import net.neferett.Survivor.Listeners.*;
import net.neferett.Survivor.Managers.MapManager;
import net.neferett.Survivor.Managers.PlScoreboardManager;
import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Timers.GameTimer;
import net.neferett.Survivor.Utils.*;
import net.neferett.linaris.BukkitAPI;
import net.neferett.linaris.api.PlayerData;
import net.neferett.linaris.api.ServerInfo;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

public class Survivor
extends JavaPlugin {
    private World m_worldGame;
    private GameState m_gameState;
    private long m_mortInstantEnd = 0;
    private static Survivor instance;

    public static Survivor getInstance() {
        return instance;
    }

    private static ServerInfo infos;

    
    
    public void onLoad(){
        
 		infos = BukkitAPI.get().getServerInfos();
 		infos.setGameName("Survivor");
 		infos.setMapName("Bunker");
    }

    
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.reloadConfig();
        File srcDir = new File("world_game");
        File destDir = new File("world_in_progress");
        FileUtils.deleteDirectory(destDir);
        FileUtils.copyDirectory(srcDir, destDir);
        WorldCreator options = new WorldCreator("world_in_progress");
        this.m_worldGame = Bukkit.createWorld((WorldCreator)options);
        this.m_worldGame.setDifficulty(Difficulty.HARD);
        this.m_worldGame.setMonsterSpawnLimit(250);
        Data.getInstance();
        Lang.init();        
        
        this.m_gameState = Survivor.getBoolean("config-mode") ? GameState.CONFIG : GameState.LOBBY;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.PLAYER) continue;
                entity.remove();
            }
        }
        PluginManager pm = this.getServer().getPluginManager();
        if (!Survivor.getBoolean("config-mode")) {
            pm.registerEvents((Listener)new LobbyListener(), (Plugin)this);
            pm.registerEvents((Listener)new ChatListener(), (Plugin)this);
            pm.registerEvents((Listener)new DieAndHurtListener(), (Plugin)this);
            pm.registerEvents((Listener)new GameListener(), (Plugin)this);
            pm.registerEvents((Listener)new WeaponsListener(), (Plugin)this);
            MapManager.getInstance();
        } else {
            ConfigHelper.getInstance();
        }
        pm.registerEvents((Listener)new GlobalListener(), (Plugin)this);
        pm.registerEvents((Listener)new WorldListener(), (Plugin)this);
        GameTimer.getInstance().start();
        this.getCommand("survivor").setExecutor((CommandExecutor)new CommandSurvivor());
        

		BukkitAPI.get().getTasksManager().addTask(() -> {
			infos.setCanJoin(true, false);
			infos.setCanSee(true, true);
		});
    }

    public static GameState getGameState() {
        return Survivor.instance.m_gameState;
    }

    public void setGameState(GameState gameState) {
        this.m_gameState = gameState;
        if (this.m_gameState == GameState.GAME) {
    		BukkitAPI.get().getTasksManager().addTask(() -> {
    			infos.setCanJoin(false, false);
    			infos.setCanSee(false, false);
    		});
            for (PlPlayer tplayer : PlayersManager.getInstance().getPlayers()) {
                Utils.resetPlayer(tplayer.getPlayer());
                Utils.setInventory(tplayer.getPlayer());
                tplayer.getPlayer().teleport(Data.getInstance().getSpawn());
                PlScoreboardManager.getInstance().refreshPlayer(tplayer);
            }
            Bukkit.broadcastMessage((String)Lang.get("START_GAME"));
        } else if (this.m_gameState == GameState.RESTART) {
            for (Player player : Bukkit.getOnlinePlayers()) {
            	net.neferett.linaris.utils.PlayerUtils.returnToHub(player);
            }
        }
    }

    public void endGame(boolean win) {
        if (win) {
            Bukkit.broadcastMessage((String)Lang.get("END_GAME_WIN"));
            for(Player p : Bukkit.getOnlinePlayers()){
                
                
                p.setAllowFlight(true);
                p.setFlying(true);
                p.setGameMode(GameMode.SPECTATOR);
                p.setVelocity(new Vector(0, 0.25, 0));
                p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
            }
        } else {
            Bukkit.broadcastMessage((String)Lang.get("END_GAME_LOOSE").replaceAll("<round>", Integer.toString(GameTimer.getRound())));
            for(Player p : Bukkit.getOnlinePlayers()){
	        	
	    		BukkitAPI.get().getTasksManager().addTask(() -> {
    				PlayerData data = BukkitAPI.get().getPlayerDataManager().getPlayerData(p.getName());
    				data.creditCoins(5, "Win", true, null);
    				data.creditLegendaryCoins(1, "Win", true, null);
    			}); 
                                
                p.setAllowFlight(true);
                p.setFlying(true);
                p.setGameMode(GameMode.SPECTATOR);
                p.setVelocity(new Vector(0, 0.25, 0));
                }
        }
        GameTimer.getInstance().setTime(3);
        this.setGameState(GameState.END);
		BukkitAPI.get().getTasksManager().addTask(() -> {
			infos.setCanJoin(false, false);
			infos.setCanSee(false, false);
		});
    }

    public static void setMortInstant() {
        Survivor.getInstance().m_mortInstantEnd = System.currentTimeMillis() + 30000;
    }

    public static boolean isMortInstant() {
        if (Survivor.getInstance().m_mortInstantEnd > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public static World getWorldGame() {
        return Survivor.instance.m_worldGame;
    }

    public static int getInt(String key) {
        return instance.getConfig().getInt(key);
    }

    public static boolean getBoolean(String key) {
        return instance.getConfig().getBoolean(key);
    }

    public static String getString(String key) {
        return instance.getConfig().getString(key);
    }

    public static int getPlayerMax() {
        return Survivor.getInt("player-max");
    }

    public static enum GameState {
        CONFIG,
        LOBBY,
        GAME,
        END,
        RESTART;
        

    }
    
    public static ServerInfo getInfos() {
		return infos;
	}
   
}

