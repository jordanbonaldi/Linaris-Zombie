
package net.neferett.Survivor.Managers;

import net.neferett.Survivor.Managers.WeaponsManager;
import net.neferett.Survivor.Weapons.GunInstance;
import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class EntitiesManager {
    private List<EntityGun> m_entitiesGun = new LinkedList<EntityGun>();
    private static EntitiesManager instance;

    public static EntitiesManager getInstance() {
        if (instance == null) {
            instance = new EntitiesManager();
        }
        return instance;
    }

    private EntitiesManager() {
    }

    public void addEntity(LivingEntity entity) {
        EntityEquipment stuff = entity.getEquipment();
        WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(stuff.getItemInHand().getType());
        if (weapon != null && weapon instanceof GunWeaponTmpl) {
            this.m_entitiesGun.add(new EntityGun(new GunInstance(entity, (GunWeaponTmpl)weapon)));
        }
    }

    public void tick() {
        int i = 0;
        while (i < this.m_entitiesGun.size()) {
            EntityGun entity = this.m_entitiesGun.get(i);
            if (entity.m_gun.getEntity() == null || entity.m_gun.getEntity().isDead() || !entity.m_gun.getEntity().isValid()) {
                this.m_entitiesGun.remove(i);
                --i;
            } else {
                entity.shot();
            }
            ++i;
        }
    }

    private class EntityGun {
        private GunInstance m_gun;
        private long m_lastShot;

        public EntityGun(GunInstance gun) {
            this.m_lastShot = 0;
            this.m_gun = gun;
        }

        public void shot() {
            if (this.m_lastShot + this.m_gun.getGun().getShotInterval() * 6 > System.currentTimeMillis()) {
                return;
            }
            this.m_lastShot = System.currentTimeMillis();
            if (this.m_gun.getEntity() instanceof Creature) {
                Creature creature = (Creature)this.m_gun.getEntity();
                LivingEntity target = creature.getTarget();
                if (target == null) {
                    return;
                }
                if (target.getLocation().distance(this.m_gun.getEntity().getLocation()) > 50.0 || target.isDead()) {
                    creature.setTarget(null);
                }
            }
            this.m_gun.shot();
        }
    }

}

