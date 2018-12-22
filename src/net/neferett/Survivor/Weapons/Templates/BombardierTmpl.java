
package net.neferett.Survivor.Weapons.Templates;

import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Weapons.Templates.LaunchWeaponTmpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class BombardierTmpl
extends LaunchWeaponTmpl {
    public BombardierTmpl(String name, Material material) {
        super(name, material, 0.2);
    }

    @Override
    public Item onRightClick(PlayerInteractEvent event) {
        final Item dropped = super.onRightClick(event);
        final Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)Survivor.getInstance(), new Runnable(){

            @Override
            public void run() {
                Location base = dropped.getLocation();
                int x = -1;
                while (x <= 1) {
                    int z = -1;
                    while (z <= 1) {
                        if (x != 0 && z != 0 || x == z) {
                            Location loc = base.clone().add((double)x, 0.0, (double)z);
                            TNTPrimed tnt = (TNTPrimed)loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                            tnt.setMetadata("owner", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)player));
                            tnt.setMetadata("oneshot", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)true));
                            tnt.setMetadata("weapon", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)BombardierTmpl.this));
                            tnt.setFuseTicks(0);
                            dropped.remove();
                        }
                        ++z;
                    }
                    ++x;
                }
            }
        }, 100);
        return dropped;
    }

}

