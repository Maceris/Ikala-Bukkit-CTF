package com.ikalagaming.bukkit.ctf;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.ikalagaming.bukkit.ctf.event.GameItemUsed;
import com.ikalagaming.bukkit.ctf.event.GameWon;

public class GameListener implements Listener {

	Ctf ctf = (Ctf) Bukkit.getPluginManager().getPlugin("CTF");

	@EventHandler
	public void onGameItemUsed(GameItemUsed event) {
		switch (event.getItem().getType()) {
		case SUGAR:
			event.getUser().addPotionEffect(
					new PotionEffect(PotionEffectType.SPEED, 100, 2));
			event.getUser().addPotionEffect(
					new PotionEffect(PotionEffectType.JUMP, 100, 1));
			break;
		case COOKED_BEEF:
			if (event.getUser().getHealth() <= (event.getUser().getMaxHealth() - 4)) {
				event.getUser().setHealth(event.getUser().getHealth() + 4);

			}
			else if (event.getUser().getHealth() < (event.getUser()
					.getMaxHealth())) {
				event.getUser().setHealth(event.getUser().getMaxHealth());

			}
			else {
				// event.setCancelled(true);
			}
			break;
		case BLAZE_ROD:
			event.getUser().launchProjectile(Fireball.class);
			break;
		case REDSTONE_BLOCK: {
			boolean isFlag = false;
			if (event.getItem().hasItemMeta()) {
				if (event.getItem().getItemMeta().hasLore()) {
					for (String str : event.getItem().getItemMeta().getLore()) {
						if (str.equalsIgnoreCase("Red Flag")) {
							isFlag = true;
							break;
						}
					}
				}
			}
			if (!isFlag) {
				break;
			}
			Arena arena = this.ctf.getGame(event.getUser()).getArena();
			// check that its the opposite team
			if (this.ctf.getGame(event.getUser()).isOnBlueTeam(event.getUser())) {
				if (arena.isNear(event.getUser().getLocation(),
						arena.getBlueFlag(), 3)) {
					Game game = this.ctf.getGame(event.getUser());
					game.setRedFlagExists(false);
					game.spawnRedFlag();
					game.incrementBlueScore();
					game.removeFlagCarrier(event.getUser());
				}
			}
		}
			break;
		case LAPIS_BLOCK: {
			boolean isFlag = false;
			if (event.getItem().hasItemMeta()) {
				if (event.getItem().getItemMeta().hasLore()) {
					for (String str : event.getItem().getItemMeta().getLore()) {
						if (str.equalsIgnoreCase("Blue Flag")) {
							isFlag = true;
							break;
						}
					}
				}
			}
			if (!isFlag) {
				break;
			}

			if (this.ctf.getGame(event.getUser()).isActive()) {
				Arena arena = this.ctf.getGame(event.getUser()).getArena();
				// check they are near their flag spawn
				if (arena.isNear(event.getUser().getLocation(),
						arena.getRedFlag(), 3)) {
					if (this.ctf.getGame(event.getUser()).isOnRedTeam(
							event.getUser())) {
						Game game = this.ctf.getGame(event.getUser());
						game.setBlueFlagExists(false);
						game.spawnBlueFlag();
						game.incrementRedScore();
						game.removeFlagCarrier(event.getUser());
					}
				}
			}
		}
			break;
		default:
			break;
		}
	}

	@EventHandler
	public void onGameWon(GameWon event) {
		// TODO reward players
		for (Player player : event.getWinners()) {
			player.sendMessage(ChatColor.GREEN + "You win!");
		}
		for (Player player : event.getLoosers()) {
			player.sendMessage(ChatColor.DARK_RED + "You loose!");
		}
		event.getGame().restartGame();

	}
}
