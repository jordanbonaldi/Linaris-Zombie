
package net.neferett.Survivor.Managers;

import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.RoundInfo;
import net.neferett.Survivor.Utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Gate;
import org.bukkit.material.MaterialData;

public class FenceManager {
    public static final int FENCE_DAMAGE = Survivor.getInt("fence-time");
    public static final int FENCE_PRICE = Survivor.getInt("fence-price");
    private File m_file;
    private FileConfiguration m_yaml;
    private List<Fence> m_fences = new LinkedList<Fence>();
    private static FenceManager instance = null;

    public static FenceManager getInstance() {
        if (instance == null) {
            instance = new FenceManager();
        }
        return instance;
    }

    private FenceManager() {
        try {
            File dirs = new File("plugins/" + Survivor.getInstance().getName() + "/data");
            dirs.mkdirs();
            this.m_file = new File("plugins/" + Survivor.getInstance().getName() + "/data/fence.yml");
            if (!this.m_file.exists()) {
                this.m_file.createNewFile();
            }
            this.m_yaml = YamlConfiguration.loadConfiguration((File)this.m_file);
            for (String dat_str : this.m_yaml.getStringList("fences")) {
                String[] data = dat_str.split("#");
                Fence fence = new Fence(this, Utils.toLocation(data[0], true), BlockFace.valueOf((String)data[1]));
                this.m_fences.add(fence);
                fence.reset();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        block0 : for (Fence f : this.m_fences) {
            if (!f.isActive()) continue;
            for (Entity e : f.getLocation().getWorld().getEntities()) {
                if (e.getType() != RoundInfo.getTheRoundInfo().getEntityType()) continue;
                double x = Math.abs(f.getLocation().getX() - e.getLocation().getX());
                double y = Math.abs(f.getLocation().getY() - e.getLocation().getY());
                double z = Math.abs(f.getLocation().getZ() - e.getLocation().getZ());
                if (x > 1.2 || y > 1.2 || z > 1.2) continue;
                f.addDamage();
                continue block0;
            }
        }
    }

    public Fence addFence(Location loc, Gate gate) {
        if (!this.isFence(loc)) {
            Fence fence = new Fence(this, loc, gate.getFacing());
            this.m_fences.add(fence);
            List fences = this.m_yaml.getStringList("fences");
            fences.add(String.valueOf(Utils.toString(loc, true)) + "#" + gate.getFacing().name());
            this.m_yaml.set("fences", (Object)fences);
            try {
                this.m_yaml.save(this.m_file);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            fence.reset();
            return fence;
        }
        return this.getFence(loc);
    }

    public boolean isFence(Location loc) {
        for (Fence f : this.m_fences) {
            if (!Utils.locEqual(f.getLocation(), loc)) continue;
            return true;
        }
        return false;
    }

    public Fence getFence(Location loc) {
        for (Fence f : this.m_fences) {
            if (!Utils.locEqual(f.getLocation(), loc)) continue;
            return f;
        }
        return null;
    }

    public class Fence {
        private Location m_loc;
        private int m_damage;
        private BlockFace m_face;
        final  FenceManager fence;

        private Fence(FenceManager fenceManager, Location loc, BlockFace face) {
            this.fence = fenceManager;
            this.m_damage = 0;
            this.m_loc = loc;
            this.m_face = face;
        }

        public Location getLocation() {
            return this.m_loc;
        }

        public int getDamage() {
            return this.m_damage;
        }

        public void addDamage() {
            ++this.m_damage;
            if (this.m_damage >= FenceManager.FENCE_DAMAGE) {
                Block b = this.m_loc.getBlock();
                b.setType(Material.FENCE_GATE);
                Gate d = (Gate)b.getState().getData();
                if (this.inverseFence(d.getFacing(), this.m_face)) {
                    b.setData((byte)(b.getData() ^ 1));
                }
                b.setData((byte)(b.getData() | 4));
            }
        }

        public boolean isActive() {
            if (this.m_loc.getBlock().getType() == Material.FENCE) {
                return true;
            }
            return false;
        }

        public void reset() {
            this.m_loc.getBlock().setType(Material.FENCE);
            this.m_damage = 0;
        }


        private boolean inverseFence(BlockFace f1, BlockFace f2) {
          if (f1 == BlockFace.SOUTH) f1 = BlockFace.NORTH;
          else if (f1 == BlockFace.WEST) f1 = BlockFace.EAST;

          if (f2 == BlockFace.SOUTH) f2 = BlockFace.NORTH;
          else if (f2 == BlockFace.WEST) f2 = BlockFace.EAST;

          return f1 != f2;
        }
    }

}

