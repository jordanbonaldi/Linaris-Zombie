
package net.neferett.Survivor.Managers;

import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Weapons.GunBullet;
import net.neferett.Survivor.Weapons.Templates.BombardierTmpl;
import net.neferett.Survivor.Weapons.Templates.GrenadeTmpl;
import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.NukeTmpl;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WeaponsManager {
    private List<WeaponTmpl> m_weapons = new LinkedList<WeaponTmpl>();
    private static WeaponsManager instance = null;

    public static WeaponsManager getInstance() {
        if (instance == null) {
            instance = new WeaponsManager();
        }
        return instance;
    }

    private WeaponsManager() {
        this.m_weapons.add(new GunWeaponTmpl("&dSW1911", Material.WOOD_PICKAXE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.SW1911").setBullet(new GunBullet(10, 100)).setGunLevels(new GunWeaponTmpl.GunLevel(0.5, 1.0), new GunWeaponTmpl.GunLevel(0.65, 1.3).setPrice(100), new GunWeaponTmpl.GunLevel(0.75, 1.5).setPrice(150), new GunWeaponTmpl.GunLevel(0.85, 1.7).setPrice(200), new GunWeaponTmpl.GunLevel(1.0, 2.0).setPrice(250)).setPrice(50));
        this.m_weapons.add(new GunWeaponTmpl("&dAK47", Material.WOOD_HOE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.AK47").setBullet(new GunBullet(30, 300)).setGunLevels(new GunWeaponTmpl.GunLevel(1.0, 1.5), new GunWeaponTmpl.GunLevel(1.3, 2.0).setPrice(100), new GunWeaponTmpl.GunLevel(1.5, 2.25).setPrice(150), new GunWeaponTmpl.GunLevel(1.7, 2.5).setPrice(200), new GunWeaponTmpl.GunLevel(2.0, 3.0).setPrice(250)).setPrice(50));
        this.m_weapons.add(new GunWeaponTmpl("&dMP5", Material.GOLD_HOE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.MP5").setBullet(new GunBullet(30, 300)).setGunLevels(new GunWeaponTmpl.GunLevel(4.0, 4.5), new GunWeaponTmpl.GunLevel(5.2, 5.9).setPrice(500), new GunWeaponTmpl.GunLevel(6.0, 6.8).setPrice(750), new GunWeaponTmpl.GunLevel(6.8, 7.7).setPrice(1000), new GunWeaponTmpl.GunLevel(8.0, 9.0).setPrice(1250)).setPrice(250));
        this.m_weapons.add(new GunWeaponTmpl("FAMAS", Material.GOLD_AXE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.FAMAS").setBullet(new GunBullet(30, 300)).setGunLevels(new GunWeaponTmpl.GunLevel(8.0, 9.5), new GunWeaponTmpl.GunLevel(10.4, 12.4).setPrice(1000), new GunWeaponTmpl.GunLevel(12.0, 14.3).setPrice(2000), new GunWeaponTmpl.GunLevel(14.0, 16.4).setPrice(2500), new GunWeaponTmpl.GunLevel(16.0, 18.8).setPrice(2500)).setPrice(500));
        this.m_weapons.add(new GunWeaponTmpl("M16A3", Material.STONE_HOE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.M16A3").setBullet(new GunBullet(30, 300)).setGunLevels(new GunWeaponTmpl.GunLevel(2.0, 2.5), new GunWeaponTmpl.GunLevel(2.6, 3.3).setPrice(200), new GunWeaponTmpl.GunLevel(3.0, 3.8).setPrice(300), new GunWeaponTmpl.GunLevel(3.4, 4.3).setPrice(400), new GunWeaponTmpl.GunLevel(4.0, 5.0).setPrice(500)).setPrice(100));
        this.m_weapons.add(new GunWeaponTmpl("UMP45", Material.IRON_HOE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.UMP45").setBullet(new GunBullet(30, 300)).setGunLevels(new GunWeaponTmpl.GunLevel(3.0, 3.5), new GunWeaponTmpl.GunLevel(3.9, 4.6).setPrice(200), new GunWeaponTmpl.GunLevel(4.5, 5.3).setPrice(450), new GunWeaponTmpl.GunLevel(5.1, 6.0).setPrice(600), new GunWeaponTmpl.GunLevel(6.0, 7.0).setPrice(750)).setPrice(150));
        this.m_weapons.add(new GunWeaponTmpl("SCAR", Material.DIAMOND_HOE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.SCAR").setBullet(new GunBullet(30, 300)).setGunLevels(new GunWeaponTmpl.GunLevel(6.0, 6.5), new GunWeaponTmpl.GunLevel(7.8, 8.5).setPrice(800), new GunWeaponTmpl.GunLevel(9.0, 10.3).setPrice(1200), new GunWeaponTmpl.GunLevel(11.5, 13.0).setPrice(1800), new GunWeaponTmpl.GunLevel(13.0, 15.0).setPrice(2000)).setPrice(400));
        this.m_weapons.add(new GunWeaponTmpl("SPAS12", Material.GOLD_SPADE).setShotInterval((long)200).setVelocityMultiplier(1.5).setWeaponSound("guns.SPAS12").setBullet(new GunBullet(8, 64)).setGunLevels(new GunWeaponTmpl.GunLevel(7.0, 8.5), new GunWeaponTmpl.GunLevel(9.1, 11.0).setPrice(700), new GunWeaponTmpl.GunLevel(10.5, 12.8).setPrice(1050), new GunWeaponTmpl.GunLevel(13.1, 15.0).setPrice(1400), new GunWeaponTmpl.GunLevel(15.0, 16.8).setPrice(2000)).setPrice(350));
        this.m_weapons.add(new GrenadeTmpl("GRENADE", Material.SNOW_BALL));
        this.m_weapons.add(new BombardierTmpl("BOMBARDIER", Material.REDSTONE));
        this.m_weapons.add(new NukeTmpl("NUKE", Material.FIREBALL));
    }

    public WeaponTmpl getWeapon(Material material) {
        for (WeaponTmpl weapon : this.m_weapons) {
            if (weapon.getMaterial() != material) continue;
            return weapon;
        }
        return null;
    }

    public GunWeaponTmpl getRandomGunWeapon(PlPlayer plPlayer) {
        PlayerInventory inv = plPlayer.getPlayer().getInventory();
        LinkedList<Material> exept = new LinkedList<Material>();
        if (inv.getItem(0) != null) {
            exept.add(inv.getItem(0).getType());
        }
        if (inv.getItem(1) != null) {
            exept.add(inv.getItem(1).getType());
        }
        Random rand = new Random();
        LinkedList<GunWeaponTmpl> guns = new LinkedList<GunWeaponTmpl>();
        for (WeaponTmpl weapon : this.m_weapons) {
            if (!(weapon instanceof GunWeaponTmpl) || exept.contains((Object)weapon.getMaterial())) continue;
            guns.add((GunWeaponTmpl)weapon);
        }
        return (GunWeaponTmpl)guns.get(rand.nextInt(guns.size()));
    }
}

