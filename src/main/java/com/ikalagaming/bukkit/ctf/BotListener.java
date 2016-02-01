package com.ikalagaming.bukkit.ctf;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import com.ikalagaming.bukkit.ctf.event.RemoveBot;

public class BotListener implements Listener {
	Ctf ctf = (Ctf) Bukkit.getPluginManager().getPlugin("CTF");

	@EventHandler
	public void onRemoveBot(RemoveBot event) {
		Zombie bot = event.getBot();
		if (this.ctf.getBotGameMap().containsKey(bot)) {
			this.ctf.getBotGameMap().get(bot).removeBot(bot);
			this.ctf.getBotGameMap().remove(bot);
		}
	}

	@EventHandler
	public void onEntityBreakDoorEvent(EntityBreakDoorEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) {
			// ignore the event if it is not a player
			return;
		}
		if (this.ctf.isInGame((Zombie) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) {
			return;
		}
		if (this.ctf.isInGame((Zombie) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) {
			// ignore the event if it is not a player
			return;
		}
		if (!this.ctf.isInGame((Zombie) event.getEntity())) {
			return;// ignore players hurt not in a game
		}
		if (!event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
			event.setCancelled(true);
			return;
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

		Zombie bot = (Zombie) event.getEntity();
		LivingEntity damager = (LivingEntity) event.getDamager();
		if (!this.ctf.areTeammates(bot, damager)) {
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
		if ((bot.getHealth() - event.getDamage()) < 1) {
			// If it would have killed the player
			this.ctf.getGame(bot).respawnBot(bot);

			// respawn them (reset health, armor, etc)
			event.setCancelled(true);
			// cancel the damage so they start off healed
			this.ctf.getGame(damager).increaseKills(damager);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!(event.getEntity().getType().equals(EntityType.ZOMBIE))) {
			return;
		}
		if (!this.ctf.isInGame(((Zombie) event.getEntity()))) {
			return;
		}
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
		if (event.getCause() == DamageCause.DROWNING) {
			event.setCancelled(true);
			((Zombie) event.getEntity()).setRemainingAir(((Zombie) event
					.getEntity()).getMaximumAir());
		}
		if (event.getCause() == DamageCause.CONTACT) {
			event.setCancelled(true);
		}
		Zombie player = (Zombie) event.getEntity();
		// If it would have killed the player
		if ((player.getHealth() - event.getDamage()) < 1) {
			// respawn them (reset health, armor, etc)
			this.ctf.getGame(player).respawnBot(player);
			// cancel the damage so they start off healed
			event.setCancelled(true);
		}

	}

	@EventHandler
	public void onEntityPortalEvent(EntityPortalEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) {
			// ignore the event if it is not a player
			return;
		}
		if (this.ctf.isInGame((Zombie) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityTargetEvent(EntityTargetEvent event) {
		// TODO make sure bots only target the correct things
	}

	@EventHandler
	public void onEntityTargetLivingEntityEvent(
			EntityTargetLivingEntityEvent event) {
		// TODO make sure bots only target the correct things
		if (event.isCancelled()) {
			return;
		}
		if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) {
			return;
		}
		if (event.getTarget() == null) {
			return;
		}
		Zombie zomb = (Zombie) event.getEntity();
		if (!this.ctf.isInGame(zomb)) {
			return;
		}
		if (!this.ctf.isInGame(event.getTarget())) {
			event.setCancelled(true);
			return;
		}
		if (this.ctf.areTeammates(zomb, event.getTarget())) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		// TODO target the item if its useful and near the bot
		// if (!this.ctf.isInArena(event.getLocation(), 0)) {
		// return;
		// }
		// TODO dont pick up team flag
	}

}
