package com.ikalagaming.bukkit.ctf.event;

import org.bukkit.entity.Zombie;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class RemoveBot extends Event implements Cancellable {
	public static HandlerList getHandlerList() {
		return RemoveBot.handlers;
	}

	private static final HandlerList handlers = new HandlerList();
	private Zombie bot;

	private boolean cancelled;

	public RemoveBot(Zombie toRemove) {
		this.bot = toRemove;
	}

	@Override
	public HandlerList getHandlers() {
		return RemoveBot.handlers;
	}

	public Zombie getBot() {
		return this.bot;
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
