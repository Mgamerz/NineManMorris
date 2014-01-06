/**
 * This class is used by the AI when selecting moves.
 * @author Michael Perez
 * 
 */

package com.cs471.ninemanmill;

/**
 * This class holds variables for a move - starting Ring, Position, and destination Ring, Position (Rs, Ps and Rd, Pd). It also holds what team this move belongs to.
 * @author Michael Perez
 * @author Sean Wright
 *
 */
public class Move {
	public int Rs; /* Position this move starts at */
	public int Ps;
	public int Rd; /* Destination coordinates */
	public int Pd;
	protected Team team;
	
	/**
	 * Defines a valid move for from a square on the board. It defines the starting and ending position of a move.
	 * @param team The team that can perform this move.
	 * @param Rs Starting ring.
	 * @param Ps Starting position on the starting ring.
	 * @param Rd Destination ring.
	 * @param Pd Destination position on the destination ring.
	 */
	public Move(Team team, int Rs, int Ps, int Rd, int Pd){
		this.team=team;
		this.Rs=Rs;
		this.Ps=Ps;
		this.Rd=Rd;
		this.Pd=Pd;
	}
	
	/**
	 * Compares if this move and another move have the same destination.
	 * @param move Move to compare to.
	 * @return true if the destination of another move and this move are the same, false otherwise.
	 */
	public boolean sameDestination(Move move){
		return (Rd == move.Rd && Pd == move.Pd);
	}
	
	/**
	 * Get's the starting position of this move. It is used when comparing the starting point of moves and pieces.
	 * @return Starting location of this move, encapsulated into a GamePiece object.
	 */
	public GamePiece getSourcePosition(){
		return new GamePiece(Rs,Ps);
	}
	
	/**
	 * String representation of this move object, showing the team symbol and starting/ending positions.
	 * @return String representation of this move
	 */
	public String toString(){
		return "Move for team "+team.getSymbol()+", source "+Rs+","+Ps+" to destination "+Rd+","+Pd;
	}
}