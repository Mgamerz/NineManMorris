package com.cs471.ninemanmill;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * A team is a collection of gamepieces, as well as a character that represents the team on a gameboard. This allows extensibility if we wanted to have 3 players, for example.
 * It also holds how many pieces it has left to place on the board, as well as other team related functions and data. This object is essential for mill detection and AI operation.
 * @author Michael Perez
 * @author Sean Wright
 *
 */
public class Team {
	private char teamSymbol;
	ArrayList<GamePiece> teamPieces;
	public ArrayList<GamePiece> getTeamPieces() {
		return teamPieces;
	}

	private int piecesRemainingToPlace;
	
	/**
	 * Constructs a new team that can play on the board. By default, Nine Man Mill gives each team 9 pieces to play with.
	 * @param teamSymbol The symbol that represents the team in the board data structure.
	 * @param piecesRemainingToPlace The number of pieces to start with for placing for this team.
	 */
	public Team(char teamSymbol, int piecesRemainingToPlace){
		teamPieces = new ArrayList<GamePiece>();
		this.teamSymbol=teamSymbol;
		this.piecesRemainingToPlace=piecesRemainingToPlace;
	}

	/**
	 * Copy constructor for the AI to use in its search.
	 * @param oldTeam The old team.
	 */
	public Team(Team oldTeam){
		teamPieces = new ArrayList<GamePiece>();
		for(int i=0;i<oldTeam.teamPieces.size();i++) {
			teamPieces.add(new GamePiece(oldTeam.getPiece(i)));
		}
		teamSymbol = oldTeam.teamSymbol;
		piecesRemainingToPlace = oldTeam.piecesRemainingToPlace;
	}
	
	/**
	 * Constructs a new team object, with the specified list of pieces. 
	 * This was used when we had a unit tester.
	 * @param pieces List of pieces this team owns.
	 */
	public Team(ArrayList<GamePiece> pieces){
		teamPieces = pieces;
	}

	/**
	 * Constructor for making a team with no parameters. It is used for making teams where a team parameter is required (for code consistency)
	 * 
	 */
	public Team() {
		// Do nothing for the default constructor. This is required to be here though.
	}

	/**
	 * Returns the amount of pieces this team has left to place on the board
	 * @return Number of pieces this team has left to place on the board.
	 */
	public int getPiecesRemainingToPlace() {
		return piecesRemainingToPlace;
	}

	/**
	 * Sets the amount of remaining pieces that this team can place on the board.
	 * @param piecesRemainingToPlace Number of pieces allowed to be placed.
	 */
	public void setPiecesRemainingToPlace(int piecesRemainingToPlace) {
		this.piecesRemainingToPlace = piecesRemainingToPlace;
	}

	/**
	 * Set's this team's symbol on the board. Valid values are defined in GameBoard's constants.
	 * This should not be changed once a team is in use.
	 * @param teamSymbol Symbol to set as a board representation
	 */
	public void setTeamSymbol(char teamSymbol) {
		this.teamSymbol = teamSymbol;
	}

	/**
	 * Gets this team's character symbol.
	 * @return Character representing this team's symbol on the board
	 */
	public char getSymbol() {
		return teamSymbol;
	}
	
	/**
	 * Gets the opponent team's character symbol.
	 * @return Character representing opponent team's symbol on the board
	 */
	public char getOpponentSymbol() {
		if(this.getSymbol() == 1){
			return 2;
		}
		else return 1;
	}

	/**
	 * Decrements this team's amount of remaining placeable pieces by 1 and adds it to the list of pieces this team knows about.
	 * @param r ring index to construct a gamepiece
	 * @param p position on the ring construct a gamepiece
	 */
	public void usePiece(int r, int p) {
		//System.out.println("Used piece of team "+getSymbol()+", remaining: "+piecesRemainingToPlace);
		piecesRemainingToPlace--;
		teamPieces.add(new GamePiece(r,p));
	}

	/**
	 * Removes the gamepiece from this team that corresponds to R, P on the board.
	 * There is no easy way to do this so we have to use our own iterator unfortunately
	 * @param r Ring
	 * @param p Position on ring
	 * @return 
	 */
	public GamePiece removePiece(int r, int p) {
 		//System.out.println("Removepiece method");
		Iterator<GamePiece> pieceIter = teamPieces.iterator();
		while (pieceIter.hasNext()) {
		  GamePiece piece = pieceIter.next();
		  if (piece.getR()==r && piece.getP()==p) {
		    pieceIter.remove();
		    return piece;
		    //System.out.println("Removed piece");
		    //break;
		  }
		}
		return null; //was not in the list
	}
	
	/**
	 * Returns the piece in pieces at index i
	 * @param i index
	 * @return piece at index i
	 */
	public GamePiece getPiece(int i){
		return teamPieces.get(i);
	}
	
	/**
	 * Returns the number of pieces this team has on the board
	 * @return Number of pieces currently on the board
	 */
	public int getNumPieces(){
		return teamPieces.size();
	}
	
	/**
	 * Updates a piece to a new position
	 * @param old the old position of the piece
	 * @param update the new position of the piece
	 */
	public void updatePiece(GamePiece old, GamePiece update){
		GamePiece removed = removePiece(old.getR(),old.getP());
		if (removed == null){
			//debugging stuff
			//System.err.println("ERROR: Piece was not removed from the list in updatePiece()");
			//System.out.println("-------New size of team list: "+getTeamPieces().size());
		} else {
			teamPieces.add(update);
			if (teamPieces.size()>9){
				System.err.println("ERROR: Number of team pieces exceeds 9");
			}
		}

	}

	/**
	 * Used for debugging. Prints out the game board and what each team know's about it's own pieces.
	 * It prints to System.out and be accessed through the debug menu.
	 */
	public void printPieces() {
		System.out.println("Team "+teamSymbol+" pieces --------------");
		for (GamePiece piece : teamPieces){
			System.out.println(piece);
		}
	}
}