package com.fishing.com.fishing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "Glaciation96", info = "SecondAttempt", name = "KlobsterWebWalk", version = 0, logo = "")

public class LobsterWebWalk extends Script {

	private fishInterpolation fishDelay = new fishInterpolation(random(20, 30), random(1800, 2300));

	public final Area KaramFishingSpot = new Area(2921, 3174, 2927, 3180);
	public final Area KaramHarbour = new Area(2950, 3147, 2957, 3146);
	public final Area PortSarim = new Area(3026, 3222, 3029, 3213);
	public final Area DepositBox = new Area(3044, 3237, 3052, 3234);
	public final Area b4DepositBox = new Area(3041, 3238, 3043, 3234);
	public final Area karamBoat = new Area(2955, 3141, 2957, 3144).setPlane(1);
	public final Area sarimBoat = new Area(3038, 3210, 3031, 3223).setPlane(1);
	public final Area DraynorBank = new Area(3092, 3246, 3096, 3241);

	// walkPath List not yet in use
	List<Position> myPositionName = new ArrayList<>();
	Position[] lobToKaramHarbour = { new Position(2922, 3172, 0), new Position(2919, 3168, 0),
			new Position(2916, 3163, 0), new Position(2916, 3153, 0), new Position(2922, 3151, 0),
			new Position(2930, 3149, 0), new Position(2939, 3146, 0), new Position(2947, 3146, 0),
			new Position(2954, 3147, 0) };
	Position[] SarimHtoDbox = { new Position(3028, 3220, 0), new Position(3027, 3227, 1), new Position(3028, 3230, 1),
			new Position(3028, 3236, 1), new Position(3032, 3235, 1), new Position(3038, 3236, 1),
			new Position(3044, 3235, 1), new Position(3048, 3235, 1) };
	Position[] DboxToSarimH = { new Position(3044, 3235, 0), new Position(3040, 3235, 1), new Position(3037, 3236, 1),
			new Position(3033, 3235, 1), new Position(3029, 3236, 1), new Position(3027, 3232, 1),
			new Position(3028, 3228, 1), new Position(3027, 3223, 1), new Position(3027, 3218, 1) };
	Position[] KarimHtoLobs = { new Position(2955, 3147, 0), new Position(2951, 3146, 1), new Position(2947, 3147, 1),
			new Position(2943, 3145, 1), new Position(2939, 3145, 1), new Position(2934, 3148, 1),
			new Position(2929, 3151, 1), new Position(2924, 3151, 1), new Position(2919, 3152, 1),
			new Position(2915, 3153, 1), new Position(2917, 3156, 1), new Position(2920, 3161, 1),
			new Position(2923, 3165, 1), new Position(2924, 3170, 1), new Position(2924, 3174, 1) };

	private karamFishing levelFishType;
	volatile static boolean hasInteracted = false;
	boolean breakFishing = false, isPoor = false;
	long currentlyIdledFor, lastActionTime;

	public final String[] dank = { "Raw lobster", "Raw swordfish", "Lobster", "Swordfish" };
	public final String[] SeamenOption = { "Yes please." };
	public final String[] CustomsOptions = { "Can I journey on this ship?", "Search away, I have nothing to hide.",
			"Ok." };

	private enum fishState {
		fish, invFullTrash, invFullLobs, idle;
	}

	private enum karamFishing {
		smallNetFishing(new String[] { "Small fishing net", "Coins" }, "Net", 1521, 621), 
		baitFishing(new String[] { "Fishing rod", "Fishing bait", "Coins" }, "Bait", 1521, 623), 
		cageFishing(new String[] { "Lobster pot", "Coins" }, "Cage", 1522, 619);

		private final String[] tool;
		private final String action;
		private final int fishingSpot;
		private final int fishingAnim;

