package com.ikalagaming.bukkit.ctf;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PlayerClass {
	public String getClassName() {
		return "ErrorClass";
	}

	public ItemStack[] getHotbarDefault() {
		ItemStack first = new ItemStack(Material.DIRT);
		List<String> lore = new ArrayList<>();
		lore.add("The Error cube");
		first.getItemMeta().setLore(lore);
		return new ItemStack[] {first};
	}
}
