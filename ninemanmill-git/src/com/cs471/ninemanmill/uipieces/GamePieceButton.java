package com.cs471.ninemanmill.uipieces;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.Timer;

import com.cs471.ninemanmill.AI;
import com.cs471.ninemanmill.GameBoard;
import com.cs471.ninemanmill.GamePiece;
import com.cs471.ninemanmill.Move;
import com.cs471.ninemanmill.NineManMill;
import com.cs471.ninemanmill.PiecePlacementException;
import com.cs471.ninemanmill.Team;
import com.cs471.ninemanmill.UINew;
import com.cs471.ninemanmill.WavePlayer;
import com.cs471.ninemanmill.WinnerDialog;
/**
 * The physical representation of a gamepiece on the interface. It handles click events passed to it via the UI, and changes the front end to indicate what to do for the human player.
 * It also has a few AI related methods that show what the AI is doing as it finishes a turn.
 * @author Mike Perez, Sasa Rkman
 *
 */
public class GamePieceButton extends JButton {
	private static final long serialVersionUID = -8451678841837353648L;
	public static final int DEMO_MODE = -1; //Demo mode. Button's won't respond to input.
	public static final int HIDDEN_MODE = 0; //Piece will not be drawn on screen, as the board techincally has no piece here.
	public static final int PLACE_MODE = 1; //Click to place a piece here -a THIS IS NOT A UNDERLYING ACTUAL PIECE
	public static final int SELECT_MODE = 2; //Click to select this piece for moving
	public static final int HIGHLIGHT_MODE = 3; //This piece has been selected, keep it highlighted
	public static final int DESTROY_MODE = 4; //Click to select this piece for being removed (due to a mill)
	public static final int NORMAL_MODE = 5; //represents a piece but cannot be clicked right now
	public static final int SELECTED_MODE = 6;
	public static final int AI_PLACE_MODE = 7; //ai is placing a piece, look like place mode but no rollover or click handling
	public static final int BREAKING_MODE = 8; //this piece is flashing and about to break cause the AI just selected it for destruction
	public static final int FLY_TO_MODE = 9; //The AI is about to fly to this position, blink it.
	public static final int FLY_FROM_MODE = 10; //the AI is moving this piece to another location
	private static final int BLINK_DELAY = 175; //blink delay in ms
	private static final int BLINK_MAX_COUNT = 8; //number of blinks to do before ending the blink sequence
	//team colors. Currently just test colors
	
	public char REPRESENTING = GameBoard.EMPTY; //by default, no team
	
	private final Color PLAYER_1_COLOR = Color.RED;
	private final Color PLAYER_2_COLOR = Color.BLUE;
	private Color CURRENT_FLASH; //color that is set when flashing in breaking mode
	//private int blinkCount; //current amount of blinks that have occured
	private Timer blinkTimer; //timer used to blink when busting
	
	//AI variables
	private boolean drawFlying = true; //true to draw piece when flying, false to not (used to blink)
	private GamePieceButton sourceFlyButton; //source of flying piece. This piece will set that one to hidden mode and clear it
	private Move AIFlyMove;
	private AI ai;
	
	private UINew parent; //this is used as a callback to the UINew object to perform the correct operation when handline a click, such as an illegal move, or completing the turn
	
	private int CURRENT_MODE = HIDDEN_MODE; //Default to hidden pieces.
	protected GamePiece coordinate;
	
	/**
	 * Constructs a new GamePieceButton.
	 * @param coordinate Coordinate that this piece represents.
	 * @param parent Parents UI object, acting as a communication hub
	 */
	public GamePieceButton(GamePiece coordinate, UINew parent){
		  super();
		  this.coordinate = coordinate;
		  this.parent = parent;
		  setRolloverEnabled(true);
		  setContentAreaFilled(false);
	}

	public char representsPiece(){
		return REPRESENTING;
	}
	
