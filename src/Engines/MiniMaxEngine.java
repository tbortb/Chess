package Engines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class MiniMaxEngine extends ChessEngine{
	
	public MiniMaxEngine(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}
	
	public MiniMaxEngine(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public ChessMove computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);;
		return this.computerMove(chessBoard, depth, depth);
	}
	
	private ChessMove computerMove(ChessBoard chessBoard, int depth, int initialDepth) {
		
		List<ChessMove> possibleMoves = new ArrayList<ChessMove>();
		
		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField()).toArray(Field[]::new);		
		
//		System.out.println((chessBoard.isWhiteTurn() ? "White" : "Black") + "Pieces: " + chessBoard.getActivePlayerPieces());
		for (Field fromField : fromFields) {
//			System.out.println("From Field: " + fromField);
			
//			System.out.println("Moves: " + fromField.getPiece().getLegalMoves());
			for(Field toField : fromField.getPiece().getLegalMoves()) {
				
//				System.out.println("From Field: " + fromField + "toField: " + toField);
				
				fromField.onClick(); //mouse click on chess piece's field
				toField.onClick(); //Move chosen ChessPiece to a field
				
				if(depth == 0) {
					possibleMoves.add(new ChessMove(fromField,
							toField,
							this.evaluatePosition(chessBoard)));
				} else {
					possibleMoves.add(new ChessMove(fromField,
							toField,
							this.computerMove(chessBoard, depth - 1, initialDepth).getValue()));
				}
				
				//Return to last move is necesary because all recursive playthroughs are played on the same chessboard!
				//If they can be played on separate boards, multithreading can be used
				chessBoard.returnToLastMove();
			}
		}
		
		Collections.sort(possibleMoves);

//		System.gc();

		return chessBoard.isWhiteTurn() ? possibleMoves.get(possibleMoves.size() - 1) : possibleMoves.get(0);
	}

	
	
	

}
