/**
 * This class searches for the best available move that the computer can pick.
 * @author Michael Perez, Sean Wright, Melissa Neibaur
 */
package com.cs471.ninemanmill;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.cs471.ninemanmill.uipieces.GamePieceButton;


/**
 * The AI class contains nearly all AI related methods, covering the three different types of actions that can be performed in this game, Placing, Moving, and Destroying.
 * The AI uses a MinMax algorithm with Alpha Beta pruning to speed up searches. Each difficulty searches to an additional depth, starting at 1 for moderate and 3 for extreme (noted as 'Impossible' in the code).
 * It also uses 'short circuit' checks before a normal search is conducted to see if there are any obvious good moves to make. Each instance of the AI object is entirely agnostic to what player it is, which introduces
   a lot of if statements/ternary statements. 
 * The AI uses a SwingWorker subclass to run its tasks in the background, off the UI thread. If the move is ready to submit before the AI_DELAY time is exceeded, the AI will wait until that time has passed before it submits its move,
   so the human can view the action that was just performed. Some moves, like destroy and fly (a type of move), use an animation, and the end of the AI action is finished in the GamePieceButton class in the uipieces package.
 * @author Sean Wright
 * @author Michael Perez
 *
 */
public class AI {
	public static final int AI_RANDOM = 1;
	public static final int AI_MEDIUM = 2;
	public static final int AI_HARD = 3;
	public static final int AI_IMPOSSIBLE = 4;
	private final long AI_DELAY = 1500; //AI delay for moving in ms
	private char playerID; //Identifies AI as player 1/player 2
	private GameBoard gamestate; //this is used by the callback, it must be updated every time the AI expects to do something
	private Team myTeam; //this is used by the callback, it must be updated every time the AI expects to do something
	private Team enemyTeam; //this is used by the callback, it must be updated every time the AI expects to do something
	public int difficulty;
	protected AIWorkerThread workerThread;
	Random r;
	UINew parent;
	
	/**
	 * Constructor method for AI. The playerID, difficulty, and the UINew is passed in.
	 * @param playerID the ID of the player
	 * @param difficulty The difficulty of the AI. It changes the depth of the alpha-beta search and some other algorithms
	 * @param parent The UINew object that is displaying the board
	 */
	public AI(char playerID, int difficulty, UINew parent){
		this.playerID = playerID; //ID of this player
		this.difficulty = difficulty;
		this.parent = parent;
		r = new Random();
	}
	
	/**
	 * This method is called when a turn has been performed. The AI thread is started, and the correct action is
	 * taken depending on the current game mode, i.e. setupMode, or move mode.
	 * @param gamestate The current gamestate
	 */
	protected void performTurn(GameBoard gamestate){
		//Update gamestate and team objects
		this.gamestate = gamestate;
		if (playerID == GameBoard.PLAYER1) {
			// Player 1
			myTeam = gamestate.getTeam1();
			enemyTeam = gamestate.getTeam2();
		} else {
			//Player 2
			myTeam = gamestate.getTeam2();
			enemyTeam = gamestate.getTeam1();
		}
		
		//Start the AI thread. It will callback to this class the appropriate action with the decision it made and will return control to the player when it has finished
		workerThread = new AIWorkerThread(playerID, (gamestate.setupMode) ? AIWorkerThread.OPERATION_PLACE : AIWorkerThread.OPERATION_MOVE, difficulty, gamestate);
		workerThread.execute(); //run the AI move finder in the background. 
	}

	
	/**
	 * Callback method for the SwingWorker that does the AI move search.
	 * This is called when the AI is ready to submit its move.
	 */
	protected void performMoveCallback(){
		//we got our move from the swingworker. 
		//Let's post our move to the UI
		if (parent == null){
			return;
		}
		if (!parent.isVisible()){
			return;
		}
		if (parent.gamestate.gameFinished){
			return; //game is over
		}
		
		AIBundle bundle = null;
		try {
			bundle = workerThread.get();
		} catch (InterruptedException e1 ){
		} 
		catch( ExecutionException e1) {
			e1.printStackTrace();
		}
		Move nextMove = bundle.getMove();
		
		if (!gamestate.allowFlying() || myTeam.getNumPieces() > 3){
			try {
				boolean mill = gamestate.movePiece(nextMove);
				GamePieceButton source = parent.pieces[nextMove.Rs][nextMove.Ps];
				GamePieceButton destination = parent.pieces[nextMove.Rd][nextMove.Pd];
				parent.pieceSelected = source; //source piece for moving against
				parent.repaint();
				destination.REPRESENTING = source.REPRESENTING;
				destination.setMode(GamePieceButton.NORMAL_MODE);
				source.setMode(GamePieceButton.HIDDEN_MODE);
				parent.updateMovedPieces();
				if (mill){
					//Make the AI find the piece to remove
					parent.repaint(); //repaint so the user sees what happens
					NineManMill.sfxplayer.playSound(WavePlayer.SOUND_MILL);
					workerThread = new AIWorkerThread(playerID, AIWorkerThread.OPERATION_DESTROY, difficulty, gamestate);
					workerThread.execute(); //run the AI destroyer in the background.
					parent.setDirections("Player "+parent.gamestate.CURRENT_TURN+" is selecting a piece to destroy...");
					//Don't finish turn. AI is not done yet. Destroy will finish the turn in it's callback
				} else {
					parent.completeTurn(); //finish turn if not a mill
				}
			}	catch (PiecePlacementException e)	{ 
				System.err.println("["+getDifficultyString()+" "+playerID+"] AI did an invalid move"); 	
			}
		} else {
			//fly mode
			GamePieceButton destination = parent.pieces[nextMove.Rd][nextMove.Pd];
			parent.pieces[nextMove.Rs][nextMove.Ps].setMode(GamePieceButton.FLY_FROM_MODE);
			parent.pieces[nextMove.Rs][nextMove.Ps].repaint(); //repaint that piece
			destination.setSourceFlyButton(parent.pieces[nextMove.Rs][nextMove.Ps]);
			//System.out.println(nextMove);
			destination.setAIFlyMove(nextMove);
			destination.setAI(this);
			destination.REPRESENTING = myTeam.getSymbol(); //this is okay as the original position will be automatically cleared.
			destination.setBlinkMode(GamePieceButton.FLY_TO_MODE);
		}
	}
	
