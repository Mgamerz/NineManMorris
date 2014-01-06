package com.cs471.ninemanmill;

/**
 * Bundle returned by NewGameDialog containing both a boolean for fly mode and the opponent choice.
 * NewGameDialog uses a bundle as multiple objects must be returned through a single return object.
 * @author Michael Perez
 *
 */
public class NewGameBundle {
	private int player1,player2;
	private boolean flyMode;
	
	/**
	 * Creates a new bundle with player 1 and player 2 difficulties, as well as the flag for allowing fly mode.
	 * @param player1
	 * @param player2
	 * @param flyMode
	 */
	public NewGameBundle(int player1, int player2, boolean flyMode){
		this.player1 = player1;
		this.player2 = player2;
		this.flyMode = flyMode;
	}
	
	/**
	 * Gets the player 1 player difficulty.
	 * @return player 1 difficulty
	 */
	public int getPlayer1() {
		return player1;
	}
	
	/**
	 * Gets the player 2 player difficulty.
	 * @return player 2 difficulty
	 */
	public int getPlayer2() {
		return player2;
	}
	
	/**
	 * Gets the flag for fly mode from this bundle. 
	 * @return flag for flymode, to set flymode enable/disable on a gamestate
	 */
	public boolean getFlyMode() {
		return flyMode;
	}
}
