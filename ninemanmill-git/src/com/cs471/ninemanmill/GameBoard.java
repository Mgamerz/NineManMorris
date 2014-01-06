package com.cs471.ninemanmill;

import java.util.ArrayList;

import com.cs471.ninemanmill.uipieces.GamePieceButton;

/**
 * The gameboard class is the backend structure for Nine Men's Morris. It contains all properties of a 'board', or a state of gameplay.
 * The board is represented by a 2D array of characters, with the first index being the Ring (R), and the second one being the Position (P). 
 * The rings are indexed from outer to inner, and the positions are indexed with 0 as the top left, then incrementing clockwise, with 7 being the left center piece.
 * 
 * Each index in this array is filled with 1 of 3 indicators: PLAYER1, PLAYER2, or EMPTY, which are defined as character constants. These show what is currently at that square in this gameboard object.
 * This array is private and is only modified via methods. It is critical that this array be correct at all times, or undefined behavior may occur.
 * 
 * Each GameBoard instance has references to two teams, player 1's team and player 2's team. These can be dynamically changed for each instance to make different, fully unique boards that can be modified independently of others.
 * 
 * @author Michael Perez
 * @author Sasa Rkman
 * @author Melissa Neibaur
 * @author Sean Wright
 * 
 */
public class GameBoard {

	/* Variables to symbolize teams and empty spaces */
	public final static char PLAYER1 = '1';
	public final static char PLAYER2 = '2';
	public final static char EMPTY = 'E';
	public final static int PLAYER1_TURN = 1;
	public final static int PLAYER2_TURN = 2;
	public boolean gameFinished = false;
	public int CURRENT_TURN;
	public boolean setupMode;
	private boolean allowFlyMode; //enabled by default
	private UINew parent;
	private char[][] board;
	Team team1;
	Team team2;
	
	

	/**
	 * Constructs a new, empty gameboard that has nothing on it. This is the default constructor, and is used when setting up a new game.
	 * @param attachedUI The UI object this board should attach itself to, allowing it to update the UI when changes this board is made.
	 * @param allowFlyMode boolean to indicate if this board should allow flying mode when a team is down to three pieces.
	 */
	public GameBoard(UINew attachedUI, boolean allowFlyMode) {
		this.parent = attachedUI;
		this.allowFlyMode = allowFlyMode;
		this.setupMode = true;
		team1 = new Team(PLAYER1, 9);
		team2 = new Team(PLAYER2, 9);
		/* Normal Constructor */
		board = new char[3][8];
		/* Initialize an empty board */
		for (int r = 0; r <= 2; r++) {
			for (int p = 0; p <= 7; p++) {
				//System.out.println("Initializing empty board: [" + r + "][" + p
//						+ "]");
				board[r][p] = EMPTY;
			}
		}
		CURRENT_TURN = 1; //Player 1's turn to start.
	}
	
	/**
	 * Copy constructor for the AI to use. Makes a deep copy of everything so the AI can perform its search without affecting the main game board.
	 * @param oldState The old state of the board to be cloned
	 */
	public GameBoard(GameBoard oldState){
		board = new char[3][8];
		for(int i=0; i<=2; i++){
			for(int j=0; j<=7; j++){
				board[i][j] = oldState.board[i][j];
			}
		}
		team1 = new Team(oldState.team1);
		team2 = new Team(oldState.team2);
		CURRENT_TURN = oldState.CURRENT_TURN;
		this.allowFlyMode = oldState.allowFlyMode;
		this.setupMode = oldState.setupMode;
	}

	/**
	 * Creates a new gameboard based on an existing setup of pieces.
	 * @param initialState String representation of the board using capital letters. It should be 24 characters long.
	 * @throws Exception If the board provided is not a valid board.
	 */
	public GameBoard(String initialState) throws Exception {
		if (!NineManMill.DEBUG_VERSION) {
			throw new Exception(
					"This is not a debugging version, this code should not be executing!");
		}

		if (initialState.length() != 24) {
			throw new Exception("Initial input state is not the correct length");
		}

		/* Creates a gameboard with a predetermined state - used for testing */
		board = new char[3][8];

		/* Parse the initial state string */
		int position = 0;
		for (int r = 0; r <= 2; r++) {
			for (int p = 0; p <= 7; p++) {
				useTeamPiece(initialState.charAt(position));
				board[r][p] = initialState.charAt(position);
				////System.out.println("Initializing existing board, setting to ["r + "][" + p + "]" + initialState.charAt(position));
				position++; //rotate around the ring
			}
		}
	}

