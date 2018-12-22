
package net.neferett.Survivor.Utils;

import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Utils;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Data {
    private File m_file;
    private FileConfiguration m_yaml;
    private Location m_spawn;
    private static Data instance = null;

    public static Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }

    private Data() {
        try {
            File dirs = new File("plugins/" + Survivor.getInstance().getName() + "/data");
            dirs.mkdirs();
            this.m_file = new File("plugins/" + Survivor.getInstance().getName() + "/data/data.yml");
            if (!this.m_file.exists()) {
                this.m_file.createNewFile();
            }
            this.m_yaml = YamlConfiguration.loadConfiguration((File)this.m_file);
            this.m_spawn = this.m_yaml.contains("spawn") ? Utils.toLocation(this.m_yaml.getString("spawn"), false) : Survivor.getWorldGame().getSpawnLocation();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getLobby() {
        if (this.m_yaml.contains("lobby")) {
            return Utils.toLocation(this.m_yaml.getString("lobby"), false);
        }
        return ((World)Bukkit.getWorlds().get(0)).getSpawnLocation();
    }

    public void setLobby(Location lobby) {
        try {
            this.m_yaml.set("lobby", (Object)Utils.toString(lobby, false));
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getSpawn() {
        return this.m_spawn;
    }

    public void setSpawn(Location spawn) {
        try {
            this.m_yaml.set("spawn", (Object)Utils.toString(spawn, false));
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getCentre() {
        return this.getSpawn();
    }
}

