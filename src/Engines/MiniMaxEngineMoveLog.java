package Engines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Model.ChessMove;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class MiniMaxEngineMoveLog extends ChessEngine{
	
	public MiniMaxEngineMoveLog(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}
	
	public MiniMaxEngineMoveLog(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public List<ChessMove> computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval= chessBoard.isWhiteTurn();
//		List<ChessMove> returnedMoves = this.computerMove(chessBoard, depth, depth);
		return this.computerMove(chessBoard, depth, depth);
	}
	
	private List<ChessMove> computerMove(ChessBoard chessBoard, int depth, int initialDepth) {
		
		List<ChessMove> possibleMoves = new ArrayList<ChessMove>(64);
		
		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField()).toArray(Field[]::new);		
		
//		System.out.println((chessBoard.isWhiteTurn() ? "White" : "Black") + "Pieces: " + chessBoard.getActivePlayerPieces());
		for (Field fromField : fromFields) {
//			System.out.println("From Field: " + fromField);
			
//			System.out.println("Moves: " + fromField.getPiece().getLegalMoves());
			for(Field toField : fromField.getPiece().getLegalMoves()) {
				
//				System.out.println("From Field: " + fromField + "toField: " + toField);
				
				fromField.onClick(); //mouse click on chess piece's field
				toField.onClick(); //Move chosen ChessPiece to a field
				
				List<ChessMove> returnChessMoves;
				
				if(depth == 0) {
					possibleMoves.add(new ChessMove(fromField,
							toField,
							this.evaluatePosition(chessBoard)));
				} else {
					returnChessMoves = this.computerMove(chessBoard, depth - 1, initialDepth);
					possibleMoves.add(new ChessMove(fromField,
							toField,
							returnChessMoves.get(0).getValue(),
							returnChessMoves));
				}
				
				//Return to last move is necesary because all recursive playthroughs are played on the same chessboard!
				//If they can be played on separate boards, multithreading can be used
				chessBoard.returnToLastMove();
			}
		}
		
		Collections.sort(possibleMoves);
		
		if(chessBoard.isWhiteTurn()) {
			Collections.reverse(possibleMoves);
		}
		return possibleMoves;
		
//		int interestingValue = chessBoard.isWhiteTurn() ?
//				possibleMoves.get(possibleMoves.size() - 1).getValue() :
//					possibleMoves.get(0).getValue();
//		return possibleMoves.stream().filter(m -> m.getValue() == interestingValue).toArray(ChessMove[]::new);
	}

	
	
	

}
