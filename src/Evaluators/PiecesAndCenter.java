package Evaluators;

import Pieces.Bishop;
import Pieces.ChessPiece;
import Pieces.Knight;
import Pieces.Pawn;
import Pieces.Queen;
import Pieces.Rook;
import application.ChessBoard;
import application.Field;

public class PiecesAndCenter implements ChessBoardEvaluator{

	@Override
	public int evaluatePosition(ChessBoard chessBoard) {
		//White is trying to get a high score, while balck is trying to get a negative score
		int points = 0;
		int center33 = 0;
		int center34 = 0;
		int center43 = 0;
		int center44 = 0;

		
		for (ChessPiece piece : chessBoard.getWhitePieces()) {
			//Points for living pieces
			points += piece.getValue() * 10;
			
			//Negative point for knight on the edge of the field
			if (piece instanceof Knight) { 
				if(piece.getField().getCol() == 0 || piece.getField().getCol() == 7 ||
						piece.getField().getRow() == 0 || piece.getField().getRow() == 7) {
					points--;
				}
			}
			
			//Points for controlling the center only for knight, bishop, pawn and queen
			if (piece instanceof Pawn || piece instanceof Knight || piece instanceof Bishop || piece instanceof Queen ||
					piece instanceof Rook) {
				for (Field move : piece.getLegalMoves()) {
					if(move.getCol() == 3 && move.getRow() == 3) {
						center33++;
					}
					else if(move.getCol() == 3 && move.getRow() == 4) {
						center34++;
					}
					else if(move.getCol() == 4 && move.getRow() == 3) {
						center43++;
					}
					else if(move.getCol() == 4 && move.getRow() == 4) {
						center44++;
					}
				}				
			}
			
			//Bonus for development of knight and bishop
			if (piece instanceof Knight || piece instanceof Bishop) {
				if (piece.hasMoved()) {
					points++;					
				}
			}
		}
		
		for (ChessPiece piece : chessBoard.getBlackPieces()) {
			//Points for living pieces
			points -= piece.getValue() * 10;
			
			//Negative point for knight on the edge of the field
			if (piece instanceof Knight) { 
				if(piece.getField().getCol() == 0 || piece.getField().getCol() == 7 ||
						piece.getField().getRow() == 0 || piece.getField().getRow() == 7) {
					points++;
				}
			}
			
			//Points for controlling the center
			if (piece instanceof Pawn || piece instanceof Knight || piece instanceof Bishop || piece instanceof Queen ||
					piece instanceof Rook) {
				for (Field move : piece.getLegalMoves()) {
					if(move.getCol() == 3 && move.getRow() == 3) {
						center33--;
					}
					else if(move.getCol() == 3 && move.getRow() == 4) {
						center34--;
					}
					else if(move.getCol() == 4 && move.getRow() == 3) {
						center43--;
					}
					else if(move.getCol() == 4 && move.getRow() == 4) {
						center44--;
					}
				}
			}
			
			//Bonus for development of knight and bishop
			if (piece instanceof Knight || piece instanceof Bishop) {
				if (piece.hasMoved()) {
					points--;					
				}
			}
		}
			
		points += center33 > 0 ? 1 : center33 < 0 ? -1 : 0;
		points += center34 > 0 ? 1 : center34 < 0 ? -1 : 0;
		points += center43 > 0 ? 1 : center43 < 0 ? -1 : 0;
		points += center44 > 0 ? 1 : center44 < 0 ? -1 : 0;
	
		return points;
	}
}