		karamFishing(String[] t, String action, int f, int anim) {
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

	private boolean idleFor(int millis) {
		if (myPlayer().isAnimating() || myPlayer().isMoving()) {
			lastActionTime = System.currentTimeMillis();
		} else {
			currentlyIdledFor = System.currentTimeMillis();
		}
		return lastActionTime + millis < currentlyIdledFor;
	}

	@Override
	public void onStart() {
		log("Let's get glitched out!");
		new Thread(fishDelay).start();
	}

	// ============================================= onLoop
	@Override
	public int onLoop() throws InterruptedException {
		fishingType();
		if (!doWeHaveEverythingYet()) {
			getNecessities();
		}
		if (getInventory().getAmount("Coins") <= 60) {
			isPoor = true;
			while (isPoor == true) {
				moneyMoneyMoneyTeeeam();
			}
		}
		if (KaramFishingSpot.contains(myPosition())) {
			breakFishing = false;
			log("breakFishing = false");
			while (breakFishing == false) {
				switch (getFishState()) {
				case fish:
					beginFishing();
					new ConditionalSleep(1200, (int) (Math.random() * 500 + 250)) {
						@Override
						public boolean condition() throws InterruptedException {
							return !myPlayer().isAnimating();
						}
					}.sleep();
					break;
				case invFullTrash:
					dropTrash();
					break;
				case invFullLobs:
					doDeposit();
					break;
				case idle:
					// Do nothing
					break;
				}
			}
		} else {
			log("onLoop - Walking to fishing spot...");
			this.walking.webWalk(KaramFishingSpot);
			/*
			 * new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
			 * 
			 * @Override public boolean condition() throws InterruptedException
			 * { return KaramFishingSpot.contains(myPosition()); } }.sleep();
			 */
			Sleep.sleepUntil(() -> KaramFishingSpot.contains(myPosition()), (int) (Math.random() * 500 + 250));
		}
		log("We broke out the switch! Looping again...");
		return random(200, 300);
	}

	// ============================================= Inventory check
	public boolean doWeHaveEverythingYet() {
		for (String item : levelFishType.getTool()) {
			if (!inventory.contains(item)) {
				return false;
			}
		}
		return true;
	}

	// ============================================= fishingType
	private void fishingType() {
		if (getSkills().getStatic(Skill.FISHING) >= 1 && getSkills().getStatic(Skill.FISHING) < 5) {
			levelFishType = karamFishing.smallNetFishing;
		} else if (getSkills().getStatic(Skill.FISHING) >= 5 && getSkills().getStatic(Skill.FISHING) < 40) {
			levelFishType = karamFishing.baitFishing;
		} else if (getSkills().getStatic(Skill.FISHING) >= 40) {
			levelFishType = karamFishing.cageFishing;
		}
	}

	// ============================================= Fishing states
	private fishState getFishState() throws InterruptedException {
		log("Getting fishState...");
		if (!getInventory().isFull() && this.myPlayer().getAnimation() == -1) {
			return fishState.fish;
		} else if (levelFishType.getFishingAnim() == this.myPlayer().getAnimation()) {
			return fishState.fish;
		} else if (getInventory().isFull() && levelFishType == karamFishing.cageFishing) {
			return fishState.invFullLobs;
		} else if (getInventory().isFull() && levelFishType != karamFishing.cageFishing) {
			return fishState.invFullTrash;
		} else {
			return fishState.idle;
		}
	}

	// ============================================= Fishing begins!!!
	private void beginFishing() throws InterruptedException {
		log("beginFishing() - Attempting to check if fish exists.");
		GroundItem freeFish = getGroundItems().closest(dank);
		if (freeFish != null && !this.inventory.isFull() && levelFishType == karamFishing.cageFishing) {
			log("beginFishing() - Fish exists, proceeding to pick up...");
			if (getMap().canReach(freeFish)) {
				int lastCount = getInventory().getEmptySlots();
				freeFish.interact("Take");
				new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
					@Override
					public boolean condition() throws InterruptedException {
						return getInventory().getEmptySlots() < lastCount || idleFor(random(3200, 5000));
					}
				}.sleep();
			}
		} else if (!this.inventory.isFull() && !myPlayer().isAnimating()) {
			if (fishInterpolation.canInteract) {
				log("beginFishing() - We are fishing!");
				NPC fishingSpot = fishingSpot(levelFishType.getFishingSpot());
				engageFishing(fishingSpot, levelFishType.getAction());
				hasInteracted = true;
				sleep(random(35, 50));
			} else {
				sleep(random(35, 50));
			}
		}
	}

	private void engageFishing(NPC fishingSpot, String action) {
		int xFish = random(0, 1);
		log("xFish = " + xFish);
		switch (xFish) {
		case 0:
			if (fishingSpot != null && fishingSpot.exists()) {
				this.mouse.move(random(100, 500), random(100, 500));
				fishingSpot.interact(action);
			}
			break;
		case 1:
			if (fishingSpot != null && fishingSpot.exists()) {
				fishingSpot.hover();
				this.mouse.click(true);
				fishingSpot.interact(action);
			}
			break;
		default:
			fishingSpot.interact(action);
		}
	}

