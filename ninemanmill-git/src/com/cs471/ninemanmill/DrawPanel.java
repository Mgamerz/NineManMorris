package com.cs471.ninemanmill;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.cs471.ninemanmill.uipieces.BoardUI;
/**
 * An extended JPanel that allows for a background image to be drawn as the background image.
 * It also is used to draw the busted pieces when a piece is destroyed.
 * @author Sasa Rkman
 * @author Michael Perez
 *
 */
@SuppressWarnings("serial")
public class DrawPanel extends JPanel {
	private Image backgroundImage;
    private Point backgroundLocation = new Point();
    public ArrayList<GamePiece> brokenPieces = new ArrayList<GamePiece>();
    private BufferedImage bustedBlueImage, bustedRedImage;
    private Point snapCoordinates[][];
    private ArrayList<BufferedImage> rotatedBlueBusted, rotatedRedBusted;

    public DrawPanel(){
    	super();
    	BoardUI ui = new BoardUI();
    	snapCoordinates = ui.getSnapCoordinates();
		try {
			bustedBlueImage = ImageIO.read(UINew.class.getResourceAsStream("resources/brokenblue.png"));
			bustedRedImage = ImageIO.read(UINew.class.getResourceAsStream("resources/brokenred.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		rotateImages();
    }
    
    /**
     * Rotates the busted images to 8 images, each 45 degrees apart from each other (diagonal and cardinal directions)
     */
    private void rotateImages() {
		if (bustedBlueImage != null && bustedRedImage != null){
			//images are not null, safe to work with
			rotatedBlueBusted = new ArrayList<BufferedImage>();
			rotatedRedBusted = new ArrayList<BufferedImage>();
			AffineTransform redTransform = new AffineTransform();
			AffineTransform blueTransform = new AffineTransform();
			
			//Add original image to list of buffered images. Then reuse these variables below.
			AffineTransformOp redOp = new AffineTransformOp(redTransform,AffineTransformOp.TYPE_BILINEAR);
			AffineTransformOp blueOp = new AffineTransformOp(blueTransform,AffineTransformOp.TYPE_BILINEAR);
			rotatedRedBusted.add(redOp.filter(bustedRedImage, null));
			rotatedBlueBusted.add(blueOp.filter(bustedBlueImage, null));
			
			//rotate 7 more times, and add a new version of the rotated images (each by 45 degrees)
			for (int i = 1; i<8; i++){
				redTransform.rotate(Math.toRadians(45),bustedBlueImage.getWidth()/2, bustedBlueImage.getHeight()/2);
				blueTransform.rotate(Math.toRadians(45),bustedRedImage.getWidth()/2, bustedRedImage.getHeight()/2);
				redOp = new AffineTransformOp(redTransform,AffineTransformOp.TYPE_BILINEAR);
				blueOp = new AffineTransformOp(blueTransform,AffineTransformOp.TYPE_BILINEAR);
				rotatedRedBusted.add(redOp.filter(bustedRedImage, null));
				rotatedBlueBusted.add(blueOp.filter(bustedBlueImage, null));
			}
		}
		
	}

	/**
     * Paints the panel
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(getBackgroundImage() != null) {
            g.drawImage(getBackgroundImage(), backgroundLocation.x, backgroundLocation.y, this);
        }
        //background is drawn, now draw broken pieces
        for (GamePiece piece : brokenPieces){
        	g.drawImage((piece.getTeamColor() == GamePiece.PLAYER1_PIECE) ? rotatedRedBusted.get(piece.getDirection()) : rotatedBlueBusted.get(piece.getDirection()), snapCoordinates[piece.getR()][piece.getP()].x-bustedRedImage.getWidth()/2,snapCoordinates[piece.getR()][piece.getP()].y-bustedRedImage.getHeight()/2,null);
        }
    }

    /**
     * Returns the panel's background images
     * @return Image the background image of the panel
     */
    public Image getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Sets the background image of the panel
     * @param backgroundImage the background image to be set
     */
    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        repaint();
    }

    /**
     * The point (x,y) location of the background
     * @return
     */
    public Point getBackgroundLocation() {
        return backgroundLocation;
    }

    /**
     * Sets the point (x,y) location of the background
     * @param backgroundLocation
     */
    public void setBackgroundLocation(Point backgroundLocation) {
        this.backgroundLocation = backgroundLocation;
        repaint();
    }
    
    /**
     * Adds a broken piece graphics to the board
     * @param piece the piece that is broken
     */
    public void addToBroken(GamePiece piece){
    	//System.out.println("Piece added color: "+piece.getTeamColor());
    	Random r = new Random();
    	piece.setDirection(r.nextInt(8)); //add direction to that piece
    	brokenPieces.add(piece);
    }
    
    /**
     * Clears the board of the broken pieces being drawn by removing them from the drawing list.
     */
    public void resetBroken(){
    	brokenPieces.clear();
    }
    
}
