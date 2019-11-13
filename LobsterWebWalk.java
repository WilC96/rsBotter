package com.fishing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Glaciation96", info = "SecondAttempt", name = "KlobsterWebWalk", version = 0, logo = "")

public class LobsterWebWalk extends Script {

	private FishInterpolation fishDelay = new FishInterpolation(random(20, 30), random(1800, 2300));
	
	// Places of interest for the bot to go
	public final Area KaramFishingSpot = new Area(2921, 3174, 2927, 3180);
	public final Area KaramHarbour = new Area(2950, 3147, 2957, 3146);
	public final Area PortSarim = new Area(3026, 3222, 3029, 3213);
	public final Area DepositBox = new Area(3044, 3237, 3052, 3234);
	public final Area b4DepositBox = new Area(3041, 3238, 3043, 3234);
	public final Area karamBoat = new Area(2955, 3141, 2957, 3144).setPlane(1);
	public final Area sarimBoat = new Area(3038, 3210, 3031, 3223).setPlane(1);
	public final Area DraynorBank = new Area(3092, 3246, 3096, 3241);

	// Custom list of walk path not yet in use
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

	private KaramFishing levelFishType;
	volatile static boolean hasInteracted = false;
	boolean breakFishing = false, isPoor = false;
	long currentlyIdledFor, lastActionTime;

	public final String[] goods = { "Raw lobster", "Raw swordfish", "Lobster", "Swordfish" };
	
	Stream<String> goods2 = Stream.of("Raw lobster", "Raw swordfish", "Lobster", "Swordfish");
	
	public final String[] trash = { "Raw sardine", "Raw herring", "Raw shrimps", "Raw anchovies" };
	public final String[] SeamenOption = { "Yes please." };
	public final String[] CustomsOptions = { "Can I journey on this ship?", "Search away, I have nothing to hide.",
			"Ok." };

	@Override
	public void onStart() {
		log("Let's get glitched out!");
		new Thread((Runnable) fishDelay).start();
	}

	// ============================================= onLoop
	@Override
	public int onLoop() throws InterruptedException {
		// Check our fishing level at each iteration
		fishingType();
		
		// Check for equipment depending on what we are fishing
		if (!doWeHaveEverythingYet()) { 
			getNecessities();
		}
		if (getInventory().getAmount("Coins") <= 60) {
			isPoor = true;
			while (isPoor) {
				moneyMoneyMoneyTeeeam();
			}
		}
		if (KaramFishingSpot.contains(myPosition())) {
			breakFishing = false;
			log("breakFishing = false");
			while (breakFishing == false) {
				switch (getFishState()) {
				case FISH:
					beginFishing();
					Sleep.sleepUntil(() -> !myPlayer().isAnimating(), (int) (Math.random() * 500 + 150));		
					break;
				case TRASHFULL:
					dropTrash();
					break;
				case LOBSFULL:
					doDeposit();
					break;
				case IDLE:
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
			Sleep.sleepUntil(() -> KaramFishingSpot.contains(myPosition()), (int) (Math.random() * 500 + 150));
			// Change up the anonymous classes to lambdas using our Sleep class with BooleanSupplier
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
			levelFishType = KaramFishing.SMALLNET;
		} else if (getSkills().getStatic(Skill.FISHING) >= 5 && getSkills().getStatic(Skill.FISHING) < 40) {
			levelFishType = KaramFishing.BAIT;
		} else if (getSkills().getStatic(Skill.FISHING) >= 40) {
			levelFishType = KaramFishing.CAGE;
		}
	}

	// ============================================= Fishing states
	private FishState getFishState() throws InterruptedException {
		log("Getting fishState...");
		if (!getInventory().isFull() && this.myPlayer().getAnimation() == -1) {
			return FishState.FISH;
		} else if (levelFishType.getFishingAnim() == this.myPlayer().getAnimation()) {
			return FishState.FISH;
		} else if (getInventory().isFull() && levelFishType == KaramFishing.CAGE) {
			return FishState.LOBSFULL;
		} else if (getInventory().isFull() && levelFishType != KaramFishing.CAGE) {
			return FishState.TRASHFULL;
		} else {
			return FishState.IDLE;
		}
	}

	// ============================================= Fishing begins!!!
	private boolean freeFish(GroundItem fish) {	
		return getMap().canReach(fish) && fish != null && levelFishType == KaramFishing.CAGE;
	}
	
	private void beginFishing() throws InterruptedException {
		log("beginFishing() - Attempting to check if there are any free fish.");
		
		if (freeFish(getGroundItems().closest(goods))) {
			int lastCount = getInventory().getEmptySlots();
			getGroundItems().closest(goods).interact("Take");
			Sleep.sleepUntil(() -> getInventory().getEmptySlots() < lastCount || idleFor(random(2500, 5000)), 
					(int) (Math.random() * 500 + 150));
		}
		
		else if (!this.inventory.isFull() && !myPlayer().isAnimating()) {
			if (FishInterpolation.canInteract) {
				log("beginFishing() - We are fishing!");
				
				NPC fishingSpot = fishingSpot(levelFishType.getFishingSpot()); 
				//Finds us the current closest fishing spot according to KaramFishings character check
				
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
		default: // Should not get called
			fishingSpot.interact(action);
		}
	}

	// ============================================= Randomised drop pattern
	private void dropTrash() throws InterruptedException {
		List<Integer> invSlots = Arrays.asList( 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
				25, 26, 27 );
		
		// Randomised dropping sequence - needs improvement
		Collections.shuffle(invSlots);

		getKeyboard().pressKey(KeyEvent.VK_SHIFT);
		for (int i = 0; i < invSlots.size(); i++) {
			Item item = getInventory().getItemInSlot(invSlots.get(i));
			if (item != null && item.nameContains(trash)) {
				getInventory().interact(invSlots.get(i));
				
				// Conditional sleep not required
				sleep(random(20, 30));
			}
		}
		getKeyboard().releaseKey(KeyEvent.VK_SHIFT);
		log("dropTrash - breakFishing = true");
		breakFishing = true;
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
						Sleep.sleepUntil(() -> getInventory().getEmptySlots() > lastCount, (int) (Math.random() * 500 + 150));
						
					} else {
						bank.depositAll();
						Sleep.sleepUntil(() -> getInventory().getEmptySlots() == 28, (int) (Math.random() * 500 + 150));
						
						bank.withdrawAll("Coins");
						Sleep.sleepUntil(() -> getInventory().getEmptySlots() == 27, (int) (Math.random() * 500 + 150));
					}
				}
				log("getNecessities - Checking if 'levelFishType.getTool()' exists in bank...");
				if (getBank().contains(levelFishType.getTool())) {
					bank.withdraw(levelFishType.getTool()[0], 1);
					Sleep.sleepUntil(() -> inventory.contains(levelFishType.getTool()[0]), (int) (Math.random() * 500 + 150));
					
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
					Sleep.sleepUntil(() -> inventory.contains(levelFishType.getTool()[1]), (int) (Math.random() * 500 + 150));
							
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
				Sleep.sleepUntil(() -> KaramFishingSpot.contains(myPosition()), (int) (Math.random() * 500 + 150));
				
			}
		}
	}

