package Engines;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import Model.ChessBoardComparator;
import Model.ChessMove;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class GlobalABThreadingLocalMoveOrder extends ChessEngine {
	// alpha = minimum value that a maximizing player is guaranteed to get
	private volatile int globalAlpha;
	// beta = maximum value that a minimizing player is guaranteed to get
	private volatile int globalBeta;

	public GlobalABThreadingLocalMoveOrder(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}

	public GlobalABThreadingLocalMoveOrder(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public List<ChessMove> computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();
		this.globalAlpha = Integer.MIN_VALUE;
		this.globalBeta = Integer.MAX_VALUE;

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Field[] fromFields = chessBoard.getActivePlayerPieces().stream().map(piece -> piece.getField())
				.toArray(Field[]::new);
		List<Future<ChessMove>> possibleMoveFutures = new ArrayList<>(64);
		List<ChessBoardComparator> possibleBoardStates = new ArrayList<>(64);

		// At first get and order all possible moves
		for (Field fromField : fromFields) {
			for (Field toField : fromField.getPiece().getLegalMoves()) {
				fromField.onClick();
				toField.onClick();
				possibleBoardStates.add(new ChessBoardComparator(
						new ChessBoard(chessBoard.getWhitePieces(), chessBoard.getBlackPieces(),
								chessBoard.isWhiteTurn()),
						this.evaluatePosition(chessBoard),
						fromField,
						toField));
				chessBoard.returnToLastMove();
			}
		}
		Collections.sort(possibleBoardStates);
		if (chessBoard.isWhiteTurn()) {
			Collections.reverse(possibleBoardStates);
		}//Now we have an ordered list of all possible chessMoves

		//Submit future with board states where the first move has already been taken
		for (ChessBoardComparator boardComparator : possibleBoardStates) {
			possibleMoveFutures.add(executor.submit(() -> {
				List<ChessMove> moves = this.alphaBeta(boardComparator.getBoard(),
						depth - 1,
						this.globalAlpha,
						this.globalBeta, chessBoard.isWhiteTurn());
				
				int value = moves.get(0).getValue();
				
				//set global alpha or beta
				if (chessBoard.isWhiteTurn()) {
					if (value > this.globalAlpha) {
						this.globalAlpha = value;
					}
				} else {
					if (value < this.globalBeta) {
						this.globalBeta = value;
					}
				}
				
				return new ChessMove(boardComparator.getFromFieldBeforeBoardState(),
						boardComparator.getToFieldBeforeBoardState(),
						moves.get(0).getValue());
			}));
		}


		// Now collect results from futures
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
		if(chessBoard.isWhiteTurn()) {
			Collections.reverse(possibleMoves);
		}
//		System.gc();
		return possibleMoves;
	}

	private List<ChessMove> alphaBeta(ChessBoard chessBoard, int depth, int alpha, int beta,
			boolean globalPlayerIsMaximizing) {

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

				int value = this.alphaBeta(chessBoard,
						depth - 1,
						alpha,
						beta,
						globalPlayerIsMaximizing).get(0).getValue();

				//Das könnte zu Schwierigkeiten führen, weil hier, wenn die Referenz aus possibleMoves einfach
				//einen anderen Wert bekommt, kann es bei Pruning von tryFields sein, dass der nicht 
				//bearbeitete Teil von Possible Moves einen Value aus der SortierPhase enthält, der Besser ist,
				//als die bisher tatsächlich rekursiv gefundenen Züge. Damit würde vielleicht ein zu guter Zug
				//an der Stelle possibleMoves.get(0) zuückgegeben!
				move.setValue(value);

				chessBoard.returnToLastMove();

				// Do the pruning (with stop criterion alpha > beta)
				if (chessBoard.isWhiteTurn()) {
					if (alpha < value) {
						alpha = value;
					}
				} else {
					if (beta > value) {
						beta = value;
					}
				}

				// Check if globalAlpha or globalBeta have been improved by another thread
				if (globalPlayerIsMaximizing && alpha < this.globalAlpha) {
					alpha = this.globalAlpha;
				} else if (!globalPlayerIsMaximizing && beta > this.globalBeta) {
					beta = this.globalBeta;
				}

				if (alpha > beta) {
					break tryFields;
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