	/**
	 * Overriden paint method that paints this piece. Changes depending on the state the piece is in.
	 * @param g The graphics object to paint on, automatically provided by Swing.
	 */
	@Override
	protected void paintComponent(Graphics g) 
	  {
		//System.out.println("Mode: "+CURRENT_MODE+", representing: "+REPRESENTING);
		switch(CURRENT_MODE){
		case DEMO_MODE:
			g.setColor(new Color(200,100,0));
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case HIDDEN_MODE:
			//Don't draw this piece. At all.
			break;
		case PLACE_MODE:
			//This piece is a clickable space to place a piece.
			if (getModel().isRollover()) {
				g.setColor(Color.GREEN);
			} 
			else {
				g.setColor(new Color(255,146,0));
			}
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case NORMAL_MODE:
			g.setColor((REPRESENTING == GameBoard.PLAYER1) ? PLAYER_1_COLOR : PLAYER_2_COLOR);
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case SELECT_MODE:
			if (getModel().isRollover()){
				g.setColor(Color.YELLOW);
			} else {
				g.setColor((REPRESENTING == GameBoard.PLAYER1) ? PLAYER_1_COLOR : PLAYER_2_COLOR);
			}
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case SELECTED_MODE:
			if (getModel().isRollover()){
				g.setColor(Color.GRAY);
			} else {
				g.setColor(Color.WHITE);
			}
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case HIGHLIGHT_MODE:
			g.setColor(Color.ORANGE);
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case DESTROY_MODE:
			g.setColor(new Color(88,88,88));
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case AI_PLACE_MODE:
			//waiting for AI to place
			g.setColor(new Color(255,146,0));
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case BREAKING_MODE:
			//waiting for AI to place
			g.setColor(CURRENT_FLASH);
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		case FLY_TO_MODE:
			//waiting for AI to place
			if (drawFlying){
				g.setColor((REPRESENTING == GameBoard.PLAYER1) ? PLAYER_1_COLOR : PLAYER_2_COLOR);
				g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			}
			break;
		case FLY_FROM_MODE:
			g.setColor((REPRESENTING == GameBoard.PLAYER1) ? PLAYER_1_COLOR : PLAYER_2_COLOR);
			g.fillOval(0, 0, getSize().width-1,getSize().height-1);
			break;
		//Do other cases here
		}
	}
	
	/**
	 * Paints a border around a button. This method is automatically called by repaint() in Swing.
	 * @param g Graphics object, automatically passed to this method.
	 */
	@Override
	protected void paintBorder(Graphics g) {
		switch (CURRENT_MODE){
		case DEMO_MODE:
			g.setColor(Color.BLACK);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		case HIDDEN_MODE:
			break;
		case PLACE_MODE:
			g.setColor(Color.LIGHT_GRAY);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		case AI_PLACE_MODE:
			g.setColor(Color.BLACK);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		case SELECT_MODE:
			if (parent!=null && parent.pieceSelected == null){
				g.setColor(Color.GRAY);
			} else {
				g.setColor(Color.BLACK);
			}
			g.drawOval(0,0, getSize().width-1, getSize().height-1);
			break;
		case SELECTED_MODE:
			g.setColor(Color.WHITE);
			g.drawOval(0,0, getSize().width-1, getSize().height-1);
			break;
		case NORMAL_MODE:
			g.setColor(Color.BLACK);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		case HIGHLIGHT_MODE:
			g.setColor(Color.WHITE);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		case DESTROY_MODE:
			g.setColor(Color.ORANGE);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		case BREAKING_MODE:
			g.setColor(Color.ORANGE);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		case FLY_TO_MODE:
			if (drawFlying){
				g.setColor(Color.BLACK);
				g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			}
			break;
		case FLY_FROM_MODE:
			g.setColor(Color.YELLOW);
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
			break;
		//other mode borders here
		}
	  }
	
	/**
	 * Checks to see if a shape is contained
	 * at point x and y
	 */
	Shape shape;
	@Override
	  public boolean contains(int x, int y) {
	    if (shape == null || 
	      !shape.getBounds().equals(getBounds())) {
	      shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
	    }
	    return shape.contains(x, y);
	  }

