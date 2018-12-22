package net.neferett.Survivor.Listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.icrotz.gameserver.api.PlayerLocal;
import fr.icrotz.gameserver.api.PlayerLocalManager;
import fr.icrotz.gameserver.events.ReturnToLobbyEvent;
import net.neferett.Survivor.BonusType;
import net.neferett.Survivor.DropType;
import net.neferett.Survivor.Survivor;
import net.neferett.Survivor.Survivor.GameState;
import net.neferett.Survivor.Managers.FenceManager;
import net.neferett.Survivor.Managers.MapManager;
import net.neferett.Survivor.Managers.PlayersManager;
import net.neferett.Survivor.Managers.WeaponsManager;
import net.neferett.Survivor.Managers.SubObjects.PlPlayer;
import net.neferett.Survivor.Timers.GameTimer;
import net.neferett.Survivor.Utils.Lang;
import net.neferett.Survivor.Utils.RoundInfo;
import net.neferett.Survivor.Utils.Utils;
import net.neferett.Survivor.Weapons.GunInstance;
import net.neferett.Survivor.Weapons.Templates.BombardierTmpl;
import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import net.neferett.Survivor.Weapons.Templates.NukeTmpl;
import net.neferett.Survivor.Weapons.Templates.WeaponTmpl;

public class GameListener
  implements Listener
{
	
	
	@EventHandler
	public void onPlayerQuit(ReturnToLobbyEvent e) {
		 Player player = e.getTarget();
		 if (Survivor.getGameState() == GameState.LOBBY) return;
		 PlayerLocal pl = PlayerLocalManager.get().getPlayerLocal(player.getName());
		 player.sendMessage("§6-----------------------------------------------------");
		 player.sendMessage("§6Fin de partie sur §b" + Bukkit.getServerName());
		 player.sendMessage("§7Gain total de §eCoins §7sur la partie : §e" + String.format("%.2f", pl.getGainedEC()));
		 player.sendMessage("§7Gain total de §bCrédits §7sur la partie : §e" + String.format("%.2f", pl.getGainedLC()));
		 player.sendMessage("§6-----------------------------------------------------");
	}
	
	
	
  @EventHandler
  public void onEntityDeath(EntityDeathEvent event)
  {
    event.getDrops().clear();
    event.setDroppedExp(0);

    int nextInt = new Random().nextInt(RoundInfo.getTheRoundInfo().getEntityAmount() * DropType.values().length / 2);
    if (nextInt == 0) event.getDrops().add(DropType.NUKE.getItem());
    else if (nextInt == 1) event.getDrops().add(DropType.BOMBARDIER.getItem());
    else if (nextInt == 2) event.getDrops().add(DropType.MORT_INSTANT.getItem());
    else if (nextInt == 3) event.getDrops().add(DropType.MUNO_MAX.getItem());

    if (Survivor.getGameState() != Survivor.GameState.GAME) return;
    if ((event.getEntityType() != RoundInfo.getTheRoundInfo().getEntityType()) && (event.getEntityType() != EntityType.PIG_ZOMBIE)) return;

    LivingEntity zombie = event.getEntity();

    if (event.getEntityType() == EntityType.PIG_ZOMBIE) {
      Utils.setBossBar(null, 0.0F);
    }

    RoundInfo round = RoundInfo.getTheRoundInfo();
    round.setLastZombieKill();
    round.removeEntityAlive();

    Player killer = null;
    if (zombie.getKiller() != null) {
      killer = zombie.getKiller();
    }
    else if ((zombie.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
      EntityDamageByEntityEvent event_damage = (EntityDamageByEntityEvent)zombie.getLastDamageCause();
      if (((event_damage.getDamager() instanceof TNTPrimed)) && 
        (event_damage.getDamager().hasMetadata("owner"))) {
        killer = (Player)Utils.getMetadata(event_damage.getDamager(), "owner");
      }

    }

    if (killer != null)
      PlayersManager.getInstance().getPlayer(killer).addMoney(Survivor.getInt("money-by-kill"));
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
  {
    if (event.getEntityType() != EntityType.PLAYER) {
      event.setDamage(1.0D);
      return;
    }

    if (event.getDamager().getType() == EntityType.PLAYER) {
      PlPlayer player = PlayersManager.getInstance().getPlayer((Player)event.getDamager());

      PlPlayer target = PlayersManager.getInstance().getPlayer((Player)event.getEntity());
      if ((target.getPlayerState() == PlPlayer.PlayerState.HURT) && (player.getPlayerState() == PlPlayer.PlayerState.ALIVE)) {
        player.setCureTarget(target);

        target.getPlayer().sendMessage(Lang.get("REANIM_YOU").replaceAll("<player>", player.getName()));
        player.getPlayer().sendMessage(Lang.get("REANIM_HIM").replaceAll("<player>", target.getName()));
      }

      event.setCancelled(true);
      return;
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (Survivor.getGameState() != Survivor.GameState.GAME) return;

    if (event.getEntityType() == EntityType.PIG_ZOMBIE) {
      PigZombie boss = (PigZombie)event.getEntity();
      Utils.setBossBar("Boss", (float)(boss.getHealth() / boss.getMaxHealth() * 100.0D));
    }

    if (event.getEntityType() != EntityType.PLAYER) return;

    if (!GameTimer.isRound()) {
      event.setCancelled(true);
      return;
    }

    Player player = (Player)event.getEntity();
    PlPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
    if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
      event.setCancelled(true);
      return;
    }

    if ((tplayer.getPlayerState() == PlPlayer.PlayerState.DIE) || 
      (tplayer.getPlayerState() == PlPlayer.PlayerState.HURT))
    {
      event.setCancelled(true);
      return;
    }

    double health = player.getHealth() - event.getDamage();

    if (health <= 0.0D) {
      Bukkit.broadcastMessage(Lang.get("PLAYER_HURT").replaceAll("<player>", player.getName()));
      if (tplayer.getPlayerState() == PlPlayer.PlayerState.CURE) {
        tplayer.getCureTarget().setPlayerState(PlPlayer.PlayerState.HURT);
      }
      tplayer.setPlayerState(PlPlayer.PlayerState.HURT);
      player.setHealth(player.getMaxHealth());

      Player nearest = Utils.getNearestPlayer(player.getLocation());
      if (nearest == null) return;
      for (Entity e : Survivor.getWorldGame().getEntities())
        if ((e.getType() == RoundInfo.getTheRoundInfo().getEntityType()) || (e.getType() == EntityType.PIG_ZOMBIE)) {
          Creature creature = (Creature)e;
          if ((creature.getTarget() != null) && (creature.getTarget().getUniqueId().equals(player.getUniqueId())))
            creature.setTarget(nearest);
        }
    }
  }

  @EventHandler
  public void onEntityTarget(EntityTargetEvent event)
  {
    if ((event.getTarget() != null) && ((event.getTarget() instanceof Player))) {
      PlPlayer player = PlayersManager.getInstance().getPlayer((Player)event.getTarget());
      if ((player.getPlayerState() == PlPlayer.PlayerState.DIE) || (player.getPlayerState() == PlPlayer.PlayerState.HURT)) {
        event.setCancelled(true);
        Player nearest = Utils.getNearestPlayer(player.getPlayer().getLocation());
        ((Creature)event.getEntity()).setTarget(nearest);
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
  {
    if (Survivor.getGameState() != Survivor.GameState.GAME) return;

    Player player = event.getPlayer();
    PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);

    final Block block = event.getClickedBlock();
    if (block == null) return;

    if (block.getType() == Material.FENCE_GATE) {
      if (FenceManager.getInstance().isFence(block.getLocation())) {
        event.setCancelled(true);
        plPlayer.addMoney(FenceManager.FENCE_PRICE);
        FenceManager.Fence fence = FenceManager.getInstance().getFence(block.getLocation());
        fence.reset();
      }
    }
    else if (block.getType() == Material.DIAMOND_ORE) {
      int slot = player.getInventory().getHeldItemSlot();

      if (slot < 2) {
        GunInstance gun = plPlayer.getGun(slot);
        if (gun.getLevel() <= 4)
          try {
            int level = gun.getLevel() + 1;
            GunWeaponTmpl.GunLevel gunLevel = gun.getGun().getGunLevel(level);
            plPlayer.subMoney(gunLevel.getPrice());
            gun.addLevel();
            player.getInventory().getItem(slot).addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
            plPlayer.updateLore(slot);

            Bukkit.broadcastMessage(Lang.get("WEAPON_UP")
              .replaceAll("<player>", player.getName())
              .replaceAll("<level>", Integer.toString(level))
              .replaceAll("<weapon>", gun.getGun().getName()));
          } catch (PlPlayer.NotEnoughMoneyException localNotEnoughMoneyException) {
          }
      }
      event.setCancelled(true);
    }
    else if (block.getType() == Material.ENDER_CHEST)
    {
      int slot = player.getInventory().getHeldItemSlot();
      if (slot < 2)
        try {
          plPlayer.subMoney(250);
          GunWeaponTmpl gun = WeaponsManager.getInstance().getRandomGunWeapon(plPlayer);
          if (!gun.isMainWeapon()) slot = 8;
          player.getInventory().setItem(slot, new ItemStack(gun.getMaterial()));
          plPlayer.setGun(slot);

          block.setType(Material.AIR);
          final byte data = block.getData();
          block.getWorld().strikeLightningEffect(block.getLocation());
          Bukkit.getScheduler().scheduleSyncDelayedTask(Survivor.getInstance(), new Runnable()
          {
            public void run() {
              block.setType(Material.ENDER_CHEST);
              block.setData(data);
            }
          }
          , 10L);

          Bukkit.broadcastMessage(Lang.get("WEAPON_MYSTERY")
            .replaceAll("<player>", player.getName())
            .replaceAll("<weapon>", gun.getName()));
        }
        catch (PlPlayer.NotEnoughMoneyException localNotEnoughMoneyException1) {
        }
      else player.sendMessage(Lang.get("USE_MAIN_SLOT"));

      event.setCancelled(true);
    }
    else if (block.getType() == Material.CAKE_BLOCK) {
      int slot = player.getInventory().getHeldItemSlot();

      if (slot < 2) {
        GunInstance gun = plPlayer.getGun(slot);
        if (plPlayer.isRechargement()) return;
        if (gun.getBullet().getCharger() == gun.getGun().getBullet().getCharger())
        {
          player.sendMessage(Lang.get("MUNO_FULL").replaceAll("<weapon>", gun.getGun().getName()));
        }
        else {
          gun.getBullet().setCharger(gun.getGun().getBullet().getCharger());
          plPlayer.updateBullet(slot);
          byte data = (byte)(block.getData() + 1);
          if (data <= 5) block.setData(data); else
            block.setType(Material.AIR);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (Survivor.getGameState() != Survivor.GameState.GAME) return;
    try
    {
      Player player = event.getPlayer();
      PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);

      int slot = player.getInventory().getHeldItemSlot();
      if (slot < 2) {
        Set transparent = new HashSet();
        transparent.add(Material.AIR);
        Block target = player.getTargetBlock(transparent, 2);
        if (target == null) return;

        if (target.getType() == Material.DIAMOND_ORE) {
          GunInstance gun = plPlayer.getGun(slot);
          if (gun.getLevel() <= gun.getGun().getMaxLevel() - 1) {
            int level = gun.getLevel() + 1;
            GunWeaponTmpl.GunLevel gunLevel = gun.getGun().getGunLevel(level);
            Utils.sendActionBar(player, "§2Améliorez votre " + gun.getGun().getName() + " §2au niveau §a" + level + " §2pour §e" + gunLevel.getPrice() + "§3$");
          }
        }
        else if (target.getType() == Material.ENDER_CHEST) {
          Utils.sendActionBar(player, "§2Achetez une arme aléatoire pour §e250§3$");
        }
      }
    } catch (Exception localException) {
    }
  }

  @EventHandler
  public void onPlayerMoveFrame(PlayerMoveEvent event) { if (Survivor.getGameState() != Survivor.GameState.GAME) return;

    Player player = event.getPlayer();
    Location pLoc = player.getLocation();

    Set transparent = new HashSet();
    transparent.add(Material.AIR);
    Block target = player.getTargetBlock(transparent, 3);
    if (target == null) return;
    
    ArrayList<Entity> e = new ArrayList<Entity>();
    e.addAll(player.getNearbyEntities(3.0D, 3.0D, 3.0D));

    for (Entity entity : e)
      if (entity.getType() == EntityType.ITEM_FRAME) {
        ItemFrame frame = (ItemFrame)entity;
        if (frame.getItem() == null) return;

        BlockFace face = frame.getAttachedFace();

        Location fLoc = frame.getLocation();
        Location bLoc = new Location(frame.getWorld(), fLoc.getX() + face.getModX(), 
          fLoc.getY() + face.getModY(), 
          fLoc.getZ() + face.getModZ());

        Block fBlock = bLoc.getBlock();

        if ((target.getX() == fBlock.getX()) && (target.getY() == fBlock.getY()) && (target.getZ() == fBlock.getZ())) {
          if ((face == BlockFace.NORTH) && (pLoc.getZ() < fLoc.getZ())) return;
          if ((face == BlockFace.SOUTH) && (pLoc.getZ() > fLoc.getZ())) return;
          if ((face == BlockFace.WEST) && (pLoc.getX() < fLoc.getX())) return;
          if ((face == BlockFace.EAST) && (pLoc.getX() > fLoc.getX())) return;

          WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(frame.getItem().getType());
          if (weapon != null) {
            StringBuilder show = new StringBuilder();
            show.append("§r§6").append(weapon.getName()).append(" - §e");
            show.append(weapon.getPrice()).append("§b$");
            if ((weapon instanceof GunWeaponTmpl)) {
              GunWeaponTmpl.GunLevel level = ((GunWeaponTmpl)weapon).getGunLevel(1);
              show.append("§r : §6Munitions: §e").append(weapon.getPrice() / 2).append("§b$");
              show.append("§r : §4Dégâts: §c").append(level.getMinDamage()).append("§1/§c").append(level.getMaxDamage());
            }

            Utils.sendActionBar(player, show.toString());
          }

          MapManager.MapPart part = MapManager.getInstance().getPartByBuyEntity(entity.getUniqueId());
          if (part != null) {
            StringBuilder show = new StringBuilder();
            show.append("§r§bPrix de §e").append(part.getName()).append(" §b: §e");
            show.append(MapManager.getInstance().getNextPrice()).append("§b$");
            Utils.sendActionBar(player, show.toString());
          }
        }
      }
  }

  @EventHandler
  public void onEntityDamageByExplosion(EntityDamageEvent event)
  {
    if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) event.setCancelled(true); 
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
  {
    try
    {
      Player player = event.getPlayer();
      PlPlayer plPlayer = PlayersManager.getInstance().getPlayer(player);

      Entity entity = event.getRightClicked();
      if (entity.getType() != EntityType.ITEM_FRAME) return;
      ItemFrame frame = (ItemFrame)entity;
      if (frame.getItem() == null) return;

      MapManager.MapPart part = MapManager.getInstance().getPartByBuyEntity(entity.getUniqueId());
      if (part != null) {
        plPlayer.subMoney(MapManager.getInstance().getNextPrice());
        entity.remove();
        part.open();

        Location l = entity.getLocation();
        l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 1.0F, false, false);

        Bukkit.broadcastMessage(Lang.get("DOOR_OPEN")
          .replaceAll("<player>", player.getName())
          .replaceAll("<door>", part.getName())
          .replaceAll("<price>", Integer.toString(MapManager.getInstance().getNextPrice())));

        event.setCancelled(true);
      }

      BonusType bonus = BonusType.getByMaterial(frame.getItem().getType());
      if (bonus != null) {
        if (plPlayer.hasBonus(bonus))
          player.sendMessage(Lang.get("BONUS_ALREADY_BUY"));
        else
          try
          {
            bonus.buy(plPlayer);

            Bukkit.broadcastMessage(Lang.get("BONUS_BUY")
              .replaceAll("<player>", player.getName())
              .replaceAll("<bonus>", bonus.getName()));
          }
          catch (PlPlayer.NotEnoughMoneyException localNotEnoughMoneyException)
          {
          }
      }
      MapManager.FrameShop shop = MapManager.getInstance().getFrameShopByEntity(entity.getUniqueId());
      if (shop != null) {
        PlayerInventory inv = player.getInventory();
        int slot = plPlayer.getGunSlot((GunWeaponTmpl)shop.getWeaponTmpl());

        if (((shop.getWeaponTmpl() instanceof GunWeaponTmpl)) && (slot >= 0))
        {
          GunInstance gun = plPlayer.getGun(slot);
          if (gun.getBullet().equals(gun.getGun().getBullet()))
          {
            player.sendMessage(Lang.get("MUNO_FULL").replaceAll("<weapon>", shop.getWeaponTmpl().getName()));
          }
          else {
            plPlayer.subMoney(shop.getWeaponTmpl().getPrice() / 2);
            plPlayer.getGun(slot).full();
            plPlayer.updateBullet(slot);
            Bukkit.broadcastMessage(Lang.get("MUNO_BUY")
              .replaceAll("<player>", player.getName())
              .replaceAll("<weapon>", shop.getWeaponTmpl().getName()));
          }
        }
        else
        {
          slot = inv.getHeldItemSlot();
          if (slot > 1) {
            if (inv.getItem(0) == null) slot = 0;
            else if (inv.getItem(1) == null) slot = 1;
          }

          if (slot < 2) {
            plPlayer.subMoney(shop.getWeaponTmpl().getPrice());
            inv.setItem(slot, new ItemStack(shop.getWeaponTmpl().getMaterial()));
            plPlayer.setGun(slot);

            Bukkit.broadcastMessage(Lang.get("WEAPON_BUY")
              .replaceAll("<player>", player.getName())
              .replaceAll("<weapon>", shop.getWeaponTmpl().getName()));
          }
          else {
            player.sendMessage(Lang.get("USE_MAIN_SLOT"));
          }
        }
        event.setCancelled(true);
      }
    } catch (PlPlayer.NotEnoughMoneyException localNotEnoughMoneyException1) {
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) { if (event.getInventory().getType() == InventoryType.CRAFTING)
      event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent event)
  {
    Player player = event.getPlayer();
    ItemStack item = event.getItem().getItemStack();

    WeaponTmpl weapon = WeaponsManager.getInstance().getWeapon(item.getType());
    if (event.getItem().hasMetadata("launch")) return;

    if (((weapon instanceof BombardierTmpl)) || ((weapon instanceof NukeTmpl))) {
      PlayerInventory inv = player.getInventory();
      for (int slot = 3; slot <= 6; slot++) {
        if (inv.getItem(slot) == null) {
          inv.setItem(slot, Utils.itemName(item, weapon.getName()));
          break;
        }
      }

    }

    event.getItem().remove();
    event.setCancelled(true);
  }
}