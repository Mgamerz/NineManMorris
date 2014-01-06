package com.cs471.ninemanmill;

/**
 * Bundles are passed around in the AI class as a way to return different types of objects. As our swingworker can only return a single type, we made a 
 * bundle like Android uses when it wants to send a 'box' of information that might contain different objects.
 * @author Michael Perez
 *
 */
public class AIBundle {
	public final static int TYPE_MOVE = 0;
	public final static int TYPE_PLACE = 1;
	public final static int TYPE_DESTROY = 2;
	
	private Move move; //move to perform, null if no this mode
	private GamePiece placeCordinate; //Coordinate to place piece, null if not this mode
	private GamePiece destroyCordinate; //Piece to destroy, null if not this mode
	
	private int type;
	
	/**
	 * Constructs a new bundle that the AI thread will return. Since the AI thread can only return a single type, we make a bundle
	 * (like android does for things like this) and extract only the specified element this bundle should have, as it
	 * will only have 1 non-null value.
	 * @param type Type this bundle will return: Move, place, destroy
	 */
	public AIBundle(int type){
		this.type = type;
	}

	/**
	 * Gets the type. This will be switched on to get the correct value out of the bundle.
	 * @return Type of this bundle, representing DESTROY, PLACE, or MOVE.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets this bundle's type of value that will be stored in it
	 * @param type Type to store, use the static constants to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Gets the move out of this bundle. Only works if the type is set to TYPE_MOVE.
	 * @return Move this bundle contains
	 */
	public Move getMove() {
		return move;
	}

	/**
	 * Set's the move this bundle will contain and return to the UI thread. Only works if the type of this bundle is set to TYPE_MOVE.
	 * @param move Move to return to UI Event dispatch thread
	 */
	public void setMove(Move move) {
		this.move = move;
	}

	/**
	 * Set's a coordinate (gamepiece) that will be returned to the ui thread. Only works if the type is set to TYPE_PLACE
	 * @param placeCoordinate coordinate to destroy
	 */
	public void setPlace(GamePiece placeCoordinate) {
		this.placeCordinate = placeCoordinate;
	}

	/**
	 * Piece to destroy that was set in this bundle. Only works if the type of this bundle is set to TYPE_DESTROY.
	 * @return gamepiece of coordinate to destroy. Called on the UI dispatch thread.
	 */
	public GamePiece getDestroy() {
		return destroyCordinate;
	}

	/**
	 * Set's the coordinate where a piece is set to be destroyed. Should only be set if the bundle type is TYPE_DESTROY.
	 * @param destroyCoordinate
	 */
	public void setDestroy(GamePiece destroyCoordinate) {
		this.destroyCordinate = destroyCoordinate;
	}
	
	@Override
	public String toString(){
		String str = "AI Bundle\n - Type:" +type;
		switch(type){
		case TYPE_MOVE:
			str+="\nMove: " + getMove();
			break;
		case TYPE_PLACE:
			str+="\nPlace: " + getPlace();
		}
		
		return str;
	}

	/**
	 * Get's the place where a piece can be placed on the board. Only works if the type of this bundle is set to TYPE_PLACE.
	 * @return
	 */
	public GamePiece getPlace() {
		return placeCordinate;
	}
}
