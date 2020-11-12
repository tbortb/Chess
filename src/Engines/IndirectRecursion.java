package Engines;

/**
 * This is a test to check if the indirect recursion is faster, because it uses less if statements
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Model.ChessMove;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class IndirectRecursion extends ChessEngine {
	// alpha = minimum value that a maximizing player is guaranteed to get
	// beta = maximum value that a minimizing player is guaranteed to get

	public IndirectRecursion(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}

	public IndirectRecursion(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public ChessMove computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();
		if (chessBoard.isWhiteTurn()) {
			return this.maximizer(chessBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} else {
			return this.minimizer(chessBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
	}

	private ChessMove maximizer(ChessBoard chessBoard, int depth, int alpha, int beta) {

		List<ChessMove> possibleMoves = new ArrayList<ChessMove>();

		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField())
				.toArray(Field[]::new);

		if (depth == 0) {
			for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					possibleMoves.add(new ChessMove(fromField, toField, this.evaluatePosition(chessBoard)));
					chessBoard.returnToLastMove();
				}
			}
		} else {

				for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					possibleMoves.add(new ChessMove(fromField, toField,
							this.minimizer(chessBoard, depth - 1, alpha, beta).getValue()));

					chessBoard.returnToLastMove();
				}
			}
		}
		Collections.sort(possibleMoves);
		return possibleMoves.get(possibleMoves.size() - 1);
	}
	
	private ChessMove minimizer(ChessBoard chessBoard, int depth, int alpha, int beta) {

		List<ChessMove> possibleMoves = new ArrayList<ChessMove>();

		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField())
				.toArray(Field[]::new);

		if (depth == 0) {
			for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					possibleMoves.add(new ChessMove(fromField, toField, this.evaluatePosition(chessBoard)));
					chessBoard.returnToLastMove();
				}
			}
		} else {
				for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick(); // mouse click on chess piece's field
					toField.onClick(); // Move chosen ChessPiece to a field
					possibleMoves.add(new ChessMove(fromField, toField,
							this.maximizer(chessBoard, depth - 1, alpha, beta).getValue()));

					chessBoard.returnToLastMove();
				}
			}
		}
		Collections.sort(possibleMoves);
		return possibleMoves.get(0);
	}

}
