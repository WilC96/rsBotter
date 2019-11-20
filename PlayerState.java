package com.fishing;

public enum PlayerState {
	SMALLNET(new String[] { "Small fishing net", "Coins" }, "Net", 1521, 621), 
	BAIT(new String[] { "Fishing rod", "Fishing bait", "Coins" }, "Bait", 1521, 623), 
	CAGE(new String[] { "Lobster pot", "Coins" }, "Cage", 1522, 619);

	private final String[] tool;
	private final String action;
	private final int fishingSpot;
	private final int fishingAnim;

	PlayerState(String[] t, String action, int f, int anim) {
		this.tool = t;
		this.action = action;
		this.fishingSpot = f;
		this.fishingAnim = anim;
	}

	public String[] getTool() {
		return tool;
	}

	public String getAction() {
		return action;
	}

	public int getFishingSpot() {
		return fishingSpot;
	}

	public int getFishingAnim() {
		return fishingAnim;
	}
}
