package Evaluators;

import Pieces.ChessPiece;
import Pieces.King;
import application.ChessBoard;

public class MaxMovesAndPieceValue implements ChessBoardEvaluator{

	@Override
	public int evaluatePosition(ChessBoard chessBoard) {
		int points = 0;
		
		for (ChessPiece piece : chessBoard.getWhitePieces()) {
			if ((piece instanceof King)) {
				points += 200;
			}else {
				points += piece.calcLegalMoves().size();	
				points += piece.getValue() * 10;
			}
		}
		for (ChessPiece piece : chessBoard.getBlackPieces()) {
			if ((piece instanceof King)) {
				points -= 200;
			}else {
				points -= piece.calcLegalMoves().size();				
				points -= piece.getValue() * 10;
			}
		}
		return points;
	}
}