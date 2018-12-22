
package net.neferett.Survivor.Weapons.Templates;

import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ActivableWeaponTmpl
extends WeaponTmpl {
    public ActivableWeaponTmpl(String name, Material material) {
        super(name, material);
    }

    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = player.getItemInHand();
        if (inHand.getAmount() <= 1) {
            player.setItemInHand(null);
        } else {
            inHand.setAmount(inHand.getAmount() - 1);
        }
        event.setCancelled(true);
    }
}

