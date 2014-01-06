package com.cs471.ninemanmill;
/**
 * Custom exception class that is thrown when an invalid move is performed. It is used for inline error checking when an invalid move is attempted.
 * @author Michael Perez
 *
 */
@SuppressWarnings("serial")
public class PiecePlacementException extends Exception {
	String message;
	
	/**
	 * This method sets the message to the passed in string.
	 * @param message The message the exception will display
	 */
	public PiecePlacementException(String message){
		super(message);
	}
	
	/**
	 * This method returns the message as an override to toString().
	 */
	public String toString(){
		return message;
	}

}
