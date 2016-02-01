package com.ikalagaming.bukkit.ctf.event;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.ikalagaming.bukkit.ctf.Game;

public class GameWon extends Event implements Cancellable {
	public static HandlerList getHandlerList() {
		return GameWon.handlers;
	}

	private static final HandlerList handlers = new HandlerList();
	private Game game;
	private ArrayList<Player> winnerList;
	private ArrayList<Player> looserList;

	private boolean cancelled;

	public GameWon(Game whichGame, ArrayList<Player> winners,
			ArrayList<Player> loosers) {
		this.game = whichGame;
		this.winnerList = winners;
		this.looserList = loosers;
	}

	public Game getGame() {
		return this.game;
	}

	@Override
	public HandlerList getHandlers() {
		return GameWon.handlers;
	}

	public ArrayList<Player> getLoosers() {
		return this.looserList;
	}

	public ArrayList<Player> getWinners() {
		return this.winnerList;
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
