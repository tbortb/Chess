package Engines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Model.ChessMove;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class AlphaBetaPruneLocalMoveOrder extends ChessEngine {
	// alpha = minimum value that a maximizing player is guaranteed to get
	// beta = maximum value that a minimizing player is guaranteed to get

	public AlphaBetaPruneLocalMoveOrder(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}

	public AlphaBetaPruneLocalMoveOrder(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public ChessMove computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();
		return this.alphaBeta(chessBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE).get(0);
	}

	private List<ChessMove> alphaBeta(ChessBoard chessBoard, int depth, int alpha, int beta) {
		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField())
				.toArray(Field[]::new);
		List<ChessMove> possibleMoves = new ArrayList<>(64);
		if (depth == 0) {
			tryFields: for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick();
					toField.onClick();
					int value = this.evaluatePosition(chessBoard);
					possibleMoves.add(new ChessMove(fromField, toField, value));
					chessBoard.returnToLastMove();

					// Do the pruning (with stop criterion alpha > beta)
					if (chessBoard.isWhiteTurn()) {
						if (value > beta) {
							break tryFields;
						}
					} else {
						if (alpha > value) {
							break tryFields;
						}
					}
				}
			}
		} else {
			// At first order all possible moves
			for (Field fromField : fromFields) {
				for (Field toField : fromField.getPiece().getLegalMoves()) {
					fromField.onClick();
					toField.onClick();
					possibleMoves.add(new ChessMove(fromField, toField, this.evaluatePosition(chessBoard)));
					chessBoard.returnToLastMove();
				}
			}
			Collections.sort(possibleMoves);
			if (chessBoard.isWhiteTurn()) {
				Collections.reverse(possibleMoves);
			}

			tryFields: for (ChessMove move : possibleMoves) {
				move.getFrom().onClick(); // mouse click on chess piece's field
				move.getTo().onClick(); // Move chosen ChessPiece to a field

				int value = this.alphaBeta(chessBoard, depth - 1, alpha, beta).get(0).getValue();

				move.setValue(value);

				chessBoard.returnToLastMove();

				// Do the pruning (with stop criterion alpha > beta)
				if (chessBoard.isWhiteTurn()) {
					if (alpha < value) {
						alpha = value;
						if (alpha > beta) {
							break tryFields;
						}
					}
				} else {
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
		if (chessBoard.isWhiteTurn()) {
			Collections.reverse(possibleMoves);
		}
		return possibleMoves;
	}

}