	/**
	 * This method places a piece on the board, and checks if there is a mill. If a mill is detected from the move, a new AI thread
	 * is created and assigned to destroy an enemy's piece.
	 */
	protected void performPlaceCallback(){
		if (parent == null){
			return;
		}
		if (!parent.isVisible()){
			return;
		}
		if (parent.gamestate.gameFinished){
			return; //game is over
		}
		AIBundle bundle = null;
		try {
			bundle = workerThread.get();
		} catch (InterruptedException e){
			
		} catch( ExecutionException e) {
			e.printStackTrace();
		}
		GamePiece placePoint = bundle.getPlace();
		//System.out.println("Placepoint: "+placePoint);
		GamePieceButton piece = parent.pieces[placePoint.getR()][placePoint.getP()];
		boolean mill = false;
		try {
			mill = parent.gamestate.placePiece(placePoint.getR(), placePoint.getP());
			piece.setMode(GamePieceButton.NORMAL_MODE);
			piece.REPRESENTING = (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER1 : GameBoard.PLAYER2;
			if (mill){
				NineManMill.sfxplayer.playSound(WavePlayer.SOUND_MILL);
				//Make the AI find the piece to remove
				parent.repaint(); //repaint so the user sees what happens
				parent.setDirections("Player "+parent.gamestate.CURRENT_TURN+" is selecting a piece to destroy...");
				workerThread = new AIWorkerThread(playerID, AIWorkerThread.OPERATION_DESTROY, difficulty, gamestate);
				workerThread.execute(); //run the AI destroyer in the background.
				//Don't finish turn. AI is not done yet. Destroy will finish the turn in it's callback
			} else {
				parent.completeTurn();
			}
			
		} catch (PiecePlacementException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is called when the AI needs to destroy a piece from the board.
	 * The destroyed piece is added to the array of broken pieces. If the human team's number of pieces is less than
	 * 3, the game is considered over.
	 */
	protected void performDestroyCallback(){
		if (parent == null){
			return;
		}
		if (!parent.isVisible()){
			return;
		}
		if (parent.gamestate.gameFinished){
			return; //game is over
		}
		AIBundle bundle = null;
		try {
			bundle = workerThread.get();
		} catch (InterruptedException e) {
			
		} catch( ExecutionException e) {
			e.printStackTrace();
		}
		GamePiece destroyPoint = bundle.getDestroy();
		GamePieceButton piece = parent.pieces[destroyPoint.getR()][destroyPoint.getP()];
		piece.setBlinkMode(GamePieceButton.BREAKING_MODE); //this should finish the turn for us, as it will destroy the piece, and turn over control.
	}
	
	/** protected GamePiece removePlayerPiece(Team team)
	 * 
	 * This method takes in the opponent's team object, randomly selects a
	 * piece from it, and returns it so it can be removed in the GameBoard class.
	 * @param team the opponent's team
	 * @return randomly selected GamePiece from the other player
	 */
	protected GamePiece removePlayerPiece(Team opponent){
		return opponent.getPiece(r.nextInt());
	}

	/**
	 * The AIWorkerThread class extends the SwingWorker class and runs a task in the background, off the UI thread.
	 * It publishes a UI bundle to the UI thread through the done() method, and will turn over control to the UINew class when it has finished its turn.
	 * 
	 * This thread is used for making the AI generate a move, placement, or destruction move.
	 * @author Michael Perez
	 * @author Sean Wright
	 *
	 */
	public class AIWorkerThread extends SwingWorker<AIBundle,Void>{
		public final static int OPERATION_MOVE = 0;
		public final static int OPERATION_PLACE = 1;
		public final static int OPERATION_DESTROY = 2;
		private int operation;
		private int difficulty;
		private GameBoard gamestate;
		private Team myteam;
		private AIBundle bundle; //bundle to pass around this class
		
		/**
		 * Makes a new WorkerThread object. Calling .execute() on this object will execute it. It calls a method on the dispatch thread through the ai parameter.
		 * @param ai reference to the AI class that made this. It lives on the event-dispatch thread and it's where our callback is
		 * @param playerID ID to identify what moves should be considered
		 * @param operation operation to do (move, place, destroy)
		 * @param difficulty int that should be switched on to make smarter results be returned
		 * @param gamestate gameboard so the AI can check what can happen in this context
		 */
		public AIWorkerThread(char playerID, int operation, int difficulty, GameBoard gamestate){
			this.operation = operation; //operation. This is passed to the bundle returned and calls diff methods in the worker part
			this.difficulty = difficulty; //difficulty of AI. This changes what switch branches are executed
			this.gamestate = gamestate; //State of the game we should base any action on
			
			//We might want to find a way to not have to do this again. Since it's done by the AI and this thread... perhaps make it take parameters
			myteam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam1() : gamestate.getTeam2());
		}
		
		
		/**
		 * This runs on a different thread than the UI. This is the intensive part of the AI selecting am ove that would block the UI.
		 * It runs a search to find the next best move, placement, or destruction.
		 */
		@Override
		protected AIBundle doInBackground() throws Exception {
			long startTime = System.currentTimeMillis(); //use this instead of a timer thread to know when to stop.
			bundle = new AIBundle(operation);
			switch(operation){
				//Switching on what we want returned.
			case OPERATION_MOVE:
				bundle = getNextMove();
				break;
			case OPERATION_PLACE:
				bundle = getNextPlace();
				break;
			case OPERATION_DESTROY:
				bundle = getNextDestroy();
				break;
			}
			long endTime = System.currentTimeMillis();
			if (endTime - startTime < AI_DELAY && operation != OPERATION_DESTROY){
				System.out.println("["+getDifficultyString()+" "+playerID+"] AI is sleeping for another "+(AI_DELAY-(endTime-startTime))+"ms");
				Thread.sleep(AI_DELAY-(endTime-startTime));
			} else {
				System.out.println("["+getDifficultyString()+" "+playerID+"] AI took "+(endTime - startTime)+"ms to choose a move");
			}
			return bundle;
		}

		/**
		 * This method is called when doInBackground() is finished. It runs on the UI's event dispatch thread, so we can safely callback to the UI and make changes without worrying about concurrency.
		 * This method performs the callback to actually perform the Move, Place, or Destroy operation that this thread was spun up for.
		 */
		@Override
		protected void done(){
			//When the doInBackground() finishes, this method is called on the UI thread. So we can't put sleep here.
			System.gc(); //free memory we used just now
			switch (operation){
			case OPERATION_MOVE:
				performMoveCallback();
				break;
			case OPERATION_PLACE:
				performPlaceCallback();
				break;
			case OPERATION_DESTROY:
				performDestroyCallback();
				break;
			}
		}
		/**
		 * Gets the next move and stuffs it into the bundle we're going to return to the game
		 * @param bundle bundle to stuff the move into
		 * @return the updated bundle since we don't have pointers in java
		 */
		private AIBundle getNextMove() {
			//MOVE OPERATION
			//Get the move we want.
			switch(difficulty){
			case AI.AI_RANDOM:
				//random
				ArrayList<Move> allMoves = gamestate.getAllMoves(myteam);
				int n = r.nextInt(allMoves.size());
				bundle.setMove(allMoves.get(n));
				break;
			//other AI levels
			default:
				if (myTeam.getNumPieces() > 3) {
					bundle.setMove(alphaBetaNextMove(difficulty+1));
				} else {
					bundle.setMove(alphaBetaNextMove(difficulty)); //there will be many more moves, don't bother searching for them.
				}
				break;
			}
			return bundle;
		}
		
		/**
		 * Alpha-beta search for where the AI should move. The difficulty level determines the depth of the search.
		 * This search works by assuming the AI is player 2, which makes the min and max methods much more simple.
		 * @param depth The depth based on the AI difficulty level
		 * @return bestMove The move that the search algorithm deems the best.
		 */
		private Move alphaBetaNextMove(int depth){
			//CLONE IMMEDIATELY - when we get a list of moves, it includes teams. It must not be the original teams or it will mess everything up.
			Team cloneTeam = new Team(myTeam); //clone our team so we don't modify it
			Team cloneEnemyTeam = new Team(enemyTeam); //clone our team so we don't modify it
			ArrayList<Move> allMyMoves = gamestate.getAllMoves(cloneTeam);
			ArrayList<Move> enemyMillMoves = gamestate.getAllMoves(cloneEnemyTeam);
			if(allMyMoves.size() <= 0) return null; //shortcircuit if there are no moves
			
			//Before we do a thorough search, we should see if there are any immediate mills we can make.
			for (Move move : allMyMoves){
				GameBoard nextState = new GameBoard(gamestate);
				try {
					boolean mill = nextState.movePiece(move);
					//see if making this move will force the enemy to have no moves
					ArrayList<Move> nextEnemyMoves = nextState.getAllMoves((playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1())); //get list of opponent moves
					if (nextEnemyMoves.size() == 0 ){
						//we win with this move
						System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Winning via forcing other side to have no moves");
						move.team = myTeam;
						return move;
					}
					
					if (mill){
						System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Moving to immediate mill");
						move.team = myTeam;
						return move;
					}
				} catch (PiecePlacementException e) {
					System.err.println("["+getDifficultyString()+" "+playerID+"] AI attempted an invalid move, this shouldn't happen");
					e.printStackTrace();
				}
			}
			
			//If this isn't possible, we should attempt to block any mills the opponent can make.
			//Before we do a thorough search, we should see if there are any immediate mills we can make.
			for (Move move : enemyMillMoves){
				GameBoard nextState = new GameBoard(gamestate);
				try {
					if (nextState.movePiece(move)){
						System.out.println("["+getDifficultyString()+" "+playerID+"] AI sees the enemy can make a mill in it's next turn at "+move.Rd+","+move.Pd+" if it is not blocked");
						for (Move blockAttempt : allMyMoves){
							if (blockAttempt.sameDestination(move)){
								System.out.println("["+getDifficultyString()+" "+playerID+"] AI can block the mill, blocking mill.");
								blockAttempt.team = myTeam;
								return blockAttempt;
							}
						}
						System.out.println("["+getDifficultyString()+" "+playerID+"] AI is unable to block opposing mill, doing normal search");
					}
				} catch (PiecePlacementException e) {
					System.err.println("["+getDifficultyString()+" "+playerID+"] AI attempted an invalid move, this shouldn't happen");
					e.printStackTrace();
				}
			}
			
			
			//Impossible only: Check if there is a wiggle move available.
			if (difficulty == AI_IMPOSSIBLE){
				ArrayList<GamePiece> millCenters = gamestate.getAllMillCenters(myteam); //list of all mill centers
				for (GamePiece wiggleCenter : millCenters){
					GamePiece wigglePiece = safeToWiggle(gamestate, wiggleCenter, enemyTeam, myTeam);
					if (wigglePiece != null){
						System.out.println("Found a wigglemill piece");
						//There's a piece that should have a move that will perform a wiggle mill operation. Let's find it.
						for (Move wiggleMove : allMyMoves){
							if (wiggleMove.getSourcePosition().equals(wigglePiece)){
								System.out.println("["+getDifficultyString()+" "+playerID+"] AI is performing a wigglemove");
								wiggleMove.team = myTeam;
								return wiggleMove; //this is our wigglemill move
							}
						}
					}
				}
			}			
			
			Move bestMove = null;
			int val, bestval=Integer.MIN_VALUE;
			
			GameBoard abState;
			for(int i=0;i<depth;i++){
				//System.out.println("Searching at depth: "+i);
				for(int j=0;j<allMyMoves.size();j++){
					abState = new GameBoard(gamestate); //reclone board so its fresh
					try {
						abState.movePiece(allMyMoves.get(j));
					} catch (PiecePlacementException e) {/*Should not happen*/}
					
					if(abState.detectNineManMill(cloneTeam, allMyMoves.get(j).Rd, allMyMoves.get(j).Rs)){
						alphaBetaDestroy(abState, (playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1()));
					}
					
					val=minValMove(abState,-100000,100000,depth);
					if(val>bestval){
						bestMove = (allMyMoves.get(j));
						bestval = val;
					}
					else if(val==bestval) {
						if(r.nextInt()%2==0) {
							bestMove = (allMyMoves.get(j));
						}
					}
				}
			}
			//our move points to a team that is not the actual one on the board. 
			//We must reconstruct the best move, but use the correct team now, otherwise it won't update the right items on the board.
			bestMove.team = myTeam;
			return bestMove;
		}
		
		/**
		 * MinVal of the Alpha-Beta search. Taken from the opponent player's perspective
		 * @param gamestate Theoretical gamestate to score and get next states of
		 * @param alpha Best possible score in this subtree
		 * @param beta Worst score possible in this subtree
		 * @param depth Depth left to search to
		 * @return worst subscore this subtree can provide.
		 */
		private int minValMove(GameBoard gamestate, int alpha, int beta, int depth){
			Team myTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam1() : gamestate.getTeam2());
			Team enemyTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1());
			if(enemyTeam.getNumPieces() <=2) return Integer.MAX_VALUE;
			else if(myTeam.getNumPieces() <=2) return Integer.MIN_VALUE;
			
			ArrayList<Move> allMoves = gamestate.getAllMoves(enemyTeam);
			if(depth==0) return evalBoardMove(gamestate, myTeam, enemyTeam);
			int val=100000;
			for(int i=0; i<allMoves.size(); i++){
				GameBoard newBoard = new GameBoard(gamestate);
				try {
					newBoard.movePiece(allMoves.get(i));
				} catch (PiecePlacementException e) {/*Should not happen*/}
				
				if(newBoard.detectNineManMill(playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1(), allMoves.get(i).Rd, allMoves.get(i).Rs)) {
					alphaBetaDestroy(newBoard, playerID == GameBoard.PLAYER1 ? gamestate.getTeam1() : gamestate.getTeam2()); //destroy a piece on the board
				}
				
				val = maxValMove(newBoard, alpha, beta, depth-1);
				if(val < beta) beta = val;
				if(alpha >= beta) {
					allMoves = null; //memory cleanup
					return beta;
				}
				newBoard = null;
			}
			return beta;
		}
		
		/**
		 * MaxVal of the Alpha-Beta search.
		 * @param gamestate Theoretical gamestate to score and get next states of
		 * @param alpha Best possible score in this subtree
		 * @param beta Worst score possible in this subtree
		 * @param depth Depth left to search to
		 * @return best subscore this subtree can provide.
		 */
		private int maxValMove(GameBoard gamestate, int alpha, int beta, int depth){
			Team myTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam1() : gamestate.getTeam2());
			Team enemyTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1());
			
