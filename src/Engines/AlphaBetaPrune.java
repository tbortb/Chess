package Engines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class AlphaBetaPrune extends ChessEngine {
	//alpha = minimum value that a maximizing player is guaranteed to get
	//beta = maximum value that a minimizing player is guaranteed to get
	
	public AlphaBetaPrune(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}
	
	public AlphaBetaPrune(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public ChessMove computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();
		return this.alphaBeta(chessBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	private ChessMove alphaBeta(ChessBoard chessBoard, int depth, int alpha, int beta) {
		
		List<ChessMove> possibleMoves = new ArrayList<ChessMove>();

		Field[] fromFields = chessBoard.getActivePlayerPieces()
											.stream()
											.map(piece -> piece.getField())
											.toArray(Field[]::new);
		
//		System.out.println("depth" + depth + (chessBoard.isWhiteTurn() ? "White" : "Black") + "Pieces: " + chessBoard.getActivePlayerPieces());

		tryFields:
		for (Field fromField : fromFields) {
//			System.out.println("depth" + depth + "From Field: " + fromField);
			
//			System.out.println("depth" + depth + "Moves: " + fromField.getPiece().getLegalMoves());
			for (Field toField : fromField.getPiece().getLegalMoves()) {
//				System.out.println("depth" + depth + "From Field: " + fromField + "toField: " + toField);
				
				//Debugging
//				if (fromField.getRow() == toField.getRow() && fromField.getCol() == toField.getCol()) {
//					throw new RuntimeException("fromField ant toField are the same");
//				}


				fromField.onClick(); // mouse click on chess piece's field
				toField.onClick(); // Move chosen ChessPiece to a field

				int value; //Value of newly discovered move
				
				if (depth == 0) {
					possibleMoves.add(new ChessMove(fromField,
							toField,
							value = this.evaluatePosition(chessBoard)));					
				} else {
					possibleMoves.add(new ChessMove(fromField,
									toField,
									value = this.alphaBeta(chessBoard, depth - 1, alpha, beta).getValue()));
				}

				chessBoard.returnToLastMove();
				
				//Do the pruning (with stop criterion alpha > beta)
				if(chessBoard.isWhiteTurn()) {
					if (alpha < value) {
						alpha = value;
					}
				}else {
					if (beta > value) {
						beta = value;
					}
				}
				if (alpha > beta) {
					break tryFields;
				}
			}
		}
		Collections.sort(possibleMoves);
//		System.gc();
		return possibleMoves.size() == 0 ? null : chessBoard.isWhiteTurn() ? possibleMoves.get(possibleMoves.size() - 1) : possibleMoves.get(0);
	}

}
