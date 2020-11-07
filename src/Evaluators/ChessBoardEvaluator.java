package Evaluators;

import application.ChessBoard;

public interface ChessBoardEvaluator {
	
	/*
	 * This Function should return a high value when 
	 * the position is in favour of white, and a 
	 * negative value when the position is in
	 * favour of black
	 */
	public int evaluatePosition(ChessBoard chessBoard);
}