	// ============================================= Randomised drop pattern
	private void dropTrash() throws InterruptedException {
		int[] invSlotIndex = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
				25, 26, 27, };

		funkyDropPattern(invSlotIndex);
		getKeyboard().pressKey(KeyEvent.VK_SHIFT);
		for (int i = 0; i < invSlotIndex.length; i++) {
			Item item = getInventory().getItemInSlot(invSlotIndex[i]);
			if (item != null && item.nameContains("Raw sardine", "Raw herring", "Raw shrimps", "Raw anchovies")) {
				getInventory().interact(invSlotIndex[i]);
				sleep(random(20, 30));
			}
		}
		getKeyboard().releaseKey(KeyEvent.VK_SHIFT);
		log("dropTrash - breakFishing = true");
		breakFishing = true;
	}

	private static void funkyDropPattern(int[] invSlot) {
		for (int i = 0; i < invSlot.length; i++) {

			int s = i + (int) (Math.random() * (invSlot.length - i));

			int temp = invSlot[s];
			invSlot[s] = invSlot[i];
			invSlot[i] = temp;
		}
	}

	// ============================================= Grabbing the necessary
	// items
	private void getNecessities() throws InterruptedException {
		checkAtBank();
		if (DraynorBank.contains(this.myPosition())) {
			log("getNecessities - At Draynor bank");
			if (!getBank().isOpen()) {
				while (!getBank().isOpen()) {
					openBank();
				}
			}
			if (getBank().isOpen()) {
				if (getInventory().getEmptySlots() <= 27) {
					log("getNecessities - Inventory contains less than or equal to 27 free slots");
					if (getInventory().contains("Coins")) {
						log("getNecessities - Inventory has coins");
						int lastCount = getInventory().getEmptySlots();
						bank.depositAllExcept("Coins");
						new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
							@Override
							public boolean condition() throws InterruptedException {
								return getInventory().getEmptySlots() > lastCount;
							}
						}.sleep();
					} else {
						bank.depositAll();
						new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {

							@Override
							public boolean condition() throws InterruptedException {
								return getInventory().getEmptySlots() == 28;
							}
						}.sleep();
						bank.withdrawAll("Coins");
						new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
							@Override
							public boolean condition() throws InterruptedException {
								return getInventory().getEmptySlots() == 27;
							}
						}.sleep();
					}
				}
				log("getNecessities - Checking if 'levelFishType.getTool()' exists in bank...");
				if (getBank().contains(levelFishType.getTool())) {
					bank.withdraw(levelFishType.getTool()[0], 1);
					new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {

						@Override
						public boolean condition() throws InterruptedException {
							return inventory.contains(levelFishType.getTool()[0]);
						}
					}.sleep();
				} else {
					log("getNecessities - 'levelFishType.getTool()' does not exist in bank, logging out");
					exitGame();
				}
			}
			if (levelFishType.getTool().length > 2) {
				log("getNecessities - karamFishing.baitFishing checked, withdrawing Fishing bait");
				if (!getBank().isOpen()) {
					while (!getBank().isOpen()) {
						openBank();
					}
				}
				if (getBank().contains(levelFishType.getTool()[1])) {
					getBank().withdrawAll(levelFishType.getTool()[1]);
					new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
						@Override
						public boolean condition() throws InterruptedException {
							return inventory.contains(levelFishType.getTool()[1]);
						}
					}.sleep();
				} else {
					log("getNecessities - Fishing bait does not exist in bank, logging out");
					exitGame();
				}
			}
			if (!KaramFishingSpot.contains(myPosition())) {
				log("getNecessities - Attempting to close bank and return to fishing spot...");
				while (getBank().isOpen()) {
					getBank().close();
				}
				this.walking.webWalk(KaramFishingSpot);
				new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
					@Override
					public boolean condition() throws InterruptedException {
						return KaramFishingSpot.contains(myPosition());
					}
				}.sleep();
			}
		}
	}

	// ============================================= Check if we are at the bank
	private void checkAtBank() {
		if (!DraynorBank.contains(this.myPosition())) {
			log("checkAtBank - Attempting to walk to Draynor bank");
			this.walking.webWalk(DraynorBank);
			new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
				@Override
				public boolean condition() throws InterruptedException {
					return DraynorBank.contains(myPosition());
				}
			}.sleep();
		}
	}

	// ============================================= MONEY MAY ALL DAY
	private boolean moneyMoneyMoneyTeeeam() {
		if (getInventory().getAmount("Coins") < 60) {
			if (DraynorBank.contains(this.myPosition())) {
				while (!getBank().isOpen()) {
					openBank();
				}
				if (getBank().isOpen()) {
					getBank().withdrawAll("Coins");
					new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
						@Override
						public boolean condition() throws InterruptedException {
							return getInventory().getAmount("Coins") > 60;
						}
					}.sleep();
				}
			} else {
				checkAtBank();
				return isPoor = true;
			}
		}
		return isPoor = false;
	}

	// ============================================= Open bank
	private boolean openBank() {
		if (!getBank().isOpen()) {
			log("Attempting to open bank");
			RS2Object bankBooth = this.getObjects().closest("Bank booth");
			if (bankBooth != null && bankBooth.exists()) {
				bankBooth.interact("Bank");
				new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
					@Override
					public boolean condition() throws InterruptedException {
						return getBank().isOpen();
					}
				}.sleep();
			} else {
				log("We are not at bank, attempting to reach destination");
				this.walking.webWalk(DraynorBank);
				new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
					@Override
					public boolean condition() throws InterruptedException {
						return DraynorBank.contains(myPosition());
					}

				}.sleep();

				// Sleep.sleepUntil(()-> myPlayer().IsAnimating(), 500);

				return !getBank().isOpen();
			}
		}
		return getBank().isOpen();
	}

	// ============================================= Depositing at deposit box
	private void doDeposit() {
		if (!DepositBox.contains(this.myPosition())) {
			this.walking.webWalk(DepositBox);
			new ConditionalSleep(1500, (int) (Math.random() * 500 + 250)) {
				@Override
				public boolean condition() throws InterruptedException {
					return b4DepositBox.contains(myPosition());
				}
			}.sleep();
		}
		log("doDeposit - Searching for Deposit Box...");
		while (!this.depositBox.isOpen()) {
			RS2Object Dbox = getObjects().closest("Bank deposit box");
			if (Dbox != null && Dbox.exists()) {
				Dbox.interact("Deposit");
				new ConditionalSleep(2000, (int) (Math.random() * 500 + 250)) {
					@Override
					public boolean condition() throws InterruptedException {
						return depositBox.isOpen();
					}
				}.sleep();
			}
		}
		log("Attempting to deposit goods...");
		if (this.depositBox.isOpen() && getInventory().isFull()) {
			this.depositBox.depositAllExcept(levelFishType.getTool());
			new ConditionalSleep(2000, (int) (Math.random() * 500 + 250)) {
				@Override
				public boolean condition() throws InterruptedException {
					return inventory.isEmptyExcept(levelFishType.getTool());
				}
			}.sleep();
			this.depositBox.close();
		}
	}

	// ============================================= Create fishing spot to call
	private NPC fishingSpot(int s) {
		NPC fishingSpot = this.npcs.closest(s);
		return fishingSpot;
	}

	// ============================================= Something's wrong. Exit
	// game :(
	private void exitGame() {
		logoutTab.logOut();
	}

	@Override
	public void onExit() {
		new Thread(fishDelay).interrupt();
		log("Houston, we have a problem...");
	}

	@Override
	public void onPaint(Graphics2D g) {
		Point mP = getMouse().getPosition();
		g.setColor(Color.WHITE);
		g.drawLine(mP.x - 3, mP.y - 3, mP.x - 3, mP.y + 3);
		g.drawLine(mP.x - 3, mP.y - 3, mP.x + 3, mP.y - 3);
		g.drawLine(mP.x - 3, mP.y + 3, mP.x + 3, mP.y + 3);
		g.drawLine(mP.x + 3, mP.y + 3, mP.x + 3, mP.y - 3);
	}
}

class fishInterpolation implements Runnable {

	volatile static boolean canInteract = false;
	int delayTimer;
	int randomTimer;

	public fishInterpolation(int randomTimer, int delayTimer) {
		this.delayTimer = delayTimer;
		this.randomTimer = randomTimer;
	}

	public void run() {
		for (int i = 0; i < 1; i = 0) {
			while (LobsterWebWalk.hasInteracted == false) {
				canInteract = true;
				try {
					Thread.sleep(randomTimer);
				} catch (InterruptedException e) {
					System.out.println("This line will never run..." + e);
				}
			}
			canInteract = false;
			LobsterWebWalk.hasInteracted = false;
			try {
				Thread.sleep(delayTimer);
			} catch (InterruptedException e) {
				System.out.println("This line will never run..." + e);
			}
			canInteract = true;
		}
	}
}
