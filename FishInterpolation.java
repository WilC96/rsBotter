package com.fishing;

public class FishInterpolation {

	volatile static boolean canInteract = false;
	boolean infiniteLoopCheck = true;
	int delayTimer;
	int randomTimer;

	public FishInterpolation(int randomTimer, int delayTimer) {
		this.randomTimer = randomTimer;
		this.delayTimer = delayTimer;
	}

	public void run() {
		while (infiniteLoopCheck) {
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
