
package net.neferett.Survivor.Weapons.Templates;

import net.neferett.Survivor.Weapons.GunBullet;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import java.util.Random;
import org.bukkit.Material;

public class GunWeaponTmpl
extends WeaponTmpl {
    protected Double m_velocityMultiplier = 1.0;
    protected int m_bulletPerShot = 1;
    protected String m_sound;
    protected GunBullet m_bullet = new GunBullet(1, 0);
    protected Long m_interval = (long) 0;
    protected boolean m_isMain = true;
    protected GunLevel[] m_levels;

    public GunWeaponTmpl(String name, Material material) {
        super(name, material);
    }

    public int getChargerPrice() {
        return this.m_price / 2;
    }

    public boolean isMainWeapon() {
        return this.m_isMain;
    }

    public GunWeaponTmpl setVelocityMultiplier(double velocityMultiplier) {
        this.m_velocityMultiplier = velocityMultiplier;
        return this;
    }

    public double getVelocityMultiplier() {
        return this.m_velocityMultiplier;
    }

    public GunWeaponTmpl setShotInterval(Long interval) {
        this.m_interval = interval;
        return this;
    }

    public Long getShotInterval() {
        return this.m_interval;
    }

    public GunWeaponTmpl setBulletPerShot(Integer bulletPerShot) {
        this.m_bulletPerShot = bulletPerShot;
        return this;
    }

    public int getBulletPerShot() {
        return this.m_bulletPerShot;
    }

    public GunWeaponTmpl setWeaponSound(String sound) {
        this.m_sound = sound;
        return this;
    }

    public String getWeaponSound() {
        return this.m_sound;
    }

    public GunWeaponTmpl setBullet(GunBullet bullet) {
        this.m_bullet = bullet;
        return this;
    }

    public GunBullet getBullet() {
        return this.m_bullet;
    }

    public  GunWeaponTmpl setGunLevels(GunLevel ... levels) {
        this.m_levels = levels;
        return this;
    }

    public GunLevel getGunLevel(int level) {
        return this.m_levels[level - 1];
    }

    public int getMaxLevel() {
        return this.m_levels.length;
    }

    public static class GunLevel {
        private double m_minDamage;
        private double m_maxDamage;
        private int m_price = 0;
        private Random m_rnd;

        public GunLevel(double minDamamage, double maxDamage) {
            this.m_minDamage = minDamamage;
            this.m_maxDamage = maxDamage;
            this.m_rnd = new Random();
        }

        public double getMinDamage() {
            return this.m_minDamage;
        }

        public double getMaxDamage() {
            return this.m_maxDamage;
        }

        public GunLevel setPrice(int price) {
            this.m_price = price;
            return this;
        }

        public int getPrice() {
            return this.m_price;
        }

        public double getDamage() {
            return this.m_minDamage + this.m_rnd.nextDouble() * (this.m_maxDamage - this.m_minDamage);
        }
    }

}

