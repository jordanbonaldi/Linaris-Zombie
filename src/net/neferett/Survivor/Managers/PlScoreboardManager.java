
package net.neferett.Survivor.Managers;

import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Timers.GameTimer;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.RoundInfo;
import net.neferett.Survivor.Utils.Utils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class PlScoreboardManager {
    public static final int LAST_ROUND = Survivor.getInt("last-round");
    private BoardInfo m_objective;
    private int m_wait = 0;
    private static PlScoreboardManager instance;

    public static PlScoreboardManager getInstance() {
        if (instance == null) {
            instance = new PlScoreboardManager();
        }
        return instance;
    }

    private PlScoreboardManager() {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        Objective obj = sm.getNewScoreboard().registerNewObjective(Survivor.getInstance().getName(), "dummy");
        this.m_objective = new BoardInfo(this, obj, Survivor.GameState.LOBBY);
        this.m_objective.clear();
    }

    public BoardInfo getBoardInfo() {
        return this.m_objective;
    }

    public Scoreboard getScoreboard() {
        return this.getBoardInfo().getScoreboard();
    }

    public void updateScoreboard() {
        this.updateScoreboard(GameTimer.getInstance().getTime());
    }

    public void updateScoreboard(long time) {
        Survivor.GameState gameState = Survivor.getGameState();
        if (this.m_objective.getBoardType() != gameState) {
            this.m_objective.clear();
            this.m_objective.setBoardType(gameState);
        }
        if (gameState == Survivor.GameState.LOBBY) {
            this.m_objective.setTitle(Lang.get("OBJECTIVE_LOBBY_NAME"));
            int nbPlayers = Bukkit.getOnlinePlayers().size();
            this.m_objective.setText("lobby_players", Lang.get("OBJECTIVE_LOBBY_PLAYERS").replaceAll("<x>", Integer.toString(nbPlayers)).replaceAll("<max>", Integer.toString(Survivor.getPlayerMax())), 2);
            if (nbPlayers >= Survivor.getInt("player-min")) {
                this.m_objective.setText("lobby_time", Lang.get("OBJECTIVE_LOBBY_START").replaceAll("<sec>", Long.toString(time)).replaceAll("<SECOND>", time > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")), 1);
            } else {
                this.m_wait = this.m_wait >= 3 ? 0 : this.m_wait + 1;
                StringBuilder sb = new StringBuilder();
                sb.append(Lang.get("OBJECTIVE_LOBBY_WAIT"));
                int i = 0;
                while (i < this.m_wait) {
                    sb.append(".");
                    ++i;
                }
                this.m_objective.setText("lobby_time", sb.toString(), 1);
            }
        } else {
            long min = time / 60;
            long sec = time % 60;
            RoundInfo round = RoundInfo.getTheRoundInfo();
            if (GameTimer.isRound()) {
                this.m_objective.setTitle(Lang.get("OBJECTIVE_GAME_ROUND_NAME").replaceAll("<x>", Integer.toString(GameTimer.getRound())).replaceAll("<max>", Integer.toString(LAST_ROUND)));
            } else {
                this.m_objective.setTitle(Lang.get("OBJECTIVE_GAME_PREPARATION_NAME").replaceAll("<x>", Integer.toString(GameTimer.getRound())).replaceAll("<max>", Integer.toString(LAST_ROUND)).replaceAll("<min>", PlScoreboardManager.getVarWithZero(min)).replaceAll("<sec>", PlScoreboardManager.getVarWithZero(sec)).replaceAll("<SECOND>", sec > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")).replaceAll("<MINUTE>", min > 1 ? Lang.get("MINUTE_PLURAL") : Lang.get("MINUTE_SINGULAR")));
            }
            this.m_objective.setText("game_space", "  ", 2);
            for (PlPlayer tplayer : PlayersManager.getInstance().getPlayers()) {
                this.m_objective.setText("game_player_" + tplayer.getName(), Lang.get("OBJECTIVE_GAME_PLAYER").replaceAll("<player>", Utils.shortString(tplayer.getName(), 5)).replaceAll("<money>", Integer.toString(tplayer.getMoney())), 1);
            }
        }
    }

    public void refreshEntitiesAlives(int entitiesAlives) {
        this.m_objective.setText("game_zombie", Lang.get("OBJECTIVE_GAME_ENTITIES").replaceAll("<x>", Integer.toString(entitiesAlives)), 3);
    }

    public void refreshPlayer(PlPlayer player) {
        this.m_objective.setText("game_player_" + player.getName(), Lang.get("OBJECTIVE_GAME_PLAYER").replaceAll("<player>", Utils.shortString(player.getName(), 5)).replaceAll("<money>", Integer.toString(player.getMoney())), 1);
    }

    public void removePlayer(Player player) {
        this.m_objective.setText("game_player_" + player.getName(), null, 0);
    }

    public static String getVarWithZero(long var) {
        return var > 9 ? Long.toString(var) : "0" + var;
    }

    private class BoardInfo {
        private Objective m_obj;
        private String m_title;
        private Map<String, String> m_boardTxt;
        Survivor.GameState m_boardType;
        final  PlScoreboardManager manager;

        private BoardInfo(PlScoreboardManager plScoreboardManager, Objective obj, Survivor.GameState boardType) {
            this.manager = plScoreboardManager;
            this.m_boardTxt = new HashMap<String, String>();
            this.m_obj = obj;
            this.m_obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            boardType = this.m_boardType;
            this.m_title = "none";
            obj.setDisplayName(this.m_title);
        }

        public Objective getObjective() {
            return this.m_obj;
        }

        public Scoreboard getScoreboard() {
            return this.m_obj.getScoreboard();
        }

        public Survivor.GameState getBoardType() {
            return this.m_boardType;
        }

        public void setBoardType(Survivor.GameState state) {
            this.m_boardType = state;
        }

        public void setText(String id, String text, int pos) {
            String last = null;
            if (this.m_boardTxt.containsKey(id)) {
                last = this.m_boardTxt.get(id);
                if (last.equalsIgnoreCase(text)) {
                    return;
                }
                this.m_obj.getScoreboard().resetScores(last);
            }
            if (text == null) {
                if (last != null) {
                    this.m_obj.getScoreboard().resetScores(last);
                }
            } else {
                this.m_boardTxt.put(id, text);
                this.m_obj.getScore(text).setScore(pos);
            }
        }

        public void setTitle(String title) {
            if (!this.m_title.equalsIgnoreCase(title)) {
                this.m_title = title;
                this.m_obj.setDisplayName(title);
            }
        }

        public void clear() {
            for (String entry : this.m_boardTxt.values()) {
                this.m_obj.getScoreboard().resetScores(entry);
            }
            this.m_boardTxt.clear();
        }


    }

}

