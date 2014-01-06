package com.cs471.ninemanmill.uipieces;

import java.awt.Point;
/**
 * Sets up the board UI piece coordinates. Only used for positioning items on the board.
 * @author Michael Perez
 * @author Sasa Rkman
 *
 */
public class BoardUI {
	private Point snapCoordinates[][] = new Point[3][8];
	private final int PIECERADIUS = 20;
	private final int PIECEDIAMETER = PIECERADIUS *2;
	private final int OUTERLENGTH = 155;
	private final int MIDDLEXOFFSET = 520/2;
	private final int MIDDLEYOFFSET = 480/2;
	private final int MIDDLELENGTH = OUTERLENGTH-50;
	private final int INNERLENGTH = MIDDLELENGTH-50;

	/**
	 * Constructs a new object that holds snap coordinates.
	 */
	public BoardUI(){
		updateSnapCoordinates(); //Initial update of coordinates
	}
	
	/**
	 * Updates the 'snap coordinates' where the center of each piece can be. It is in R,P form:
	 */
	public void updateSnapCoordinates(){
		// You won't believe this but this code used to be way scarier before we scrapped some ideas
		/* Outer coordinates */
		snapCoordinates[0][0] = new Point(MIDDLEXOFFSET-OUTERLENGTH-PIECEDIAMETER,MIDDLEYOFFSET-OUTERLENGTH-PIECEDIAMETER);
		snapCoordinates[0][1] = new Point(MIDDLEXOFFSET, MIDDLEYOFFSET-OUTERLENGTH-PIECEDIAMETER);
		snapCoordinates[0][2] = new Point(MIDDLEXOFFSET+OUTERLENGTH+PIECEDIAMETER,MIDDLEYOFFSET-OUTERLENGTH-PIECEDIAMETER);
		snapCoordinates[0][3] = new Point(MIDDLEXOFFSET+OUTERLENGTH+PIECEDIAMETER,MIDDLEYOFFSET);
		
		snapCoordinates[0][4] = new Point(MIDDLEXOFFSET+OUTERLENGTH+PIECEDIAMETER,MIDDLEYOFFSET+OUTERLENGTH+PIECEDIAMETER);
		snapCoordinates[0][5] = new Point(MIDDLEXOFFSET,MIDDLEYOFFSET+OUTERLENGTH+PIECEDIAMETER);
		snapCoordinates[0][6] = new Point(MIDDLEXOFFSET-OUTERLENGTH-PIECEDIAMETER,MIDDLEYOFFSET+OUTERLENGTH+PIECEDIAMETER);
		snapCoordinates[0][7] = new Point(MIDDLEXOFFSET-OUTERLENGTH-PIECEDIAMETER,MIDDLEYOFFSET);
		
		/* Middle coordinates */
		snapCoordinates[1][0] = new Point(MIDDLEXOFFSET-MIDDLELENGTH-PIECEDIAMETER,MIDDLEYOFFSET-MIDDLELENGTH-PIECEDIAMETER);
		snapCoordinates[1][1] = new Point(MIDDLEXOFFSET, MIDDLEYOFFSET-MIDDLELENGTH-PIECEDIAMETER);
		snapCoordinates[1][2] = new Point(MIDDLEXOFFSET+MIDDLELENGTH+PIECEDIAMETER,MIDDLEYOFFSET-MIDDLELENGTH-PIECEDIAMETER);
		snapCoordinates[1][3] = new Point(MIDDLEXOFFSET+MIDDLELENGTH+PIECEDIAMETER,MIDDLEYOFFSET);
		
		snapCoordinates[1][4] = new Point(MIDDLEXOFFSET+MIDDLELENGTH+PIECEDIAMETER,MIDDLEYOFFSET+MIDDLELENGTH+PIECEDIAMETER);
		snapCoordinates[1][5] = new Point(MIDDLEXOFFSET,MIDDLEYOFFSET+MIDDLELENGTH+PIECEDIAMETER);
		snapCoordinates[1][6] = new Point(MIDDLEXOFFSET-MIDDLELENGTH-PIECEDIAMETER,MIDDLEYOFFSET+MIDDLELENGTH+PIECEDIAMETER);
		snapCoordinates[1][7] = new Point(MIDDLEXOFFSET-MIDDLELENGTH-PIECEDIAMETER,MIDDLEYOFFSET);
		
		/* Inner coordinates */
		snapCoordinates[2][0] = new Point(MIDDLEXOFFSET-INNERLENGTH-PIECEDIAMETER,MIDDLEYOFFSET-INNERLENGTH-PIECEDIAMETER);
		snapCoordinates[2][1] = new Point(MIDDLEXOFFSET, MIDDLEYOFFSET-INNERLENGTH-PIECEDIAMETER);
		snapCoordinates[2][2] = new Point(MIDDLEXOFFSET+INNERLENGTH+PIECEDIAMETER,MIDDLEYOFFSET-INNERLENGTH-PIECEDIAMETER);
		snapCoordinates[2][3] = new Point(MIDDLEXOFFSET+INNERLENGTH+PIECEDIAMETER,MIDDLEYOFFSET);
		
		snapCoordinates[2][4] = new Point(MIDDLEXOFFSET+INNERLENGTH+PIECEDIAMETER,MIDDLEYOFFSET+INNERLENGTH+PIECEDIAMETER);
		snapCoordinates[2][5] = new Point(MIDDLEXOFFSET,MIDDLEYOFFSET+INNERLENGTH+PIECEDIAMETER);
		snapCoordinates[2][6] = new Point(MIDDLEXOFFSET-INNERLENGTH-PIECEDIAMETER,MIDDLEYOFFSET+INNERLENGTH+PIECEDIAMETER);
		snapCoordinates[2][7] = new Point(MIDDLEXOFFSET-INNERLENGTH-PIECEDIAMETER,MIDDLEYOFFSET);
	}
	
	/**
	 * Gets the coordinates to place pieces on the draw panel.
	 * This provides great convenience.
	 * @return The point (x,y) of all the button coordinates
	 */
	public Point[][] getSnapCoordinates(){
		return snapCoordinates;
	}
}
