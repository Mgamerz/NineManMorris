package com.cs471.ninemanmill;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.cs471.ninemanmill.uipieces.BoardUI;
import com.cs471.ninemanmill.uipieces.GamePieceButton;

/**
 * Represents the frame that the content panels and menu items are added to. This is the top level UI object, and all user facing objects are placed onto this window (dialog boxes are spawned through it).
 * Front end gameplay takes place here.
 * @author Melissa Neibaur
 * @author Sean Wright
 * @author Michael Perez
 * @author Sasa Rkman
 *
 */
public class UINew extends JFrame implements ActionListener {
	private static final long serialVersionUID = -8916222900522783822L;
	public final static int CURSOR_HAND = 0;
	public final static int CURSOR_HOLDING = 1;
	public final static int CURSOR_HAMMER = 2;
	BoardUI coordinates;
	Point[][] snapCoordinates;
	public GamePieceButton pieceSelected = null; //Previous piece selected, used for moving.
	public GameBoard gamestate; //current backend gamestate.
	public GamePieceButton[][] pieces; //R,P array of clickable front end buttons.
	JMenuBar menuBar;
	JPanel contentPanel;
	JLabel directionsLabel;
	
	public DrawPanel drawPanel;
	private DrawPanel creditsPanel;
	MouseListener handler;
	
	public AI p1ai = null; //p1 playing as an AI
	public AI p2ai = null; //p2 playing as an AI
	
	/**
	 * Sets up the main window frame, initialized to a state where no game is currently playing.
	 */
	public UINew(){
		setMinimumSize(new Dimension(520, 480));
		setResizable(false);
		setLocationRelativeTo(null); //center the window
		setTitle("Nine Men's Morris");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		coordinates = new BoardUI(); //get a list of coordinates of where to draw all pieces. This is a large piece of math, so this is in a separate class.
		setupMenu(); //Setup the menu bar.
		loadSounds(); //Load sounds into memory
		setupUI(); //setup all UI elements, and define actions for clicking on them.
		pack(); //pack all elements
	}
	
	/**
	 * Loads up the background audio player and the sound effects player, loading all audio into memory, even if the options are not currently enabled.
	 */
	private void loadSounds() {
		NineManMill.bgplayer = new MIDIPlayer();
		NineManMill.sfxplayer = new WavePlayer();
	}

