
package net.neferett.Survivor.Managers.SubObjects;

import net.neferett.Survivor.BonusType;
import net.neferett.Survivor.Managers.PlScoreboardManager;
import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.WeaponsManager;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Data;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.Utils;
import net.neferett.Survivor.Weapons.GunBullet;
import net.neferett.Survivor.Weapons.GunInstance;
import net.neferett.Survivor.Weapons.Templates.BazookaTmpl;
import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlPlayer {
    private Player m_player;
    private int m_money;
    private PlayerState m_state = PlayerState.ALIVE;
    private HashMap<PlayerState, Float> m_timeState = new HashMap();
    private PlPlayer m_cureTarget = null;
    private List<BonusType> m_bonus = new LinkedList<BonusType>();
    private HashMap<Integer, GunInstance> m_guns = new HashMap();
    private boolean m_rechargement = false;
    private long m_rechargementEnd = 0;

    public PlPlayer(Player player) {
        this.m_player = player;
        this.m_money = Survivor.getInt("start-money");
    }

    public void addBonus(BonusType bonus) {
        if (!this.m_bonus.contains((Object)bonus)) {
            this.m_bonus.add(bonus);
        }
    }

    public boolean hasBonus(BonusType bonus) {
        return this.m_bonus.contains((Object)bonus);
    }

    public void clearBonus() {
        this.m_bonus.clear();
        this.m_player.setMaxHealth(20.0);
    }

    public void clearGun() {
        this.m_guns.clear();
    }

    public PlPlayer getCureTarget() {
        return this.m_cureTarget;
    }

    public void setCureTarget(PlPlayer target) {
        this.m_cureTarget = target;
        if (this.m_cureTarget != null) {
            this.setPlayerState(PlayerState.CURE);
        } else if (this.m_state == PlayerState.CURE) {
            this.setPlayerState(PlayerState.ALIVE);
        }
    }

    public PlayerState getPlayerState() {
        return this.m_state;
    }

    public void setPlayerState(PlayerState state) {
        this.m_state = state;
        if (this.m_state != PlayerState.CURE) {
            this.m_cureTarget = null;
        } else if (this.m_cureTarget == null) {
            this.m_state = PlayerState.ALIVE;
        }
        if (this.m_state == PlayerState.CURE) {
            this.setTimeState(PlayersManager.CURE_TIME);
        } else if (this.m_state == PlayerState.HURT) {
            this.setTimeState(PlayersManager.HURT_TIME);
        }
        if (this.m_state == PlayerState.DIE) {
            this.m_player.setGameMode(GameMode.SPECTATOR);
            this.m_player.setAllowFlight(true);
            this.m_player.setFlying(true);
            Utils.setInventory(this.m_player);
        } else {
            this.m_player.setGameMode(GameMode.SURVIVAL);
            this.m_player.setAllowFlight(false);
            this.m_player.setFlying(false);
        }
    }

    public void tick() {
        if (this.m_rechargement && this.m_rechargementEnd <= System.currentTimeMillis()) {
            this.m_rechargement = false;
            int slot = this.m_player.getInventory().getHeldItemSlot();
            GunInstance gun = this.m_guns.get(slot);
            GunWeaponTmpl tmpl = gun.getGun();
            int need = tmpl.getBullet().getBullet() - gun.getBullet().getBullet();
            int bullet = Math.min(need, gun.getBullet().getCharger());
            gun.getBullet().addBullet(bullet);
            gun.getBullet().decrementCharger(bullet);
            this.updateBullet(slot);
        }
    }

    public void updatePlayer() {
        this.decrementTimeState();
        float time = this.getTimeState();
        if (this.m_state == PlayerState.HURT) {
            if (!this.isOnCure()) {
                this.m_player.setLevel((int)time);
                if (time <= 0.0f) {
                    Bukkit.broadcastMessage((String)Lang.get("PLAYER_DEAD").replaceAll("<player>", this.m_player.getName()));
                    this.setPlayerState(PlayerState.DIE);
                    if (PlayersManager.getInstance().getGamers().size() <= 0) {
                        Survivor.getInstance().endGame(false);
                    }
                }
            }
        } else if (this.m_state == PlayerState.CURE) {
            this.m_player.setExp((PlayersManager.CURE_TIME - time) / PlayersManager.CURE_TIME);
            this.m_cureTarget.getPlayer().setExp((PlayersManager.CURE_TIME - time) / PlayersManager.CURE_TIME);
            if (time <= 0.0f) {
                this.m_cureTarget.getPlayer().setLevel(0);
                this.m_cureTarget.getPlayer().setExp(0.0f);
                this.m_player.setExp(0.0f);
                Bukkit.broadcastMessage((String)Lang.get("PLAYER_HEALTH").replaceAll("<player>", this.m_player.getName()).replaceAll("<hurted>", this.m_cureTarget.getName()));
                this.m_cureTarget.setPlayerState(PlayerState.ALIVE);
                this.setPlayerState(PlayerState.ALIVE);
            }
        }
    }

    public void startRound() {
        if (this.m_state == PlayerState.DIE) {
            this.m_player.teleport(Data.getInstance().getSpawn());
            this.clearBonus();
            this.clearGun();
            this.setPlayerState(PlayerState.ALIVE);
            Utils.setInventory(this.m_player);
        } else {
            this.setPlayerState(PlayerState.ALIVE);
        }
        this.m_player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
    }

    public boolean isOnCure() {
        for (PlPlayer player : PlayersManager.getInstance().getPlayers()) {
            if (player.getPlayerState() != PlayerState.CURE || !player.getCureTarget().getUUID().equals(this.getUUID())) continue;
            return true;
        }
        return false;
    }



    public int getMoney() {
        return this.m_money;
    }

    public void addMoney(int money) {
        this.setMoney(this.m_money + money);
    }

    public void subMoney(int money) throws NotEnoughMoneyException {
        if (this.m_money < money) {
            this.m_player.sendMessage(Lang.get("NOT_ENOUGH_MONEY"));
            throw new NotEnoughMoneyException(this);
        }
        this.setMoney(this.m_money - money);
    }

    public void setMoney(int money) {
        this.m_money = money;
        PlScoreboardManager.getInstance().refreshPlayer(this);
    }

    public float getTimeState() {
        if (!this.m_timeState.containsKey((Object)this.m_state)) {
            float time = this.m_state == PlayerState.CURE ? PlayersManager.CURE_TIME : (this.m_state == PlayerState.HURT ? PlayersManager.HURT_TIME : 0.0f);
            this.m_timeState.put(this.m_state, Float.valueOf(time));
        }
        return this.m_timeState.get((Object)this.m_state).floatValue();
    }

    public void setTimeState(float time) {
        this.m_timeState.put(this.m_state, Float.valueOf(time));
    }

    public void decrementTimeState() {
        this.m_timeState.put(this.m_state, Float.valueOf(this.getTimeState() - 1.0f));
    }

    public Player getPlayer() {
        return this.m_player;
    }

    public String getName() {
        return this.m_player.getName();
    }

    public UUID getUUID() {
        return this.m_player.getUniqueId();
    }

    public boolean isRechargement() {
        return this.m_rechargement;
    }

    public void setRechargement(ItemStack is) {
        this.m_rechargement = true;
        this.m_rechargementEnd = System.currentTimeMillis() + this.getTimeToRecharge();
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("\u00a71Rechargement ...");
        is.setItemMeta(im);
    }

    public void setGun(int slot) {
        ItemStack is = this.m_player.getInventory().getItem(slot);
        WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(is.getType());
        if (weapon instanceof GunWeaponTmpl) {
            GunWeaponTmpl gunTmpl = (GunWeaponTmpl)weapon;
            this.m_guns.put(slot, new GunInstance((LivingEntity)this.m_player, gunTmpl));
            this.updateBullet(slot);
            this.updateLore(slot);
        }
    }

    public GunInstance getGun(int slot) {
        return this.m_guns.containsKey(slot) ? this.m_guns.get(slot) : null;
    }

    public int getGunSlot(GunWeaponTmpl tmpl) {
        Iterator<Integer> iterator = this.m_guns.keySet().iterator();
        while (iterator.hasNext()) {
            int slot = iterator.next();
            if (!this.m_guns.get(slot).getGun().getName().equalsIgnoreCase(tmpl.getName())) continue;
            return slot;
        }
        return -1;
    }

    public void deleteGun(int slot) {
        if (this.m_guns.containsKey(slot)) {
            this.m_guns.remove(slot);
        }
    }

    public void updateBullet(int slots) {
        ItemStack is = this.m_player.getInventory().getItem(slots);
        WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(is.getType());
        if (weapon instanceof GunWeaponTmpl) {
            GunWeaponTmpl gunTmpl = (GunWeaponTmpl)weapon;
            GunInstance gun = this.m_guns.get(slots);
            GunBullet bullet = gun.getBullet();
            if (bullet.getBullet() <= 0 && bullet.getCharger() > 0 && !this.m_rechargement) {
                this.setRechargement(is);
            } else if (!this.m_rechargement) {
                ItemMeta im = is.getItemMeta();
                im.setDisplayName(String.valueOf(gunTmpl.getName()) + "\u00a7r - \u00a71" + bullet.getBullet() + "\u00a72/\u00a71" + bullet.getCharger());
                is.setItemMeta(im);
            }
        }
    }

    public void updateLore(int slot) {
        ItemStack is = this.m_player.getInventory().getItem(slot);
        WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(is.getType());
        if (weapon instanceof GunWeaponTmpl) {
            GunInstance gun = this.m_guns.get(slot);
            ItemMeta im = is.getItemMeta();
            LinkedList<String> lore = new LinkedList<String>();
            if (gun.getGun().isMainWeapon()) {
                lore.add("\u00a76Niveau: \u00a7a" + gun.getLevel());
            }
            if (!(gun.getGun() instanceof BazookaTmpl)) {
                lore.add("\u00a76D\u00e9g\u00e2ts: \u00a7a" + gun.getGunLevel().getMinDamage() + "\u00a7r - \u00a7a" + gun.getGunLevel().getMaxDamage());
            }
            im.setLore(lore);
            is.setItemMeta(im);
        }
    }

    public long getTimeToRecharge() {
        return this.hasBonus(BonusType.PASSE_PASSE) ? 250 : 2000;
    }

    public void setWeapon(int slot) {
        ItemStack is = this.m_player.getInventory().getItem(slot);
        WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(is.getType());
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(weapon.getName());
        is.setItemMeta(im);
    }

    public static class NotEnoughMoneyException
    extends Exception {
        private static final long serialVersionUID = -6890519856213219761L;
        private PlPlayer m_player;

        public NotEnoughMoneyException(PlPlayer plPlayer) {
            this.m_player = plPlayer;
        }

        public PlPlayer getPlayer() {
            return this.m_player;
        }
    }

    public static enum PlayerState {
        ALIVE,
        HURT,
        CURE,
        DIE;
        

    }

}

