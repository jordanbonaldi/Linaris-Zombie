
package net.neferett.Survivor.Utils;

import net.neferett.Survivor.Managers.MapManager;
import net.neferett.Survivor.Managers.PlScoreboardManager;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Timers.GameTimer;
import net.neferett.Survivor.Utils.Data;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.Utils;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RoundInfo {
    private static HashMap<Integer, RoundInfo> m_rounds = new HashMap();
    private EntityType m_entity;
    private double m_health;
    private int m_amount;
    private int m_entitiesAlives;
    private long m_lastZombieKill = 0;

    public static RoundInfo getTheRoundInfo() {
        return RoundInfo.getRoundInfo(GameTimer.getRound());
    }

    public static RoundInfo getRoundInfo(int round) {
        if (!m_rounds.containsKey(round)) {
            EntityType entity = EntityType.ZOMBIE;
            if (round == 8 || round == 18 || round == 28) {
                entity = EntityType.WOLF;
            }
            m_rounds.put(round, new RoundInfo(entity, entity == EntityType.WOLF ? 1.0 : (double)round * 2.0, round * 7));
        }
        return m_rounds.get(round);
    }

    private RoundInfo(EntityType entity, double health, int amount) {
        this.m_entity = entity;
        this.m_health = health;
        this.m_amount = amount;
        this.m_entitiesAlives = 0;
        PlScoreboardManager.getInstance().refreshEntitiesAlives(0);
    }

    public EntityType getEntityType() {
        return this.m_entity;
    }

    public double getEntityHealth() {
        return this.m_health;
    }

    public int getEntityAmount() {
        return this.m_amount;
    }

    public void startRound() {
        this.m_entitiesAlives = this.m_amount;
        PlScoreboardManager.getInstance().refreshEntitiesAlives(this.m_entitiesAlives);
    }

    public int getEntitiesAlives() {
        return this.m_entitiesAlives;
    }

    public void addEntitiesAlives(int nb) {
        this.m_entitiesAlives += nb;
        PlScoreboardManager.getInstance().refreshEntitiesAlives(this.m_entitiesAlives);
    }

    public void removeEntityAlive() {
        this.setEntityAlive(this.m_entitiesAlives - 1);
    }

    public void setLastZombieKill() {
        this.m_lastZombieKill = System.currentTimeMillis();
    }

    public void updateEntityAlive() {
        if (this.m_lastZombieKill + 2000 > System.currentTimeMillis()) {
            return;
        }
        int amount = 0;
        for (Entity e : Survivor.getWorldGame().getEntities()) {
            if (e.getType() != this.getEntityType() && e.getType() != EntityType.PIG_ZOMBIE) continue;
            ++amount;
        }
        if (amount != RoundInfo.getTheRoundInfo().getEntitiesAlives()) {
            RoundInfo.getTheRoundInfo().setEntityAlive(amount);
        }
    }

    private void setEntityAlive(int amount) {
        this.m_entitiesAlives = amount;
        PlScoreboardManager.getInstance().refreshEntitiesAlives(this.m_entitiesAlives);
        if (this.getEntitiesAlives() <= 0) {
            GameTimer.getInstance().endRound();
        }
        if (this.getEntitiesAlives() == 5 && MapManager.getInstance().getAvailableMapParts().size() > 1) {
            Bukkit.broadcastMessage((String)Lang.get("LAST_ZOMBIE_TP"));
            Player nearest = Utils.getNearestPlayer(Data.getInstance().getCentre());
            for (Entity e : Survivor.getWorldGame().getEntities()) {
                if (e.getType() != this.getEntityType() && e.getType() != EntityType.PIG_ZOMBIE) continue;
                e.teleport(Data.getInstance().getCentre());
                Creature creature = (Creature)e;
                creature.setTarget((LivingEntity)nearest);
            }
        }
    }
}

