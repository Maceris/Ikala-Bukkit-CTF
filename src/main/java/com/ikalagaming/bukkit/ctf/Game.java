package com.ikalagaming.bukkit.ctf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.ikalagaming.bukkit.ctf.event.GameWon;
import com.ikalagaming.bukkit.ctf.event.RemoveBot;

public class Game {
	private static String getRandomBotName() {
		int pos = (int) (Math.random() * Game.botNames.length);
		if (pos >= Game.botNames.length) {
			pos = Game.botNames.length - 1;
		}
		String name;
		try {
			name = Game.botNames[pos];
		}
		catch (IndexOutOfBoundsException ex) {
			name = "ERROR";
		}
		return name;
	}

	private HashSet<Player> redTeam, blueTeam;
	private HashSet<LivingEntity> flagCarriers;
	private HashSet<Zombie> redBots, blueBots;
	private int maxPlayers, currentPlayers, redScore, blueScore, maxScore;
	private boolean active, redFlagAwayFromSpawn, blueFlagAwayFromSpawn,
			redFlagExists, blueFlagExists;
	private Arena arena;
	private HashSet<Item> ingameItems;
	private HashSet<Integer> taskIDList;

	private int flagLifetimeCounter_Red, flagLifetimeCounter_Blue,
			maxFlagLifetime;
	private HashMap<String, ItemStack[]> originalInventories;
	private HashMap<String, Location> originalLocations;
	private HashMap<String, GameMode> originalGamemode;
	private HashMap<String, ItemStack[]> originalArmor;
	private HashMap<String, Integer> originalHunger;
	private HashMap<String, Double> originalHealth;

	private HashMap<String, Float> originalSaturation;
	private ScoreboardManager scoreboardManager;
	private Scoreboard scoreboard;
	private Team scoreboardTeam_Red, scoreboardTeam_Blue;
	private Objective objective_captures, objective_kills;

	private Score scoreRed, scoreBlue;
	// private boolean redWins, blueWins;
	private boolean restartingGame;
	private HashSet<Integer> restartingTaskList;

	private int currentRespawnTime, maxRespawnTime;

	// 100 common names for bots
	private static final String[] botNames = new String[] {"Aaron", "Alan",
			"Alexander", "Amanda", "Amy", "Andrew", "Ann", "Anthony", "Ashley",
			"Barbara", "Betty", "Billy", "Brandon", "Brian", "Bruce", "Carl",
			"Carolyn", "Charles", "Christian", "Christine", "Crystal",
			"Daniel", "David", "Debra", "Dennis", "Diane", "Donna", "Dorothy",
			"Dylan", "Elizabeth", "Emma", "Ethan", "Evelyn", "Frank", "George",
			"Gloria", "Gregory", "Harold", "Heather", "Henry", "Jack",
			"Jacqueline", "Jane", "Janice", "Jean", "Jennifer", "Jerry",
			"Jessica", "Joe", "Johnny", "Jordan", "Joseph", "Joyce", "Judith",
			"Julia", "Justin", "Katherine", "Kathryn", "Keith", "Kenneth",
			"Kimberly", "Larry", "Lauren", "Linda", "Lori", "Madison", "Maria",
			"Marilyn", "Martha", "Matthew", "Melissa", "Michelle", "Nancy",
			"Nicholas", "Olivia", "Patricia", "Paul", "Philip", "Rachel",
			"Randy", "Rebecca", "Robert", "Ronald", "Roy", "Ruth", "Samantha",
			"Sandra", "Sarah", "Sean", "Shawn", "Stephanie", "Steven", "Tammy",
			"Terry", "Thomas", "Timothy", "Victoria", "Virginia", "Wayne",
			"Willie"};

	public Game(Arena aren) {
		this.redTeam = new HashSet<>();
		this.redBots = new HashSet<>();
		this.blueTeam = new HashSet<>();
		this.blueBots = new HashSet<>();
		this.flagCarriers = new HashSet<>();
		this.maxPlayers = aren.getMaxPlayers();
		this.currentPlayers = 0;
		this.arena = aren;
		this.originalInventories = new HashMap<>();
		this.originalLocations = new HashMap<>();
		this.originalGamemode = new HashMap<>();
		this.originalArmor = new HashMap<>();
		this.originalHealth = new HashMap<>();
		this.originalHunger = new HashMap<>();
		this.originalSaturation = new HashMap<>();
		this.ingameItems = new HashSet<>();
		this.active = false;
		this.redScore = 0;
		this.blueScore = 0;
		this.maxScore =
				Bukkit.getPluginManager().getPlugin("CTF").getConfig()
						.getInt("maxscore") + 1;
		this.taskIDList = new HashSet<>();
		this.scoreboardManager = Bukkit.getScoreboardManager();
		this.scoreboard = this.getScoreboardManager().getNewScoreboard();
		this.scoreboardTeam_Red =
				this.getScoreboard().registerNewTeam("Red Team");
		this.scoreboardTeam_Blue = this.getScoreboard().registerNewTeam("Blue");
		this.scoreboardTeam_Blue.setAllowFriendlyFire(false);
		this.scoreboardTeam_Red.setAllowFriendlyFire(false);
		this.scoreboardTeam_Blue.setPrefix(ChatColor.BLUE + "[BLUE]");
		this.scoreboardTeam_Red.setPrefix(ChatColor.RED + "[RED]");
		this.objective_captures =
				this.scoreboard.registerNewObjective("captures", "dummy");
		this.objective_captures.setDisplayName("Captures");
		this.objective_captures.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.objective_kills =
				this.scoreboard.registerNewObjective("kills", "dummy");
		this.objective_kills.setDisplayName("Kills");
		this.objective_kills.setDisplaySlot(DisplaySlot.BELOW_NAME);
		this.scoreBlue =
				this.objective_captures.getScore(ChatColor.BLUE + "Blue:");
		this.scoreRed =
				this.objective_captures.getScore(ChatColor.RED + "Red:");
		this.scoreBlue.setScore(0);
		this.scoreRed.setScore(0);
		this.redFlagAwayFromSpawn = false;
		this.blueFlagAwayFromSpawn = false;
		this.flagLifetimeCounter_Blue = 0;
		this.flagLifetimeCounter_Red = 0;
		this.maxFlagLifetime =
				Bukkit.getPluginManager().getPlugin("CTF").getConfig()
						.getInt("flagLifespan");
		this.restartingGame = false;
		this.restartingTaskList = new HashSet<>();
		this.maxRespawnTime =
				Bukkit.getPluginManager().getPlugin("CTF").getConfig()
						.getInt("restartDelay");
		this.currentRespawnTime = this.maxRespawnTime;
		this.redFlagExists = false;
		this.blueFlagExists = false;
	}

