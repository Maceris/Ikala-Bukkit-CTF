package com.ikalagaming.bukkit.ctf;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Arena {
	public Location corner1, corner2, redSpawn, blueSpawn, redFlag, blueFlag;
	public String arenaName;
	public HashSet<Game> games;
	public int maxPlayers;

	public Arena(World world, String name, int x1, int y1, int z1, int x2,
			int y2, int z2, int blueflagx, int blueflagy, int blueflagz,
			int redflagx, int redflagy, int redflagz, int bluespawnx,
			int bluespawny, int bluespawnz, int redspawnx, int redspawny,
			int redspawnz, int maxPlayer) {
		this.arenaName = name;
		this.corner1 = new Location(world, x1, y1, z1);
		this.corner2 = new Location(world, x2, y2, z2);
		this.blueFlag = new Location(world, blueflagx, blueflagy, blueflagz);
		this.redFlag = new Location(world, redflagx, redflagy, redflagz);
		this.blueSpawn =
				new Location(world, bluespawnx, bluespawny, bluespawnz);
		this.redSpawn = new Location(world, redspawnx, redspawny, redspawnz);
		this.games = new HashSet<>();
		this.maxPlayers = maxPlayer;
	}

	public Game getAnOpenGame() {
		for (Game g : this.games) {
			if (!g.isActive()) {
				if (!g.isFull()) {
					return g;
				}
			}
		}
		return null;
	}

	public Location getBlueFlag() {
		return this.blueFlag;
	}

	public Location getBlueSpawn() {
		return this.blueSpawn;
	}

	public Location getCorner1() {
		return this.corner1;
	}

	public Location getCorner2() {
		return this.corner2;
	}

	public HashSet<Game> getGames() {
		return this.games;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public String getName() {
		return this.arenaName;
	}

	public Game getPlayersGame(Player p) {
		for (Game g : this.games) {
			if (g.isInGame(p)) {
				return g;
			}
		}
		return null;
	}

	public Location getRedFlag() {
		return this.redFlag;
	}

	public Location getRedSpawn() {
		return this.redSpawn;
	}

	public boolean hasOpenGame() {
		for (Game g : this.games) {
			if (!g.isActive()) {
				if (!g.isFull()) {
					return true;
				}
			}
		}
		return false;
	}

	public void initAGame() {
		Game newGame = new Game(this);
		this.games.add(newGame);
	}

	public boolean isNear(Location one, Location two, double maxDistance) {
		double dtotal;
		dtotal = one.distance(two);
		return (dtotal <= maxDistance);
	}

	public void setBlueFlag(Location newLocation) {
		this.blueFlag = newLocation;
	}

	public void setBlueSpawn(Location newLocation) {
		this.blueSpawn = newLocation;
	}

	public void setCorner1(Location newLocation) {
		this.corner1 = newLocation;
	}

	public void setCorner2(Location newLocation) {
		this.corner2 = newLocation;
	}

	public void setGames(HashSet<Game> gamesMap) {
		this.games = gamesMap;
	}

	public void setMaxPlayers(int maxPlayer) {
		this.maxPlayers = maxPlayer;
	}

	public void setName(String name) {
		this.arenaName = name;
	}

	public void setRedFlag(Location newLocation) {
		this.redFlag = newLocation;
	}

	public void setRedSpawn(Location newLocation) {
		this.redSpawn = newLocation;
	}

	public void shutDownAllGames() {
		for (Game gam : this.getGames()) {
			gam.closeDown();
		}
	}

	public void startAllGames() {
		for (Game g : this.games) {
			if (!g.isActive()) {
				g.startGame();
			}
		}
	}

	public void stopAllGames() {
		for (Game g : this.games) {
			if (g.isActive()) {
				g.stopGame();
			}
		}
	}
}
