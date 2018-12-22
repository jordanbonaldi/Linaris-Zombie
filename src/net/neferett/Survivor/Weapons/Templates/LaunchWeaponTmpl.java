
package net.neferett.Survivor.Weapons.Templates;

import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public abstract class LaunchWeaponTmpl
extends WeaponTmpl {
    protected Double m_distanceMultiplier;

    public LaunchWeaponTmpl(String name, Material material, Double distanceMultiplier) {
        super(name, material);
        this.m_distanceMultiplier = distanceMultiplier;
    }

    public Item onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item dropped = player.getWorld().dropItem(player.getEyeLocation().subtract(0.0, 0.1, 0.0), new ItemStack(this.m_material));
        dropped.setPickupDelay(Integer.MAX_VALUE);
        dropped.setVelocity(player.getLocation().getDirection().multiply(this.m_distanceMultiplier.doubleValue()));
        dropped.setMetadata("launch", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)true));
        ItemStack inHand = player.getItemInHand();
        if (inHand.getAmount() <= 1) {
            player.setItemInHand(null);
        } else {
            inHand.setAmount(inHand.getAmount() - 1);
        }
        event.setCancelled(true);
        return dropped;
    }
}

