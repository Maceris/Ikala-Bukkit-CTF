package com.ikalagaming.bukkit.ctf.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public final class GameItemUsed extends Event implements Cancellable {
	public static HandlerList getHandlerList() {
		return GameItemUsed.handlers;
	}

	private static final HandlerList handlers = new HandlerList();
	private ItemStack item;
	private LivingEntity itemUser;

	private boolean cancelled;

	public GameItemUsed(ItemStack itemUsed, LivingEntity user) {
		this.item = itemUsed;
		this.itemUser = user;
	}

	@Override
	public HandlerList getHandlers() {
		return GameItemUsed.handlers;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public LivingEntity getUser() {
		return this.itemUser;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
