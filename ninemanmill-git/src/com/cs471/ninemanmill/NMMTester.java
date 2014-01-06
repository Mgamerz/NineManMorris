/**
 * Testing class for our gameboard. Should help us change stuff and still have it work.
 * This class is not used very much, and was not updated to test our code as the project got more complex.
 * 
 * @author Michael Perez
 * @author Melissa Simpson
 */

package com.cs471.ninemanmill;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;


public class NMMTester {

	@Test
	public void test() {
		//fail("Not yet implemented");
	}
	
	@Test
	/**
	 * Tests boards to make sure they are valid.
	 */
	public void testBoards(){
		/*Make some testcase boards here:
		The format for a board for passing to the board class (to use as the initial one
		"000000001111111122222222" where 000... is position 0 1 2 3 4 5 6 7 8 on the outermost ring.
		The board will parse this as a current "state" if you pass it to the constructor that takes a string.
		Valid letters are defined at the top of the GameBoard class as constants.
		*/
		String boardStates[] = {
			"2212E1E212121212E12112E1" /* Add more states here */
		};
		
		int l = boardStates.length;
		
		/* Run tests on all the boards */
		for(int i = 0; i<l;i++){
			GameBoard board = null; //Make eclipse happy
			try {
				board = new GameBoard(boardStates[i]);
			} catch (Exception e) {
				fail("Gameboard threw exception in test case "+i);
				e.printStackTrace();
			}
			//System.out.println(board.toString());
		}
	}
	
	@Test
	/**
	 * Tests moving pieces on a board.
	 */
	public void testPieceMovement(){
		String testBoard1 = "EEEE22E2111111112222222E";
		GameBoard board = null;
		try {
			board = new GameBoard(testBoard1);
			System.out.println("Testing board:");
			System.out.println(board.toString());
			Team team1 = board.getTeam1(), team2 = board.getTeam2();
			board.movePiece(new Move(team2, 0, 7, 0, 6));
			System.out.println();
			System.out.println(board.toString());
			if(board.detectNineManMill(team2, 0, 6)){
				System.out.println("It's a mill");
			}
			else{
				System.out.println("It's not a mill");
			}
			board.movePiece(new Move(team1, 1, 3, 0, 3));
			System.out.println();
			System.out.println(board.toString());
			if(board.detectNineManMill(team1, 0, 3)){
				System.out.println("It's a mill");
			}
			else{
				System.out.println("It's not a mill");
			}
			board.movePiece(new Move(team2, 2, 0, 2, 7));
			System.out.println();
			System.out.println(board.toString());
			if(board.detectNineManMill(team2, 2, 7)){
				System.out.println("It's a mill");
			}
			else{
				System.out.println("It's not a mill");
			}
			
			board.movePiece(new Move(team1, 1, 7, 0, 7));
			System.out.println();
			System.out.println(board.toString());
			if(board.detectNineManMill(team1, 1, 3)){
				System.out.println("It's a mill");
			}
			else{
				System.out.println("It's not a mill");
			}
			board.movePiece(new Move(team2, 2, 7, 1, 7));
			System.out.println();
			System.out.println(board.toString());
			if(board.detectNineManMill(team2, 2, 7)){
				System.out.println("It's a mill");
			}
			else{
				System.out.println("It's not a mill");
			}
			
			board.movePiece(new Move(team1, 0, 7, 0, 0));
			System.out.println();
			System.out.println(board.toString());
			if(board.detectNineManMill(team1, 1, 3)){
				System.out.println("It's a mill");
			}
			else{
				System.out.println("It's not a mill");
			}
			
		} catch (Exception e) {
			fail("Exception occured while moving piece(s)");
			e.printStackTrace();
		}
		

	}
	
	//@Test
	/**
	 * Tests moving pieces on a board.
	 *//*
	public void testAIMovement(){
		String testBoard1 = "EEEE22E2111111112222222E";
		GameBoard board = null;
		AI ai = null;
		try {
			board = new GameBoard(testBoard1);
			ai = new AI('0',0,null); //this might be broken now.
			System.out.println();
			System.out.println();
			System.out.println("Testing AI:");
			System.out.println(board.toString());
			Team team1 = board.getTeam1(), team2 = board.getTeam2();
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");

			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");
		
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");
		
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI1 moved");
			
			ai.performMove(board);
			System.out.println();
			System.out.println(board.toString());
			//cannot print mill or not here because don't know random's move
			System.out.println("AI2 moved");
			
		} catch (Exception e) {
			fail("Exception occured while moving piece(s)");
			e.printStackTrace();
		}
		*/

	
	/**
	 * Test for bad mills. It did not detect them properly so it wasn't useful.
	 */
		@Test
		public void testBadMill(){
			String boardRep = "11EE1111EE2222EEEEE1222E";
			try {
				GameBoard board = new GameBoard(boardRep);
				ArrayList<GamePiece> tp = new ArrayList<GamePiece>();
				//Team 1
				tp.add(new GamePiece(0,0));
				tp.add(new GamePiece(0,1));
				tp.add(new GamePiece(0,4));
				tp.add(new GamePiece(0,5));
				tp.add(new GamePiece(0,6));
				tp.add(new GamePiece(0,7));
				tp.add(new GamePiece(2,3));






				
				Team team1 = new Team(tp);
				tp = new ArrayList<GamePiece>();
				tp.add(new GamePiece(1,2));
				tp.add(new GamePiece(1,3));
				tp.add(new GamePiece(1,4));
				tp.add(new GamePiece(1,5));
				tp.add(new GamePiece(2,4));
				tp.add(new GamePiece(2,5));
				tp.add(new GamePiece(2,6));
				Team team2 = new Team(tp);
				
				board.team1 = team1;
				board.team2 = team2;
				board.getTeam1().printPieces();
				board.getTeam2().printPieces();
				
				System.out.println(board);
				for(int r = 0; r < 3; r++){
					for(int p = 0; p < 7; p++){
						System.out.println("T1: Mill at "+r+","+p+": "+board.detectNineManMill(team1,r,p));
						System.out.println("T2: Mill at "+r+","+p+": "+board.detectNineManMill(team2,r,p));
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