	/**
	 * Sets the mode of this button. Essentially, the state.
	 * @param mode Mode to be switched to. Use the static constants from this class.
	 */
	public void setMode(int mode) {
		//System.out.println("Piece "+coordinate.getR()+","+coordinate.getP()+" setting mode to "+mode);
		CURRENT_MODE = mode;	
	}
	
	/**
	 * Sets this piece to breaking mode. It has additional code attached to it that would not fit well into setMode().
	 * @param newMode The new mode that will be set and executed on a blink timer.
	 */
	public void setBlinkMode(int newMode){
		//System.out.println("Blinking mode set with mode "+newMode);
		//blinkCount = 0; //set count to 0 again.
		setMode(newMode);
		if (blinkTimer == null){
			blinkTimer = new Timer(BLINK_DELAY, new Blinker());
		}
		blinkTimer.start();
	}

	/**
	 * This method is called whenever this piece is clicked on. It is called by the actionlistener set on it in the UINew class.
	 * Depending on the state this piece is in, different actions will occur. This method is complex as it handles nearly all human interaction with the board.
	 * @param originalPiece The original piece if a piece was selected [have to have a reference to the original one so we know which one to remove when moving]. Otherwise pass this as null.
	 */
	public void performClick(GamePieceButton originalPiece) {
		//System.out.println(coordinate.getR()+", "+coordinate.getP()+" Mode: "+CURRENT_MODE+" team: "+REPRESENTING);
		boolean mill = false;
		switch(CURRENT_MODE){
		case DEMO_MODE:
			//Do nothing for this mode.
			break;
		case PLACE_MODE:
			mill = parent.setPiecePlaced(coordinate);
			if (mill){
				NineManMill.sfxplayer.playSound(WavePlayer.SOUND_MILL);
			} else {
				NineManMill.sfxplayer.playSound(WavePlayer.SOUND_CLICK);
			}
			setMode(NORMAL_MODE);
			REPRESENTING = (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER1 : GameBoard.PLAYER2;
			
			if (mill){
				parent.setCursor(UINew.CURSOR_HAMMER,false);	// Mill found so set mouse cursor to hammer (destroy) state
				//set enemy pieces to destroy mode, allowing me to click and destroy one.
				//Additionally set empty pieces to AI_PLACE_MODE, where they look placeable, but really aren't clickable.
				//NineManMill.sfxplayer.playSound(WavePlayer.SOUND_MILL);
				Team other = (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? parent.gamestate.getTeam2() : parent.gamestate.getTeam1();
				ArrayList<GamePieceButton> destroyablePieces = parent.getButtons(parent.gamestate.getAllDestroyable(other));
				for (int i = 0; i<3; i++){
					for(GamePieceButton piece : parent.pieces[i]){
						if (piece.representsPiece() == GameBoard.EMPTY) {
							//Set it to look clickable, but not really. This also hides the border
							piece.setMode(AI_PLACE_MODE);
							continue;
						}
						if (piece.representsPiece() == ((parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER2 : GameBoard.PLAYER1 )){
							//for all rings, for all pieces which are the enemies...
							//boolean destroyable = (destroyablePieces.contains(piece));
							//System.out.println("Piece destroyable at "+piece.coordinate.getR()+","+piece.coordinate.getP()+"? "+destroyable);
							if (destroyablePieces.contains(piece)) {
								piece.setMode(DESTROY_MODE);
							}
							continue; //performance increase to avoid next if statement
						}
						if (piece.representsPiece() == ((parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER1 : GameBoard.PLAYER2 )){
							//team who got mill cannot attempt to continue moving pieces as we are in destroy enemy piece mode
							//piece.setMode(AI_PLACE_MODE);
							piece.setMode(NORMAL_MODE);
							continue;
						}
					}
				}
				parent.setDirectionsText("Player "+parent.gamestate.CURRENT_TURN+", select an enemy piece to destroy.");
			} else {
				parent.setCursor(UINew.CURSOR_HOLDING, true);
				parent.completeTurn(); //no mill. switch turns
			}
			parent.repaint();
			break;
		case SELECT_MODE:
			if (parent!=null){
				if (parent.pieceSelected==null){
					for (GamePiece piece : (REPRESENTING == GameBoard.PLAYER1) ? parent.gamestate.getTeam1().getTeamPieces() : parent.gamestate.getTeam2().getTeamPieces()){
						//set all pieces to normal mode
						if (piece.getR() != coordinate.getR() || piece.getP() != coordinate.getP()){
							parent.pieces[piece.getR()][piece.getP()].setMode(NORMAL_MODE);
						}
					}
					highlightAdjacent(true); //set the neighbors to highlight
					setMode(SELECTED_MODE);
					parent.pieceSelected = this;
					parent.setCursor(UINew.CURSOR_HOLDING,false);
					NineManMill.sfxplayer.playSound(WavePlayer.SOUND_CLICK);
				}
			}
			break;
		case HIGHLIGHT_MODE:
			if (parent != null){
				//we are now performing a move.	
				if (parent.pieceSelected == null){
					System.err.println("ERROR: Attempting to move piece, but parent has no record of what piece is moving");
					break;
				}
				// This piece was clicked on because it was a highlighted piece. THIS IS NOT THE ORIGINAL PIECE that is getting moved,
				// but it is being moved here.
				
				//Access the original piece via parent.pieceSelected
				
				//Tell the underlying board that we are moving a piece
				try {
					mill = parent.gamestate.movePiece(new Move((parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? parent.gamestate.getTeam1() : parent.gamestate.getTeam2(), parent.pieceSelected.coordinate.getR(), parent.pieceSelected.coordinate.getP(), coordinate.getR(), coordinate.getP()));
					if (mill){
						NineManMill.sfxplayer.playSound(WavePlayer.SOUND_MILL);
					} else {
						NineManMill.sfxplayer.playSound(WavePlayer.SOUND_CLICK);
					}
					//if we get to here, the piece was successfully moved in the underlying structure.
					//We can now update the UI to reflect the changes
					
					REPRESENTING = parent.pieceSelected.REPRESENTING; //change what this place on the board is representing, in this case the piece that moved
					parent.pieceSelected.highlightAdjacent(false); //clear the highlights of the old piece
					setMode(NORMAL_MODE); //this new piece is now in normal mode as turns will switch
					parent.updateMovedPieces(); //turn off highlights, remove selection
					//if we got a mill, now we should allow the user to attempt to pick an enemy piece to destroy.
					if (mill){
						parent.setCursor(UINew.CURSOR_HAMMER,false);	// Mill found so set mouse cursor to hammer (destroy) state
						//set enemy pieces to destroy mode, allowing me to click and destroy one.
						Team other = (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? parent.gamestate.getTeam2() : parent.gamestate.getTeam1();
						ArrayList<GamePieceButton> destroyablePieces = parent.getButtons(parent.gamestate.getAllDestroyable(other));
						for (int i = 0; i<3; i++){
							for(GamePieceButton piece : parent.pieces[i]){
								if (piece.representsPiece() == GameBoard.EMPTY) continue; //ignore blank spaces
								if (piece.representsPiece() == ((parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER2 : GameBoard.PLAYER1 )){
									//for all rings, for all pieces which are the enemies...
									if (destroyablePieces.contains(piece)) {
										piece.setMode(DESTROY_MODE);
									}
									continue; //performance increase to avoid next if statement
								}
								if (piece.representsPiece() == ((parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? GameBoard.PLAYER1 : GameBoard.PLAYER2 )){
									//team who got mill cannot attempt to continue moving pieces as we are in destroy enemy piece mode
									piece.setMode(NORMAL_MODE);
									continue;
								}
							}
						}
						parent.setDirectionsText("Player "+parent.gamestate.CURRENT_TURN+", select an enemy piece to destroy.");
					} else {
						parent.setCursor(UINew.CURSOR_HAND, true);
						parent.completeTurn(); //no mill. switch turns
					}
				} catch (PiecePlacementException e) {
					e.printStackTrace();
				}
				parent.repaint(); //repaint the screen
				
				/*
				 * Note: If a mill is formed, the turn does not end for this player. When a piece is clicked in destroy mode,
				 * that case will end the turn. It will also revert all pieces back to normal mode.
				 */
			}
			break;
		case SELECTED_MODE:
			highlightAdjacent(false); //we are deselecting this piece
			
			for (GamePiece piece : (REPRESENTING == GameBoard.PLAYER1) ? parent.gamestate.getTeam1().getTeamPieces() : parent.gamestate.getTeam2().getTeamPieces()){
				//set all pieces to normal mode
				if (piece.getR() != coordinate.getR() || piece.getP() != coordinate.getP()){
					parent.pieces[piece.getR()][piece.getP()].setMode(SELECT_MODE);
				}
			}
			parent.setCursor(UINew.CURSOR_HAND, false);
			setMode(SELECT_MODE);
			parent.pieceSelected = null;
			break;
		case DESTROY_MODE:
			//Clicking this piece will destroy this piece
			Team opponent = (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? parent.gamestate.getTeam2() : parent.gamestate.getTeam1();
			parent.gamestate.removePiece(opponent, coordinate.getR(), coordinate.getP());
			setMode(HIDDEN_MODE);
			parent.drawPanel.addToBroken(new GamePiece(coordinate.getR(),coordinate.getP(), REPRESENTING));
			NineManMill.sfxplayer.playSound(WavePlayer.SOUND_HAMMER);
			parent.setCursor(UINew.CURSOR_HAMMER, true); //keep hammer for a sec - so it syncs with the hand. It will return to the hand after a brief period (true)
			REPRESENTING = GameBoard.EMPTY; //this piece no longer represents anything

			for (int i = 0; i<3; i++){
				for(GamePieceButton piece : parent.pieces[i]){
					if (piece.representsPiece() != GameBoard.EMPTY){
						//for all rings, for all pieces which are the enemies...
						piece.setMode(NORMAL_MODE);
					} else {
						if (parent.gamestate.setupMode){
							piece.setMode(PLACE_MODE);
						}
					}
				}
			}
			//Check if opponent has 2 or less pieces
			if(!parent.gamestate.setupMode && opponent.getNumPieces() < 3){
				//they can no longer win, so they lose.
				//JOptionPane.showMessageDialog(null, "Player "+parent.gamestate.CURRENT_TURN+" wins!");
				//set all pieces to normal mode so they can't be clicked but show as normal
				for (int i = 0; i<3; i++){
					for(GamePieceButton piece : parent.pieces[i]){
						if (piece.representsPiece() != GameBoard.EMPTY){
							//for all rings, for all pieces which are the enemies...
							piece.setMode(NORMAL_MODE);
						}
					}
				}
				parent.setDirectionsText("Player "+parent.gamestate.CURRENT_TURN+" wins the game!");
				parent.repaint();
				parent.gamestate.gameFinished = true;
				new WinnerDialog(parent, parent.gamestate.CURRENT_TURN, "Player "+(parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN ? GameBoard.PLAYER2 : GameBoard.PLAYER1)+" has less than three pieces remaining");
			} else {
				parent.completeTurn(); //finish the turn since a mill was formed
			}
			break;
		}
	}

	/**
	 * Highlights (or dehighlights) adjacent selectable pieces, for showing where you can click to move a piece.
	 * @param highlight True to make them highlighted, false to make them not highlighted.
	 */
	public void highlightAdjacent(boolean highlight) {
		if (!parent.gamestate.allowFlying() || ((REPRESENTING == GameBoard.PLAYER1) ? parent.gamestate.getTeam1() : parent.gamestate.getTeam2()).getTeamPieces().size() > 3){ // if not fly mode
			int R = coordinate.getR();
			int P = coordinate.getP();
			//System.out.println("Highlight test: P -1 mod 8: "+Math.abs(P-1 % 8));
			//This code is ugly here
			if (parent!=null){
				//Each of these statements checks the four pieces to each side - one ring out and in, and one piece before and after.
				//If this piece is on an outside ring, the statements will catch this and not roll around as you cannot select that piece 
				// as it is two squares away.
				if((R+1<=2) && P % 2 == 1 && parent.pieces[R+1][P].representsPiece() == GameBoard.EMPTY){ //represents the same piece as this one represents
					parent.pieces[R+1][P].setMode((highlight == true) ? HIGHLIGHT_MODE : HIDDEN_MODE);
				}
				if((R-1>=0) && P % 2 == 1 && parent.pieces[R-1][P].representsPiece() == GameBoard.EMPTY){ //represents the same piece as this one represents
					parent.pieces[R-1][P].setMode((highlight == true) ? HIGHLIGHT_MODE : HIDDEN_MODE);
				}
				if(parent.pieces[R][(P+1) % 8].representsPiece() == GameBoard.EMPTY){
					parent.pieces[R][(P+1) % 8].setMode((highlight == true) ? HIGHLIGHT_MODE : HIDDEN_MODE);
				}
				int rollUnderP = (P-1) % 8;
				if (rollUnderP < 0 ) rollUnderP +=8; //roll it up to 7
				
				if(parent.pieces[R][rollUnderP].representsPiece() == GameBoard.EMPTY){
					//this code looks diff cause of the negative mod
					parent.pieces[R][rollUnderP].setMode((highlight == true) ? HIGHLIGHT_MODE : HIDDEN_MODE);
				}
			}
		} else {
			//fly mode
			for (int r = 0; r<=2; r++){
				for (GamePieceButton piece : parent.pieces[r]){
					if(piece.representsPiece() == GameBoard.EMPTY){
						//this code looks diff cause of the negative mod
						piece.setMode((highlight == true) ? HIGHLIGHT_MODE : HIDDEN_MODE);
					}
				}
			}
		}
		parent.repaint(); //force redraw on pieces
	}
	
	/**
	 * Prints the location and team of the game piece
	 */
	@Override
	public String toString(){
		return coordinate.getR()+","+coordinate.getP()+" Mode: "+this.CURRENT_MODE+" Team: "+REPRESENTING;
	}
	
	/**
	 * Checks to see if a button is equal to another button
	 */
	@Override
	public boolean equals(Object o){
		if (!(o instanceof GamePieceButton)) {
			//System.err.println("comparing bad object to GPB");
			return false;
		}
		GamePieceButton gpb = (GamePieceButton) o;
		//System.out.println("Comparing: "+gpb.coordinate.getP() +","+ gpb.coordinate.getP()+" and "+ this.coordinate.getR()+","+this.coordinate.getR());
		if(gpb.coordinate.getP() == this.coordinate.getP() && gpb.coordinate.getR() == this.coordinate.getR()) return true;
		else return false;
	}
	
	/**
	 * Gets the source fly button. The blinker class uses this when finalizing a move and updating the front end when the move is finished.
	 * @return Source button that a piece is flying from
	 */
	public GamePieceButton getSourceFlyButton() {
		return sourceFlyButton;
	}

	/**
	 * Sets the source button that the fly move will have. This makes a reference to the source button, while this button is the destination one.
	 * @param sourceFlyButton Source button where a piece is moving from
	 */
	public void setSourceFlyButton(GamePieceButton sourceFlyButton) {
		this.sourceFlyButton = sourceFlyButton;
	}

	/**
	 * Get's the fly move the AI is making. This is used by the Blinker class to know what pieces to blink and finalize the move when enough blinks have occured.
	 * @return AI's fly move
	 */
	public Move getAIFlyMove() {
		return AIFlyMove;
	}

	/**
	 * Sets the AI's fly move that is about to be performed. The AI calls this.
	 * @param aIFlyMove AI's move that's about to be performed.
	 */
	public void setAIFlyMove(Move aIFlyMove) {
		AIFlyMove = aIFlyMove;
	}

	/**
	 * The Blinker innerclass only has an action listener that is called when a timer fires an event to it in the upper class. It makes things blink on the front end.
	 * @author Michael Perez
	 *
	 */
	class Blinker implements ActionListener {
		boolean on = false;
		int blinkCount = 0;

		public void actionPerformed(ActionEvent e) {
			// blink this button color black to normal and back
			switch (CURRENT_MODE) {
			case BREAKING_MODE:
				CURRENT_FLASH = (on ? Color.BLACK
						: (REPRESENTING == GameBoard.PLAYER1) ? PLAYER_1_COLOR
								: PLAYER_2_COLOR); // double ternary - black if
													// its
				// in flash mode, otherwise set
				// to color we are using
				on = !on;
				blinkCount++;
				if (blinkCount > BLINK_MAX_COUNT) {
					// perform destroy
					blinkTimer.stop();
					//Team turnTeam = (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? parent.gamestate.getTeam1() : parent.gamestate.getTeam2();
					Team nextTurnTeam = (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN) ? parent.gamestate.getTeam2() : parent.gamestate.getTeam1();
					GamePiece destroyPoint = new GamePiece(coordinate.getR(),
							coordinate.getP());

					NineManMill.sfxplayer.playSound(WavePlayer.SOUND_HAMMER);
					parent.gamestate.removePiece(nextTurnTeam, destroyPoint.getR(), destroyPoint.getP());
					setMode(GamePieceButton.HIDDEN_MODE);
					destroyPoint.setTeamColor(REPRESENTING);
					parent.drawPanel.addToBroken(destroyPoint);
					REPRESENTING = GameBoard.EMPTY;
					// Check if opponent has 2 or less pieces
					if (!parent.gamestate.setupMode
							&& nextTurnTeam.getNumPieces() < 3) {
						// they can no longer win, so they lose.
						
						// set all pieces to normal mode so they can't be
						// clicked but show as normal mode so the human can review the final game state.
						for (int i = 0; i < 3; i++) {
							for (GamePieceButton finalizePiece : parent.pieces[i]) {
								if (representsPiece() != GameBoard.EMPTY) {
									// for all rings, for all pieces which are
									// the enemies...
									finalizePiece.setMode(GamePieceButton.NORMAL_MODE);
								}
							}
						}
						parent.setDirectionsText("Player "
								+ parent.gamestate.CURRENT_TURN
								+ " wins the game!");
						parent.repaint();
						parent.gamestate.gameFinished = true;
						new WinnerDialog(
								parent,
								parent.gamestate.CURRENT_TURN,
								"Player "
										+ (parent.gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN ? GameBoard.PLAYER2
												: GameBoard.PLAYER1)
										+ " has less than three pieces remaining");
					} else {
						parent.completeTurn();
					}
				}
				// System.out.println("Piece is black: "+on);
				// repaint();
				break;
			case FLY_TO_MODE:
				// This piece is now blinking because the AI is going to fly
				// here
				drawFlying = !drawFlying;
				blinkCount++;
				if (blinkCount > BLINK_MAX_COUNT) {
					blinkTimer.stop(); // stop blinking
					boolean mill;
					try {
						mill = parent.gamestate.movePiece(getAIFlyMove());
						GamePieceButton source = parent.pieces[getAIFlyMove().Rs][getAIFlyMove().Ps];
						GamePieceButton destination = parent.pieces[getAIFlyMove().Rd][getAIFlyMove().Pd];
						parent.pieceSelected = source; // source piece for
														// moving against
						// parent.repaint();
						destination.REPRESENTING = source.REPRESENTING;
						destination.setMode(GamePieceButton.NORMAL_MODE);
						source.setMode(GamePieceButton.HIDDEN_MODE);
						parent.updateMovedPieces();
						if (mill) {
							// Make the AI find the piece to remove
							NineManMill.sfxplayer.playSound(WavePlayer.SOUND_MILL);
							ai.flyMillFormed();
							ai = null; //release memory, we don't need this anymore
						} else {
							parent.completeTurn();
							//ai will now shutdown
						}
						setAIFlyMove(null);
					} catch (PiecePlacementException e1) {
						System.err
								.println("Error: AI attempted to make an invalid move");
						e1.printStackTrace();
					}
					
				} 
				break;
			}// end switch
			repaint();
		}
	}

	/**
	 * Sets the AI object this GamePieceButton is associated with temporarily, which will be called back to if a flying piece is moved into a mill.
	 * It is then set to null after no more AI actions can take place.
	 * @param ai AI object to temporarily associate this button with.
	 */
	public void setAI(AI ai) {
		this.ai = ai;		
	}
}