	public void addBlueTeamArmor(LivingEntity player) {
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
		Color blue = Color.BLUE;
		LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
		helmetMeta.setColor(blue);
		helmet.setItemMeta(helmetMeta);
		helmet.addEnchantment(Enchantment.OXYGEN, 3);
		player.getEquipment().setHelmet(helmet);

		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		LeatherArmorMeta chestplateMeta =
				(LeatherArmorMeta) chestplate.getItemMeta();
		chestplateMeta.setColor(blue);
		chestplate.setItemMeta(chestplateMeta);
		player.getEquipment().setChestplate(chestplate);

		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		LeatherArmorMeta leggingsMeta =
				(LeatherArmorMeta) leggings.getItemMeta();
		leggingsMeta.setColor(blue);
		leggings.setItemMeta(leggingsMeta);
		player.getEquipment().setLeggings(leggings);

		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
		bootsMeta.setColor(blue);
		boots.setItemMeta(bootsMeta);
		player.getEquipment().setBoots(boots);
	}

	public void addFlagCarrier(Player p) {
		if (!this.flagCarriers.contains(p)) {
			this.flagCarriers.add(p);
		}
	}

	public void addItemToIngameItems(Item itemToAdd) {
		this.ingameItems.add(itemToAdd);
	}

	public void addRedTeamArmor(LivingEntity player) {
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
		Color red = Color.RED;
		LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
		helmetMeta.setColor(red);
		helmet.setItemMeta(helmetMeta);
		helmet.addEnchantment(Enchantment.OXYGEN, 3);
		player.getEquipment().setHelmet(helmet);

		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		LeatherArmorMeta chestplateMeta =
				(LeatherArmorMeta) chestplate.getItemMeta();
		chestplateMeta.setColor(red);
		chestplate.setItemMeta(chestplateMeta);
		player.getEquipment().setChestplate(chestplate);

		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		LeatherArmorMeta leggingsMeta =
				(LeatherArmorMeta) leggings.getItemMeta();
		leggingsMeta.setColor(red);
		leggings.setItemMeta(leggingsMeta);
		player.getEquipment().setLeggings(leggings);

		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
		bootsMeta.setColor(red);
		boots.setItemMeta(bootsMeta);
		player.getEquipment().setBoots(boots);
	}

	public void addToBlueTeam(Player player) {
		this.blueTeam.add(player);
		this.currentPlayers++;
		player.teleport(this.arena.getBlueSpawn(), TeleportCause.PLUGIN);
		player.setScoreboard(this.getScoreboard());
		this.getScoreboardTeam_Blue().addPlayer(player);
		this.addBlueTeamArmor(player);
		player.sendMessage("Joined Team Blue!");
	}

	public void addToBlueTeam(Zombie bot) {
		this.blueBots.add(bot);
		bot.teleport(this.arena.getBlueSpawn(), TeleportCause.PLUGIN);
		this.addBlueTeamArmor(bot);
	}

	public void addToRedTeam(Player player) {
		this.redTeam.add(player);
		this.currentPlayers++;
		player.teleport(this.arena.getRedSpawn(), TeleportCause.PLUGIN);
		player.setScoreboard(this.getScoreboard());
		this.getScoreboardTeam_Red().addPlayer(player);
		this.addRedTeamArmor(player);
		player.sendMessage("Joined Team Red!");
	}

	public void addToRedTeam(Zombie bot) {
		this.redBots.add(bot);
		bot.teleport(this.arena.getRedSpawn(), TeleportCause.PLUGIN);
		this.addRedTeamArmor(bot);
		// TODO add to scoreboard
	}

	public void closeDown() {// shut down the game and kick everyone (for
								// shutting off the plugin)
		this.stopGame();
		for (Player pl : this.redTeam) {// TODO clear out bots
			this.leaveGame(pl);
			pl.sendMessage("You have been kicked because the game is shutting down.");
		}
		for (Player pl : this.blueTeam) {
			this.leaveGame(pl);
			pl.sendMessage("You have been kicked because the game is shutting down.");
		}
		this.originalInventories.clear();
		this.originalLocations.clear();
		this.redTeam.clear();
		this.blueTeam.clear();
		this.getScoreboardTeam_Blue().unregister();
		this.getScoreboardTeam_Red().unregister();
		this.scoreboard = null;
		this.scoreboardManager = null;

	}

	public void decreaseCurrentRespawnTime() {
		this.currentRespawnTime--;
	}

	private void fillBlueTeam() {
		int toSpawn = this.getNumEmptySlotsBlue();
		for (int i = 0; i < toSpawn; ++i) {
			this.spawnBlueBot();
		}
	}

	private void fillRedTeam() {
		// number to spawn minus max red team size is number of empty slots
		int toSpawn = this.getNumEmptySlotsRed();
		for (int i = 0; i < toSpawn; ++i) {
			this.spawnRedBot();
		}
	}

