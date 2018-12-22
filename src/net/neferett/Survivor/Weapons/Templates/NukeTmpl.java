
package net.neferett.Survivor.Weapons.Templates;

import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Timers.GameTimer;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.RoundInfo;
import net.neferett.Survivor.Weapons.Templates.ActivableWeaponTmpl;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class NukeTmpl
extends ActivableWeaponTmpl {
    public NukeTmpl(String name, Material material) {
        super(name, material);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        super.onRightClick(event);
        final Player player = event.getPlayer();
        Bukkit.broadcastMessage((String)Lang.get("NUKE_LAUNCH").replaceAll("<player>", player.getName()));
        GameTimer.getInstance().addTask(new Runnable(){
            int time;

            @Override
            public void run() {
                if (this.time > 0) {
                    Bukkit.broadcastMessage((String)Lang.get("NUKE_TIME").replaceAll("<sec>", Integer.toString(this.time)).replaceAll("<SECOND>", this.time > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")));
                } else {
                    Bukkit.broadcastMessage((String)Lang.get("NUKE_EXPLODE"));
                    for (Entity e : player.getWorld().getEntities()) {
                        if (e.getType() != RoundInfo.getTheRoundInfo().getEntityType() && e.getType() != EntityType.PIG_ZOMBIE) continue;
                        Location loc = e.getLocation();
                        TNTPrimed tnt = (TNTPrimed)loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                        tnt.setMetadata("owner", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)player));
                        tnt.setMetadata("oneshot", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)true));
                        tnt.setMetadata("weapon", (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), (Object)NukeTmpl.this));
                        tnt.setFuseTicks(0);
                    }
                }
                --this.time;
            }
        }, 20, 5);
    }

}

