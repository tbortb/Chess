package Engines;

/**
 * This is a test to check if the negamax framework is faster then the alphabeta prune that was implemented previously
 * It is expected to be slightly faster, becuase it has one less if statement
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Model.ChessMove;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class IndirectRecursionPrune extends ChessEngine {
	// alpha = minimum value that a maximizing player is guaranteed to get
	// beta = maximum value that a minimizing player is guaranteed to get

	public IndirectRecursionPrune(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}

	public IndirectRecursionPrune(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public ChessMove computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();
		if (chessBoard.isWhiteTurn()) {
			return this.alphaBetaMax(chessBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} else {
			return this.alphaBetaMin(chessBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
	}

	private ChessMove alphaBetaMax(ChessBoard chessBoard, int depth, int alpha, int beta) {

		List<ChessMove> possibleMoves = new ArrayList<ChessMove>(64);

		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField())
				.toArray(Field[]::new);

		if (depth == 0) {
			tryFields: for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					int value; // Value of newly discovered move
					possibleMoves.add(new ChessMove(fromField, toField, value = this.evaluatePosition(chessBoard)));
					chessBoard.returnToLastMove();

					//alpha muss nicht mehr neu gesetz werden, weil es nicht mehr tiefer geht
					if (value > beta) {
						break tryFields;
					}
				}
			}
		} else {

			tryFields: 
				for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					int value; // Value of newly discovered move
					possibleMoves.add(new ChessMove(fromField, toField,
							value = this.alphaBetaMin(chessBoard, depth - 1, alpha, beta).getValue()));

					chessBoard.returnToLastMove();

					// Do the pruning (with stop criterion alpha > beta)
					if (alpha < value) {
						alpha = value;
						if (alpha > beta) {
							break tryFields;
						}
					}
				}
			}
		}
		Collections.sort(possibleMoves);
		return possibleMoves.get(possibleMoves.size() - 1);
	}
	
	private ChessMove alphaBetaMin(ChessBoard chessBoard, int depth, int alpha, int beta) {

		List<ChessMove> possibleMoves = new ArrayList<ChessMove>(64);

		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField())
				.toArray(Field[]::new);

		if (depth == 0) {
			tryFields: for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					int value; // Value of newly discovered move
					possibleMoves.add(new ChessMove(fromField, toField, value = this.evaluatePosition(chessBoard)));
					chessBoard.returnToLastMove();

					//beta muss nicht mehr neu gesetz werden, weil es nicht mehr tiefer geht
					if (value < alpha) {
						break tryFields;
					}
				}
			}
		} else {

			tryFields: 
				for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					int value; // Value of newly discovered move
					possibleMoves.add(new ChessMove(fromField, toField,
							value = this.alphaBetaMax(chessBoard, depth - 1, alpha, beta).getValue()));

					chessBoard.returnToLastMove();

					// Do the pruning (with stop criterion alpha > beta)
					if (beta > value) {
						beta = value;
						if (alpha > beta) {
							break tryFields;
						}
					}
				}
			}
		}
		Collections.sort(possibleMoves);
		return possibleMoves.get(0);
	}

}
