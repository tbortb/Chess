package Engines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import Model.ChessMove;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class AlphaBetaPruneMultiThread extends ChessEngine {
	// alpha = minimum value that a maximizing player is guaranteed to get
	// beta = maximum value that a minimizing player is guaranteed to get

	public AlphaBetaPruneMultiThread(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}

	public AlphaBetaPruneMultiThread(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public ChessMove computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<ChessMove>> possibleMoveFutures = new ArrayList<>();

		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField()).toArray(Field[]::new);

		for (Field fromField : fromFields) {
			ChessBoard copiedBoard = new ChessBoard(chessBoard.getWhitePieces(), chessBoard.getBlackPieces(),
					chessBoard.isWhiteTurn());
			Field[] fromFieldOnNewBoard = { copiedBoard.getFields()[fromField.getCol()][fromField.getRow()] };
			
			possibleMoveFutures.add(executor.submit(() -> {
				ChessMove bestMove = this.alphaBeta(copiedBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE,
						fromFieldOnNewBoard);
				if (bestMove != null) {
					return new ChessMove(fromField,
							chessBoard.getFields()[bestMove.getTo().getCol()][bestMove.getTo().getRow()],
							bestMove.getValue());					
				}else {
					return null;
				}
			}));
		}

		//Without futures, sequentially in a loop, no multithreading
//		List<ChessMove> possibleMoves = new ArrayList<>();
//		for (Field fromField : fromFields) {
//			ChessBoard copiedBoard = new ChessBoard(chessBoard.getWhitePieces(), chessBoard.getBlackPieces(),
//					chessBoard.isWhiteTurn());
//			Field[] fromFieldOnNewBoard = { copiedBoard.getFields()[fromField.getCol()][fromField.getRow()] };
//			ChessMove bestMove = this.alphaBeta(copiedBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE,
//					fromFieldOnNewBoard);
//			if (bestMove != null) {
//				possibleMoves.add(new ChessMove(fromField,
//						chessBoard.getFields()[bestMove.getTo().getCol()][bestMove.getTo().getRow()],
//						bestMove.getValue()));
//			}
//		}

		//With Futures, but in a loop, that fills possibleMoves
//		List<ChessMove> possibleMoves = new ArrayList<>();
//		for (Future<ChessMove> futureMove : possibleMoveFutures) {
//			try {
//				ChessMove newMove = futureMove.get();
//				if (newMove != null) {
//					possibleMoves.add(newMove);					
//				}
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		}
		
		//With futures in a stream
		List<ChessMove> possibleMoves = possibleMoveFutures.stream().map(futureMove -> {
			try {
				return futureMove.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				return null;
			}
		}).filter(move -> move != null).collect(Collectors.toList());

		executor.shutdown();
		Collections.sort(possibleMoves);
		System.out.println("ThisIsit:" + possibleMoves);
//		System.gc();
		return possibleMoves.size() == 0 ? null
				: chessBoard.isWhiteTurn() ? possibleMoves.get(possibleMoves.size() - 1) : possibleMoves.get(0);
	}

	private ChessMove alphaBeta(ChessBoard chessBoard, int depth, int alpha, int beta) {

		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField())
				.toArray(Field[]::new);

		return this.alphaBeta(chessBoard, depth, alpha, beta, fromFields);
	}

	private ChessMove alphaBeta(ChessBoard chessBoard, int depth, int alpha, int beta, Field[] fromFields) {

		List<ChessMove> possibleMoves = new ArrayList<ChessMove>(64);

//		System.out.println("depth" + depth + (chessBoard.isWhiteTurn() ? "White" : "Black") + "Pieces: "
//				+ chessBoard.getActivePlayerPieces());

		tryFields: for (Field fromField : fromFields) {
//			System.out.println("depth" + depth + "Piece: " + fromField);

//			System.out.println("depth" + depth + "Moves: " + fromField.getPiece().getLegalMoves());
			for (Field toField : fromField.getPiece().getLegalMoves()) {
//				System.out.println("depth" + depth + "From Field: " + fromField + "toField: " + toField);

				fromField.onClick(); // mouse click on chess piece's field
				toField.onClick(); // Move chosen ChessPiece to a field
				
//				System.out.println("LogSize: " + chessBoard.getLogSize() + " ,evalCalls: " + this.getEvaluatorCalls());

				int value; // Value of newly discovered move

				if (depth == 0) {
					possibleMoves.add(new ChessMove(fromField, toField, value = this.evaluatePosition(chessBoard)));
				} else {
					possibleMoves.add(new ChessMove(fromField, toField,
							value = this.alphaBeta(chessBoard, depth - 1, alpha, beta).getValue()));
				}

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
//		System.gc();
		return possibleMoves.size() == 0 ? null
				: chessBoard.isWhiteTurn() ? possibleMoves.get(possibleMoves.size() - 1) : possibleMoves.get(0);
	}

}
