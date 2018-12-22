package net.neferett.Survivor;


import java.util.Iterator;
import java.util.List;

import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.WeaponsManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.Utils;
import net.neferett.Survivor.Weapons.GunInstance;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public enum DropType
{
  BOMBARDIER("Bombarider", Material.REDSTONE), 
  NUKE("Nuke", Material.FIREBALL), 
  MUNO_MAX("Munition max", Material.GLOWSTONE_DUST), 
  MORT_INSTANT("Mort instantané", Material.TNT);

  private String m_name;
  private ItemStack m_item;

  private DropType(String name, Material material) { this.m_name = name;
    this.m_item = new ItemStack(material); }

  public String getName() {
    return this.m_name; } 
  public ItemStack getItem() { return this.m_item; }

  public void give(PlPlayer plPlayer) {
      Player player = plPlayer.getPlayer();
      PlayerInventory inv = player.getInventory();
      if (this == BOMBARDIER || this == NUKE) {
          WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(this.getItem().getType());
          inv.setItem(5, Utils.item(weapon.getMaterial(), weapon.getName()));
      } else if (this == MORT_INSTANT) {
          Survivor.setMortInstant();
          Bukkit.broadcastMessage((String)Lang.get("MORT_INSTANT"));
      } else if (this == MUNO_MAX) {
          for (PlPlayer aplayer : PlayersManager.getInstance().getGamers()) {
              int i = 0;
              while (i < 2) {
                  GunInstance gun = aplayer.getGun(0);
                  if (gun != null) {
                      gun.full();
                  }
                  ++i;
              }
          }
          Bukkit.broadcastMessage((String)Lang.get("MUNO_MAX"));
      }
  }

  public static DropType getByMaterial(Material m) {
      DropType[] arrdropType = DropType.values();
      int n = arrdropType.length;
      int n2 = 0;
      while (n2 < n) {
          DropType bonus = arrdropType[n2];
          if (bonus.getItem().getType() == m) {
              return bonus;
          }
          ++n2;
      }
      return null;
  }
}