			if(enemyTeam.getNumPieces() <=2) return Integer.MAX_VALUE;
			else if(myTeam.getNumPieces() <=2) return Integer.MIN_VALUE;
			ArrayList<Move> allMoves = gamestate.getAllMoves(myTeam);
			if(depth<=0) return evalBoardMove(gamestate, enemyTeam, myTeam);
			int val=Integer.MIN_VALUE;
			for(int i=0; i<allMoves.size(); i++){
				GameBoard newBoard = new GameBoard(gamestate);
				try {
					newBoard.movePiece(allMoves.get(i));
				} catch (PiecePlacementException e) {/*Should not happen*/}
				
				if(newBoard.detectNineManMill(playerID == GameBoard.PLAYER1 ? gamestate.getTeam1() : gamestate.getTeam2(), allMoves.get(i).Rd, allMoves.get(i).Rs)) {
					alphaBetaDestroy(newBoard, playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1());
				}
				val = minValMove(newBoard, alpha, beta, depth-1);
				if(val > alpha) alpha = val;
				if(alpha >= beta) return alpha;
			}
			return alpha;
		}
		
		/**
		 * Gets the coordinates of where the AI wishes to place a piece on the board.
		 * @return AIBundle with operation set to PLACE and coordinate that can be obtained via getPlace()
		 */
		private AIBundle getNextPlace(){
			//PLACEMENT OPERATION
			//Get the placement we want.
			switch(difficulty){
			case AI.AI_RANDOM:
				ArrayList<GamePiece> emptyPlaces = gamestate.getAllEmpty();
				int n = r.nextInt(emptyPlaces.size());
				bundle.setPlace(emptyPlaces.get(n));
				break;
			default:
				bundle.setPlace(alphaBetaPlace(difficulty+1));
				break;
			}
			return bundle;
		}
		
		/**
		 * Finds the optimal position to place a piece on the board during setup.
		 * This algorithm first checks if the AI can make any mills, and if it can, it takes them.
		 * If the enemy is about to make a mill, the AI blocks them, but only on Hard and Extreme difficulties.
		 * @param depth depth to search for the best place to place
		 * @return gamepiece where a piece will be placed
		 */
		private GamePiece alphaBetaPlace(int depth) {
			GamePiece bestPlace = null;
			int val, bestval=Integer.MIN_VALUE;
			ArrayList<GamePiece> empty = gamestate.getAllEmpty();
			//Before we do a thorough search, we should see if there are any immediate mills we can make.
			//If so, choose that one. Alpha beta destroy should find the optimal piece to destroy.
			for (GamePiece piece : empty){
				if (gamestate.detectNineManMill(myTeam, piece.getR(), piece.getP())){
					//there is a mill within immediate reach - make that move.
					System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Placing mill");
					return piece;
				}
			}
			System.out.println("["+getDifficultyString()+" "+playerID+"] AI did not see any formable mills");
			if (difficulty >= AI_HARD){
			for (GamePiece piece : empty){
				if (gamestate.detectNineManMill(enemyTeam, piece.getR(), piece.getP())){
					//there is a mill within immediate reach - make that move.
					
						System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Blocking enemy mill");
						return piece;
					}
				}
				System.out.println("["+getDifficultyString()+" "+playerID+"] AI did not see any blockable mills.");
			}
			
			if (difficulty >= AI_HARD){
				for (GamePiece piece : empty){
					if (doubleMillDetection(gamestate, piece, myTeam)){
						//there is a mill within immediate reach - make that move.
						if (difficulty >= AI_HARD){
							System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Setting up double mill");
							return piece;
						}
					}
					if (doubleMillDetection(gamestate, piece, enemyTeam)){
						//there is a mill within immediate reach - make that move.
						System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Blocking enemy double mill");
						if (difficulty >= AI_HARD){
							return piece;
						}
					}
				}
				System.out.println("["+getDifficultyString()+" "+playerID+"] AI did not see any double mills to block/grab.");
			}
			
			//try to get the middle sections if you can (33% chance of selecting one)
			for (int m = 1; m<7; m+=2){
				if (gamestate.checkIfEmptyPlace(1, m)){
					if (r.nextInt(3) == 0){
						System.out.println("["+getDifficultyString()+" "+playerID+"] Capturing centerpiece");
						return new GamePiece(1,m);
					}
				}
			}
			
			System.out.println("["+getDifficultyString()+" "+playerID+"] AI did not attempt to capture centerpiece (or none were available)");
			
			
			//do a normal search to a depth.
				for(int j=0;j<empty.size();j++){
					GameBoard newBoard = new GameBoard(gamestate);
					if (newBoard.getTeam1().getPiecesRemainingToPlace() <= 0 && newBoard.getTeam2().getPiecesRemainingToPlace() <= 0) {
						System.out.println("Exceeding number of pieces to place");
						continue;
					}
					
					GamePiece bc = empty.get(j);
					try {
						newBoard.placePiece(bc.getR(), bc.getP());
					} catch (PiecePlacementException e) {/*Should not happen*/}
					
					if(newBoard.detectNineManMill((newBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? newBoard.getTeam1() : newBoard.getTeam2(), bc.getR(), bc.getP())) {
						alphaBetaDestroy(newBoard, (newBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? newBoard.getTeam2() : newBoard.getTeam1());
					}
					
					//change turns.
					newBoard.CURRENT_TURN = (newBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER2_TURN : GameBoard.PLAYER1_TURN;
					val=minValPlace(newBoard,Integer.MIN_VALUE,Integer.MAX_VALUE,depth);
					
					if(val>bestval){
						bestPlace = bc;
						bestval = val;
					}
					else if(val==bestval) {
						if(r.nextInt()%2==0) { 
							bestPlace = bc;
						}
					}
				}
			return bestPlace;
		}
		
		/**
		 * MinVal of the Alpha-Beta search for placing. This method is evaluated from the perspective of the opponent
		 * @param gamestate Gamestate to evaluate
		 * @param alpha Best score the opponent team can get
		 * @param beta Worst score I can get in this subtree
		 * @param depth depth left to search.
		 * @return Best board rating you can get if you get to this state, including future states.
		 */
		private int minValPlace(GameBoard gamestate, int alpha, int beta, int depth){
			Team myTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam1() : gamestate.getTeam2());
			Team enemyTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1());
			
			if(depth==0 || enemyTeam.getPiecesRemainingToPlace()<=0) return evalBoardPlace(gamestate, myTeam, enemyTeam);
			int val=Integer.MAX_VALUE;
			
			ArrayList<GamePiece> empty = gamestate.getAllEmpty();
			for(GamePiece e : empty){
				boolean mill = false;
				GameBoard newBoard = new GameBoard(gamestate);
				try {
					mill = newBoard.placePiece(e.getR(), e.getP());
				} catch (PiecePlacementException ex) {/*Should not happen*/}
				
				if(mill) {
					alphaBetaDestroy(newBoard, playerID == GameBoard.PLAYER1 ? newBoard.getTeam1() : newBoard.getTeam2());
				}
				
				newBoard.CURRENT_TURN = (newBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER2_TURN : GameBoard.PLAYER1_TURN;
				
				val = maxValPlace(newBoard, alpha, beta, depth-1);
				if(val < beta) beta = val;
				if(alpha >= beta) return beta;
			}
			return beta;
		}
		
		/**
		 * MaxVal of the Alpha-Beta search. This is evaluated from the perspective of this player
		 * @param gamestate gamestate to evaluate
		 * @param alpha Best score my team can get
		 * @param beta Worst score the opponent can get in this subtree
		 * @param depth depth left to search.
		 * @return Best score I can get given the opponent makes all the best moves in this tree
		 */
		private int maxValPlace(GameBoard gamestate, int alpha, int beta, int depth){
			Team myTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam1() : gamestate.getTeam2());
			Team enemyTeam = (playerID == GameBoard.PLAYER1 ? gamestate.getTeam2() : gamestate.getTeam1());
			if(depth==0 || myTeam.getPiecesRemainingToPlace()<=0) return evalBoardPlace(gamestate, myTeam, enemyTeam);
			int val=100000;
			
			ArrayList<GamePiece> empty = gamestate.getAllEmpty();
			for(GamePiece e : empty){
				//For every piece from this state : 
				GameBoard newBoard = new GameBoard(gamestate);
				try {
					if (newBoard.placePiece(e.getR(), e.getP())){
						alphaBetaDestroy(newBoard, playerID == GameBoard.PLAYER1 ? newBoard.getTeam2() : newBoard.getTeam1());
					}
				} catch (PiecePlacementException ex) {/*Should not happen*/ System.err.println("Piece placement exception in MaxValPlace.");}
				
				newBoard.CURRENT_TURN = (newBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER2_TURN : GameBoard.PLAYER1_TURN;
				
				val = minValPlace(newBoard, alpha, beta, depth-1);
				if(val > alpha) alpha = val;
				if(alpha >= beta) return alpha;
			}
			return alpha;
		}
		
		/**
		 * Rates the board during placement. Returns a score.
		 * This heuristic values the first team's mills, while subtracting value for each mill the opponent has.
		 * @param board Board to evaluate
		 * @param t1 Team 1 (not necessarily player 1)
		 * @param t2 Team 2 (not necessarily player 2)
		 * @return val The value that the heuristic determines this board should be scored as.
		 */
		private int evalBoardPlace(GameBoard board, Team t1, Team t2){
			//favor making a mill 
			int val = 0;
			//this turns team
			for(GamePiece p : t1.teamPieces){
				if (!p.isInMill()){
					if(board.detectNineManMill(t1, p.getR(), p.getP())) {
						p.setInMill(true);
						val += 20;
					}
				}
				if (p.getP() % 2 == 1){
					val+=2;
				} else {
					val++; //1 for corner piece
				}
			}
			
			//enemy team
			for(GamePiece p : t2.teamPieces){
				if (!p.isInMill()){
					if(board.detectNineManMill(t2, p.getR(), p.getP())) {
						p.setInMill(true);
						val -= 15;
					}
				}
				if (p.getP() % 2 == 1){
					val-=2;
				} else {
					val--; //1 for corner piece
				}
			}
			
			return val;
		}
		
		/**
		 * Board eval for making moves. If the first team cannot move, -1000000 points. If the other team cannot move, +7.
		 * Lastly, each piece is worth +/-(1.5) points.
		 * @param board board to get a score for.
		 * @param t1 Current team we are getting points for
		 * @param t2 Team we are attempting to suppress
		 * @return Score of this board
		 */
		private int evalBoardMove(GameBoard board, Team t1, Team t2){
			int val = 0;
			if(board.getAllMoves(t1).size()<=0){
				val -= 1000000;
			}
			else if(board.getAllMoves(t2).size()<=0){
				val += 10;
			}
			
			//favor cramping the player if it's harder so it can't do as much as it wants
			if (difficulty >= AI_HARD){
				val += board.getAllMoves(t1).size();
				val -= board.getAllMoves(t2).size();
			}
			
			val += (int) (t1.getNumPieces()*1.5); //lower the favoring of pieces
			val -= (int) (t2.getNumPieces()*1.5); //lower the amount of pieces vs other stuff
			//System.out.println("Value of board is "+val);
			return val;
		}
		
		
		
		
		/**
		 * Returns a bundle with the coordinates where a destruction of a piece should occur have been set
		 * @return AIBundle with destroy set
		 */
		private AIBundle getNextDestroy() {
			switch(difficulty){
			case AI.AI_RANDOM:
				//random
				ArrayList<GamePiece> enemyPieces = enemyTeam.teamPieces;
				int n = r.nextInt(enemyPieces.size());
				GamePiece destroyPiece = enemyPieces.get(n); //get the piece to destroy
				bundle.setDestroy(new GamePiece(destroyPiece.getR(),destroyPiece.getP()));
				break;
			default:
				bundle.setDestroy(alphaBetaDestroy(gamestate, enemyTeam));
				break;
			}
			return bundle;
		}
		
		/**
		 * The destroy algorithm for coupling with alpha beta.
		 * First, the algorithm checks if the player can win by forcing the enemy to have no moves.
		 * Then, if the AI difficulty is Extreme, it will call impossibleAlmostMill() to find any high priority targets.
		 * Otherwise, it will run the almostMill() method.
		 * If there are no high priority targets, then a random piece is selected to be destroyed.
		 * @param board Gameboard to perform a destruction on
		 * @param other Other team based on this board, with a list of pieces that can be potentially destroyed.
		 * @return piece to destroy
		 */
		private GamePiece alphaBetaDestroy(GameBoard board, Team other){
			GamePiece destroy = null; //piece to destroy
			Team cloneOther = new Team(other);
			ArrayList<GamePiece> canBeDestroyed = board.getAllDestroyable(cloneOther);

			for(GamePiece p : canBeDestroyed){
				if (!board.setupMode) { //move phase
					GameBoard destroyedBoard = new GameBoard(board);
					Team simOther = (destroyedBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? destroyedBoard.getTeam2() : destroyedBoard.getTeam1();
					//first check if we can win by making the enemy have no moves
					destroyedBoard.removePiece(simOther, p.getR(), p.getP());
					if (destroyedBoard.getAllMoves(simOther).size() <= 0){
						//destroying this piece will make them have no moves, making the ai win
						return p; //kill this one
					} 
				}
				
				
				boolean highPriorityTarget; //search for pieces that are almost in a mill, and axe one of them if you find one.
				if (difficulty == AI_IMPOSSIBLE) {
					highPriorityTarget = impossibleAlmostMill(board, p, cloneOther); //better detection (checks for blocks)
				} else {
					highPriorityTarget = almostMill(board,p,cloneOther); //normal detection (doesn't check for blocked mills)
				}
				if(highPriorityTarget){
					if(destroy == null) {
						destroy = p;
					}
					else if(r.nextInt(3)==0) {
						//50% chance we'll change if we find another one that is almost in a mill.
						destroy = p;
					}
				}
				
				/*if (isBlockingMill(board, p)){
					System.out.println("Destroy enemy blocking mill: "+p);
					destroy = p;
				} */
				
			}
			//None are next to each other, destroy a random piece
			if(destroy == null) {
				destroy = canBeDestroyed.get(r.nextInt(canBeDestroyed.size()));
			}
			return destroy;
		}
		
		/**
		 * THIS METHOD IS CURRENTLY NOT USED DUE TO DESYNCHRONIZATION ISSUES.
		 * IT MAY BE FIXED IN THE FUTURE.
		 * This method is used to identify if a piece at position p is blocking the formation of a mill. It is used when the AI is determining what piece to destroy.
		 * @param board Gameboard to evaluate if piece p is blocking a mill
		 * @param p Piece position on the board to check if its blocking a mill
		 * @return true if this piece is blocking a mill formation, false otherwise.
		 */
		private boolean isBlockingMill(GameBoard board, GamePiece p) {
			
			//we need to see if this is even 'millable', assuming this piece was destroyed and replaced with a piece we have.
			if (!board.detectNineManMill((board.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? board.getTeam1() : board.getTeam2(), p.getR(), p.getP())){
				//we didn't use the board we clone below (thisTeam) as it would tie the variable to the original teams and modify them.
				return false; //no direct mill potential if this piece is destroyed
			}
			GameBoard simulationBoard = new GameBoard(board); //clone so we don't modify our real board
			Team thisTeam = (simulationBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? simulationBoard.getTeam1() : simulationBoard.getTeam2();
			Team otherTeam = (simulationBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? simulationBoard.getTeam2() : simulationBoard.getTeam1();

			simulationBoard.removePiece(otherTeam, p.getR(), p.getP()); //simulate killing it

			ArrayList<Move> possibleMillMoves = simulationBoard.getAllMoves(thisTeam);
			for (Move millmove : possibleMillMoves){
				if (millmove.Rd == p.getR() && millmove.Pd == p.getP()){
					//simulate a move here first
					GameBoard moveBoard = new GameBoard(simulationBoard);
					try {
						Team simTeam = (moveBoard.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? moveBoard.getTeam1() : moveBoard.getTeam2(); //still the current turns' team - ignoring enemy movement/placements, is it possible to move here when my turn happens again?
						millmove.team = simTeam; //this prevents desyncs front=to=backend
						moveBoard.movePiece(millmove);
						if (moveBoard.detectNineManMill(simTeam, p.getR(), p.getP())){
							//mill will occur here
							return true;
						}
					} catch (PiecePlacementException e) {
						System.err.println("Simulating millmove when destroying a blocking piece in isBlockingMill(): "+e.getMessage());
					}
				}
			}
			
			return false;
		}


		/**
		 * Detects if a piece is almost in a mill, assuming it is not already in one. This function is not used with impossible.
		 * @param board Board to check pieces on
		 * @param p Position on the board to check if this is almost ready to be in a mill.
		 * @param other Team who we are checking for pieces to destroy if they are almost in a mill.
		 * @return true if the piece is almost in a mill
		 */
		private boolean almostMill(GameBoard board, GamePiece p, Team other){
			int pNext = (p.getP()+1)%8; //next spot on the ring
			int pPrev = p.getP()-1; //p minus one mod eight
			if (pPrev < 0) {
				pPrev+=8;
			}
			
			//--MILL DETECTION---------------------------------------
			if(p.getP()%2 == 0){ //corner piece
				if(board.checkForTeamPiece(other, p.getR(),pNext) || board.checkForTeamPiece(other, p.getR(),pPrev)){
					return true; 
				}
			}
			else{ //center piece
				int rNext = (p.getR()+1)%3;
				int rPrev = p.getR()-1;
				if (rPrev<0){
					rPrev+=3; //rollover
				}
				
				if(rPrev <= 0) rPrev = 2;
				if(board.checkForTeamPiece(other, p.getR(),pNext) || board.checkForTeamPiece(other, p.getR(),pPrev)
						|| board.checkForTeamPiece(other, rNext, p.getP()) || board.checkForTeamPiece(other, rPrev, p.getP())){
					return true; 
				}
			}
			return false;
		}
		
		/**
		 * Detects pieces that are almost in a mill, but does a better detection. This is for impossible where it will validate that the mill isn't already blocked.
		 * @param board Board to check pieces on
		 * @param p Position on the board to check if this is almost ready to be in a mill.
		 * @param other Team who we are checking for pieces to destroy if they are almost in a mill.
		 * @return true if the piece is almost in a mill, false otherwise
		 */
		private boolean impossibleAlmostMill(GameBoard board, GamePiece p, Team other){
			int pNext = (p.getP()+1) % 8; //next spot on the ring

			int pPrev = p.getP()-1; //p minus one mod eight
			if (pPrev < 0) {
				pPrev+=8;
			}
			
			//--MILL DETECTION---------------------------------------
			if(p.getP() % 2 == 0) { //corner piece
				//if(pPrev <= 0) pPrev = 7;
				if(board.checkForTeamPiece(other, p.getR(),pNext)) {
					//There is an adjacent piece that is a piece that belongs to the enemy. It is almost in a mill.
					//However, it might be blocked already by our piece, so ignore it being almost in a mill.
					if (board.checkForTeamPiece(other, p.getR(),pPrev)){
						return true; //it can get a mill if a piece moves here. Kill it
					}
				}
			}
			else{ //center piece
				int rNext = (p.getR()+1) % 3;
				int rPrev = p.getR()-1;
				if (rPrev<0){
					rPrev+=3; //rollover
				}
				
				if(board.checkForTeamPiece(other, p.getR(),pNext)) { //piece adjacent to this square in the next position
					if (board.checkForTeamPiece(other, p.getR(),pPrev)) {
						//there is a piece on both sides of this square, this code might be redundant
						return true;
					}
				}
				if (board.checkForTeamPiece(other, rNext, p.getP())) {
					if (board.checkForTeamPiece(other, rPrev, p.getP())){
						return true;
					}
				}
			}
			//no mill can be formed directly here
			return false;
		}
		
		/**
		 * This method checks to see if the other player can make a mill in the next turn.
		 * @param gameboard The current game board
		 * @param enemy The enemy player's team
		 * @return true if the enemy player can make a mill the next turn, false otherwise
		 */
		private boolean enemyMillNextTurn(GameBoard gameboard, Team enemy){
			Team cloneTeam = new Team(enemy);
			ArrayList<Move> enemyMillMoves = gameboard.getAllMoves(cloneTeam);
			
			//If this isn't possible, we should attempt to block any mills the opponent can make.
			//Before we do a thorough search, we should see if there are any immediate mills we can make.
			for (Move move : enemyMillMoves){
				GameBoard nextState = new GameBoard(gamestate);
				try {
					if (nextState.movePiece(move)){
						return true;
					}
				} catch (PiecePlacementException e) {
					System.err.println("["+getDifficultyString()+" "+playerID+"] AI attempted an invalid move in enemyMillNextTurn(), this shouldn't happen");
					e.printStackTrace();
				}
			}
			return false; //enemy can not make a mill in the next turn
		}
		
		/**
		 * This method is used by impossible/extreme difficulty. It checks to see if there is a 'wiggle mill' which is a mill that can have one piece move out then
		 * immediately back in in order to quickly grind the enemy down. 
		 * In order for a wiggle mill to be safe, there must be certain conditions that are met:
		 * 
		 * The enemy cannot be able to make a mill in the next turn.
		 * There cannot be any enemies adjacent to the piece that would move out of the wiggle mill, as it might be able to be blocked if it were to move.
		 * There must be an adjacent spot for the wiggle piece to wiggle to.
		 * 
		 * @param gameboard Current game board.
		 * @param enemy enemy team
		 * @param myTeam this player's team
		 * @return null if its not safe to wiggle, gamepiece position of a wiggle piece otherwise.
		 */
		private GamePiece safeToWiggle(GameBoard gameboard, GamePiece wiggleCenter, Team enemy, Team myTeam){
			//Before we do a thorough search, we should see if there are any immediate mills we can make.
			//If so, choose that one. Alpha beta destroy should find the optimal piece to destroy.
			System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Checking if it is safe to wiggle.");
			if (enemyMillNextTurn(gameboard,enemy)){
				System.out.println("Enemy has mill next turn: abort wigglemill.");
				return null; //don't wiggle - still, it might be best move based on normal search.
			}
			
			if (!gameboard.detectNineManMill(myTeam, wiggleCenter.getR(), wiggleCenter.getP())){
				System.err.println("ERROR: safe to wiggle was called on a piece that isn't in a mill.");
				return null; //this isn't even in a mill!
			}
			
			ArrayList<GamePiece> millPieces = new ArrayList<GamePiece>(3); //holds gamepieces, preallocates space for 3 pieces of pieces that are in a mill.
			millPieces.add(wiggleCenter);
			
			//Get adjacent allies.
			int pNext = (wiggleCenter.getP()+1)%8; //next spot on the ring
			int pPrev = wiggleCenter.getP()-1; //previous spot on the ring
			if (pPrev < 0) {
				pPrev+=8;
			}
	
			if(wiggleCenter.getR() % 2 == 0) {
				//it's on ring 0 or 2, not the center, this is the center we're talking about here of the current mill.
				millPieces.add(new GamePiece(wiggleCenter.getR(),pPrev));
				millPieces.add(new GamePiece(wiggleCenter.getR(),pNext));
				//we now have our mill pieces
			} else {
				// It's on ring 1. We have to do additional checking.
				if (gameboard.checkForTeamPiece(myTeam, wiggleCenter.getR(), pPrev) && gameboard.checkForTeamPiece(myTeam, wiggleCenter.getR(), pNext)){
					millPieces.add(new GamePiece(wiggleCenter.getR(),pPrev));
					millPieces.add(new GamePiece(wiggleCenter.getR(),pNext));
				} else {
					//it's across the rings
					millPieces.add(new GamePiece(0,wiggleCenter.getP()));
					millPieces.add(new GamePiece(2,wiggleCenter.getP()));
				}
			}
			
			Team emptyTeam = new Team();
			emptyTeam.setTeamSymbol(GameBoard.EMPTY);
			//Check for adjacent enemy pieces.
			for (GamePiece ally : millPieces){
				boolean adjacentEmpty = false; //must be true or we can't move
				int allypNext = (ally.getP()+1)%8; //next spot on the ring
				int allypPrev = ally.getP()-1; //previous spot on the ring
				if (allypPrev < 0) {
					allypPrev+=8;
				}
				
				if (ally.getR() % 2 == 1){
					//Center ring
					if(gamestate.checkForTeamPiece(enemy, 0, ally.getP()) || gamestate.checkForTeamPiece(enemy, 2, ally.getP())){
						// enemy piece on another ring in the center. REALLY NOT SAFE.
						System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Can't perform wigglemove with middle piece ["+ally.getR()+","+ally.getP()+"]: Adjacent human piece on adjacent ring will block wigglemill plans");
						continue;
					}
					if(gamestate.checkForTeamPiece(emptyTeam, 0, ally.getP()) || gamestate.checkForTeamPiece(emptyTeam, 2, ally.getP())){
						adjacentEmpty = true;
					}
				} 
				//side ring check for human pieces
				if(gamestate.checkForTeamPiece(enemy, ally.getR(), allypPrev) || gamestate.checkForTeamPiece(enemy, ally.getR(), allypNext)){
					// enemy piece is adjacent in the ring. Probably not a good idea to move.
					System.out.println("["+getDifficultyString()+" "+playerID+"] AI - Can't perform wigglemove with corner piece ["+ally.getR()+","+ally.getP()+"]: Adjacent human piece on adjacent ring will block wigglemill plans");
					continue;
				}
				
				//the mill center is on the outer ring. WE should see if the middle ring is open for the center to move as the prev/next pieces of the ends of the mill are guaranteed to be taken.
				if(ally.getP() % 2 == 1  && gamestate.checkForTeamPiece(emptyTeam, 1, ally.getP())){
					adjacentEmpty = true;
				}
			
				//free spot check
				if(gamestate.checkForTeamPiece(emptyTeam, ally.getR(), allypPrev) || gamestate.checkForTeamPiece(emptyTeam, ally.getR(), allypNext)){
					adjacentEmpty = true; 
				}
				
				//Return if this piece can't move
				if (adjacentEmpty == false){
					System.out.println("["+getDifficultyString()+" "+playerID+"] AI - No nearby adjacent for wigglemill piece at "+ally.getR()+","+ally.getP());
					continue;
				}
				
				//There is an empty space adjacent to this piece that it can move to.
				//There are no adjacent enemy pieces to this piece that might be able to block it from moving back.
				//There are no mills the enemy can make in this turn that can destroy this mill if the mill is temporarily deformed on the next turn.
				//Therefore, this piece is safe to wiggle.
				
				return ally;
			}
			return null; //we found no moves that are wiggle safe, so I guess this mill can't wiggle.
		}
		
		/**
		 * Attempts to see if there is a double mill available if a piece was to be placed at position doubleMillPosition. If it makes a double mill spot, it should be picked as it can block/setup a double mill.
		 * @param Board
		 * @param doubleMillPosition A gamepiece that holds a position. This should have the teamColor variable set so this method knows what team to check for against a mill.
		 * @param doubleMillTeam team to check for a mill.
		 * @return true if a double mill was detected, false otherwise
		 */
		private boolean doubleMillDetection(GameBoard gamestate, GamePiece doubleMillPosition, Team doubleMillTeam){
			if (doubleMillPosition.getP() % 2 == 1) { return false;} //double mill detection in this method does not consider the 4 middle square double mills.
			
			Team emptyTeam = new Team();
			emptyTeam.setTeamSymbol(GameBoard.EMPTY); //empty square checking
			
			//assumes the square doubleMillPosition is already empty
			//check side pieces
			int pNext = (doubleMillPosition.getP()+1)%8; //next spot on the ring
			int pPrev = doubleMillPosition.getP()-1; //p minus one mod eight
			if (pPrev < 0) {
				pPrev+=8;
			}
			
			if (gamestate.checkForTeamPiece(doubleMillTeam, doubleMillPosition.getR(), pNext) && gamestate.checkForTeamPiece(doubleMillTeam, doubleMillPosition.getR(), pPrev)){
				//There's a piece both before and after this one. We need to check one square farther to make sure they are also empty.
				pNext = (pNext+1)%8; //next spot on the ring
				pPrev = pPrev-1; //p minus one mod eight
				if (pPrev < 0) {
					pPrev+=8;
				}
				if (gamestate.checkForTeamPiece(emptyTeam, doubleMillPosition.getR(), pNext) && gamestate.checkForTeamPiece(emptyTeam, doubleMillPosition.getR(), pPrev)) {
					return true; //this is a double mill spot
				}
			}
			
			return false; //no double mill found
		}
	}

	/**
	 * This method is called when a mill due to flying has been formed. It is called by a GamePieceButton. This is because that class controls the timer for blinking and flying a piece.
	 */
	public void flyMillFormed() {
		System.out.println("A flying mill was formed");
		workerThread = new AIWorkerThread(playerID, AIWorkerThread.OPERATION_DESTROY, difficulty, gamestate);
		workerThread.execute(); //run the AI destroyer in the background.
		parent.setDirections("Player "+parent.gamestate.CURRENT_TURN+" is selecting a piece to destroy...");
		//ai will automatically destroy and end the turn
	}

	
	/**
	 * Converts a difficulty level into a human readable string.
	 * @return Human readable difficulty level string
	 */
	public String getDifficultyString() {
		switch (difficulty){
		case 0:
			return "Human";
		case 1:
			return "Random";
		case 2:
			return "Moderate";
		case 3:
			return "Hard";
		case 4:
			return "Extreme";
		default:
			return "Unknown difficulty";
		}
	}
}