	// ============================================= Check if we are at the bank
	private void checkAtBank() {
		if (!DraynorBank.contains(this.myPosition())) {
			log("checkAtBank - Attempting to walk to Draynor bank");
			this.walking.webWalk(DraynorBank);
			Sleep.sleepUntil(() -> DraynorBank.contains(myPosition()), (int) (Math.random() * 500 + 150));	
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
					Sleep.sleepUntil(() -> getInventory().getAmount("Coins") > 60, (int) (Math.random() * 500 + 150));
					
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
				Sleep.sleepUntil(() -> getBank().isOpen(), (int) (Math.random() * 500 + 150));
						
			} else {
				log("We are not at bank, attempting to reach destination");
				this.walking.webWalk(DraynorBank);
				Sleep.sleepUntil(() -> DraynorBank.contains(myPosition()), (int) (Math.random() * 500 + 150));

				return !getBank().isOpen();
			}
		}
		return getBank().isOpen();
	}

	// ============================================= Depositing at deposit box
	private void doDeposit() {
		if (!DepositBox.contains(this.myPosition())) {
			this.walking.webWalk(DepositBox);
			Sleep.sleepUntil(() -> b4DepositBox.contains(myPosition()), (int) (Math.random() * 500 + 150));
					
		}
		log("doDeposit - Searching for Deposit Box...");
		while (!this.depositBox.isOpen()) {
			RS2Object Dbox = getObjects().closest("Bank deposit box");
			if (Dbox != null && Dbox.exists()) {
				Dbox.interact("Deposit");
				Sleep.sleepUntil(() -> depositBox.isOpen(), (int) (Math.random() * 500 + 150));
						
			}
		}
		log("Attempting to deposit goods...");
		if (this.depositBox.isOpen() && getInventory().isFull()) {
			this.depositBox.depositAllExcept(levelFishType.getTool());
			Sleep.sleepUntil(() -> inventory.isEmptyExcept(levelFishType.getTool()), (int) (Math.random() * 500 + 150));
					
			this.depositBox.close();
		}
	}

	// ============================================= Create fishing spot to call
	private NPC fishingSpot(int s) {
		NPC fishingSpot = this.npcs.closest(s);
		return fishingSpot;
	}
	
	// ============================================= Check idle time
	private boolean idleFor(int millis) {
		if (myPlayer().isAnimating() || myPlayer().isMoving()) {
			lastActionTime = System.currentTimeMillis();
		} else {
			currentlyIdledFor = System.currentTimeMillis();
		}
		return lastActionTime + millis < currentlyIdledFor;
	}

	// ============================================= Something's wrong. Exit
	// game :(
	private void exitGame() {
		logoutTab.logOut();
	}

	@Override
	public void onExit() {
		new Thread((Runnable) fishDelay).interrupt(); // Best practice?
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
