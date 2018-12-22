package net.neferett.Survivor;

import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Utils.Utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public enum BonusType
{
  PASSE_PASSE("Passe Passe", Material.GLOWSTONE_DUST, 1000), 
  MASTODONTE("Mastodonte", Material.IRON_CHESTPLATE, 1000);

  private String m_name;
  private ItemStack m_item;
  private int m_price;

  private BonusType(String name, Material material, int price) { this.m_name = name;
    this.m_item = Utils.item(material, name + " " + price + "$");
    this.m_price = price; }

  public String getName() {
    return this.m_name; } 
  public ItemStack getItem() { return this.m_item; } 
  public int getPrice() { return this.m_price; }

  public void buy(PlPlayer plPlayer) throws PlPlayer.NotEnoughMoneyException {
    plPlayer.subMoney(getPrice());
    plPlayer.addBonus(this);

    if (this == MASTODONTE) {
      Player player = plPlayer.getPlayer();
      PlayerInventory inv = player.getInventory();

      player.setMaxHealth(40.0D);
      player.setHealth(40.0D);
      inv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
      inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
      inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
      inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }
  }

  public static BonusType getByMaterial(Material m) {
    for (BonusType bonus : values()) {
      if (bonus.getItem().getType() == m) return bonus;
    }
    return null;
  }
}