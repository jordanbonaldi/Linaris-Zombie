
package net.neferett.Survivor.Utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_8_R3.DedicatedPlayerList;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.util.Vector;

public abstract class UtilParticle {
    public static void sendParticleToLocation(Location loc, float speed, int amount) {
        try {
            PacketPlayOutWorldParticles sPacket = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), 1.0f, 0.0f, 0.0f, speed, amount, new int[0]);
            DedicatedPlayerList handleCraftServer = ((CraftServer)Bukkit.getServer()).getHandle();
            WorldServer worldServer = ((CraftWorld)loc.getWorld()).getHandle();
            int dimension = worldServer.dimension;
            handleCraftServer.sendPacketNearby(null, Double.valueOf(loc.getX()).doubleValue(), Double.valueOf(loc.getY()).doubleValue(), Double.valueOf(loc.getZ()).doubleValue(), (double)Integer.valueOf(64).intValue(), Integer.valueOf(dimension).intValue(), (Packet)sPacket);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Location> buildLine(Location the_loc1, Location the_loc2) {
        Location loc1 = the_loc1.clone();
        Location loc2 = the_loc2.clone();
        ArrayList<Location> locs = new ArrayList<Location>();
        Vector v = new Vector(loc2.getX() - loc1.getX(), loc2.getY() - loc1.getY(), loc2.getZ() - loc1.getZ());
        v.normalize().multiply(0.1);
        double lastDistance = loc1.distanceSquared(loc2);
        loc1.add(v);
        while (loc1.distanceSquared(loc2) < lastDistance) {
            locs.add(loc1.clone());
            lastDistance = loc1.distanceSquared(loc2);
            loc1.add(v);
        }
        return locs;
    }

    public static void playFirework(Location loc1, Location loc2) {
        List<Location> locs = UtilParticle.buildLine(loc1, loc2);
        for (Location loc : locs) {
            UtilParticle.sendParticleToLocation(loc, 1.0f, 1);
        }
    }
}