	/**
	 * Sets up the main content panels such as the drawing panel, directions panel, and buttons on them.
	 */
	private void setupUI() {
		
		/*	Creates a draw panel that has a background, we do all our drawing on here */ //http://stackoverflow.com/questions/10961023/move-a-jlabel-to-front
		drawPanel = new DrawPanel();
		drawPanel.setLayout(null);
		try {
			// Pretty much the cleanest way I've seen this done...
			drawPanel.setBackgroundImage(new ImageIcon(ImageIO.read(getClass().getResource("resources/mainBackground.png"))).getImage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		setCursor(CURSOR_HAND,false); //set cursor, do not wait
		drawPanel.setPreferredSize(new Dimension(520, 480));
		drawPanel.setMaximumSize(new Dimension(520, 480));
		
		/*	Creates a credits panel */
		creditsPanel = new DrawPanel();
		creditsPanel.setLayout(null);
		try {
			// Pretty much the cleanest way I've seen this done...
			creditsPanel.setBackgroundImage(new ImageIcon(ImageIO.read(getClass().getResource("resources/creditsPanel.png"))).getImage());
		} catch (IOException e) {
			e.printStackTrace(); // This doesn't print the error correctly...
		}
		creditsPanel.setPreferredSize(new Dimension(520, 480));
		creditsPanel.setMaximumSize(new Dimension(520, 480));
		creditsPanel.addMouseListener(new MouseListener(){ //glass pane window for credits
			@Override
			public void mouseClicked(MouseEvent arg0) {
				getGlassPane().setVisible(false);
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		});
		setGlassPane(creditsPanel);
		getGlassPane().setVisible(false);
		
		/* Set the application icon [windows only]*/
		ArrayList<Image> icons = new ArrayList<Image>(3);
		try {
			icons.add(new ImageIcon(ImageIO.read(getClass().getResource("resources/icon32.png"))).getImage());
			icons.add(new ImageIcon(ImageIO.read(getClass().getResource("resources/icon16.png"))).getImage());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		setIconImages(icons);

		/* Creates a directions/status panel at the bottom */
		JPanel directionsPanel = new JPanel();
		directionsPanel.setLayout(new BoxLayout(directionsPanel,BoxLayout.LINE_AXIS));
		directionsPanel.add(Box.createHorizontalGlue());
		directionsLabel = new JLabel("Select new game from the actions menu to start a new game.");
		directionsLabel.setHorizontalAlignment(SwingConstants.CENTER); // Centers the text
		directionsLabel.setForeground(Color.WHITE);
		directionsPanel.add(directionsLabel);
		directionsPanel.add(Box.createHorizontalGlue());
		directionsPanel.setBackground(new Color(32,21,6));
		
		
		//Draw circles to look like pieces. These pieces however will do nothing, as this is the title screen.
		pieces = new GamePieceButton[3][8];
		snapCoordinates = coordinates.getSnapCoordinates();		
		for(int r = 0; r < 3; r++){
			for(int p = 0; p < 8; p++){
				Point pt = snapCoordinates[r][p];
				pieces[r][p] = new GamePieceButton(new GamePiece(r,p), this);
				pieces[r][p].setBounds(pt.x-15, pt.y-15, 30, 30); //this draws from the top left. snap coordinates returns the centerpoint so move a radius up-left to make it draw properly
				pieces[r][p].setMode(GamePieceButton.DEMO_MODE);				
				pieces[r][p].addActionListener(this);
				drawPanel.add(pieces[r][p]);
			}
		}
		add(drawPanel);												// Add it to the main Frame
		//add(creditsPanel);
		add(directionsPanel, BorderLayout.SOUTH);
	}

	/**
	 * Sets this JFrame to be visible, popping it up to the user.
	 */
	public void setVisible(){
		this.setVisible(true);
	}
	
	
	/**
	 * Sets up a new game, asking for the human to choose the players, and doing preliminary work to make the game playable.
	 * If player 1 is an AI, it is kickstarted to make the first move.
	 */
	protected void setupNewGame(){		
		//Semi test code: Get the opponent (AI, Human)
		NewGameDialog ngd = new NewGameDialog(this); //make modal dialog
		NewGameBundle ngb = ngd.getGameSetup();
		
		int player1 = ngb.getPlayer1(); //this code will execute after the dialog box closes (modal)
		if (player1 == -1) {
			//user clicked X, ignore new game request
			return;
		}
		if(player1 > 0){
			p1ai = new AI(GameBoard.PLAYER1, player1, this);
		} else {
			p1ai = null;
		}
		
		int player2 = ngb.getPlayer2(); //this code will execute after the dialog box closes (modal)
		if (player2 == -1 ) {
			//user clicked X, ignore new game request
			return;
		}
		if(player2 > 0){
			p2ai = new AI(GameBoard.PLAYER2, player2, this);
		} else {
			p2ai = null;
		}
		//System.out.println("Players: "+player1+" vs "+player2);
		drawPanel.removeAll();
		drawPanel.revalidate();
		drawPanel.resetBroken();
		drawPanel.repaint();
		
		
		//Make a new gamestate, clear the old one
		gamestate = new GameBoard(this,ngb.getFlyMode());
	
		//Draw circles to look like pieces. These pieces however will do nothing, as this is the title screen.
		pieces = new GamePieceButton[3][8];
		
		//Get snap coordinates - if the window has changed size (for some, any reason, not just resizing) we should update our snap coordiantes reference.
		snapCoordinates = coordinates.getSnapCoordinates();

		for(int r = 0; r < 3; r++){ //each ring
			for(int p = 0; p < 8; p++){ //each position
				Point pt = snapCoordinates[r][p];
				pieces[r][p] = new GamePieceButton(new GamePiece(r,p), this);
				pieces[r][p].setBounds(pt.x-15, pt.y-15, 30, 30); //this draws from the top left. snap coordinates returns the centerpoint so move a radius up-left to make it draw properly
				if (p1ai == null) {
					pieces[r][p].setMode(GamePieceButton.PLACE_MODE); //pieces are ready to place.
				} else {
					pieces[r][p].setMode(GamePieceButton.AI_PLACE_MODE); //pieces are ready to place, AI turn.
				}
				pieces[r][p].addActionListener(this);
				drawPanel.add(pieces[r][p]);
			}
		}
		if (p1ai == null) {
			setDirections("Player 1's turn to place a piece");
		} else {
			setDirections("AI Player 1 is choosing where to place a piece...");
			p1ai.performTurn(gamestate); //player 1's ai will now perform the first turn
		}
		gamestate.playBackgroundMusic();
		drawPanel.repaint();
	}

	/**
	 * Sets new text at the bottom of the main window.
	 * @param directions Text to set.
	 */
	protected void setDirections(String directions) {
		directionsLabel.setText(directions);
	}
	
	/**
	 * Sets up the menu systems. 
	 * In the final implementation, the debug menu will be disabled so the user will not be able to access it. It contains many useful debugging features.
	 */
	private void setupMenu() {
		menuBar = new JMenuBar();
		JMenu actions = new JMenu("Actions");
		JMenu debug = new JMenu("Debug");
		JMenu help = new JMenu("Help");
		JMenuItem newgame, preferences, exit;
		JMenuItem showWinnerDialog, printBoard, printModes, setNewDirections, debugRepaint, debugBreaking, randomlyFillBoard, printTeamPieces, garbageCollect, credits;


		newgame = new JMenuItem("New game");
		newgame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				//System.out.println("Newgame clicked");
				setupNewGame();
			}
		});
		preferences = new JMenuItem("Preferences");
		preferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				new PreferencesDialog(UINew.this);
			}
		});
		exit = new JMenuItem("Exit");
		
		showWinnerDialog = new JMenuItem("Show winner dialog");
		showWinnerDialog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new WinnerDialog(UINew.this,2, "Test dialog");
			}
			
		});
		
		printBoard = new JMenuItem("Print gamestate");
		printBoard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			System.out.println(gamestate);
			}
			
		});
		
		printModes = new JMenuItem("Print piece modes");
		printModes.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			for (int i = 0; i< 3; i++)
				for (GamePieceButton piece: pieces[i]){
				System.out.println(piece);
				}
			}
			
		});
		
		setNewDirections = new JMenuItem("Set new directions");
		setNewDirections.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String newDirection = JOptionPane.showInputDialog("Enter a new direction");
				setDirections(newDirection);
			}
			
		});
		
		debugRepaint = new JMenuItem("Repaint JWindow");
		debugRepaint.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
			
		});

		randomlyFillBoard = new JMenuItem("Randomly fill board");
		randomlyFillBoard.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				Random rand = new Random(); //our random generator
				while(gamestate.setupMode){
					//randomly fill the board until we have no more pieces to place
					pieces[rand.nextInt(3)][rand.nextInt(8)].performClick(null);
				}
			}
		});
		
		printTeamPieces = new JMenuItem("Print Team Pieces");
		printTeamPieces.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				gamestate.getTeam1().printPieces();
				gamestate.getTeam2().printPieces();
			}
		});
		
		garbageCollect = new JMenuItem("Force Garbage Collection");
		garbageCollect.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				//cleanup garbage
				System.gc();
			}
		});
		
		debugBreaking = new JMenuItem("Set 0,0 to breakingmode");
		debugBreaking.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				pieces[0][0].setBlinkMode(GamePieceButton.BREAKING_MODE);
			}
			
		});
		
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				System.exit(0);
			}
		});

		credits = new JMenuItem("Credits");
		credits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getGlassPane().setVisible(true);
			}
		});
		
		actions.add(newgame);
		actions.add(preferences);
		actions.addSeparator();
		actions.add(exit);
		debug.add(showWinnerDialog);
		debug.add(setNewDirections);
		debug.add(printBoard);
		debug.add(printModes);
		debug.add(debugRepaint);
		debug.add(randomlyFillBoard);
		debug.add(printTeamPieces);
		debug.add(garbageCollect);
		debug.add(debugBreaking);
		help.add(credits);
		menuBar.add(actions);
		menuBar.add(debug); //comment this out to remove the debug menu
		menuBar.add(help);

		this.setJMenuBar(menuBar);
	}

	/**
	 * The mouse click listener for the gameboard
	 * @param e the type of event that happened
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof GamePieceButton){
			//its a gamepiecebutton that was clicked, tell that button it was clicked so it can handle the click.
			GamePieceButton piece = (GamePieceButton) e.getSource();
			piece.performClick(pieceSelected);
		}
	}

	/**
	 * Tries to place a piece into the underlying board structure.
	 * @param coordinate place where a piece will be placed
	 * @return false if piece cannot be places
	 */
	public boolean setPiecePlaced(GamePiece coordinate) {
		try {
			return gamestate.placePiece(coordinate.getR(), coordinate.getP());
		} catch (PiecePlacementException e) {
			System.err.println("Cannot place piece!");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Changes the text of the directions label
	 * to represent what the user should do next
	 * @param text The direction given to user
	 */
	public void setDirectionsText(String text){
		directionsLabel.setText(text);
	}
	
	/**
	 * Sets the current turn to the other side and repaints the screen if any changes occured.
	 */
	public void completeTurn(){
		gamestate.completeTurn();
		
		//check for game synchronization between struct and ui
		if (checkBoardSynchronization(gamestate) == false){
			System.err.println("ERROR: GAMEBOARD IS OUT OF SYNC WITH UI!");
		}
		
		if (!gamestate.gameFinished) {
			//Perform the following if one of these conditions is met:
			// - It's setup mode
			// - It's not setup mode, and both sides have more than 3 pieces
			
			if (gamestate.setupMode || (gamestate.team1.getNumPieces() >= 3 && gamestate.team2.getNumPieces() >= 3)){
				if (gamestate.CURRENT_TURN == GameBoard.PLAYER1_TURN && p1ai!=null ) {
					//set indicator of whats happening
					if (gamestate.setupMode) {
						setDirections("AI Player 1 is choosing where to place a piece...");
					} else {
						setDirections("AI Player 1 is choosing a move...");
					}
					
					p1ai.performTurn(gamestate); //player 2 will now perform it's turn
					repaint();
					return;
				}
				
				if (gamestate.CURRENT_TURN == GameBoard.PLAYER2_TURN && p2ai!=null ) {
					//set indicator of whats happening
					if (gamestate.setupMode) {
						setDirections("AI Player 2 is choosing where to place a piece...");
					} else {
						setDirections("AI Player 2 is choosing a move...");
					}
					p2ai.performTurn(gamestate); //player 2 will now perform it's turn
					repaint();
					return;
				}
				
				//theres no ai performing on this turn
				if (gamestate.setupMode){ 
					setDirections("Player "+gamestate.CURRENT_TURN+"'s turn to place a piece"); //finish the turn, place a piece.
				} else {
					setDirections("Player "+gamestate.CURRENT_TURN+", choose a piece to move"); //finish the turn since a mill was formed
				}
			}
		}
		repaint(); //update graphics
	}
	
	/**
	 * Updates the UI when a piece has been set to move. This clears the temporary holding cache for the first piece, and clears the highlighting on a piece, if any.
	 * This also sets the origin spot to hidden piece
	 */
	public void updateMovedPieces(){
		if (pieceSelected!=null){
			pieceSelected.REPRESENTING = GameBoard.EMPTY; //the place where the piece was is no longer a piece anymore
			pieceSelected.setMode(GamePieceButton.HIDDEN_MODE); //so lets set it to hidden mode
			pieceSelected.highlightAdjacent(false); //clear the highlights of the old piece
			pieceSelected = null; //perform the action, no more selection
		}
	}
	
	/**
	 * Changes the cursor icon
	 * Constants are passed to determine the cursor.
	 * 
	 * NOTE: There is another method that JFrame natively has, called setCursor(int). Do not use this as it is deprecated and will not do what you want
	 * most likely.
	 * @param mode Cursor to set [see constants]
	 * @param animate if the UI should set a timer to show the animation of the piece before setting back to default hand cursor
	 */
	public void setCursor(int mode, boolean animate) {
		//System.out.println("Set cursor: "+mode+" animate: "+animate);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		Image newCursor = null;
		switch(mode){
		case CURSOR_HAND:
			newCursor = toolkit.getImage(getClass().getResource("resources/hand.png"));
			break;
		case CURSOR_HOLDING:
			newCursor = toolkit.getImage(getClass().getResource("resources/handDown.png"));
			break;
		case CURSOR_HAMMER:
			newCursor = toolkit.getImage(getClass().getResource("resources/hammer.png"));
			break;
		}
		
		if (newCursor== null){
			System.err.println("ERROR: Cursor value passed to setCursor was not a valid selection.");
			return;
		}
		
		Cursor c = toolkit.createCustomCursor(newCursor, new Point(drawPanel.getX(),
				drawPanel.getY()), "img");
		

		
		if (animate){
			ActionListener returnToHand = new ActionListener(){
				  public void actionPerformed(ActionEvent event){
				    //System.out.println("hello");
					  setCursor(UINew.CURSOR_HAND,false); //do not wait when resetting the hand
				  }
				};
				Timer timer = new Timer(120, returnToHand); //set time value and callback listener
				timer.setRepeats(false); //don't do over and over
				timer.start(); //start the timer asynchronously
		}
		drawPanel.setCursor(c);
	}

	/**
	 * Converts a list of pieces and returns a list of their cooresponding buttons
	 * @param destroyablePieces
	 * @return all the gamepiece buttons, in an arraylist.
	 */
	public ArrayList<GamePieceButton> getButtons(ArrayList<GamePiece> destroyablePieces) {
		ArrayList<GamePieceButton> returnList = new ArrayList<GamePieceButton>();
		for (GamePiece coordinate : destroyablePieces){
			returnList.add(pieces[coordinate.getR()][coordinate.getP()]);
		}
		return returnList;
	}
	
	/**
	 * Debugging method. Gets a gameboard representation of the UI frontend, for comparing with the backend to identify desync issues.
	 * @return GameBoard that the UI thinks it has, based on the states of all the buttons. It is dynamically built from each individual piece. 
	 This is compared with the backend GameBoard object.
	 */
	protected GameBoard getUIgamestate(){
		String boardRepresentation = "";
		for(int r = 0; r < 3; r++){ //each ring
			for(GamePieceButton gpb : pieces[r]){ //each position
				boardRepresentation+=gpb.representsPiece(); //concat the entire board.
			}
		}
		try {
			return new GameBoard(boardRepresentation);
		} catch (Exception e) {
			System.err.println("Exception occured trying to get debug gamestate that the UI thinks it has");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Debug method - checks to see if the board is in sync or not with the UI.
	 * @param state State to check against
	 * @return true if the pieces are in sync, false otherwise [VERY BAD!]
	 */
	public boolean checkBoardSynchronization(GameBoard state){
		Team team1 = state.getTeam1();
		Team team2 = state.getTeam2();
		
		//First we will check if the teams pieces match to a piece they own on the board.
		
		for(GamePiece piece : team1.teamPieces){
			//For each piece this team has, check if the piece on the board matches
			GamePieceButton uiPiece = pieces[piece.getR()][piece.getP()];
			if (uiPiece.representsPiece() != team1.getSymbol()){
				System.err.println("Error: Team 1 has a piece that is not set on the board, it is out of sync.");
				return false;
			}
		}
		
		for(GamePiece piece : team2.teamPieces){
			//For each piece this team has, check if the piece on the board matches
			GamePieceButton uiPiece = pieces[piece.getR()][piece.getP()];
			if (uiPiece.representsPiece() != team2.getSymbol()){
				System.err.println("Error: Team 2 has a piece that is not set on the board, it is out of sync.");
				return false;
			}
		}
		
		//All team pieces are on the board - now we should reverse match 
		for (int r = 0; r < 3; r++){
			for (int p = 0; p < 7; p++){
				switch (pieces[r][p].representsPiece()){
				case GameBoard.EMPTY:
					break; //no point to check empty... or is there?
				case GameBoard.PLAYER1:
					if (!team1.teamPieces.contains(new GamePiece(r,p))){
						System.err.println("Error: Team 1 does not contain a piece that the UI has marked as owning at "+r+","+p);
						System.err.println("Backend:\n"+gamestate);
						System.err.println("Frontend (UI):\n"+getUIgamestate());
						team1.printPieces();
						return false;
					}
					break;
				case GameBoard.PLAYER2:
					if (!team2.teamPieces.contains(new GamePiece(r,p))){
						System.err.println("Error: Team 2 does not contain a piece that the UI has marked as owning at "+r+","+p);
						return false;
					}
					break;
				default:
					System.err.println("CRITICAL ERROR: Gameboard has unknown symbol on the map!");
				}
					
			}
		}
		//all tests passed
		return true;
	}
}
