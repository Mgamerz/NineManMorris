/** This class represents a game piece on the board
 *  Each player has an array list of GamePieces that keeps track of
 *  their current game pieces on the board. Each piece has a variable for
 *  its row and position in the row. The equals method is overwritten so
 *  it is easier to remove them from the arraylist.
 *  
 *  @author swright
 */

package com.cs471.ninemanmill;

/**
 * The gamepiece class holds information about a piece, such as its position and ring, and optional parameters such as what it represents, the color, and other pieces of information.
 * @author Sean Wright
 *
 */
public class GamePiece{
	public final static char PLAYER1_PIECE = '1'; //for broken pieces
	public final static char PLAYER2_PIECE = '2'; //for broken pieces
	private int r, p;
	private char teamColor;
	private int direction; //used for drawing busted ones
	private boolean inMill;
	
	public void setInMill(boolean inMill) {
		this.inMill = inMill;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * Returns the team color of this piece
	 * @return Get's the color of that this piece represents. This is an optional variable and should only be considered when it is explicitly set by the calling context.
	 */
	public int getTeamColor() {
		return teamColor;
	}

	/**
	 * Constructor
	 * @param r The ring the piece is located on (0-2)
	 * @param p The position of the piece on a ring (0-7)
	 */
	public GamePiece(int r, int p){
		this.r = r;
		this.p = p;
	}
	
	/**
	 * Constructor
	 * @param gp Game piece reference
	 */
	public GamePiece(GamePiece gp){
		r = gp.r;
		p = gp.p;
	}
	
	/**
	 * Constructor
	 * @param r The ring the piece is located on (0-2)
	 * @param p The position of the piece on a ring (0-7)
	 * @param teamColor The team color of the piece (1 or 2)
	 */
	public GamePiece(int r, int p, char teamColor){
		this.r = r;
		this.p = p;
		this.teamColor = teamColor;
	}
	
	/**
	 * Gets the ring of the piece (0-2)
	 * @return The ring where the piece is at
	 */
	public int getR(){
		return r;
	}
	
	/**
	 * Gets the position of the piece on a ring (0-7)
	 * @return The position on the ring where a piece is at
	 */
	public int getP(){
		return p;
	}
	
	/**
	 * Sets the ring for a piece to be on
	 * @param r The target ring
	 */
	public void setRing(int r){
		this.r = r;
	}
	
	/**
	 * Sets the position on a ring for a piece to be on
	 * @param p The target position
	 */
	public void setPos(int p){
		this.p = p;
	}
	
	/**
	 * Compares two gamepieces to see if they are the same
	 * @param gp Game piece reference
	 * @return True if the game piece is the same as this
	 */
	@Override
	public boolean equals(Object o){
		if (!(o instanceof GamePiece)){
			return false; //its not even comparable
		}
		GamePiece gp = (GamePiece) o;
//		System.out.println("Comparing gamepieces");
		if(gp.p == this.p && gp.r == this.r) return true;
		else return false;
	}
	
	/**
	 * String representation of the game piece
	 */
	@Override
	public String toString(){
		return ("Piece at "+r+","+p);
	}

	/**
	 * Sets the team color of this piece (1 for red, 2 for blue)
	 * @param REPRESENTING The target team
	 */
	public void setTeamColor(char REPRESENTING) {
		this.teamColor = REPRESENTING;
	}

	public boolean isInMill() {
		return inMill;
	}
}