	private void fillTeams() {
		this.fillBlueTeam();
		this.fillRedTeam();
	}

	public HashSet<Zombie> getAllBots() {
		HashSet<Zombie> list = new HashSet<>();
		list.addAll(this.redBots);
		list.addAll(this.blueBots);
		return list;
	}

	public HashSet<Player> getAllPlayers() {
		HashSet<Player> list = new HashSet<>();
		list.addAll(this.redTeam);
		list.addAll(this.blueTeam);
		return list;
	}

	public Arena getArena() {
		return this.arena;
	}

	public HashSet<Zombie> getBlueBots() {
		return this.blueBots;
	}

	public boolean getBlueFlagAwayFromSpawn() {
		return this.blueFlagAwayFromSpawn;
	}

	public int getBlueScore() {
		return this.blueScore;
	}

	public HashSet<Player> getBlueTeam() {
		return this.blueTeam;
	}

	public int getCurrentPlayers() {
		return this.currentPlayers;
	}

	public int getCurrentRespawnTime() {
		return this.currentRespawnTime;
	}

	public HashSet<LivingEntity> getFlagCarriers() {
		return this.flagCarriers;
	}

	public int getFlagLifetimeBlue() {
		return this.flagLifetimeCounter_Blue;
	}

	public int getFlagLifetimeCounter_Blue() {
		return this.flagLifetimeCounter_Blue;
	}

	public int getFlagLifetimeCounter_Red() {
		return this.flagLifetimeCounter_Red;
	}

	public int getFlagLifetimeRed() {
		return this.flagLifetimeCounter_Red;
	}

	public HashSet<Item> getIngameItems() {
		return this.ingameItems;
	}