	/** 
	 * Gets a reference to team1.
	 * @return Team1
	 */
	public Team getTeam1() {
		return team1;
	}

	/**
	 * Gets a reference to team2.
	 * @return Team2
	 */
	public Team getTeam2() {
		return team2;
	}
	
	public boolean allowFlying(){
		return allowFlyMode;
	}

	/**
	 * Used for building a debugging board. This is used to properly decrement the number of pieces a team has as it builds the board.
	 * @param teamSymbol The symbol that represents the team. This is how the team is looked up so values are not hard-coded.
	 * @throws Exception If an error occurs when attempting to set a piece, or this is not a debugging version of the program.
	 */
	private void useTeamPiece(char teamSymbol) throws Exception{
		if (!NineManMill.DEBUG_VERSION){
			throw new Exception ("This is not a debug version, cannot call useTeamPiece()!");
		}
		
		switch (teamSymbol){
		case EMPTY:
			return;
		case PLAYER1:
			/* Find some way to get the coordinates */
//			team1.usePiece(new GamePiece());
			return;
		case PLAYER2:
			/* Find some way to get the coordinates */
//			team2.usePiece(new GamePiece());
			return;
		default:
			throw new PiecePlacementException("Building debug board failed, encountered unknown teamsymbol");
		}
	}
	
