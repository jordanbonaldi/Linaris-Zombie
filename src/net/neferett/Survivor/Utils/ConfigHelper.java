
package net.neferett.Survivor.Utils;

import net.neferett.Survivor.BonusType;
import net.neferett.Survivor.Managers.FenceManager;
import net.neferett.Survivor.Managers.MapManager;
import net.neferett.Survivor.Managers.WeaponsManager;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Utils.Data;
import net.neferett.Survivor.Utils.Utils;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Gate;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

public class ConfigHelper
implements Listener {
    private List<ConfigUser> m_users = new LinkedList<ConfigUser>();
    private static ConfigHelper instance = null;

    public static ConfigHelper getInstance() {
        if (instance == null) {
            instance = new ConfigHelper();
        }
        return instance;
    }

    private ConfigHelper() {
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)Survivor.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Survivor.getGameState() == Survivor.GameState.CONFIG) {
            ConfigHelper.getInstance().launchConfig(event.getPlayer());
        }
    }

    public void launchConfig(Player player) {
        ConfigUser user = new ConfigUser(player);
        this.m_users.add(user);
        PlayerInventory inv = player.getInventory();
        inv.setItem(0, Utils.item(Material.STICK, "&bD\u00e9finir le spawn des joueurs (clique droit)"));
        inv.setItem(1, Utils.item(Material.BLAZE_ROD, "&bAjouter une barriere (clique droit sur une fence gate)"));
        inv.setItem(2, Utils.item(Material.SLIME_BALL, "&bAjouter un spawner de base"));
        inv.setItem(3, Utils.item(Material.FEATHER, user.getMapStep().getText()));
        inv.setItem(4, Utils.item(Material.ITEM_FRAME, "&bAjouter un shop"));
        inv.setItem(5, null);
        player.updateInventory();
        player.sendMessage((Object)ChatColor.DARK_GREEN + "------------------");
        player.sendMessage((Object)ChatColor.GREEN + "STICK : d\u00e9fini le spawn des joueurs");
        player.sendMessage((Object)ChatColor.GREEN + "BLAZE STICK : Ajoute une barri\u00e8re");
        player.sendMessage((Object)ChatColor.GREEN + "SLIME BALL : Ajoute un spawner de base");
        player.sendMessage((Object)ChatColor.GREEN + "PLUME : Ajoute une zone de map");
        player.sendMessage((Object)ChatColor.GREEN + "ITEM FRAME : Ajoute un shop");
        player.sendMessage((Object)ChatColor.DARK_GREEN + "------------------");
    }

    public ConfigUser getConfigUser(Player player) {
        for (ConfigUser user : this.m_users) {
            if (!user.getUUID().equals(player.getUniqueId())) continue;
            return user;
        }
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING) {
            return;
        }
        if (event.getSlot() <= 5) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ConfigUser user = this.getConfigUser(player);
        if (user == null) {
            return;
        }
        ItemStack is = event.getItem();
        if (is == null) {
            return;
        }
        if (is.getType() == Material.STICK) {
            Data.getInstance().setSpawn(player.getLocation());
            player.sendMessage((Object)ChatColor.GREEN + "Spawn d\u00e9fini !");
        } else if (is.getType() == Material.SLIME_BALL) {
            MapManager.getInstance().addBaseSpawner(player.getLocation());
            player.sendMessage((Object)ChatColor.GREEN + "Spawner de base ajouter ...");
        } else if (is.getType() == Material.FEATHER) {
            if (user.getMapStep() == MapStep.SPAWNERS) {
                user.addSpawner(player.getLocation());
                player.sendMessage((Object)ChatColor.GREEN + "Spawner ajouter ...");
            }
        } else if (is.getType() == Material.DIAMOND && user.getMapStep() == MapStep.SPAWNERS) {
            user.nextMapStep(MapStep.VALIDATE);
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (is.getType() == Material.BLAZE_ROD) {
            if (block.getType() != Material.FENCE_GATE) {
                return;
            }
            FenceManager.getInstance().addFence(block.getLocation(), (Gate)block.getState().getData()).reset();
            player.sendMessage((Object)ChatColor.GREEN + "Barriere ajout\u00e9 !");
        } else if (is.getType() == Material.FEATHER) {
            if (user.getMapStep() == MapStep.LOC1) {
                user.setDoorLoc1(block.getLocation());
                user.nextMapStep(MapStep.LOC2);
            } else if (user.getMapStep() == MapStep.LOC2) {
                user.setDoorLoc2(block.getLocation());
                user.nextMapStep(MapStep.NAME);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ConfigUser user = this.getConfigUser(player);
        if (user == null) {
            return;
        }
        if (user.getMapStep() == MapStep.NAME) {
            user.setDoorDoorName(event.getMessage());
            user.nextMapStep(MapStep.SPAWNERS);
            PlayerInventory inv = player.getInventory();
            inv.setItem(4, Utils.item(Material.DIAMOND, "&2Valider la zone (irr\u00e9versible)"));
            player.updateInventory();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ConfigUser user = this.getConfigUser(player);
        if (user == null) {
            return;
        }
        ItemStack is = player.getItemInHand();
        Entity entity = event.getRightClicked();
        if (is.getType() == Material.FEATHER) {
            if (user.getMapStep() == MapStep.FRAME) {
                if (entity.getType() != EntityType.ITEM_FRAME) {
                    return;
                }
                user.setDoorBuyEntity(entity.getUniqueId());
                user.nextMapStep(MapStep.LOC1);
            }
            event.setCancelled(true);
        } else if (is.getType() == Material.ITEM_FRAME) {
            if (user.getMapStep() == MapStep.FRAME) {
                if (entity.getType() != EntityType.ITEM_FRAME) {
                    return;
                }
                ItemFrame frame = (ItemFrame)entity;
                ItemStack item_target = frame.getItem();
                if (item_target != null) {
                    BonusType bonus;
                    WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(item_target.getType());
                    if (weapon != null) {
                        MapManager.getInstance().addFrameShop(new MapManager.FrameShop(entity.getUniqueId(), weapon));
                        player.sendMessage((Object)ChatColor.GREEN + "Shop ajouter pour " + weapon.getName());
                    }
                    if ((bonus = BonusType.getByMaterial(item_target.getType())) != null) {
                        MapManager.getInstance().addFrameShop(entity.getUniqueId());
                        player.sendMessage((Object)ChatColor.GREEN + "Shop ajouter pour " + bonus.getName());
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    private class ConfigUser {
        private Player m_player;
        private MapStep m_mapStep;
        private UUID m_doorBuyUUID;
        private Location m_doorLoc1;
        private Location m_doorLoc2;
        private String m_doorName;
        private List<Location> m_spawners;

        public ConfigUser(Player player) {
            this.m_spawners = new LinkedList<Location>();
            this.m_player = player;
            this.m_mapStep = MapStep.FRAME;
        }

        public UUID getUUID() {
            return this.m_player.getUniqueId();
        }

        public MapStep getMapStep() {
            return this.m_mapStep;
        }

        public void nextMapStep(MapStep mapStep) {
            this.m_mapStep = mapStep;
            if (this.m_mapStep != MapStep.VALIDATE) {
                PlayerInventory inv = this.m_player.getInventory();
                Utils.itemName(inv.getItem(3), this.m_mapStep.getText());
                inv.setItem(4, null);
                this.m_player.updateInventory();
            }
            if (this.m_mapStep != MapStep.FRAME) {
                this.m_player.sendMessage(this.m_mapStep.getText());
            }
            if (this.m_mapStep == MapStep.VALIDATE) {
                MapManager.getInstance().addPart(this.m_doorName, this.m_doorBuyUUID, new MapManager.Wall(this.m_doorLoc1, this.m_doorLoc2), this.m_spawners);
                this.validMapPart();
                this.nextMapStep(MapStep.FRAME);
            }
        }

        public void setDoorBuyEntity(UUID uuid) {
            this.m_doorBuyUUID = uuid;
        }

        public void setDoorLoc1(Location loc) {
            this.m_doorLoc1 = loc;
        }

        public void setDoorLoc2(Location loc) {
            this.m_doorLoc2 = loc;
        }

        public void setDoorDoorName(String name) {
            this.m_doorName = name;
        }

        public void addSpawner(Location loc) {
            this.m_spawners.add(loc);
        }

        private void validMapPart() {
            this.m_spawners = new LinkedList<Location>();
            this.m_mapStep = MapStep.FRAME;
            this.m_doorBuyUUID = null;
            this.m_doorLoc1 = null;
            this.m_doorLoc2 = null;
        }
    }

    private static enum MapStep {
        FRAME("&bClique droit sur la frame de la port"), 
        LOC1("&6Clique droit sur la 1er position de la porte"), 
        LOC2("&6Clique droit sur la 2er position de la porte"), 
        NAME("&eEntrez le nom de la porte dans le chat"), 
        SPAWNERS("&6Ajouter un spawner dans cette zone (valider la zone : emeraude)"), 
        VALIDATE("&2Zone ajouter !");

        private String m_text;

        private MapStep(String text) { this.m_text = ChatColor.translateAlternateColorCodes('&', text); }

        public String getText() {
          return this.m_text;
        }
      }

}

