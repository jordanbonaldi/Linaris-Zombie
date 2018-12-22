
package net.neferett.Survivor.Managers;

import net.neferett.Survivor.Managers.PlScoreboardManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Survivor;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;

public class PlayersManager {
    public static final float HURT_TIME = Survivor.getInt("hurt-time-to-death");
    public static final float CURE_TIME = Survivor.getInt("cure-time");
    private Map<UUID, PlPlayer> m_players = new HashMap<UUID, PlPlayer>();
    private static PlayersManager instance;

    public static PlayersManager getInstance() {
        if (instance == null) {
            instance = new PlayersManager();
        }
        return instance;
    }

    private PlayersManager() {
    }

    public void removePlayer(Player player) {
        PlScoreboardManager.getInstance().removePlayer(player);
        this.m_players.remove(player.getUniqueId());
    }

    public PlPlayer getPlayer(Player player) {
        if (this.m_players.containsKey(player.getUniqueId())) {
            return this.m_players.get(player.getUniqueId());
        }
        final PlPlayer tplayer = new PlPlayer(player);
        this.m_players.put(player.getUniqueId(), tplayer);
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)Survivor.getInstance(), new Runnable(){

            @Override
            public void run() {
                tplayer.getPlayer().setScoreboard(PlScoreboardManager.getInstance().getScoreboard());
            }
        });
        return tplayer;
    }

    public Collection<PlPlayer> getPlayers() {
        return this.m_players.values();
    }

    public List<PlPlayer> getGamers() {
        LinkedList<PlPlayer> players = new LinkedList<PlPlayer>();
        for (PlPlayer player : this.m_players.values()) {
            if (player.getPlayerState() == PlPlayer.PlayerState.DIE) continue;
            players.add(player);
        }
        return players;
    }

    public List<PlPlayer> getSpectators() {
        LinkedList<PlPlayer> players = new LinkedList<PlPlayer>();
        for (PlPlayer player : this.m_players.values()) {
            if (player.getPlayerState() != PlPlayer.PlayerState.DIE) continue;
            players.add(player);
        }
        return players;
    }

}

