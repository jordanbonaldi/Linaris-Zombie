
package net.neferett.Survivor.Utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Lang;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.bossbar.BossBar;
import org.inventivetalent.bossbar.BossBarAPI;

public abstract class Utils {
    public static List<Player> m_playersJustConnected = new LinkedList<Player>();
    private static String packageName = Bukkit.getServer().getClass().getPackage().getName();
    private static String version = packageName.substring(packageName.lastIndexOf(".") + 1);

    public static void setInventory(Player player) {
        PlPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
        PlayerInventory inv = player.getInventory();
        if (Survivor.getGameState() == Survivor.GameState.LOBBY) {
            inv.clear();
            int slot = 0;
            ItemStack is_kit = new ItemStack(Material.NETHER_STAR);
            ItemMeta im_kit = is_kit.getItemMeta();
            im_kit.setDisplayName(Lang.get("ITEM_CHOOSE_KIT"));
            is_kit.setItemMeta(im_kit);
            inv.setItem(slot++, is_kit);
            player.updateInventory();
        } else if (tplayer.getPlayerState() == PlPlayer.PlayerState.DIE) {
            inv.clear();
            ItemStack itemMenu = new ItemStack(Material.COMPASS);
            ItemMeta metaMenu = itemMenu.getItemMeta();
            metaMenu.setDisplayName(Lang.get("ITEM_SPECTATOR_TELEPORT"));
            itemMenu.setItemMeta(metaMenu);
            inv.setItem(0, itemMenu);
            if (Survivor.getBoolean("enable-bungeecord")) {
                ItemStack itemLobby = new ItemStack(Material.BED);
                ItemMeta metaLobby = itemMenu.getItemMeta();
                metaLobby.setDisplayName(Lang.get("ITEM_SPECTATOR_LOBBY"));
                itemLobby.setItemMeta(metaLobby);
                inv.setItem(8, itemLobby);
            }
            player.updateInventory();
        } else if (Survivor.getGameState() == Survivor.GameState.GAME) {
            inv.clear();
            inv.setItem(0, new ItemStack(Material.WOOD_PICKAXE));
            tplayer.setGun(0);
        }
    }

    public static void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.setFireTicks(0);
        LinkedList effects = new LinkedList(player.getActivePotionEffects());
        List effects1 = new LinkedList(player.getActivePotionEffects());
        PotionEffect effect;
        for (Iterator localIterator = effects1.iterator(); localIterator.hasNext(); player.removePotionEffect(effect.getType())) effect = (PotionEffect)localIterator.next();

