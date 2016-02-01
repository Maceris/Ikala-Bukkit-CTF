package com.ikalagaming.bukkit.ctf;

import org.bukkit.Location;

public class BlockMath {

	public static boolean intersectsWith(final Location corner1,
			final Location corner2, final Location testLocation1,
			final Location testLocation2) {
		return testLocation2.getBlockX() >= corner1.getBlockX()
				&& testLocation1.getBlockX() <= corner2.getBlockX() ? testLocation2
				.getBlockY() >= corner1.getBlockY()
				&& testLocation1.getBlockY() <= corner2.getBlockY() ? testLocation2
				.getBlockZ() >= corner1.getBlockZ()
				&& testLocation1.getBlockZ() <= corner2.getBlockZ() : false
				: false;
	}

	public static boolean isInside(final Location corner1,
			final Location corner2, final Location testLocation) {
		boolean betweenX = false;
		boolean betweenY = false;
		boolean betweenZ = false;
		if (corner1.getX() < corner2.getX()) {// if 1 is less than 2
			if (testLocation.getX() >= corner1.getX()) {
				// 1
				if (testLocation.getX() <= corner2.getX()) {
					betweenX = true;
				}
			}
		}
		else if (testLocation.getX() <= corner1.getX()) {
			if (testLocation.getX() >= corner2.getX()) {
				betweenX = true;
			}
		}

		if (corner1.getY() < corner2.getY()) {// if 1 is less than 2
			if (testLocation.getY() >= corner1.getY()) {
				// 1
				if (testLocation.getY() <= corner2.getY()) {
					betweenY = true;
				}
			}
		}
		else if (testLocation.getY() <= corner1.getY()) {
			if (testLocation.getY() >= corner2.getY()) {
				betweenY = true;
			}
		}

		if (corner1.getZ() < corner2.getZ()) {// if 1 is less than 2
			if (testLocation.getZ() >= corner1.getZ()) {
				// 1
				if (testLocation.getZ() <= corner2.getZ()) {
					betweenZ = true;
				}
			}
		}
		else if (testLocation.getZ() <= corner1.getZ()) {
			if (testLocation.getZ() >= corner2.getZ()) {
				betweenZ = true;
			}
		}
		return betweenX && betweenY && betweenZ;
	}
}