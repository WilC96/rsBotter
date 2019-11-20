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

	private KaramFishing levelFishType;
	volatile static boolean hasInteracted = false;
	boolean breakFishing = false, isPoor = false;
	long currentlyIdledFor, lastActionTime;

	public final String[] goods = { "Raw lobster", "Raw swordfish", "Lobster", "Swordfish" };
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
		// Check if we have enough money to make the fishing trip
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
			Sleep.sleepUntil(() -> KaramFishingSpot.contains(myPosition()), (int) (Math.random() * 500 + 150));	
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
			return FishState.FISH; // Infinite loop to check for free fish in
									// real time
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
				// Finds us the current closest fishing spot according to
				// KaramFishings character check

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
		List<Integer> invSlots = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
				21, 22, 23, 24, 25, 26, 27);

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

	// ============================================= Grabbing necessities
	private void getNecessities() throws InterruptedException {
		if (!checkAtBank()) {
			this.walking.webWalk(DraynorBank);
			Sleep.sleepUntil(this::checkAtBank, (int) (Math.random() * 500 + 150));
		}

		log("getNecessities - At Draynor bank");
		if (!getBank().isOpen())
			openBank();

		if (!getInventory().isEmpty() && getBank().depositAll()) {
			log("getNecessities - Inventory contains items so deposit them all");
			Sleep.sleepUntil(() -> getInventory().isEmpty(), (int) (Math.random() * 500 + 150));

		}
		withdrawMoney(); // Get that money!
		withdrawTools(); // Get them tools!
	}

	// ============================================= Check for coins in bank
	// (reusable)
	public void withdrawMoney() {

		// Stream bank items for coins if we have any or else exit the game
		Item needMoney = Stream.of(bank.getItems()).filter(this::isMoney).findFirst().orElse(null);

		if (needMoney != null) {
			getBank().withdrawAll("Coins");
			Sleep.sleepUntil(() -> getInventory().contains("Coins"), (int) (Math.random() * 500 + 150));
		} else
			exitGame(); // No money so exit game
	}

	public boolean isMoney(Item item) {
		return item.getName().contains("Coins");
	}

	// ============================================= Check for tools in bank
	public void withdrawTools() {

		// Check for tools availability
		List<String> myTools = new ArrayList<>();

		// We don't want to include the coins at the end of array
		for (int i = 0; i < levelFishType.getTool().length - 1; i++) {

			if (bank.contains(levelFishType.getTool()[i]))
				myTools.add(levelFishType.getTool()[i]);
			else
				exitGame(); // Missing required tools so exit game
		}

		myTools.forEach(item -> {
			if (item.equals("Fishing bait"))
				bank.withdrawAll(item);
			else
				bank.withdraw(item, 1);
			Sleep.sleepUntil(() -> getInventory().contains(item), (int) (Math.random() * 500 + 150));
		});
	}

	// ============================================= Check if we are at the bank
	private boolean checkAtBank() {
		return DraynorBank.contains(this.myPosition());
	}

	// ============================================= Open bank
	private void openBank() throws InterruptedException {

		RS2Object bank = getObjects().closest("Bank booth");

		if (bank.exists() && bank != null && bank.interact("Bank"))
			Sleep.sleepUntil(() -> getBank().isOpen(), (int) (Math.random() * 500 + 150));
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
	private NPC fishingSpot(int id) {
		// NPC fishingSpot = this.npcs.closest(id);
		NPC fishingSpot = getNpcs().closest(id);
		return fishingSpot;
	}

	// ============================================= MONEY MAY ALL DAY
	private boolean moneyMoneyMoneyTeeeam() throws InterruptedException {
		if (checkAtBank()) {
			if (!getBank().isOpen()) {
				openBank();
			}
			if (getBank().isOpen()) {
				withdrawMoney();

			}
		} else {
			this.walking.webWalk(DraynorBank);
			Sleep.sleepUntil(this::checkAtBank, (int) (Math.random() * 500 + 150));
			return isPoor = true;
		}
		return isPoor = false;
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

	// ============================================= We messed up, exit game
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
