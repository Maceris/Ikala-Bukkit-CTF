package com.ikalagaming.bukkit.ctf;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

import com.ikalagaming.bukkit.ctf.event.GameItemUsed;

public class MyPlayerListener implements Listener {
	Ctf ctf = (Ctf) Bukkit.getPluginManager().getPlugin("CTF");

	private void createArenaData(PlayerInteractEvent event) {
		if (!this.ctf.hasArenaCreationData(event.getPlayer())) {
			// create arena data for the player if it does not exist
			this.ctf.addAreanCreationData(event.getPlayer());
		}
	}

	private void handleTools(PlayerInteractEvent event) {
		if (event.hasItem()) {
			if (event.getItem().hasItemMeta()) {
				if (event.getItem().getItemMeta().hasLore()) {
					for (String str : event.getItem().getItemMeta().getLore()) {
						if (str.equalsIgnoreCase("CTF Tool [Corners]")) {
							this.toolCorner(event);
						}
						if (str.equalsIgnoreCase("CTF Tool [Spawns]")) {
							this.toolSpawn(event);
						}
						if (str.equalsIgnoreCase("CTF Tool [Flags]")) {
							this.toolFlag(event);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (this.ctf.getConfig().getBoolean("stopFireInArenas")) {
			if (this.ctf.isInArena(event.getBlock().getLocation(), 0)) {

				event.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void onBlockForm(BlockFormEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (event.getBlock().getType().equals(Material.FIRE)) {
			if (this.ctf.getConfig().getBoolean("stopFireInArenas")) {
				if (this.ctf.isInArena(event.getBlock().getLocation(), 0)) {
					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getBlock().getType().equals(Material.FIRE)) {
			if (this.ctf.getConfig().getBoolean("stopFireInArenas")) {
				if (this.ctf.isInArena(event.getBlock().getLocation(), 0)) {
					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getBlock().getType().equals(Material.FIRE)) {
			if (this.ctf.getConfig().getBoolean("stopFireInArenas")) {
				if (this.ctf.isInArena(event.getBlock().getLocation(), 0)) {
					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!(event.getEntity().getType().equals(EntityType.PLAYER))) {
			return;
		}
		if (!this.ctf.isInGame(((Player) event.getEntity()))) {
			return;
		}
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
		if (event.getCause() == DamageCause.DROWNING) {
			event.setCancelled(true);
			((Player) event.getEntity()).setRemainingAir(((Player) event
					.getEntity()).getMaximumAir());
		}
		if (event.getCause() == DamageCause.CONTACT) {
			event.setCancelled(true);
		}
		Player player = (Player) event.getEntity();
		// If it would have killed the player
		if ((player.getHealth() - event.getDamage()) < 1) {
			// respawn them (reset health, armor, etc)
			this.ctf.getGame(player).respawnPlayer(player);
			// cancel the damage so they start off healed
			event.setCancelled(true);
		}

	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
			// ignore the event if it is not a player
			return;
		}
		if (!this.ctf.isInGame((Player) event.getEntity())) {
			return;// ignore players hurt not in a game
		}

		if (!event.getDamager().getType().equals(EntityType.PLAYER)
				&& !event.getDamager().getType().equals(EntityType.ZOMBIE)) {
			event.setCancelled(true);
			return;// ignore damage from non-players
		}
		if (!this.ctf.isInGame((LivingEntity) event.getDamager())) {
			// only in-game players can hurt other in-game players
			event.setCancelled(true);
			return;
		}
		Player player = (Player) event.getEntity();
		LivingEntity damager = (LivingEntity) event.getDamager();
		if (!this.ctf.areTeammates(player, damager)) {
			if (damager.getEquipment().getItemInHand().getType() == Material.IRON_SWORD) {
				// reduce sword damage
				event.setDamage(4);// TODO handle other classes weapons
			}
			if (damager.getEquipment().getItemInHand() == null) {
				// increase punch damage
				event.setDamage(2);
			}
		}
		else {
			event.setCancelled(true);
			return;
		}
		if ((player.getHealth() - event.getDamage()) < 1) {
			// If it would have killed the player
			this.ctf.getGame(player).respawnPlayer(player);

			// respawn them (reset health, armor, etc)
			event.setCancelled(true);
			// cancel the damage so they start off healed
			this.ctf.getGame(damager).increaseKills(damager);
		}

	}

	@EventHandler
	public void onEntityPortalEvent(EntityPortalEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
			// ignore the event if it is not a player
			return;
		}
		if (this.ctf.isInGame((Player) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (this.ctf.getConfig().getBoolean("stopExplosionsInArenas")) {
			List<Block> affectedBlocks = event.blockList();
			for (Block block : affectedBlocks) {
				if (this.ctf.isInArena(block.getLocation(), 0)) {
					event.blockList().remove(block);
				}
			}
		}

	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!event.isCancelled()) {
			if (event.getEntity() instanceof Player) {
				if (this.ctf.isInGame((Player) event.getEntity())) {
					event.setFoodLevel(20);
				}
			}
		}
	}

	@EventHandler
	public void onInventorySlotClick(InventoryClickEvent event) {
		if (!event.isCancelled()) {
			if (event.getWhoClicked() instanceof Player) {
				if (this.ctf.isInGame(((Player) event.getWhoClicked()))) {
					if (event.getSlotType().equals(SlotType.ARMOR)) {
						event.setCancelled(true);// disallows removing armor
					}
					if (event.getSlotType().equals(SlotType.CRAFTING)) {
						event.setCancelled(true);// disallows crafting
					}
					if (event.getSlotType().equals(SlotType.FUEL)) {
						event.setCancelled(true);// disallows furnaces
					}
					if (event.getSlotType().equals(SlotType.RESULT)) {
						event.setCancelled(true);// disallows taking results
													// from crafting/furnace
					}
				}
			}
		}
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if (!event.isCancelled()) {
			for (MetadataValue isFlagMetadata : event.getEntity().getMetadata(
					"isCTFFlag")) {
				if (isFlagMetadata.asBoolean()) {
					event.setCancelled(true);
					for (MetadataValue isDespawnableMetadata : event
							.getEntity().getMetadata("despawnable")) {
						if (isDespawnableMetadata.asBoolean()) {
							event.setCancelled(false);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		if (event.isCancelled()) {
			return;
		}// TODO dont pick up flag when out of game
		if (!this.ctf.isInGame(event.getPlayer())) {
			if (event.getItem().hasMetadata("isCTFFlag")) {
				event.setCancelled(true);
			}
			return;
		}
		if (this.ctf.getGame(event.getPlayer()).isActive()) {
			Game game = this.ctf.getGame(event.getPlayer());
			Player player = event.getPlayer();
			boolean isFlag = false;
			if (event.getItem().hasMetadata("isCTFFlag")) {
				isFlag = true;
			}
			if (isFlag) {
				if (event.getItem().getItemStack().getItemMeta().hasLore()) {
					for (String str : event.getItem().getItemStack()
							.getItemMeta().getLore()) {
						if (str.equalsIgnoreCase("Blue Flag")) {
							if (game.isOnBlueTeam(player)) {
								event.setCancelled(true);
								if (game.getBlueFlagAwayFromSpawn()) {
									event.getItem().remove();
									game.spawnBlueFlag();
								}
							}
							else {
								if (!game.getBlueFlagAwayFromSpawn()) {
									game.setBlueFlagAwayFromSpawn(true);
								}
								game.addFlagCarrier(event.getPlayer());
							}
						}
						if (str.equalsIgnoreCase("Red Flag")) {
							if (game.isOnRedTeam(player)) {
								event.setCancelled(true);
								if (game.getRedFlagAwayFromSpawn()) {
									event.getItem().remove();
									game.spawnRedFlag();
								}
							}
							else {
								if (!game.getRedFlagAwayFromSpawn()) {
									game.setRedFlagAwayFromSpawn(true);
								}
								game.addFlagCarrier(event.getPlayer());
							}
						}
					}
				}
			}
			if (event.getItem().getItemStack().getType() == Material.IRON_SWORD) {
				if (event.getPlayer().getInventory()
						.contains(Material.IRON_SWORD)) {
					event.setCancelled(true);
				}
			}
			if (event.getItem().getItemStack().getType() == Material.SUGAR) {
				if (event.getPlayer().getInventory().contains(Material.SUGAR)) {
					event.setCancelled(true);
				}
			}
			if (event.getItem().getItemStack().getType() == Material.COOKED_BEEF) {
				if (event.getPlayer().getInventory()
						.contains(Material.COOKED_BEEF)) {
					event.setCancelled(true);
				}
			}
			if (event.getItem().getItemStack().getType() == Material.BLAZE_ROD) {
				if (event.getPlayer().getInventory()
						.contains(Material.BLAZE_ROD)) {
					event.setCancelled(true);
				}
			}
			this.ctf.getGame(event.getPlayer()).removeItemFromIngameItems(
					event.getItem());
		}

	}

	@EventHandler
	public void onPlayerBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (this.ctf.isInGame(event.getPlayer())) {
			if (this.ctf.getConfig().getBoolean("stopBuildingIngame")) {
				event.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (event.getItemDrop().getItemStack().hasItemMeta()) {
			if (event.getItemDrop().getItemStack().getItemMeta().hasLore()) {
				for (String str : event.getItemDrop().getItemStack()
						.getItemMeta().getLore()) {
					if (str.equalsIgnoreCase("CTF Tool [Corners]")) {
						event.getItemDrop().remove();
					}
					if (str.equalsIgnoreCase("CTF Tool [Spawns]")) {
						event.getItemDrop().remove();
					}
					if (str.equalsIgnoreCase("CTF Tool [Flags]")) {
						event.getItemDrop().remove();
					}
				}
			}
		}
		if (!this.ctf.isInGame(event.getPlayer())) {
			return;
		}
		if (!this.ctf.getGame(event.getPlayer()).isActive()) {
			return;
		}
		this.ctf.getGame(event.getPlayer()).addItemToIngameItems(
				event.getItemDrop());
		boolean removeCarrier = false;
		if (this.ctf.getGame(event.getPlayer()).getFlagCarriers()
				.contains(event.getPlayer())) {
			if (event.getItemDrop().getItemStack().getType() == Material.REDSTONE_BLOCK) {
				removeCarrier = true;
			}
			if (event.getItemDrop().getItemStack().getType() == Material.LAPIS_BLOCK) {
				removeCarrier = true;
			}
		}
		if (removeCarrier) {
			this.ctf.getGame(event.getPlayer()).getFlagCarriers()
					.remove(event.getPlayer());
		}

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (this.ctf.isInGame(event.getPlayer())) {
			if (!this.ctf.getGame(event.getPlayer()).isActive()) {
				return;
			}
			// TODO dont drop item on hit
			if (event.hasItem()) {
				switch (event.getItem().getType()) {
				case REDSTONE_BLOCK:
				case LAPIS_BLOCK:
					GameItemUsed itemUsed =
							new GameItemUsed(event.getItem(), event.getPlayer());
					Bukkit.getServer().getPluginManager().callEvent(itemUsed);
					break;
				case SUGAR:
				case COOKED_BEEF:
				case BLAZE_ROD:
					itemUsed =
							new GameItemUsed(event.getItem(), event.getPlayer());
					Bukkit.getServer().getPluginManager().callEvent(itemUsed);
					event.getPlayer().getInventory()
							.removeItem(event.getItem());
					event.setCancelled(true);
					break;
				default:
					break;
				}
			}
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				// stop players from opening inventories
				switch (event.getClickedBlock().getType()) {
				case CHEST:
				case BREWING_STAND:
				case BEACON:
				case COMMAND:
				case DISPENSER:
				case DROPPER:
				case FURNACE:
				case HOPPER:
				case WORKBENCH:
				case ANVIL:
				case ENCHANTMENT_TABLE:
					event.setCancelled(true);
				default:
					break;
				}

			}
		}
		this.handleTools(event);
	}

	@EventHandler
	public void onPlayerPlace(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			if (this.ctf.isInGame(event.getPlayer())) {
				if (this.ctf.getConfig().getBoolean("stopBuildingIngame")) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (this.ctf.isInGame(event.getPlayer())) {
			this.ctf.getPlayerGameMap().get(event.getPlayer())
					.leaveGame(event.getPlayer());
		}
	}

	private void toolCorner(PlayerInteractEvent event) {
		if (event.getPlayer().hasPermission("ctf.tools")) {
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				this.createArenaData(event);
				this.ctf.getArenaCreationData(event.getPlayer()).setLocCorner1(
						event.getClickedBlock().getLocation());
				event.getPlayer().sendMessage(
						ChatColor.GOLD + "Corner 1 selected! ("
								+ event.getClickedBlock().getX() + ","
								+ event.getClickedBlock().getY() + ","
								+ event.getClickedBlock().getZ() + ")");
				event.setCancelled(true);
			}
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				this.createArenaData(event);
				this.ctf.getArenaCreationData(event.getPlayer()).setLocCorner2(
						event.getClickedBlock().getLocation());
				event.getPlayer().sendMessage(
						ChatColor.GOLD + "Corner 2 selected! ("
								+ event.getClickedBlock().getX() + ","
								+ event.getClickedBlock().getY() + ","
								+ event.getClickedBlock().getZ() + ")");
				event.setCancelled(true);
			}
		}
		else {
			event.setCancelled(true);
			event.getItem().setType(Material.STICK);
			event.getPlayer().sendMessage("You lack the required permission.");
		}
	}

	private void toolFlag(PlayerInteractEvent event) {
		if (event.getPlayer().hasPermission("ctf.tools")) {
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				this.createArenaData(event);
				this.ctf.getArenaCreationData(event.getPlayer())
						.setLocBlueFlag(event.getClickedBlock().getLocation());
				event.getPlayer().sendMessage(
						ChatColor.GOLD + "Blue Flag selected! ("
								+ event.getClickedBlock().getX() + ","
								+ event.getClickedBlock().getY() + ","
								+ event.getClickedBlock().getZ() + ")");
				event.setCancelled(true);
			}
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				this.createArenaData(event);
				this.ctf.getArenaCreationData(event.getPlayer()).setLocRedFlag(
						event.getClickedBlock().getLocation());
				event.getPlayer().sendMessage(
						ChatColor.GOLD + "Red Flag selected! ("
								+ event.getClickedBlock().getX() + ","
								+ event.getClickedBlock().getY() + ","
								+ event.getClickedBlock().getZ() + ")");
				event.setCancelled(true);
			}
		}
		else {
			event.setCancelled(true);
			event.getItem().setType(Material.STICK);
			event.getPlayer().sendMessage("You lack the required permission.");
		}
	}

	private void toolSpawn(PlayerInteractEvent event) {
		if (event.getPlayer().hasPermission("ctf.tools")) {
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				this.createArenaData(event);
				this.ctf.getArenaCreationData(event.getPlayer())
						.setLocBlueSpawn(event.getClickedBlock().getLocation());
				event.getPlayer().sendMessage(
						ChatColor.GOLD + "Blue Spawn selected! ("
								+ event.getClickedBlock().getX() + ","
								+ event.getClickedBlock().getY() + ","
								+ event.getClickedBlock().getZ() + ")");
				event.setCancelled(true);
			}
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				this.createArenaData(event);
				this.ctf.getArenaCreationData(event.getPlayer())
						.setLocRedSpawn(event.getClickedBlock().getLocation());
				event.getPlayer().sendMessage(
						ChatColor.GOLD + "Red Spawn selected! ("
								+ event.getClickedBlock().getX() + ","
								+ event.getClickedBlock().getY() + ","
								+ event.getClickedBlock().getZ() + ")");
				event.setCancelled(true);
			}
		}
		else {
			event.setCancelled(true);
			event.getItem().setType(Material.STICK);
			event.getPlayer().sendMessage("You lack the required permission.");
		}
	}

}