	public int getMaxFlagLifetime() {
		return this.maxFlagLifetime;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public int getMaxRespawnTime() {
		return this.maxRespawnTime;
	}

	public int getMaxScore() {
		return this.maxScore;
	}

	/**
	 * Returns the number of player slots that are available on the blue team.
	 *
	 * @return how many players can join the blue team before it is full
	 */
	public int getNumEmptySlotsBlue() {
		return (this.maxPlayers / 2) - this.blueTeam.size();
	}

	/**
	 * Returns the number of player slots that are available on the red team.
	 *
	 * @return how many players can join the red team before it is full
	 */
	public int getNumEmptySlotsRed() {
		return (this.maxPlayers / 2) - this.redTeam.size();
	}

	public Objective getObjective_captures() {
		return this.objective_captures;
	}

	public Objective getObjective_kills() {
		return this.objective_kills;
	}

	public HashMap<String, ItemStack[]> getOriginalArmor() {
		return this.originalArmor;
	}

	public HashMap<String, GameMode> getOriginalGamemode() {
		return this.originalGamemode;
	}

	public HashMap<String, Double> getOriginalHealth() {
		return this.originalHealth;
	}

	public HashMap<String, Integer> getOriginalHunger() {
		return this.originalHunger;
	}

	public HashMap<String, ItemStack[]> getOriginalInventories() {
		return this.originalInventories;
	}

	public HashMap<String, Location> getOriginalLocations() {
		return this.originalLocations;
	}

	public HashMap<String, Float> getOriginalSaturation() {
		return this.originalSaturation;
	}

	/*
	 * private boolean between(double one, double two, double test){ if (one<two
	 * && one<test && test<two){ return true; } if (one>two && one>test &&
	 * test>two){ return true; } return false; }
	 */
	public ItemStack getRandomItem() {
		List<Integer> items =
				Bukkit.getPluginManager().getPlugin("CTF").getConfig()
						.getIntegerList("items");
		int index = 0;
		int size = items.size();
		Material material;
		index = (int) (Math.random() * size);
		switch (items.get(index)) {
		case 267:
			material = Material.IRON_SWORD;
			break;
		case 353:
			material = Material.SUGAR;
			break;
		case 364:
			material = Material.COOKED_BEEF;
			break;
		case 369:// TODO don't spawn sword or rod
			material = Material.BLAZE_ROD;
			break;
		default:
			material = Material.AIR;
			break;
		}
		ItemStack stack = new ItemStack(material);
		if (material == Material.IRON_SWORD) {
			stack.setDurability((short) 241);
		}
		return stack;
	}

	public Location getRandomLocation(int iterations) {
		boolean complete = true;
		Block highestBlock;
		Location returned = this.arena.getCorner1();// defualt value so it will
													// at
		// least spawn
		double x1 = this.arena.getCorner1().getX();
		double x2 = this.arena.getCorner2().getX();
		double y1 = this.arena.getCorner1().getY();
		double y2 = this.arena.getCorner2().getY();
		double z1 = this.arena.getCorner1().getZ();
		double z2 = this.arena.getCorner2().getZ();
		double xfinal, yfinal, zfinal;
		xfinal = this.arena.getCorner1().getX();
		yfinal = this.arena.getCorner1().getY();
		zfinal = this.arena.getCorner1().getZ();
		World worldObjRef = this.arena.getCorner1().getWorld();
		if (x1 <= x2) {
			xfinal = x1 + (Math.random() * (x2 - x1));
		}
		if (x1 > x2) {
			xfinal = x2 + (Math.random() * (x1 - x2));
		}
		if (y1 <= y2) {
			yfinal = y1 + (Math.random() * (y2 - y1));
		}
		if (y1 > y2) {
			yfinal = y2 + (Math.random() * (y1 - y2));
		}
		if (z1 <= z2) {
			zfinal = z1 + (Math.random() * (z2 - z1));
		}
		if (z1 > z2) {
			zfinal = z2 + (Math.random() * (z1 - z2));
		}

		highestBlock =
				worldObjRef.getHighestBlockAt((int) xfinal, (int) zfinal);

		// checks for invalid blocks to spawn on (eg. water or open space)
		if (this.isInvalidTopBlock(highestBlock)) {
			complete = false;
		}
		if (complete || iterations >= 3) {// if the current location is a valid
											// block to spawn over, return the
											// block above it's position.
			yfinal = highestBlock.getLocation().getY() + 1;// only on the top of
															// the blocks
			if (yfinal >= worldObjRef.getMaxHeight() - 1) {
				yfinal = worldObjRef.getMaxHeight() - 2;
			}
			double yrandom = this.getRandomValidY(worldObjRef, xfinal, zfinal);
			if (yrandom != 0 && yrandom <= worldObjRef.getMaxHeight() - 1) {
				yfinal = yrandom + 1;
			}
			returned =
					new Location(this.arena.getCorner1().getWorld(), xfinal,
							yfinal, zfinal);
		}
		else {
			returned = this.getRandomLocation(iterations + 1);
		}
		return returned;
	}

	public double getRandomValidY(World world, double xPos, double zPos) {
		ArrayList<Block> blocks = new ArrayList<>();// arranged from
													// bedrock to sky
		ArrayList<Block> potentialValidBlocks = new ArrayList<>();
		for (int height = 0; height <= (world.getMaxHeight() - 4); height++) {
			blocks.add(world.getBlockAt((int) xPos, height, (int) zPos));
		}
		// search from bedrock to sky
		for (int index = 0; index < (blocks.size() - 2); index++) {
			// if the current block is below an air block
			if (blocks.get(index + 1).getType().equals(Material.AIR)) {
				// make sure the current block is not air (called less often if
				// checked after below air check)

				if (!blocks.get(index).getType().equals(Material.AIR)) {
					// it is two air blocks above the block
					if (blocks.get(index + 2).getType().equals(Material.AIR)) {
						// if this is not one of the blocks items dont spawn
						// above
						if (!(blocks.get(index).getType()
								.equals(Material.ENDER_PORTAL)
								|| blocks.get(index).getType()
										.equals(Material.FIRE)
								|| blocks.get(index).getType()
										.equals(Material.LAVA)
								|| blocks.get(index).getType()
										.equals(Material.PORTAL) || blocks
								.get(index).getType().equals(Material.WATER))) {
							potentialValidBlocks.add(blocks.get(index));
						}
					}
				}
			}
		}
		if (potentialValidBlocks.size() <= 0) {
			// if there are no valid blocks, return the highest non-air block
			return world.getHighestBlockYAt((int) xPos, (int) zPos);
		}
		return potentialValidBlocks.get(
				(int) ((Math.random()) * (potentialValidBlocks.size()))).getY();
	}

	public HashSet<Zombie> getRedBots() {
		return this.redBots;
	}

	public boolean getRedFlagAwayFromSpawn() {
		return this.redFlagAwayFromSpawn;
	}

	public int getRedScore() {
		return this.redScore;
	}

	public HashSet<Player> getRedTeam() {
		return this.redTeam;
	}

	public HashSet<Integer> getRestartingTaskList() {
		return this.restartingTaskList;
	}

	public Score getScore_blue() {
		return this.scoreBlue;
	}

	public Score getScore_red() {
		return this.scoreRed;
	}

	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	public ScoreboardManager getScoreboardManager() {
		return this.scoreboardManager;
	}

	public Team getScoreboardTeam_Blue() {
		return this.scoreboardTeam_Blue;
	}

	public Team getScoreboardTeam_Red() {
		return this.scoreboardTeam_Red;
	}

	public HashSet<Integer> getTaskIDList() {
		return this.taskIDList;
	}

	public boolean increaseKills(LivingEntity p) {
		try {
			this.objective_kills.getScore(p.getCustomName())
					.setScore(
							this.objective_kills.getScore(p.getCustomName())
									.getScore() + 1);
		}
		catch (IllegalArgumentException argE) {// Player is null
			return false;
		}
		catch (IllegalStateException stateE) {// objective not registered
			return false;
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	public void incrementBlueScore() {
		this.blueScore++;
		this.scoreBlue.setScore(this.scoreBlue.getScore() + 1);
		if (this.blueScore >= this.maxScore) {
			this.setActive(false);

			ArrayList<Player> wins = new ArrayList<>();
			ArrayList<Player> loses = new ArrayList<>();

			wins.addAll(this.blueTeam);
			loses.addAll(this.redTeam);
			GameWon event = new GameWon(this, wins, loses);
			Bukkit.getPluginManager().callEvent(event);

			for (Player player : this.blueTeam) {
				player.sendMessage(ChatColor.GREEN + "You win!");
			}
			for (Player player : this.redTeam) {
				player.sendMessage(ChatColor.DARK_RED + "You loose!");
			}
			this.restartGame();
		}
	}

	public void incrementBothLifetimeCounters() {
		this.incrementLifetimeCounterBlue();
		this.incrementLifetimeCounterRed();
	}

	public void incrementLifetimeCounterBlue() {
		this.flagLifetimeCounter_Blue++;
	}

	public void incrementLifetimeCounterRed() {
		this.flagLifetimeCounter_Red++;
	}

	/**
	 * Increases the red teams score
	 */
	public void incrementRedScore() {
		this.redScore++;
		this.scoreRed.setScore(this.scoreRed.getScore() + 1);
		if (this.redScore >= this.maxScore) {
			this.setActive(false);
			ArrayList<Player> wins = new ArrayList<>();
			ArrayList<Player> loses = new ArrayList<>();

			wins.addAll(this.redTeam);
			loses.addAll(this.blueTeam);
			GameWon event = new GameWon(this, wins, loses);
			Bukkit.getPluginManager().callEvent(event);
			// TODO make sure the scoreboard shows
		}
	}

	public boolean isActive() {
		return this.active;
	}

	public boolean isBlueFlagExists() {
		return this.blueFlagExists;
	}

	public boolean isFull() {
		return (this.currentPlayers >= this.maxPlayers);
	}

	public boolean isInGame(LivingEntity p) {
		if (this.redTeam.contains(p) || this.blueTeam.contains(p)
				|| this.redBots.contains(p) || this.blueBots.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isInGame(Player p) {
		if (this.redTeam.contains(p) || this.blueTeam.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isInGame(Zombie p) {
		if (this.redBots.contains(p) || this.blueBots.contains(p)) {
			return true;
		}
		return false;
	}

	private boolean isInvalidTopBlock(Block highestBlock) {
		if (highestBlock == null) {
			return true;
		}
		switch (highestBlock.getType()) {
		case AIR:
		case ENDER_PORTAL:
		case FIRE:
		case LAVA:
		case PORTAL:
		case WATER:
			return true;
		default:
			return false;
		}

	}

	public boolean isOnBlueTeam(LivingEntity p) {
		if (this.blueTeam.contains(p) || this.blueBots.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isOnBlueTeam(Player p) {
		if (this.blueTeam.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isOnBlueTeam(Zombie p) {
		if (this.blueBots.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isOnRedTeam(LivingEntity p) {
		if (this.redTeam.contains(p) || this.redBots.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isOnRedTeam(Player p) {
		if (this.redTeam.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isOnRedTeam(Zombie p) {
		if (this.redBots.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean isRedFlagExists() {
		return this.redFlagExists;
	}

	public boolean isRestarting() {
		return this.restartingGame;
	}

	public boolean isRestartingGame() {
		return this.restartingGame;
	}

	public boolean joinGame(Player p) {
		if (!this.active) {
			if (this.currentPlayers < this.maxPlayers) {
				if (!this.isInGame(p)) {
					this.saveArmor(p);
					this.savePlayerData(p);
					try {
						if (this.redTeam.size() >= this.blueTeam.size()) {
							// add to blue team because red has more players
							this.addToBlueTeam(p);
							if (this.isFull()) {
								this.startGame();
							}
							return true;
						}
						// add to the red team because blue has more players
						this.addToRedTeam(p);
						if (this.isFull()) {
							this.startGame();
						}
						return true;
					}
					catch (Exception e) {
						p.sendMessage("Error adding to team.");
					}
				}
				else {
					p.sendMessage("You are already in that game!");
				}
			}
			else {
				p.sendMessage("That game is full!");
			}
		}
		else {
			p.sendMessage("That game is currently in progress.");
		}
		return false;
	}

	public void leaveGame(Player p) {
		boolean logic_isFlagCarrier = false;
		if (this.flagCarriers.contains(p)) {
			logic_isFlagCarrier = true;
		}
		if (this.redTeam.contains(p)) {
			if (logic_isFlagCarrier) {
				this.spawnRedFlagAtLocation(p.getLocation());
				this.flagCarriers.remove(p);
			}
			this.redTeam.remove(p);
			this.scoreboardTeam_Red.removePlayer(p);
		}
		if (this.blueTeam.contains(p)) {
			if (logic_isFlagCarrier) {
				this.spawnBlueFlagAtLocation(p.getLocation());
				this.flagCarriers.remove(p);
			}
			this.blueTeam.remove(p);
			this.scoreboardTeam_Blue.removePlayer(p);
		}
		this.getScoreboard().resetScores(p.getDisplayName());
		// p.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
		// p.getScoreboard().getObjective(DisplaySlot.BELOW_NAME).unregister();
		p.getInventory().clear();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		for (int i = 0; i < this.originalInventories.get(p.getName()).length; i++) {
			if (this.originalInventories.get(p.getName())[i] != null) {
				p.getInventory().addItem(
						this.originalInventories.get(p.getName())[i].clone());
			}
		}
		p.setCustomNameVisible(false);
		p.setCustomName(null);

		p.getInventory().setHelmet(
				this.originalArmor.get(p.getName())[0].clone());
		p.getInventory().setChestplate(
				this.originalArmor.get(p.getName())[1].clone());
		p.getInventory().setLeggings(
				this.originalArmor.get(p.getName())[2].clone());
		p.getInventory().setBoots(
				this.originalArmor.get(p.getName())[3].clone());
		this.originalInventories.remove(p.getName());
		p.teleport(this.originalLocations.get(p.getName()));
		p.setGameMode(this.originalGamemode.get(p.getName()));
		p.setHealth(this.originalHealth.get(p.getName()));
		p.setFoodLevel(this.originalHunger.get(p.getName()));
		p.setSaturation(this.originalSaturation.get(p.getName()));
		this.originalHealth.remove(p.getName());
		this.originalHunger.remove(p.getName());
		this.originalSaturation.remove(p.getName());
		this.originalGamemode.remove(p.getName());
		this.originalLocations.remove(p);
		this.currentPlayers--;
		if (this.currentPlayers <= 0) {
			this.currentPlayers = 0;
			if (this.isActive()) {
				this.stopGame();
			}
		}
	}

	public void removeBot(Zombie bot) {
		if (bot == null) {
			return;
		}
		if (this.redBots.contains(bot)) {
			this.redBots.remove(bot);
			bot.setHealth(0);
			bot.remove();// (ooh) kill 'em
		}
		if (this.blueBots.contains(bot)) {
			this.blueBots.remove(bot);
			bot.setHealth(0);
			bot.remove();// (ooh) kill 'em
		}
	}

	public void removeFlagCarrier(LivingEntity p) {
		if (this.flagCarriers.contains(p)) {
			this.flagCarriers.remove(p);
		}
	}

	public void removeItemFromIngameItems(Item itemToRemove) {
		this.ingameItems.remove(itemToRemove);
	}

	public void resetFlagLifetimeCounterBlue() {
		this.flagLifetimeCounter_Blue = 0;
	}

	public void resetFlagLifetimeCounterRed() {
		this.flagLifetimeCounter_Red = 0;
	}

	public void respawnBot(Zombie bot) {
		if (this.flagCarriers != null) {
			if (this.flagCarriers.contains(bot)) {
				this.flagCarriers.remove(bot);
			}
		}
		bot.setHealth(bot.getMaxHealth());
		bot.setTarget(null);// stop targeting things
		bot.setFireTicks(0);
		bot.setRemainingAir(bot.getMaximumAir());
		for (PotionEffect effect : bot.getActivePotionEffects()) {
			bot.removePotionEffect(effect.getType());
		}
		if (this.isOnBlueTeam(bot)) {
			this.addBlueTeamArmor(bot);
			bot.teleport(this.getArena().getBlueSpawn(), TeleportCause.PLUGIN);
		}
		if (this.isOnRedTeam(bot)) {
			this.addRedTeamArmor(bot);
			bot.teleport(this.getArena().getRedSpawn(), TeleportCause.PLUGIN);
		}

	}

	public void respawnPlayer(Player p) {
		if (this.flagCarriers != null) {
			if (this.flagCarriers.contains(p)) {
				this.flagCarriers.remove(p);
			}
		}
		p.setHealth(p.getMaxHealth());
		p.setExhaustion(0);
		p.setSaturation(20);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setRemainingAir(p.getMaximumAir());
		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		if (this.isOnBlueTeam(p)) {
			this.addBlueTeamArmor(p);
			p.teleport(this.getArena().getBlueSpawn(), TeleportCause.PLUGIN);
		}
		if (this.isOnRedTeam(p)) {
			this.addRedTeamArmor(p);
			p.teleport(this.getArena().getRedSpawn(), TeleportCause.PLUGIN);
		}

	}

	public void restartGame() {
		if (!this.isRestarting()) {
			this.setRestarting(true);
			this.startRestartingTask();
		}
	}

	private void saveArmor(Player p) {
		try {
			ItemStack[] inventory = p.getInventory().getContents();
			ItemStack[] saveInventory = new ItemStack[inventory.length];
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] != null) {
					saveInventory[i] = inventory[i].clone();
				}
			}
			this.originalInventories.put(p.getName(), saveInventory);
			p.getInventory().clear();
			ItemStack[] armorInventory = p.getInventory().getArmorContents();
			if (!(p.getInventory().getHelmet() == null)) {
				armorInventory[0] = p.getInventory().getHelmet().clone();
			}
			else {
				armorInventory[0] = new ItemStack(Material.AIR);
			}
			if (!(p.getInventory().getChestplate() == null)) {
				armorInventory[1] = p.getInventory().getChestplate().clone();
			}
			else {
				armorInventory[1] = new ItemStack(Material.AIR);
			}
			if (!(p.getInventory().getLeggings() == null)) {
				armorInventory[2] = p.getInventory().getLeggings().clone();
			}
			else {
				armorInventory[2] = new ItemStack(Material.AIR);
			}
			if (!(p.getInventory().getBoots() == null)) {
				armorInventory[3] = p.getInventory().getBoots().clone();
			}
			else {
				armorInventory[3] = new ItemStack(Material.AIR);
			}
			p.getInventory().setHelmet(null);
			p.getInventory().setChestplate(null);
			p.getInventory().setLeggings(null);
			p.getInventory().setBoots(null);
			this.originalArmor.put(p.getName(), armorInventory);
		}
		catch (Exception e) {
			p.sendMessage("Error saving armor." + e.getStackTrace()[0]);
		}
	}

	private void savePlayerData(Player p) {
		try {
			this.originalLocations.put(p.getName(), p.getLocation());
			this.originalGamemode.put(p.getName(), p.getGameMode());
			this.originalHealth.put(p.getName(), p.getHealth());
			this.originalHunger.put(p.getName(), p.getFoodLevel());
			this.originalSaturation.put(p.getName(), p.getSaturation());
			p.setGameMode(GameMode.SURVIVAL);
		}
		catch (Exception e) {
			p.sendMessage("Error storing player data");
		}
	}

	public void setActive(boolean isActive) {
		this.active = isActive;
		if (isActive) {
			if (this.isRestarting()) {
				this.stopRestartingTasks();
				this.setRestarting(false);
				this.currentRespawnTime = this.maxRespawnTime;
			}
			this.startItemTask();
			this.startFlagCarrierDisplayTask();
			this.startFlagLogicTask();
		}
		else {
			this.stopGameTasks();
			this.stopRestartingTasks();
		}
	}

	public void setBlueFlagAwayFromSpawn(boolean isAway) {
		this.blueFlagAwayFromSpawn = isAway;
	}

	public void setBlueFlagExists(boolean doesExist) {
		this.blueFlagExists = doesExist;
	}

	public void setRedFlagAwayFromSpawn(boolean isAway) {
		this.redFlagAwayFromSpawn = isAway;
	}

	public void setRedFlagExists(boolean doesExist) {
		this.redFlagExists = doesExist;
	}

	public void setRestarting(boolean restarting) {
		this.restartingGame = restarting;
	}

	public void setScoreBlue(Score score) {
		this.scoreBlue = score;
	}

	public void setScoreboardTeam_Blue(Team team) {
		this.scoreboardTeam_Blue = team;
	}

	public void setScoreboardTeam_Red(Team team) {
		this.scoreboardTeam_Red = team;
	}

	public void setScoreRed(Score score) {
		this.scoreRed = score;
	}

	private void spawnBlueBot() {
		Location spawnb = this.arena.getBlueSpawn();
		Zombie npctest =
				(Zombie) spawnb.getWorld().spawnEntity(spawnb,
						EntityType.ZOMBIE);
		npctest.setCustomName(ChatColor.BLUE + "BOT_" + Game.getRandomBotName()
				+ ChatColor.RESET);
		npctest.setCustomNameVisible(true);
		npctest.setBaby(false);
		npctest.setVillager(false);
		npctest.setTarget(null);
		npctest.setRemoveWhenFarAway(false);
		npctest.setCanPickupItems(true);
		this.addBlueTeamArmor(npctest);
		((Ctf) Bukkit.getPluginManager().getPlugin("CTF")).getBotGameMap().put(
				npctest, this);
		this.blueBots.add(npctest);
		// TODO pick class
	}

	public void spawnBlueFlag() {
		this.spawnBlueFlagAtLocation(this.arena.getBlueFlag());
	}

	/**
	 * Spawns a blue flag at the specified location
	 *
	 * @param loc
	 */
	public void spawnBlueFlagAtLocation(Location loc) {
		if (this.blueFlagExists) {
			return;
		}
		ItemStack flag = new ItemStack(Material.LAPIS_BLOCK);
		Item theItem =
				this.arena.getBlueFlag().getWorld()
						.dropItemNaturally(loc, flag);
		ItemMeta metaData = theItem.getItemStack().getItemMeta();
		theItem.setMetadata("isCTFFlag", new FixedMetadataValue(Bukkit
				.getPluginManager().getPlugin("CTF"), true));
		List<String> meta = new ArrayList<>();
		meta.add("Blue Flag");
		metaData.setLore(meta);
		theItem.getItemStack().setItemMeta(metaData);
		this.ingameItems.add(theItem);
		this.blueFlagAwayFromSpawn = true;
		this.blueFlagExists = true;
		this.resetFlagLifetimeCounterBlue();

	}

	public void spawnBothFlags() {
		this.spawnBlueFlag();
		this.spawnRedFlag();
	}

	private void spawnRedBot() {
		Location spawnr = this.arena.getRedSpawn();
		Zombie npctest =
				(Zombie) spawnr.getWorld().spawnEntity(spawnr,
						EntityType.ZOMBIE);
		npctest.setCustomName(ChatColor.RED + "BOT_" + Game.getRandomBotName()
				+ ChatColor.RESET);
		npctest.setCustomNameVisible(true);
		npctest.setBaby(false);
		npctest.setVillager(false);
		npctest.setTarget(null);
		npctest.setRemoveWhenFarAway(false);
		npctest.setCanPickupItems(true);
		this.addRedTeamArmor(npctest);
		((Ctf) Bukkit.getPluginManager().getPlugin("CTF")).getBotGameMap().put(
				npctest, this);
		this.redBots.add(npctest);
	}

	/**
	 * Spawns the red flag at the arenas red flag spawn location. If there is
	 * already a flag in play, it will not spawn one. The flag spawns without an
	 * initial velocity.
	 *
	 * @see Game#spawnRedFlagAtLocation(Location)
	 */
	public void spawnRedFlag() {
		this.spawnRedFlagAtLocation(this.arena.getRedFlag());
	}

	/**
	 * Spawns a red flag at the specified location. If there is already a flag
	 * in play, it will not spawn one. The flag spawns without an initial
	 * velocity.
	 *
	 * @param loc the location to spawn a flag at
	 */
	public void spawnRedFlagAtLocation(Location loc) {
		if (this.redFlagExists) {
			return;
		}
		ItemStack flag = new ItemStack(Material.REDSTONE_BLOCK);
		Item theItem = this.arena.getRedFlag().getWorld().dropItem(loc, flag);
		theItem.setMetadata("isCTFFlag", new FixedMetadataValue(Bukkit
				.getPluginManager().getPlugin("CTF"), true));
		ItemMeta metaData = theItem.getItemStack().getItemMeta();
		List<String> meta = new ArrayList<>();
		meta.add("Red Flag");
		metaData.setLore(meta);
		theItem.getItemStack().setItemMeta(metaData);
		this.ingameItems.add(theItem);
		this.redFlagAwayFromSpawn = false;
		this.redFlagExists = true;
		this.resetFlagLifetimeCounterRed();

	}

	public void startFlagCarrierDisplayTask() {
		this.taskIDList.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(
				Bukkit.getPluginManager().getPlugin("CTF"),

				new Runnable() {

					@Override
					public void run() {
						int random = (int) (Math.random() * 5);
						if (random == 1) {
							for (LivingEntity player : Game.this
									.getFlagCarriers()) {
								player.getWorld().strikeLightningEffect(
										player.getLocation());
							}
						}
					}

				}, 0, 20));
	}

	public void startFlagLogicTask() {
		this.taskIDList.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(
				Bukkit.getPluginManager().getPlugin("CTF"),

				new Runnable() {

					@Override
					public void run() {
						if (Game.this.getRedFlagAwayFromSpawn()) {
							Game.this.incrementLifetimeCounterRed();
						}
						if (Game.this.getBlueFlagAwayFromSpawn()) {
							Game.this.incrementLifetimeCounterBlue();
						}

						HashSet<Item> removalBuffer = new HashSet<>();
						if (Game.this.getFlagLifetimeRed() > Game.this
								.getMaxFlagLifetime()) {
							for (Item flag : Game.this.getIngameItems()) {
								if (flag.getItemStack().hasItemMeta()) {
									if (flag.getItemStack().getItemMeta()
											.hasLore()) {
										for (String str : flag.getItemStack()
												.getItemMeta().getLore()) {
											if (str.equalsIgnoreCase("Red Flag")) {
												removalBuffer.add(flag);
												Game.this
														.resetFlagLifetimeCounterRed();
												Game.this
														.setRedFlagExists(false);
											}
										}
									}
								}
							}
						}
						if (Game.this.getFlagLifetimeBlue() > Game.this
								.getMaxFlagLifetime()) {
							for (Item flag : Game.this.getIngameItems()) {
								if (flag.getItemStack().hasItemMeta()) {
									if (flag.getItemStack().getItemMeta()
											.hasLore()) {
										for (String str : flag.getItemStack()
												.getItemMeta().getLore()) {
											if (str.equalsIgnoreCase("Blue Flag")) {
												removalBuffer.add(flag);
												Game.this
														.resetFlagLifetimeCounterBlue();
												Game.this
														.setBlueFlagExists(false);
											}
										}
									}
								}
							}
						}
						for (Item itemToRemove : removalBuffer) {
							itemToRemove.setMetadata("despawnable",
									new FixedMetadataValue(Bukkit
											.getPluginManager()
											.getPlugin("CTF"), true));
							Game.this.getIngameItems().remove(itemToRemove);// remove
							// items
							// from flags
							// (this should
							// prevent
							// concurrent
							// modification
							// of the
							// hashset)
							itemToRemove.remove();// mark the item for removal
													// from the world after its
													// removed from the flags
													// set to prevent null
													// reference
						}
						removalBuffer.clear();// clear the new references to the
												// items to allow memory
												// collection
					}// end of run

				}, 0, 20));
	}

	public void startGame() {
		this.setActive(true);
		this.getScore_blue().setScore(0);
		this.getScore_red().setScore(0);
		this.spawnBothFlags();
		for (Player pl : this.redTeam) {
			pl.getInventory().clear();
			this.respawnPlayer(pl);
		}
		for (Player pl : this.blueTeam) {
			pl.getInventory().clear();
			this.respawnPlayer(pl);
		}
		this.fillTeams();
	}

	public void startItemTask() {
		this.taskIDList.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(
				Bukkit.getPluginManager().getPlugin("CTF"),

				new Runnable() {

					@Override
					public void run() {

						int random =
								(int) (Math.random() * Bukkit
										.getPluginManager().getPlugin("CTF")
										.getConfig().getInt("itemspawnchance"));
						if (random == 0) {
							Game.this
									.getIngameItems()
									.add(Game.this
											.getArena()
											.getCorner1()
											.getWorld()
											.dropItemNaturally(
													Game.this
															.getRandomLocation(1),
													Game.this.getRandomItem()));
						}
					}

				}, 0, 10));
	}

	public void startRestartingTask() {
		this.taskIDList.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(
				Bukkit.getPluginManager().getPlugin("CTF"),

				new Runnable() {

					@Override
					public void run() {
						if (Game.this.isRestarting()) {
							for (Player p : Game.this.getAllPlayers()) {
								if (Game.this.getCurrentRespawnTime() > 0) {
									p.sendMessage(ChatColor.GREEN + ""
											+ ChatColor.BOLD + "Restarting in:"
											+ ChatColor.RESET + ""
											+ ChatColor.BOLD + ""
											+ ChatColor.RED
											+ Game.this.getCurrentRespawnTime());
									Game.this.decreaseCurrentRespawnTime();
								}
								else {
									Game.this.startGame();
									break;
								}
							}
						}
						else {
							Game.this.setRestarting(false);
							Game.this.stopRestartingTasks();
						}
					}

				}, 0, 20));
	}

	public void stopGame() {
		this.setActive(false);
		// this.getScoreboard().resetScores(Bukkit.getOfflinePlayer(ChatColor.BLUE+"Blue:"));
		// this.getScoreboard().resetScores(Bukkit.getOfflinePlayer(ChatColor.RED+"Red:"));
		this.getScore_blue().setScore(0);
		this.getScore_red().setScore(0);
		for (Item item : this.ingameItems) {
			this.redFlagExists = false;
			this.blueFlagExists = false;
			item.setMetadata("despawnable", new FixedMetadataValue(Bukkit
					.getPluginManager().getPlugin("CTF"), true));
			item.remove();
		}
		for (Player pl : this.redTeam) {
			pl.getInventory().clear();
		}
		for (Player pl : this.blueTeam) {
			pl.getInventory().clear();
		}
		// prevents concurrent modification of bot lists
		ArrayDeque<Zombie> tempBotList = new ArrayDeque<>();

		for (Zombie zomb : this.redBots) {
			tempBotList.add(zomb);
		}
		for (Zombie zomb : this.blueBots) {
			tempBotList.add(zomb);
		}
		for (Zombie zomb : tempBotList) {
			RemoveBot event = new RemoveBot(zomb);
			Bukkit.getPluginManager().callEvent(event);
		}
		tempBotList.clear();
	}

	public void stopGameTasks() {
		for (int i : this.taskIDList) {
			Bukkit.getScheduler().cancelTask(i);
		}
	}

	public void stopRestartingTasks() {
		for (int i : this.restartingTaskList) {
			Bukkit.getScheduler().cancelTask(i);
		}

	}

	public boolean teamsMatch(LivingEntity p1, LivingEntity p2) {
		boolean p1Red = false;
		boolean p1Blue = false;
		boolean p2Red = false;
		boolean p2Blue = false;
		if (this.redTeam.contains(p1) || this.redBots.contains(p1)) {
			p1Red = true;
		}// else because if its red it must not be blue
		if (this.blueTeam.contains(p1) || this.blueBots.contains(p1)) {
			p1Blue = true;
		}
		if (this.redTeam.contains(p2) || this.redBots.contains(p2)) {
			p2Red = true;
		}// else because if its red it must not be blue
		if (this.blueTeam.contains(p2) || this.blueBots.contains(p2)) {
			p2Blue = true;
		}

		return (p1Red && p2Red) || (p1Blue && p2Blue);
	}
}