        player.updateInventory();
        player.updateInventory();
    }

    public static void tpToLobby(Player player) {
        if (Survivor.getBoolean("enable-bungeecord")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(Survivor.getString("bungeecord-lobby"));
            player.sendPluginMessage((Plugin)Survivor.getInstance(), "BungeeCord", out.toByteArray());
        } else {
            player.kickPlayer("Kicked by the plugin");
        }
    }

    public static Location toLocation(String string, boolean block) {
        String[] splitted = string.split(";");
        World world = Bukkit.getWorld((String)splitted[0]);
        if (world == null || splitted.length < 4) {
            return null;
        }
        Location location = new Location(world, (double)Integer.parseInt(splitted[1]), (double)Integer.parseInt(splitted[2]), (double)Integer.parseInt(splitted[3]));
        if (!block && splitted.length >= 6) {
            location.setYaw(Float.parseFloat(splitted[4]));
            location.setPitch(Float.parseFloat(splitted[5]));
        }
        return location;
    }

    public static String toString(Location l, boolean block) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getWorld().getName()).append(";");
        sb.append(l.getBlockX()).append(";").append(l.getBlockY()).append(";").append(l.getBlockZ());
        if (!block) {
            sb.append(";").append(l.getYaw()).append(";").append(l.getPitch());
        }
        return sb.toString();
    }

    public static <T> void sortList(List<T> list, Comparator<T> c) {
        int i = 0;
        while (i < list.size() - 1) {
            T obj2;
            T obj1 = list.get(i);
            if (c.compare(obj1, obj2 = list.get(i + 1)) > 0) {
                list.set(i, obj2);
                list.set(i + 1, obj1);
                if ((i -= 2) <= -2) {
                    ++i;
                }
            }
            ++i;
        }
    }

    public static String shortString(String str, int length) {
        return length > str.length() ? str : str.substring(0, length);
    }

    public static boolean locEqual(Location l1, Location l2) {
        if (l1 == null && l2 == null) {
            return true;
        }
        if (l1 == null || l2 == null) {
            return false;
        }
        if (l1.getWorld().getUID().equals(l2.getWorld().getUID()) && l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ()) {
            return true;
        }
        return false;
    }

    public static Entity getByUUID(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!entity.getUniqueId().equals(uuid)) continue;
                return entity;
            }
        }
        return null;
    }

    public static ItemStack item(Material m, String name) {
        ItemStack is = new ItemStack(m);
        return Utils.itemName(is, name);
    }

    public static ItemStack itemName(ItemStack is, String name) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes((char)'&', (String)name));
        is.setItemMeta(im);
        return is;
    }

    public static Object getMetadata(Metadatable meta, String key) {
        if (!meta.hasMetadata(key)) {
            return null;
        }
        for (MetadataValue value : meta.getMetadata(key)) {
            if (!value.getOwningPlugin().getName().equalsIgnoreCase(Survivor.getInstance().getName())) continue;
            return value.value();
        }
        return null;
    }

    public static void setMetadata(Metadatable meta, String key, Object obj) {
        meta.setMetadata(key, (MetadataValue)new FixedMetadataValue((Plugin)Survivor.getInstance(), obj));
    }

    public static Class<?> getNMSClass(String class_name) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + class_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getCraftClass(String class_name) {
        try {
            return Class.forName(String.valueOf(packageName) + "." + class_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendActionBar(Player player, String message) {
        CraftPlayer p = (CraftPlayer)player;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"" + message + "\"}"));
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        p.getHandle().playerConnection.sendPacket((Packet)ppoc);
    }

    public static void sendTitle(Player player, String message, ChatColor color) {
        CraftPlayer p = (CraftPlayer)player;
        IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"" + message + "\",color:" + color.name().toLowerCase() + "}"));
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(10, 50, 10);
        p.getHandle().playerConnection.sendPacket((Packet)title);
        p.getHandle().playerConnection.sendPacket((Packet)length);
    }

    public static void playSound(Location loc, String sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(loc, "guns.SW1911", 1.0f, 1.0f);
        }
    }

    public static void setBossBar(String message, float percent) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar bar = BossBarAPI.getBossBar((Player)player);
            if (bar == null) {
                if (percent <= 0.0f || message == null) continue;
                BossBarAPI.setMessage((Player)player, (String)message, (float)percent, (int)1000000);
                continue;
            }
            if (percent <= 0.0f || message == null) {
                bar.setVisible(false);
                BossBarAPI.removeBar((Player)player);
                continue;
            }
            if (!bar.isVisible()) {
                bar.setVisible(true);
            }
            bar.setHealth(percent);
        }
    }

    public static Player getNearestPlayer(Location location) {
        Player nearest = null;
        for (PlPlayer player : PlayersManager.getInstance().getGamers()) {
            if (!player.getPlayer().getWorld().getUID().equals(location.getWorld().getUID()) || player.getPlayerState() == PlPlayer.PlayerState.DIE || player.getPlayerState() == PlPlayer.PlayerState.HURT || nearest != null && player.getPlayer().getLocation().distanceSquared(location) >= nearest.getLocation().distanceSquared(location)) continue;
            nearest = player.getPlayer();
        }
        return nearest;
    }
}

