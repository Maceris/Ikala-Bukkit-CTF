package com.ikalagaming.bukkit.ctf;

import org.bukkit.Location;

public class ArenaCreationData {
	public Location locCorner1, locCorner2, locRedSpawn, locBlueSpawn,
			locRedFlag, locBlueFlag;
	public boolean corner1Complete, corner2Complete, redSpawnComplete,
			blueSpawnComplete, redFlagComplete, blueFlagComplete;

	public String getIncomplete() {
		int completed = 0;
		String corner1, corner2, redSpawn, blueSpawn, redFlag, blueFlag;
		corner1 = "";
		corner2 = "";
		redSpawn = "";
		blueSpawn = "";
		redFlag = "";
		blueFlag = "";
		String output = "You are missing the following:";
		if (!this.corner1Complete) {
			completed++;
			corner1 = " Corner 1";
		}
		if (!this.corner2Complete) {
			completed++;
			corner2 = " Corner 2";
		}
		if (!this.redSpawnComplete) {
			completed++;
			redSpawn = " Red Spawn";
		}
		if (!this.blueSpawnComplete) {
			completed++;
			blueSpawn = " Blue Spawn";
		}
		if (!this.redFlagComplete) {
			completed++;
			redFlag = " Red Flag";
		}
		if (!this.blueFlagComplete) {
			completed++;
			blueFlag = " Blue Flag";
		}
		output += corner1;
		output += corner2;
		output += redSpawn;
		output += blueSpawn;
		output += redFlag;
		output += blueFlag;
		if (completed <= 0) {
			output += " Nothing";
		}
		return output;
	}

	public String getInvalidlocations() {
		String redSpawnValid = "";
		String blueSpawnValid = "";
		String redFlagValid = "";
		String blueFlagValid = "";
		String output = "The following are invalid:";
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locRedSpawn)) {
			redSpawnValid = " Red Spawn";
		}
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locBlueSpawn)) {
			blueSpawnValid = " Blue Spawn";
		}
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locRedFlag)) {
			redFlagValid = " Red Flag";
		}
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locBlueFlag)) {
			blueFlagValid = " Blue Flag";
		}
		output += redSpawnValid;
		output += blueSpawnValid;
		output += redFlagValid;
		output += blueFlagValid;
		return output;
	}

	public Location getLocBlueFlag() {
		return this.locBlueFlag;
	}

	public Location getLocBlueSpawn() {
		return this.locBlueSpawn;
	}

	public Location getLocCorner1() {
		return this.locCorner1;
	}

	public Location getLocCorner2() {
		return this.locCorner2;
	}

	public Location getLocRedFlag() {
		return this.locRedFlag;
	}

	public Location getLocRedSpawn() {
		return this.locRedSpawn;
	}

	public boolean isComplete() {
		return (this.corner1Complete && this.corner2Complete
				&& this.redSpawnComplete && this.blueSpawnComplete
				&& this.redFlagComplete && this.blueFlagComplete);

	}

	public boolean locationsAreValid() {
		boolean redSpawnValid = false;
		boolean blueSpawnValid = false;
		boolean redFlagValid = false;
		boolean blueFlagValid = false;
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locRedSpawn)) {
			redSpawnValid = true;
		}
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locBlueSpawn)) {
			blueSpawnValid = true;
		}
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locRedFlag)) {
			redFlagValid = true;
		}
		if (BlockMath.isInside(this.locCorner1, this.locCorner2,
				this.locBlueFlag)) {
			blueFlagValid = true;
		}
		return (redSpawnValid && blueSpawnValid && redFlagValid && blueFlagValid);
	}

	public void setLocBlueFlag(Location newLocation) {
		this.locBlueFlag = newLocation;
		this.blueFlagComplete = true;
	}

	public void setLocBlueSpawn(Location newLocation) {
		this.locBlueSpawn = newLocation;
		this.blueSpawnComplete = true;
	}

	public void setLocCorner1(Location newLocation) {
		this.locCorner1 = newLocation;
		this.corner1Complete = true;
	}

	public void setLocCorner2(Location newLocation) {
		this.locCorner2 = newLocation;
		this.corner2Complete = true;
	}

	public void setLocRedFlag(Location newLocation) {
		this.locRedFlag = newLocation;
		this.redFlagComplete = true;
	}

	public void setLocRedSpawn(Location newLocation) {
		this.locRedSpawn = newLocation;
		this.redSpawnComplete = true;
	}

}
