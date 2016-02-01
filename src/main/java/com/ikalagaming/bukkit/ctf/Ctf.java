package com.ikalagaming.bukkit.ctf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Ctf extends JavaPlugin {
	private HashMap<String, Arena> arenas;
	private HashMap<Player, Game> playerGameMap;
	private HashMap<Zombie, Game> botGameMap;
	private HashMap<Player, ArenaCreationData> arenaCreationDataMap;

	public ArenaCreationData addAreanCreationData(Player p) {
		if (this.hasArenaCreationData(p)) {
			return this.getArenaCreationData(p);
		}
		this.arenaCreationDataMap.put(p, new ArenaCreationData());
		return this.getArenaCreationData(p);
	}

	public boolean areTeammates(LivingEntity p1, LivingEntity p2) {
		Game g1;
		Game g2;
		if (this.playerGameMap.containsKey(p1)) {
			g1 = this.playerGameMap.get(p1);
		}
		else if (this.botGameMap.containsKey(p1)) {
			g1 = this.botGameMap.get(p1);
		}
		else {
			return false;// p1 is not in a game
		}

		if (this.playerGameMap.containsKey(p2)) {
			g2 = this.playerGameMap.get(p2);
		}
		else if (this.botGameMap.containsKey(p2)) {
			g2 = this.botGameMap.get(p2);
		}
		else {
			return false;// p2 is not in a game
		}
		if (g1 != g2) {
			return false;// different games
		}
		return g1.teamsMatch(p1, p2);
	}

	/**
	 * Checks if the given player has the supplied permission. Returns true if
	 * they do. If they do not, they are informed they lack permission and it
	 * returns false.
	 *
	 * @param player the player to check for permission status
	 * @param permission the name of the permission to check
	 * @return true if they have permission, false if they do not
	 */
	private boolean checkPermission(Player player, String permission) {
		if (player.hasPermission(permission)) {
			return true;
		}

		player.sendMessage(ChatColor.RED + "You lack the required permission.");
		return false;
	}

	private void cmdCreate(Player player, String name, String players) {
		if (this.hasArenaCreationData(player)) {
			ArenaCreationData arenaDataP = this.getArenaCreationData(player);
			if (arenaDataP.isComplete()) {
				if (!this.arenas.containsKey(name)) {
					if (arenaDataP.locationsAreValid()) {
						List<String> configArenas =
								this.getConfig().getStringList("arenalist");
						configArenas.add(name);
						this.getConfig().set("arenalist", configArenas);

						this.getConfig()
								.set("arenas." + name + ".world",
										arenaDataP.getLocCorner1().getWorld()
												.getName());
						this.getConfig().set("arenas." + name + ".x1",
								arenaDataP.getLocCorner1().getBlockX());
						this.getConfig().set("arenas." + name + ".y1",
								arenaDataP.getLocCorner1().getBlockY());
						this.getConfig().set("arenas." + name + ".z1",
								arenaDataP.getLocCorner1().getBlockZ());
						this.getConfig().set("arenas." + name + ".x2",
								arenaDataP.getLocCorner2().getBlockX());
						this.getConfig().set("arenas." + name + ".y2",
								arenaDataP.getLocCorner2().getBlockY());
						this.getConfig().set("arenas." + name + ".z2",
								arenaDataP.getLocCorner2().getBlockZ());
						this.getConfig().set("arenas." + name + ".blueflagx",
								arenaDataP.getLocBlueFlag().getBlockX());
						this.getConfig().set("arenas." + name + ".blueflagy",
								arenaDataP.getLocBlueFlag().getBlockY());
						this.getConfig().set("arenas." + name + ".blueflagz",
								arenaDataP.getLocBlueFlag().getBlockZ());
						this.getConfig().set("arenas." + name + ".redflagx",
								arenaDataP.getLocRedFlag().getBlockX());
						this.getConfig().set("arenas." + name + ".redflagy",
								arenaDataP.getLocRedFlag().getBlockY());
						this.getConfig().set("arenas." + name + ".redflagz",
								arenaDataP.getLocRedFlag().getBlockZ());
						this.getConfig().set("arenas." + name + ".bluespawnx",
								arenaDataP.getLocBlueSpawn().getBlockX());
						this.getConfig().set("arenas." + name + ".bluespawny",
								arenaDataP.getLocBlueSpawn().getBlockY());
						this.getConfig().set("arenas." + name + ".bluespawnz",
								arenaDataP.getLocBlueSpawn().getBlockZ());
						this.getConfig().set("arenas." + name + ".redspawnx",
								arenaDataP.getLocRedSpawn().getBlockX());
						this.getConfig().set("arenas." + name + ".redspawny",
								arenaDataP.getLocRedSpawn().getBlockY());
						this.getConfig().set("arenas." + name + ".redspawnz",
								arenaDataP.getLocRedSpawn().getBlockZ());
						this.getConfig().set("arenas." + name + ".maxplayers",
								Integer.parseInt(players));
						this.saveConfig();
						this.reloadArenas();
						this.arenaCreationDataMap.remove(player);
					}
					else {
						player.sendMessage(ChatColor.RED
								+ arenaDataP.getInvalidlocations());
					}
				}
				else {
					player.sendMessage(ChatColor.RED
							+ "An arena with that name already exists.");
				}
			}
			else {
				player.sendMessage(ChatColor.RED + arenaDataP.getIncomplete());
			}
		}
		else {
			player.sendMessage(ChatColor.RED
					+ "You have not selected any blocks.");
		}
		player.sendMessage(ChatColor.RED + "-Work In Progress-");
	}

	private void cmdDestroy(Player player, String arenaName) {
		if (this.checkPermission(player, "ctf.destroy")) {
			if (this.arenas.containsKey(arenaName)) {
				this.arenas.get(arenaName).stopAllGames();
				this.arenas.get(arenaName).shutDownAllGames();
				this.arenas.remove(arenaName);
				List<String> configArenas =
						this.getConfig().getStringList("arenalist");
				configArenas.remove(arenaName);
				this.getConfig().set("arenalist", configArenas);
				for (Arena aren : this.arenas.values()) {
					this.getConfig().set("arenas." + aren.getName() + ".world",
							aren.getCorner1().getWorld().getName());
					this.getConfig().set("arenas." + aren.getName() + ".x1",
							aren.getCorner1().getBlockX());
					this.getConfig().set("arenas." + aren.getName() + ".y1",
							aren.getCorner1().getBlockY());
					this.getConfig().set("arenas." + aren.getName() + ".z1",
							aren.getCorner1().getBlockZ());
					this.getConfig().set("arenas." + aren.getName() + ".x2",
							aren.getCorner2().getBlockX());
					this.getConfig().set("arenas." + aren.getName() + ".y2",
							aren.getCorner2().getBlockY());
					this.getConfig().set("arenas." + aren.getName() + ".z2",
							aren.getCorner2().getBlockZ());
					this.getConfig().set(
							"arenas." + aren.getName() + ".blueflagx",
							aren.getBlueFlag().getBlockX());
					this.getConfig().set(
							"arenas." + aren.getName() + ".blueflagy",
							aren.getBlueFlag().getBlockY());
					this.getConfig().set(
							"arenas." + aren.getName() + ".blueflagz",
							aren.getBlueFlag().getBlockZ());
					this.getConfig().set(
							"arenas." + aren.getName() + ".redflagx",
							aren.getRedFlag().getBlockX());
					this.getConfig().set(
							"arenas." + aren.getName() + ".redflagy",
							aren.getRedFlag().getBlockY());
					this.getConfig().set(
							"arenas." + aren.getName() + ".redflagz",
							aren.getRedFlag().getBlockZ());
					this.getConfig().set(
							"arenas." + aren.getName() + ".bluespawnx",
							aren.getBlueSpawn().getBlockX());
					this.getConfig().set(
							"arenas." + aren.getName() + ".bluespawny",
							aren.getBlueSpawn().getBlockY());
					this.getConfig().set(
							"arenas." + aren.getName() + ".bluespawnz",
							aren.getBlueSpawn().getBlockZ());
					this.getConfig().set(
							"arenas." + aren.getName() + ".redspawnx",
							aren.getRedSpawn().getBlockX());
					this.getConfig().set(
							"arenas." + aren.getName() + ".redspawny",
							aren.getRedSpawn().getBlockY());
					this.getConfig().set(
							"arenas." + aren.getName() + ".redspawnz",
							aren.getRedSpawn().getBlockZ());
					this.getConfig().set(
							"arenas." + aren.getName() + ".maxplayers",
							aren.getMaxPlayers());
				}
				this.saveConfig();
				this.reloadArenas();
				player.sendMessage("Destroyed!");
			}
			else {
				player.sendMessage(ChatColor.RED + "That arena does not exist.");
			}
		}
	}

	private void cmdDisable(Player player) {
		if (this.checkPermission(player, "ctf.disable")) {
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}

	}

	private void cmdGoto(Player player, String arenaName) {
		if (this.checkPermission(player, "ctf.goto")) {
			if (this.arenas.containsKey(arenaName)) {
				player.teleport(this.arenas.get(arenaName).getBlueSpawn(),
						TeleportCause.PLUGIN);
			}
			else {
				player.sendMessage(ChatColor.RED + "That arena does not exist.");
			}
		}
	}

	private void cmdHelp(Player player) {
		if (this.checkPermission(player, "ctf.help")) {
			player.sendMessage(ChatColor.AQUA
					+ "/ctf <help:leave:reload:list:tools:disable>");
			player.sendMessage(ChatColor.AQUA
					+ "/ctf <join:start:stop:destroy:goto> <Arena>");
			player.sendMessage(ChatColor.AQUA
					+ "/ctf <create> <Arena Name> <Max Players>");
			player.sendMessage(ChatColor.AQUA + "/ctf <kick> <Player>");
		}
	}

	private void cmdJoin(Player player, String[] args) {
		if (this.checkPermission(player, "ctf.join")) {
			if (this.arenas.containsKey(args[1])) {
				if (this.arenas.get(args[1]).hasOpenGame()) {
					if (!this.isInGame(player)) {
						try {
							this.arenas.get(args[1]).getAnOpenGame()
									.joinGame(player);
							this.playerGameMap.put(
									player,
									this.arenas.get(args[1]).getPlayersGame(
											player));
						}
						catch (Exception e) {
							player.sendMessage(ChatColor.RED
									+ "There was an error joining the game. Here is a cookie.");
							player.sendMessage("Sorry, I accidentally your whole inventory.");
							player.getInventory().addItem(
									new ItemStack(Material.COOKIE));
						}
					}
					else {
						player.sendMessage(ChatColor.RED
								+ "You are already in a game!");
					}
				}
				else {
					player.sendMessage(ChatColor.RED
							+ "There are no open games.");
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "That arena does not exist.");
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void cmdKick(Player player, String toKick) {
		if (this.checkPermission(player, "ctf.kick")) {
			/*
			 * The deprecation is fine since we just kick by name So kicking
			 * whoever has that name is the intended functionality.
			 * 
			 * That is, "Its not a bug, its a feature"
			 */

			if (this.playerGameMap.containsKey(Bukkit.getServer().getPlayer(
					toKick))) {
				this.playerGameMap.get(Bukkit.getServer().getPlayer(toKick))
						.leaveGame(Bukkit.getServer().getPlayer(toKick));
				this.playerGameMap.remove(Bukkit.getServer().getPlayer(toKick));
				player.sendMessage("Kicked!");
			}
			else {
				player.sendMessage(ChatColor.RED
						+ "That player does not seem to be in a game.");
			}
		}
	}

	private void cmdLeave(Player player) {
		if (this.checkPermission(player, "ctf.leave")) {
			if (this.playerGameMap.containsKey(player)) {
				this.playerGameMap.get(player).leaveGame(player);
				this.playerGameMap.remove(player);
			}
			else {
				player.sendMessage(ChatColor.RED + "You are not in a game!");
			}
		}

	}

	private void cmdList(Player player) {
		if (this.checkPermission(player, "ctf.list")) {
			String output = ChatColor.AQUA + "Arenas: ";
			for (String st : this.arenas.keySet()) {
				output += st + " ";
			}
			player.sendMessage(output);
		}
	}

	private boolean cmdOneArg(CommandSender sender, String[] args) {
		UUID uuid = ((Player) sender).getUniqueId();
		Player player = Bukkit.getServer().getPlayer(uuid);
		if (args[0].equalsIgnoreCase("leave")) {
			this.cmdLeave(player);
			return true;
		}
		else if (args[0].equalsIgnoreCase("reload")) {
			this.cmdReload(player);
			return true;
		}
		else if (args[0].equalsIgnoreCase("disable")) {
			this.cmdDisable(player);
			return true;
		}
		else if (args[0].equalsIgnoreCase("help")) {
			this.cmdHelp(player);
			return true;
		}
		else if (args[0].equalsIgnoreCase("tools")) {
			this.cmdTools(player);
			return true;
		}
		else if (args[0].equalsIgnoreCase("list")) {
			this.cmdList(player);
			return true;
		}
		else {
			sender.sendMessage(ChatColor.RED + "Unknown command.");
			return false;
		}
	}

	private void cmdReload(Player player) {
		if (this.checkPermission(player, "ctf.reload")) {
			this.onDisable();
			this.onEnable();
			this.reloadConfig();
		}

	}

	private void cmdStart(Player player, String arenaName) {
		if (this.checkPermission(player, "ctf.start")) {
			// TODO alert on already started
			if (this.arenas.containsKey(arenaName)) {
				this.arenas.get(arenaName).startAllGames();
				player.sendMessage("Started!");
			}
			else {
				player.sendMessage(ChatColor.RED + "That arena does not exist.");
			}
		}
	}

	private void cmdStop(Player player, String arenaName) {
		if (this.checkPermission(player, "ctf.stop")) {
			// TODO alert on already stopped
			if (this.arenas.containsKey(arenaName)) {
				this.arenas.get(arenaName).stopAllGames();
				player.sendMessage("Stopped!");
			}
			else {
				player.sendMessage(ChatColor.RED + "That arena does not exist.");
			}
		}
	}

	private boolean cmdThreeArg(CommandSender sender, String[] args) {
		UUID uuid = ((Player) sender).getUniqueId();
		Player player = Bukkit.getServer().getPlayer(uuid);
		if (args[0].equalsIgnoreCase("create")) {
			if (this.checkPermission(player, "ctf.create")) {
				this.cmdCreate(player, args[1], args[2]);
			}
			return true;
		}
		sender.sendMessage(ChatColor.RED + "Unknown command.");
		return false;
	}

	private void cmdTools(Player player) {
		if (this.checkPermission(player, "ctf.tools")) {
			ItemStack cornerTool = new ItemStack(Material.GOLD_PICKAXE);
			cornerTool.setAmount(1);
			List<String> cornerMeta = new ArrayList<>();
			cornerMeta.add("CTF Tool [Corners]");
			ItemMeta metaData_Corner = cornerTool.getItemMeta();
			metaData_Corner.setLore(cornerMeta);
			metaData_Corner.setDisplayName("CTF Tool [Corners]");
			cornerTool.setItemMeta(metaData_Corner);
			player.getInventory().addItem(cornerTool);

			ItemStack spawnTool = new ItemStack(Material.GOLD_SPADE);
			spawnTool.setAmount(1);
			List<String> spawnMeta = new ArrayList<>();
			spawnMeta.add("CTF Tool [Spawns]");
			ItemMeta metaData_Spawn = spawnTool.getItemMeta();
			metaData_Spawn.setLore(spawnMeta);
			metaData_Spawn.setDisplayName("CTF Tool [Spawns]");
			spawnTool.setItemMeta(metaData_Spawn);
			player.getInventory().addItem(spawnTool);

			ItemStack flagTool = new ItemStack(Material.GOLD_HOE);
			flagTool.setAmount(1);
			List<String> flagMeta = new ArrayList<>();
			flagMeta.add("CTF Tool [Flags]");
			ItemMeta metaData_Flag = flagTool.getItemMeta();
			metaData_Flag.setLore(flagMeta);
			metaData_Flag.setDisplayName("CTF Tool [Flags]");
			flagTool.setItemMeta(metaData_Flag);
			player.getInventory().addItem(flagTool);
		}
	}

	private boolean cmdTwoArg(CommandSender sender, String[] args) {
		UUID uuid = ((Player) sender).getUniqueId();
		Player player = Bukkit.getServer().getPlayer(uuid);
		if (args[0].equalsIgnoreCase("join")) {
			this.cmdJoin(player, args);
			return true;
		}
		if (args[0].equalsIgnoreCase("goto")) {
			this.cmdGoto(player, args[1]);
			return true;
		}
		else if (args[0].equalsIgnoreCase("start")) {
			this.cmdStart(player, args[1]);
			return true;
		}
		else if (args[0].equalsIgnoreCase("stop")) {
			this.cmdStop(player, args[1]);
			return true;
		}
		else if (args[0].equalsIgnoreCase("kick")) {
			this.cmdKick(player, args[1]);
			return true;
		}
		else if (args[0].equalsIgnoreCase("destroy")) {
			this.cmdDestroy(player, args[1]);
			return true;
		}
		else {
			sender.sendMessage(ChatColor.RED + "Unknown command.");
			return false;
		}
	}

	public ArenaCreationData getArenaCreationData(Player p) {
		return this.arenaCreationDataMap.get(p);
	}

	public HashMap<Zombie, Game> getBotGameMap() {
		return this.botGameMap;
	}

	public Game getGame(LivingEntity entity) {
		if (entity.getType().equals(EntityType.ZOMBIE)) {
			return this.botGameMap.get(entity);
		}
		else if (entity.getType().equals(EntityType.PLAYER)) {
			return this.playerGameMap.get(entity);
		}
		throw new NullPointerException();
		// return null;
	}

	public HashMap<Player, Game> getPlayerGameMap() {
		return this.playerGameMap;
	}

	public boolean hasArenaCreationData(Player p) {
		return this.arenaCreationDataMap.containsKey(p);
	}

	public boolean isInArena(Location testLocation, int radius) {

		for (Arena arena : this.arenas.values()) {
			Location loc1 =
					new Location(testLocation.getWorld(), testLocation.getX()
							- radius, testLocation.getY() - radius,
							testLocation.getZ() - radius);
			Location loc2 =
					new Location(testLocation.getWorld(), testLocation.getX()
							+ radius, testLocation.getY() + radius,
							testLocation.getZ() + radius);
			if (BlockMath.intersectsWith(arena.corner1, arena.corner2, loc1,
					loc2)) {
				return true;
			}
		}
		return false;
	}

	public boolean isInGame(Player p) {
		return this.playerGameMap.containsKey(p);
	}

	public boolean isInGame(Zombie bot) {
		return this.botGameMap.containsKey(bot);
	}

	public boolean isInGame(LivingEntity p) {
		return this.playerGameMap.containsKey(p)
				|| this.botGameMap.containsKey(p);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!cmd.getName().equalsIgnoreCase("ctf")) {
			return false;
		}
		UUID uuid;
		try {
			uuid = ((Player) sender).getUniqueId();
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "You must be online.");
			return false;
		}
		if (Bukkit.getServer().getPlayer(uuid) == null) {
			sender.sendMessage(ChatColor.RED + "You must be online.");
			return false;
		}

		if (args.length == 1) {
			return this.cmdOneArg(sender, args);
		}
		else if (args.length == 2) {
			return this.cmdTwoArg(sender, args);
		}
		else if (args.length == 3) {
			return this.cmdThreeArg(sender, args);
		}
		else {
			sender.sendMessage(ChatColor.RED + "Error in syntax");
			return false;
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		for (Arena aren : this.arenas.values()) {
			aren.shutDownAllGames();
		}
		this.arenas.clear();
		for (Zombie zomb : this.botGameMap.keySet()) {
			this.botGameMap.get(zomb).removeBot(zomb);
		}
		this.botGameMap.clear();
		this.playerGameMap.clear();
		for (Player player : this.arenaCreationDataMap.keySet()) {
			player.sendMessage(ChatColor.RED
					+ "You have lost your selections due to the plugin shutting down.");
		}
		this.arenaCreationDataMap.clear();
		this.getLogger().info("CTF has been deactivated!");
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.arenas = new HashMap<>();
		this.playerGameMap = new HashMap<>();
		this.botGameMap = new HashMap<>();
		this.arenaCreationDataMap = new HashMap<>();
		for (String s : this.getConfig().getStringList("arenalist")) {
			this.arenas.put(
					s,
					new Arena(Bukkit.getWorld(this.getConfig().getString(
							"arenas." + s + ".world")), s, this.getConfig()
							.getInt("arenas." + s + ".x1"), this.getConfig()
							.getInt("arenas." + s + ".y1"), this.getConfig()
							.getInt("arenas." + s + ".z1"), this.getConfig()
							.getInt("arenas." + s + ".x2"), this.getConfig()
							.getInt("arenas." + s + ".y2"), this.getConfig()
							.getInt("arenas." + s + ".z2"), this.getConfig()
							.getInt("arenas." + s + ".blueflagx"), this
							.getConfig().getInt("arenas." + s + ".blueflagy"),
							this.getConfig().getInt(
									"arenas." + s + ".blueflagz"), this
									.getConfig().getInt(
											"arenas." + s + ".redflagx"), this
									.getConfig().getInt(
											"arenas." + s + ".redflagy"), this
									.getConfig().getInt(
											"arenas." + s + ".redflagz"), this
									.getConfig().getInt(
											"arenas." + s + ".bluespawnx"),
							this.getConfig().getInt(
									"arenas." + s + ".bluespawny"), this
									.getConfig().getInt(
											"arenas." + s + ".bluespawnz"),
							this.getConfig().getInt(
									"arenas." + s + ".redspawnx"), this
									.getConfig().getInt(
											"arenas." + s + ".redspawny"), this
									.getConfig().getInt(
											"arenas." + s + ".redspawnz"), this
									.getConfig().getInt(
											"arenas." + s + ".maxplayers")));
		}
		for (Arena ar : this.arenas.values()) {
			ar.initAGame();
		}
		this.getServer().getPluginManager()
				.registerEvents(new MyPlayerListener(), this);
		this.getServer().getPluginManager()
				.registerEvents(new BotListener(), this);
		this.getServer().getPluginManager()
				.registerEvents(new GameListener(), this);

		this.getLogger().info("CTF has been activated!");
	}

	public void reloadArenas() {
		for (Arena aren : this.arenas.values()) {
			for (Game gam : aren.getGames()) {
				gam.closeDown();
			}
		}
		this.arenas.clear();
		for (String s : this.getConfig().getStringList("arenalist")) {
			this.arenas.put(
					s,
					new Arena(Bukkit.getWorld(this.getConfig().getString(
							"arenas." + s + ".world")), s, this.getConfig()
							.getInt("arenas." + s + ".x1"), this.getConfig()
							.getInt("arenas." + s + ".y1"), this.getConfig()
							.getInt("arenas." + s + ".z1"), this.getConfig()
							.getInt("arenas." + s + ".x2"), this.getConfig()
							.getInt("arenas." + s + ".y2"), this.getConfig()
							.getInt("arenas." + s + ".z2"), this.getConfig()
							.getInt("arenas." + s + ".blueflagx"), this
							.getConfig().getInt("arenas." + s + ".blueflagy"),
							this.getConfig().getInt(
									"arenas." + s + ".blueflagz"), this
									.getConfig().getInt(
											"arenas." + s + ".redflagx"), this
									.getConfig().getInt(
											"arenas." + s + ".redflagy"), this
									.getConfig().getInt(
											"arenas." + s + ".redflagz"), this
									.getConfig().getInt(
											"arenas." + s + ".bluespawnx"),
							this.getConfig().getInt(
									"arenas." + s + ".bluespawny"), this
									.getConfig().getInt(
											"arenas." + s + ".bluespawnz"),
							this.getConfig().getInt(
									"arenas." + s + ".redspawnx"), this
									.getConfig().getInt(
											"arenas." + s + ".redspawny"), this
									.getConfig().getInt(
											"arenas." + s + ".redspawnz"), this
									.getConfig().getInt(
											"arenas." + s + ".maxplayers")));
		}
		for (Arena ar : this.arenas.values()) {
			ar.initAGame();
		}
	}

}
