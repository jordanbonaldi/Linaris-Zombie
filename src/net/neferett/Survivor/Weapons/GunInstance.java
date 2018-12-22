
package net.neferett.Survivor.Weapons;

import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Utils;
import net.neferett.Survivor.Weapons.GunBullet;
import net.neferett.Survivor.Weapons.Templates.BazookaTmpl;
import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.LaserTmpl;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class GunInstance {
    private LivingEntity m_entity;
    private GunWeaponTmpl m_gun;
    private GunBullet m_bullet;
    private int m_level;

    public GunInstance(LivingEntity entity, GunWeaponTmpl gun) {
        this.m_entity = entity;
        this.m_gun = gun;
        this.m_bullet = new GunBullet(gun.getBullet());
        this.m_level = 1;
    }

    public LivingEntity getEntity() {
        return this.m_entity;
    }

    public GunWeaponTmpl getGun() {
        return this.m_gun;
    }

    public GunBullet getBullet() {
        return this.m_bullet;
    }

    public void addLevel() {
        ++this.m_level;
    }

    public int getLevel() {
        return this.m_level;
    }

    public GunWeaponTmpl.GunLevel getGunLevel() {
        return this.m_gun.getGunLevel(this.m_level);
    }

    public void full() {
        this.m_bullet.setBullet(this.m_gun.getBullet().getBullet());
        this.m_bullet.setCharger(this.m_gun.getBullet().getCharger());
    }

    public boolean shot() {
        if (this.m_bullet.getBullet() <= 0) {
            return false;
        }
        if (!(this.m_gun instanceof LaserTmpl) && !(this.m_gun instanceof BazookaTmpl)) {
            int i = 0;
            while (i < this.m_gun.getBulletPerShot()) {
                Vector vec = this.m_entity.getLocation().getDirection().multiply(this.m_gun.getVelocityMultiplier());
                Snowball snowball = (Snowball)this.m_entity.launchProjectile((Class)Snowball.class);
                snowball.setMetadata("weapon", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)this));
                snowball.setVelocity(vec);
                ++i;
            }
        }
        if (this.m_gun.getWeaponSound() != null) {
            Utils.playSound(this.m_entity.getLocation(), this.m_gun.getWeaponSound());
        }
        return true;
    }
}

