
package net.neferett.Survivor.Timers;

import net.neferett.Survivor.Managers.EntitiesManager;
import net.neferett.Survivor.Managers.FenceManager;
import net.neferett.Survivor.Managers.MapManager;
import net.neferett.Survivor.Managers.PlScoreboardManager;
import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Data;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.RoundInfo;
import net.neferett.Survivor.Utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.neferett.linaris.BukkitAPI;
import net.neferett.linaris.api.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class GameTimer {
    private int m_round = 0;
    private boolean m_isRound = false;
    int m_tick = 0;
    private static GameTimer instance;
    private List<GameTask> m_tasks = new LinkedList<GameTask>();
    private int taskId = -1;
    private long m_time = 9999;

    public static GameTimer getInstance() {
        if (instance == null) {
            instance = new GameTimer();
        }
        return instance;
    }

    private GameTimer() {
    }

    public void start() {
        if (this.taskId != -1) {
            return;
        }
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)Survivor.getInstance(), new Runnable(){

            @Override
            public void run() {
                int i = 0;
                while (i < GameTimer.this.m_tasks.size()) {
                    Object task;
                    Object object = task = (GameTask)GameTimer.this.m_tasks.get(i);
                    long l = ((GameTask)object).m_time;
                    GameTask.setGameTime((GameTask)object, l - 1);
                    if (l <= 0) {
                        ((GameTask)task).m_task.run();
                        Object object2 = task;
                        GameTask.setGameTime((GameTask)object2, ((GameTask)object2).m_repeat - 1);
                        if (((GameTask)task).m_repeat < 0) {
                            GameTimer.this.m_tasks.remove(i);
                            --i;
                        } else {
                            GameTask.setGameTime((GameTask)task, ((GameTask)task).m_timeBase);
                        }
                    }
                    ++i;
                }
                for (PlPlayer player : PlayersManager.getInstance().getPlayers()) {
                    player.tick();
                }
                EntitiesManager.getInstance().tick();
                ++GameTimer.this.m_tick;
                if (GameTimer.this.m_tick % 20 != 0) {
                    return;
                }
                int secondes = (int)(GameTimer.this.m_time % 60);
                if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
                    Location loc = Data.getInstance().getLobby();
                    loc = Survivor.getGameState() == Survivor.GameState.LOBBY ? Data.getInstance().getLobby() : Data.getInstance().getSpawn();
                    for (Player player2 : Utils.m_playersJustConnected) {
                        if (player2.getLocation().getWorld().getUID().equals(loc.getWorld().getUID()) && player2.getLocation().distance(loc) <= 15.0) continue;
                        player2.teleport(loc);
                    }
                    Utils.m_playersJustConnected.clear();
     
                    if (GameTimer.this.m_time <= 0) {
                        if (PlayersManager.getInstance().getPlayers().size() >= Survivor.getInt("player-min")) {
                            GameTimer gameTimer = GameTimer.this;
                            GameTimer.setGameRound(gameTimer, gameTimer.m_round + 1);
                            GameTimer.setGameTime(GameTimer.this, Survivor.getInt("preparation-time"));
                            Survivor.getInstance().setGameState(Survivor.GameState.GAME);

                    		BukkitAPI.get().getTasksManager().addTask(() -> {
                    			Survivor.getInfos().setCanJoin(true, false);
                    			Survivor.getInfos().setCanSee(true, true);
                    		}); 
                    		} else {
                            GameTimer.setGameTime(GameTimer.this, 9999);
                        }
                    }
                } else if (Survivor.getGameState() == Survivor.GameState.GAME) {
                    if (GameTimer.isRound()) {
                        RoundInfo.getTheRoundInfo().updateEntityAlive();
                    }
                    for (Entity e : Survivor.getWorldGame().getEntities()) {
                        if (e.getType() != EntityType.PIG_ZOMBIE || GameTimer.this.m_tick % 600 != 0 || GameTimer.this.m_round != 10) continue;
                        int i2 = 0;
                        while (i2 < 5) {
                            Zombie zombie = (Zombie)e.getWorld().spawnEntity(e.getLocation(), EntityType.ZOMBIE);
                            zombie.setVillager(false);
                            zombie.getEquipment().clear();
                            ++i2;
                        }
                        RoundInfo.getTheRoundInfo().addEntitiesAlives(5);
                    }
                    if (GameTimer.this.m_time < 0) {
                        GameTimer.setGameTime(GameTimer.this, Long.MAX_VALUE);
                        GameTimer.setGameRound(GameTimer.this, true);
                        RoundInfo.getTheRoundInfo().startRound();
                        List<Location> spawners = MapManager.getInstance().getAvailableSpawners();
                        RoundInfo info = RoundInfo.getRoundInfo(GameTimer.this.m_round);
                        int nb_zombie = info.getEntityAmount();
                        Random rand = new Random();
                        block5 : while (nb_zombie > 0) {
                            int zombie_per_spawner = Math.max(nb_zombie / spawners.size(), 1);
                            int i3 = 0;
                            while (i3 < spawners.size()) {
                                Player nearest = Utils.getNearestPlayer(spawners.get(i3));
                                Location spawner = spawners.get(i3);
                                int j = 0;
                                while (j < zombie_per_spawner) {
                                    LivingEntity entity = (LivingEntity)spawner.getWorld().spawnEntity(spawner, info.getEntityType());
                                    entity.setMaxHealth(info.getEntityHealth());
                                    entity.setHealth(info.getEntityHealth());
                                    Creature creature = (Creature)entity;
                                    creature.setTarget((LivingEntity)nearest);
                                    if (entity instanceof Wolf) {
                                        Wolf wolf = (Wolf)entity;
                                        wolf.setAngry(true);
                                    } else if (entity instanceof Zombie) {
                                        Zombie zombie = (Zombie)entity;
                                        zombie.setVillager(false);
                                        EntityEquipment stuff = zombie.getEquipment();
                                        int next_zombie_type = rand.nextInt(40);
                                        if (next_zombie_type == 0) {
                                            stuff.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                                            stuff.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                                            stuff.setChestplate(new ItemStack(Material.LEATHER_BOOTS));
                                            stuff.setItemInHand(new ItemStack(Material.WOOD_HOE));
                                            EntitiesManager.getInstance().addEntity((LivingEntity)zombie);
                                        } else if (next_zombie_type == 1) {
                                            stuff.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                                        }
                                    }
                                    if (--nb_zombie <= 0) break block5;
                                    ++j;
                                }
                                ++i3;
                            }
                        }
                        if (GameTimer.this.m_round % 10 == 0) {
                            info.addEntitiesAlives(1);
                            Location loc = Data.getInstance().getCentre();
                            PigZombie boss = (PigZombie)loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);
                            boss.setBaby(false);
                            boss.setMaxHealth(500.0);
                            boss.setHealth(500.0);
                            boss.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                            Utils.setBossBar("Boss", 100.0f);
                        }
                        Bukkit.broadcastMessage((String)Lang.get("START_ROUND").replaceAll("<round>", Integer.toString(GameTimer.this.m_round)));
                    }
                } else if (Survivor.getGameState() == Survivor.GameState.END) {
                    if (GameTimer.this.m_time <= 0) {
                        GameTimer.setGameTime(GameTimer.this, 3);
                        Survivor.getInstance().setGameState(Survivor.GameState.RESTART);
                        return;
                    }
                } else if (Survivor.getGameState() == Survivor.GameState.RESTART && GameTimer.this.m_time <= 0) {
                    Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)"stop");
                }
                for (PlPlayer player3 : PlayersManager.getInstance().getPlayers()) {
                    player3.updatePlayer();
                }
                PlScoreboardManager.getInstance().updateScoreboard(GameTimer.this.m_time);
                FenceManager.getInstance().update();
                GameTimer gameTimer = GameTimer.this;
                GameTimer.setGameTime(gameTimer, gameTimer.m_time - 1);
                Data.getInstance().getLobby().getWorld().setTime(5000);
                Survivor.getWorldGame().setTime(18000);
            }
        }, 20, 1);
    }

    public long getTime() {
        return this.m_time;
    }

    public void setTime(long time) {
        this.m_time = time;
    }

    public static boolean isRound() {
        return GameTimer.instance.m_isRound;
    }

    public static int getRound() {
        return GameTimer.instance.m_round;
    }

    public void endRound() {
    	for(Player p : Bukkit.getOnlinePlayers()){
          	BukkitAPI.get().getTasksManager().addTask(() -> {
				PlayerData data = BukkitAPI.get().getPlayerDataManager().getPlayerData(p.getName());
				data.creditCoins(0.5, "Round termin�", true, null);
			}); 
    	}
        Bukkit.broadcastMessage((String)Lang.get("END_ROUND").replaceAll("<round>", Integer.toString(this.m_round)));
        ++this.m_round;
        if (this.m_round > Survivor.getInt("last-round")) {
            Survivor.getInstance().endGame(true);
            return;
        }
        for (PlPlayer player : PlayersManager.getInstance().getPlayers()) {
            player.addMoney(Survivor.getInt("money-by-round"));
            player.startRound();
        }
        this.m_isRound = false;
        this.m_time = Survivor.getInt("preparation-time");
    }

    public void addTask(Runnable task, long time, int repeat) {
        this.m_tasks.add(new GameTask(task, time, repeat));
    }

    static  void setGameRound(GameTimer gameTimer, int n) {
        gameTimer.m_round = n;
    }

    static  void setGameTime(GameTimer gameTimer, long l) {
        gameTimer.m_time = l;
    }

    static  void setGameRound(GameTimer gameTimer, boolean bl) {
        gameTimer.m_isRound = bl;
    }

    public static class GameTask {
        private Runnable m_task;
        private long m_time;
        private long m_timeBase;
        private int m_repeat;

        public GameTask(Runnable task, long time, int repeat) {
            this.m_task = task;
            this.m_time = time;
            this.m_timeBase = time;
            this.m_repeat = repeat;
        }

        static  void setGameTime(GameTask gameTask, long l) {
            gameTask.m_time = l;
        }

        static  void setGameTime(GameTask gameTask, int n) {
            gameTask.m_repeat = n;
        }
    }

}