	/**
	 * Returns whether the passed team player has a mill on the board.
	 * This does not check if the piece at R,P actually exists, only the pieces on the sides.
	 * 
	 * @param team
	 *            The character representation of a team
	 * @param R
	 *            Ring the last piece moved to on this turn
	 * @param P
	 *            Position the last piece moved to on this turn
	 * @return true if a ninemanmill is on the board using the newest moved
	 *         piece, false otherwise. The calling method should take into
	 *         account whose turn was just played
	 */
	public boolean detectNineManMill(Team team, int R, int P) {
		/*
		 * board[R,P] 
		 * R is the ring the we are looking at (0 outermost,1,2 innermost)
		 * P is the number starting from the top left, going clockwise 0 1 2 3 4 5 6 7
		 */
		//System.out.println("Checking for team "+team.getSymbol()+"mill at "+R+","+P);
		
		//----GET P-1, P-2, R-1 mod their sizes; must do rollunder checking cause of java.
		int pMinusOneModEight = (P - 1) % 8;
		if (pMinusOneModEight < 0)
		{
		    pMinusOneModEight += 8;
		}
		int pMinusTwoModEight = (P - 2) % 8;
		if (pMinusTwoModEight < 0)
		{
		    pMinusTwoModEight += 8;
		}
		int rMinusOneModThree = (R - 1) % 3;
		if (rMinusOneModThree < 0)
		{
		    rMinusOneModThree += 3;
		}
		
		// ---------------------------CORNER PIECE-----------------------------------------

		if (P % 2 == 0) {
			/* Check +1 +2 and -1 -2 [all mod 8] since this is a corner square */
			/* I'm sure there's a better way to do this */
			if (checkForTeamPiece(team, R, ((P + 1) % 8))) {
				if (checkForTeamPiece(team, R, ((P + 2) % 8))) {
					//System.out.println("Mill on P+1 P+2");
					return true;
				}
			}
			if (checkForTeamPiece(team, R, pMinusOneModEight)) { /* P-1 */
				if (checkForTeamPiece(team, R, pMinusTwoModEight)) { /* P-2 */
					//System.out.println("Mill on P-1 P-2");
					return true;
				}
			}
			return false;
		} else {
			// -----------------------------MIDSECTION PIECE-----------------------------------------
			/* Check up/down, left/right */
			if (checkForTeamPiece(team, R, ((P + 1) % 8))) { /* Square After */
				if (checkForTeamPiece(team, R, pMinusOneModEight)) { /* Square before in the ring*/
					//System.out.println("Mill on midsection and corners");
					return true;
				}
			}
			if (checkForTeamPiece(team, rMinusOneModThree, P)) { /* Ring Left if possible */
				if (checkForTeamPiece(team, ((R + 1) % 3), P)) { /* Ring Right if possible */
					//System.out.println("Mill on rings");
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Checks a space on the board and returns true if the space there is occupied by the specified team's piece.
	 * @param team.getSymbol() The team's piece to check for.
	 * @param R The ring that should be checked.
	 * @param P The position on that ring that should be checked
	 * @return True if the space at R,P 
	 */
	protected boolean checkForTeamPiece(Team team, int R, int P) {
		return board[R][P] == team.getSymbol();
	}
	
	/**
	 * Checks if there is not a piece at position R,P.
	 * @param R Ring to check
	 * @param P position on the ring to check
	 * @return True if empty, false otherwise
	 */
	public boolean checkIfEmptyPlace(int R, int P) {
		return (board[R][P] == EMPTY);
	}

	/**
	 * Places a piece on the board.
	 * 
	 * @param R The ring level that the piece is on.
	 * @param P The place in the ring the piece is on.
	 * @throws PiecePlacementException
	 */
	public boolean placePiece(int R, int P)	throws PiecePlacementException {
		if (board[R][P] != EMPTY) {
			//System.err.println("Error: Piece is taken at "+R+","+P+" by "+board[R][P]);
			throw new PiecePlacementException("Board space is not empty in placePiece() in GameBoard.java");
		}
		Team team = (CURRENT_TURN == 1) ? getTeam1() : getTeam2(); //get the team to place the piece as
		if (team.getPiecesRemainingToPlace()<=0){
			throw new PiecePlacementException("Team "+team.getSymbol()+" has no more pieces to place, yet tried to place anyway.");
		}
		
		team.usePiece(R,P);
		board[R][P] = team.getSymbol();
		return detectNineManMill(team, R, P);
	}
	
	/**
	 * Returns a list of possible moves for a given team.
	 * @param team Team to get moves for 
	 * @return Array of moves
	 */
	public ArrayList<Move> getAllMoves(Team team){
		ArrayList<Move> possibleMoves = new ArrayList<Move>();
		if (!allowFlyMode || team.teamPieces.size() > 3) {
		Move m1, m2, m3, m4;

		for(int r=0; r<=2;r++)
			for(int p=0; p<=7;p++){
				int pMinusOneModEight = (p - 1) % 8;
				if (pMinusOneModEight < 0)
				{
				    pMinusOneModEight += 8;
				}
				int rMinusOneModEight = (r - 1) % 8;
				if (rMinusOneModEight < 0)
				{
				    rMinusOneModEight += 8;
				}
				if(board[r][p]==team.getSymbol()){
					// We found one of our team pieces 
					// Find adjacent moves 
					if (p % 2 == 0){
						// Corner Piece 
						m1 = new Move(team, r,p,r,(p+1)%8);
						m2 = new Move(team, r,p,r,pMinusOneModEight);
						if(board[r][(p+1)%8]==EMPTY) possibleMoves.add(m1);
						if(board[r][pMinusOneModEight]==EMPTY) possibleMoves.add(m2);
					} 
					else {
					//	 Midsection piece 
						m1 = new Move(team, r,p,r,(p+1)%8);
						m2 = new Move(team, r,p,r,pMinusOneModEight);
						if(board[r][(p+1)%8]==EMPTY) possibleMoves.add(m1);
						if(board[r][pMinusOneModEight]==EMPTY) possibleMoves.add(m2);
						
						switch(r){
						case 0:
							m3 = new Move(team, r,p,(r+1),p);
							if(board[r+1][p]==EMPTY) possibleMoves.add(m3);
							break;
							
						case 1:
							m3 = new Move(team, r,p,(r+1),p);
							m4 = new Move(team, r,p,(r-1),p);
							if(board[r+1][p]==EMPTY) possibleMoves.add(m3);
							if(board[r-1][p]==EMPTY) possibleMoves.add(m4);
							break;
							
						case 2:
							m3 = new Move(team, r,p,(r-1),p);
							if(board[r-1][p]==EMPTY) possibleMoves.add(m3);
							break;
							
						default:
							//System.err.println("Ring error");
							break;
						}
					}
				}
			}
		} else if (team.teamPieces.size() == 3) {
			// fly mode
			// for all pieces, add moves for every piece to every empty position.
			for (GamePiece piece : team.teamPieces) {
				for (int r = 0; r <= 2; r++) {
					for (int p = 0; p <= 7; p++) {
						// for all rings and all positions
						if (board[r][p] == EMPTY) {
							possibleMoves.add(new Move(team, piece.getR(),
									piece.getP(), r, p));
						}
					}
				}
			}
		}
		return possibleMoves;
	}
	
	/**
	 * Removes a piece from the board.
	 * @param team The team whose piece is being removed.
	 * @param R The ring that the piece is on.
	 * @param P The position on the ring that the piece is being removed.
	 */
	public void removePiece(Team team, int R, int P){
		board[R][P]=EMPTY;
		team.removePiece(R,P);
	}
	
	/**
	 * Moves a piece on the board to an adjacent square specified by the direction.
	 * @param team The team of the piece that is moving. 
	 * @param Rs The ring the initial piece is on.
	 * @param Ps The position the initial piece is at in a ring.
	 * @param Rd The destination ring where the piece will be moved to.
	 * @param Pd The destination position in the ring where the piece will be moved to.
	 * @return True if a mill is made, false otherwise.
	 */
	public boolean movePiece(Move move) throws PiecePlacementException {
		if (!(board[move.Rs][move.Ps]==move.team.getSymbol())){
			throw new PiecePlacementException("Attempting to move piece that does not belong to team "+move.team.getSymbol()+"\n Piece belongs to: "+board[move.Rs][move.Ps]);
		}
		
		if (!(board[move.Rd][move.Pd]==EMPTY)){
			//System.out.println(move.Rs+","+move.Ps+" "+board[move.Rs][move.Ps]+" -> "+move.Rd+","+move.Pd+" "+board[move.Rd][move.Pd]);
			throw new PiecePlacementException("Attempting to move piece to space that is not empty");
		}
		move.team.updatePiece(new GamePiece(move.Rs,move.Ps), new GamePiece(move.Rd, move.Pd)); //update the team's known pieces and locations
		board[move.Rd][move.Pd]=board[move.Rs][move.Ps];
		board[move.Rs][move.Ps]=EMPTY;
		
		
		return detectNineManMill(move.team, move.Rd, move.Pd);
	}
	
	/**
	 * Gets the current board state in a text form.
	 * 
	 * @return Returns a string representation of this board that can be used to
	 *         print out information on the screen.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		/*
		 * Don't question this code, only accept that it works
		 */
		int i, rl;
		String spacing = "      ";//Length: 8 +1 for null termination.
		for (rl = 0; rl <= 2; rl++) {
			sb.append(spacing.substring(0, rl * 3));
			for (i = 0; i <= 2; i++) {
				sb.append(bracketEncapsulator(board[rl][i]));
				sb.append(spacing.substring(0, spacing.length() - (3 * rl)));
			}
			sb.append("\n");
		}

		/* Middle area */
		for (rl = 0; rl <= 2; rl++) {
			sb.append(bracketEncapsulator(board[rl][7]));
			//	sb.append(spacing.substring(0, spacing.length()-(3*rl)));
		}
		sb.append(spacing.substring(0, 3));
		//sb.append("\t");
		for (rl = 2; rl >= 0; rl--) {
			sb.append(bracketEncapsulator(board[rl][3]));
			//sb.append(spacing.substring(0, spacing.length()-(3*rl)));
		}
		/* End middle area */
		sb.append("\n");
		for (rl = 2; rl >= 0; rl--) {
			sb.append(spacing.substring(0, rl * 3));
			for (i = 6; i >= 4; i--) {
				sb.append(bracketEncapsulator(board[rl][i]));
				sb.append(spacing.substring(0, spacing.length() - (3 * rl)));
			}
			sb.append("\n");
		}

		sb.append("\n");
		if (allowFlying()) {
			sb.append("Flying is enabled\n");
		} else {
			sb.append("Flying is disabled\n");
		}
		
		if (setupMode){
			sb.append("Currently in setupMode");
		} else {
			sb.append("SetupMode has ended\n");
		}
		
		return sb.toString();
	}

	/**
	 * Encapsulates a string in [ and ], for use in the toString method
	 * 
	 * @param contents
	 *            Character to put in brackets
	 * @return Enccapsulated item between brackets
	 */
	private String bracketEncapsulator(char content) {
		StringBuilder sb = new StringBuilder();
		/* Bracket encapsulator for different types of spots*/
		String openBracket;
		String closeBracket;
		switch (content){
		case PLAYER2:
			openBracket = "{";
			closeBracket = "}";
			break;
		case PLAYER1:
			openBracket = "|";
			closeBracket = "|";
			break;
		case EMPTY:
			openBracket = "(";
			closeBracket = ")";
			break;
		default:
			openBracket = "[";
			closeBracket = "]";
			
		}
		sb.append(openBracket);
		sb.append(content);
		sb.append(closeBracket);
		return sb.toString();
	}

	/**
	 * Marks this turn as completed
	 */
	public void completeTurn() {
		// Check that we aren't in setup mode anymore
		if (setupMode){
			if (team1.getPiecesRemainingToPlace() == 0 && team2.getPiecesRemainingToPlace()==0){
				//we should remove all non representing buttons, move them to hidden mode.
				if (parent != null){
					for (int r = 0; r<=2; r++){
						//Clearing empty pieces on each ring.
						for (GamePieceButton piece : parent.pieces[r]){ //iterate on each ring
							if (piece.representsPiece() == EMPTY){
								piece.setMode(GamePieceButton.HIDDEN_MODE);
							}
						}
						parent.repaint(); //redraw the screen. this makes the pieces actually disappear
						setupMode = false; //avoid redoing this loop
					}
				}
			} else {
				//still setup mode, pieces left to place. Set pieces to the correct modes based on what's playing on what team.
				//These are based on the turn that is ending - not the turn that is about to start. This is the final stage of the current turn before it is changed.
				if ((CURRENT_TURN == PLAYER1_TURN && parent.p2ai != null)||(CURRENT_TURN == PLAYER2_TURN && parent.p1ai != null)){
					//it's about to become an AI player's turn. Turn off human interaction.
					for (int r = 0; r<=2; r++){
						for (GamePieceButton piece : parent.pieces[r]){ //iterate on each ring
							if (piece.representsPiece() == EMPTY){
								piece.setMode(GamePieceButton.AI_PLACE_MODE);
							} else {
								//there's a piece here
								piece.setMode(GamePieceButton.NORMAL_MODE); //set them to normal modes
							}
						}
					}
				} else {
					//it's about to change to a human players turn
					for (int r = 0; r<=2; r++){
						//"Clearing empty pieces on ring r
						for (GamePieceButton piece : parent.pieces[r]){ //iterate on each ring
							if (piece.representsPiece() == EMPTY) {
								piece.setMode(GamePieceButton.PLACE_MODE);
							} else {
								//there's a piece here
								piece.setMode(GamePieceButton.NORMAL_MODE); //set them to normal modes
							}
						}
					}
				}
			}
		}
		//end setup mode checking.
		
		int prevTurn = CURRENT_TURN; //used for checking if the other team has won (after the turn has changed)
		CURRENT_TURN = (CURRENT_TURN == PLAYER1_TURN) ? PLAYER2_TURN : PLAYER1_TURN;	// switch turns
		if (setupMode == false){
			//int nextTurn = (CURRENT_TURN == PLAYER1_TURN) ? PLAYER2_TURN : PLAYER1_TURN;
			//We have now switched turns, pieces should change modes for each side.
			
			//We will now check to see if the new team has a move. If they don't, they will lose.
			Team nextTurnTeam = (CURRENT_TURN == PLAYER1_TURN) ? getTeam1() : getTeam2();
			if (getAllMoves(nextTurnTeam).size() == 0){
				//This team loses
				
				//force all pieces to normal mode to stop interaction with the board
				for (int r = 0; r<=2; r++){
					for (GamePieceButton piece : parent.pieces[r]){ //iterate on each ring
						if (piece.representsPiece() == EMPTY) continue; //don't do anything to pieces that are empty
						piece.setMode(GamePieceButton.NORMAL_MODE); //this side can't select now
					}
				}
				parent.setDirections("Player "+((prevTurn == PLAYER1_TURN) ? PLAYER1 : PLAYER2)+" wins!");
				gameFinished = true;
				new WinnerDialog(parent,prevTurn, "Player "+((CURRENT_TURN == PLAYER1_TURN) ? PLAYER1 : PLAYER2)+" has no moves left");
				return; //kill the turn, don't play anymore.
			}
			
			if (parent != null) {
				// If other player is AI
				if((CURRENT_TURN == PLAYER1_TURN && parent.p1ai != null) || (CURRENT_TURN == PLAYER2_TURN && parent.p2ai != null)) {
					for (int r = 0; r<=2; r++){
						for (GamePieceButton piece : parent.pieces[r]){ //iterate on each ring
							if (piece.representsPiece() == EMPTY) continue; //don't do anything to pieces that are empty
							piece.setMode(GamePieceButton.NORMAL_MODE); //this side can't select now
						}
					}
				} else {
					//human turn
					for (int r = 0; r<=2; r++){
						for (GamePieceButton piece : parent.pieces[r]){ //iterate on each ring
							if (piece.representsPiece() == EMPTY) continue; //don't do anything to pieces that are empty
							if (piece.representsPiece() == ((CURRENT_TURN == PLAYER1_TURN) ? PLAYER1 : PLAYER2)){
								piece.setMode(GamePieceButton.SELECT_MODE); //this side can now select
							} else {
								piece.setMode(GamePieceButton.NORMAL_MODE); //this side can't select now
							}
						}
					}
				}
			}
		}
	} //method close
	
	/** 
	 * Returns a list of all empty places on the board.
	 * @return list of all empty places on the board
	 */
	protected ArrayList<GamePiece> getAllEmpty(){
		ArrayList<GamePiece> emptyPieces = new ArrayList<GamePiece>();
		for (int R = 0; R<=2; R++){
			for (int P = 0; P<=7; P++){
				//For all pieces
				if (checkIfEmptyPlace(R, P)){
					emptyPieces.add(new GamePiece(R,P));
				}
			}
		}
		return emptyPieces;
	}
	
	/**
	 * Starts playing the background music for the board. Checks if the music is already playing, and if it is,
	   it ignores the play request.
	 */
	public void playBackgroundMusic(){
		if (NineManMill.PLAY_MUSIC){
			if (NineManMill.bgplayer!=null && NineManMill.bgplayer.isActive()){
				return; //already playing
			}
			try {
				NineManMill.bgplayer.playBGMusic();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops the background music, if it is possible.
	 */
	protected void stopBackgroundMusic(){
		if (NineManMill.bgplayer!=null){
			NineManMill.bgplayer.stopMusic();
		}
	}

	/**
	 * Gets a list of pieces that can be destroyed. It ignores pieces in a mill, unless all pieces are in a mill.
	 * @param other team to get all pieces that are destroyable
	 * @return list of pieces that can be destroyed.
	 */
	public ArrayList<GamePiece> getAllDestroyable(Team other) {
		// TODO Auto-generated method stub
		//System.out.println("Start getalldestroyable()");
		ArrayList<GamePiece> destroyablePieces = new ArrayList<GamePiece>();
		for (GamePiece piece : other.teamPieces){ //for all pieces
			if (!detectNineManMill(other, piece.getR(), piece.getP())){
				//this piece is not in a mill
				//System.out.println("Piece at "+piece.getR()+","+ piece.getP() +" is not in a mill");
				destroyablePieces.add(piece); //add this piece to the list of pieces that can be destroyed
			}
		}
		
		//Check if there are no pieces that can be destroyed
		if (destroyablePieces.size() == 0){
			return other.teamPieces; //all their pieces are in mills. Mark them all destroyable and just return the list
		}
		//System.out.println("end all destroyable()");
		return destroyablePieces;
	}
	
	/**
	 * Returns a list of pieces that are in the center of a mill.
	 * @param team team to find mills for
	 * @return list of mill centers
	 */
	protected ArrayList<GamePiece> getAllMillCenters(Team team){
		ArrayList<GamePiece> millCenters = new ArrayList<GamePiece>();
		for(int r = 0; r < 3; r++){
			//For every ring...
			for(int p = 1; p < 8; p+=2){
				//System.out.println("Center check: "+r+","+p);
				//for all odd positions - so add two, only get the centers.
				if (checkForTeamPiece(team,r,p) && detectNineManMill(team, r, p)){
					millCenters.add(new GamePiece(r,p));
				}
			}
		}
		return millCenters;
		
	}
} //class close