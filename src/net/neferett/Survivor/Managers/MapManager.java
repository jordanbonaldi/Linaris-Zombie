
package net.neferett.Survivor.Managers;

import net.neferett.Survivor.BonusType;
import net.neferett.Survivor.Managers.WeaponsManager;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.Utils;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MapManager {
    private File m_file;
    private FileConfiguration m_yaml;
    private List<MapPart> m_mapPart = new LinkedList<MapPart>();
    private List<FrameShop> m_framesShops = new LinkedList<FrameShop>();
    private static MapManager instance = null;

    public static MapManager getInstance() {
        if (instance == null) {
            instance = new MapManager();
        }
        return instance;
    }

    private MapManager() {
        try {
            File dirs = new File("plugins/" + Survivor.getInstance().getName() + "/data");
            dirs.mkdirs();
            this.m_file = new File("plugins/" + Survivor.getInstance().getName() + "/data/map.yml");
            if (!this.m_file.exists()) {
                this.m_file.createNewFile();
            }
            this.m_yaml = YamlConfiguration.loadConfiguration((File)this.m_file);
            for (String uuid_str : this.m_yaml.getStringList("shops")) {
                UUID frame_uuid = UUID.fromString(uuid_str);
                ItemFrame frame = (ItemFrame)Utils.getByUUID(frame_uuid);
                ItemStack is = frame.getItem();
                BonusType bonus = BonusType.getByMaterial(is.getType());
                if (bonus != null) {
                    frame.setItem(bonus.getItem());
                    continue;
                }
                WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(is.getType());
                this.m_framesShops.add(new FrameShop(frame_uuid, weapon));
                ItemMeta im = is.getItemMeta();
                StringBuilder show = new StringBuilder();
                show.append("\u00a7r").append(ChatColor.stripColor((String)weapon.getName()));
                im.setDisplayName(show.toString());
                is.setItemMeta(im);
                frame.setItem(is);
            }
            LinkedList<Location> base_spawners = new LinkedList<Location>();
            for (String loc_str : this.m_yaml.getStringList("base.spawners")) {
                base_spawners.add(Utils.toLocation(loc_str, false));
            }
            MapPart base = new MapPart(this, "base", null, null, base_spawners);
            base.m_isAvailable = true;
            this.m_mapPart.add(base);
            this.m_mapPart.add(base);
            if (this.m_yaml.contains("parts")) {
                for (String part_id : this.m_yaml.getConfigurationSection("parts").getKeys(false)) {
                    ConfigurationSection part_section = this.m_yaml.getConfigurationSection("parts." + part_id);
                    String name = part_section.getString("name");
                    UUID buyEntity = UUID.fromString(part_section.getString("buy-entity"));
                    Wall wall = new Wall(Utils.toLocation(part_section.getString("wall.loc1"), true), Utils.toLocation(part_section.getString("wall.loc2"), true));
                    LinkedList<Location> spawners = new LinkedList<Location>();
                    for (String loc_str2 : part_section.getStringList("spawners")) {
                        spawners.add(Utils.toLocation(loc_str2, false));
                    }
                    MapPart part = new MapPart(this, name, buyEntity, wall, spawners);
                    this.m_mapPart.add(part);
                }
                for (MapPart part : this.getUnavailableMapParts()) {
                    ItemFrame frame = (ItemFrame)Utils.getByUUID(part.getBuyEntityUUID());
                    frame.setItem(Utils.item(Material.IRON_DOOR, Lang.get("ITEM_OPEN_DOOR").replaceAll("<door>", part.getName())));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBaseSpawner(Location spawner) {
        List spawners_str = this.m_yaml.getStringList("base.spawners");
        spawners_str.add(Utils.toString(spawner, false));
        this.m_yaml.set("base.spawners", (Object)spawners_str);
        try {
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPart(String name, UUID buyEntity, Wall wall, List<Location> spawners) {
        try {
            this.m_mapPart.add(new MapPart(this, name, buyEntity, wall, spawners));
            ConfigurationSection part = this.m_yaml.createSection("parts." + UUID.randomUUID().toString());
            part.set("name", (Object)name);
            part.set("buy-entity", (Object)buyEntity.toString());
            part.set("wall.loc1", (Object)Utils.toString(wall.getLoc1(), true));
            part.set("wall.loc2", (Object)Utils.toString(wall.getLoc2(), true));
            LinkedList<String> spawners_str = new LinkedList<String>();
            for (Location loc : spawners) {
                spawners_str.add(Utils.toString(loc, false));
            }
            part.set("spawners", spawners_str);
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MapPart> getMapParts() {
        return this.m_mapPart;
    }

    public MapPart getPartByBuyEntity(UUID uuid) {
        for (MapPart part : this.m_mapPart) {
            if (!uuid.equals(part.getBuyEntityUUID())) continue;
            return part;
        }
        return null;
    }

    public List<MapPart> getAvailableMapParts() {
        LinkedList<MapPart> mapPart = new LinkedList<MapPart>();
        for (MapPart part : this.m_mapPart) {
            if (!part.isAvailable()) continue;
            mapPart.add(part);
        }
        return mapPart;
    }

    public List<MapPart> getUnavailableMapParts() {
        LinkedList<MapPart> mapPart = new LinkedList<MapPart>();
        for (MapPart part : this.m_mapPart) {
            if (part.isAvailable()) continue;
            mapPart.add(part);
        }
        return mapPart;
    }

    public List<Location> getAvailableSpawners() {
        LinkedList<Location> spawners = new LinkedList<Location>();
        for (MapPart part : this.getAvailableMapParts()) {
            spawners.addAll(part.getSpawners());
        }
        return spawners;
    }

    public int getNextPrice() {
        return 500 + 100 * (this.getAvailableMapParts().size() - 1);
    }

    public FrameShop getFrameShopByEntity(UUID uuid) {
        for (FrameShop shop : this.m_framesShops) {
            if (!uuid.equals(shop.getFrameUUID())) continue;
            return shop;
        }
        return null;
    }

    public void addFrameShop(FrameShop shop) {
        this.m_framesShops.add(shop);
        this.addFrameShop(shop.getFrameUUID());
    }

    public void addFrameShop(UUID shop) {
        List frames = this.m_yaml.getStringList("shops");
        frames.add(shop.toString());
        this.m_yaml.set("shops", (Object)frames);
        try {
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FrameShop {
        private UUID m_frame;
        private WeaponTmpl m_weapon;

        public FrameShop(UUID frame, WeaponTmpl weapon) {
            this.m_frame = frame;
            this.m_weapon = weapon;
        }

        public UUID getFrameUUID() {
            return this.m_frame;
        }

        public WeaponTmpl getWeaponTmpl() {
            return this.m_weapon;
        }
    }

    public class MapPart {
        private String m_partName;
        private UUID m_buyEntity;
        private Wall m_wall;
        private List<Location> m_spawners;
        private boolean m_isAvailable;
        final  MapManager map;

        private MapPart(MapManager mapManager, String partName, UUID buyEntity, Wall wall, List<Location> spawners) {
            this.map = mapManager;
            this.m_spawners = new LinkedList<Location>();
            this.m_isAvailable = false;
            this.m_partName = ChatColor.translateAlternateColorCodes((char)'&', (String)partName);
            this.m_buyEntity = buyEntity;
            this.m_wall = wall;
            this.m_spawners = spawners;
        }

        public String getName() {
            return this.m_partName;
        }

        public UUID getBuyEntityUUID() {
            return this.m_buyEntity;
        }

        public List<Location> getSpawners() {
            return this.m_spawners;
        }

        public void open() {
            this.m_isAvailable = true;
            this.m_wall.breakWall();
        }

        public boolean isAvailable() {
            return this.m_isAvailable;
        }
    }

    public static class Wall {
        private Location m_loc1;
        private Location m_loc2;

        public Wall(Location loc1, Location loc2) {
            this.m_loc1 = new Location(loc1.getWorld(), (double)(loc1.getBlockX() <= loc2.getBlockX() ? loc1.getBlockX() : loc2.getBlockX()), (double)(loc1.getBlockY() <= loc2.getBlockY() ? loc1.getBlockY() : loc2.getBlockY()), (double)(loc1.getBlockZ() <= loc2.getBlockZ() ? loc1.getBlockZ() : loc2.getBlockZ()));
            this.m_loc2 = new Location(loc1.getWorld(), (double)(loc1.getBlockX() > loc2.getBlockX() ? loc1.getBlockX() : loc2.getBlockX()), (double)(loc1.getBlockY() > loc2.getBlockY() ? loc1.getBlockY() : loc2.getBlockY()), (double)(loc1.getBlockZ() > loc2.getBlockZ() ? loc1.getBlockZ() : loc2.getBlockZ()));
        }

        public void breakWall() {
            int x = this.m_loc1.getBlockX();
            while (x <= this.m_loc2.getBlockX()) {
                int y = this.m_loc1.getBlockY();
                while (y <= this.m_loc2.getBlockY()) {
                    int z = this.m_loc1.getBlockZ();
                    while (z <= this.m_loc2.getBlockZ()) {
                        this.m_loc1.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                        ++z;
                    }
                    ++y;
                }
                ++x;
            }
        }

        public boolean isWall(Location loc) {
            int x1 = this.m_loc1.getBlockX() - 1;
            int y1 = this.m_loc1.getBlockY() - 1;
            int z1 = this.m_loc1.getBlockZ() - 1;
            int x2 = this.m_loc2.getBlockX() + 1;
            int y2 = this.m_loc2.getBlockY() + 1;
            int z2 = this.m_loc2.getBlockZ() + 1;
            if (loc.getBlockX() >= x1 && loc.getBlockX() <= x2 && loc.getBlockY() >= y1 && loc.getBlockY() <= y2 && loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2) {
                return true;
            }
            return false;
        }

        public Location getLoc1() {
            return this.m_loc1.clone();
        }

        public Location getLoc2() {
            return this.m_loc2.clone();
        }
    }

}

