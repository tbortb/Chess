package Evaluators;

import Pieces.ChessPiece;
import Pieces.King;
import application.ChessBoard;

public class MaxPossibleMoves implements ChessBoardEvaluator{

	@Override
	public int evaluatePosition(ChessBoard chessBoard) {
		int points = 0;
		
		for (ChessPiece piece : chessBoard.getWhitePieces()) {
			if ((piece instanceof King)) {
				points += 200;
			}else {
				points += piece.calcLegalMoves().size();				
			}
		}
		for (ChessPiece piece : chessBoard.getBlackPieces()) {
			if ((piece instanceof King)) {
				points -= 200;
			}else {
				points -= piece.calcLegalMoves().size();				
			}
		}
		return points;
	}
}