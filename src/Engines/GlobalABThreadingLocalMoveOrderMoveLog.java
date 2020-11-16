package Engines;
/**
 * Probably one of the problems is that the field that is used for the onClick method can be a field
 * on a copied board, not on the original one
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
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
import Model.LogContainer;
import Pieces.King;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class GlobalABThreadingLocalMoveOrderMoveLog extends ChessEngine {
	// alpha = minimum value that a maximizing player is guaranteed to get
	private volatile int globalAlpha;
	// beta = maximum value that a minimizing player is guaranteed to get
	private volatile int globalBeta;
	
	private int lastWhiteInvokation = -1;
	private List<ChessMove> lastWhiteTree;
	private int lastBlackInvokation = -1;
	private List<ChessMove> lastBlackTree;

	public GlobalABThreadingLocalMoveOrderMoveLog(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}

	public GlobalABThreadingLocalMoveOrderMoveLog(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public List<ChessMove> computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();
		this.globalAlpha = Integer.MIN_VALUE;
		this.globalBeta = Integer.MAX_VALUE;
		
		
		
		List<ChessMove> possibleMoves;
		int lastInvokation;
		List<ChessMove> lastTree;
		
		//Set last invokation and lastTree to the correct variables
		 if(this.whiteEvaluator.getClass().equals(this.blackEvaluator.getClass())){
			 //Game where black and white engine are the same
			 if (this.lastWhiteInvokation > this.lastBlackInvokation) {
				 lastInvokation = this.lastWhiteInvokation;
				 lastTree = this.lastWhiteTree;
			 }else {
				 lastInvokation = this.lastBlackInvokation;
				 lastTree = this.lastBlackTree;				 
			 }
		 }else {
			 if(chessBoard.isWhiteTurn()) {
				 lastInvokation = this.lastWhiteInvokation;
				 lastTree = this.lastWhiteTree;				 
			 }else {
				 lastInvokation = this.lastBlackInvokation;
				 lastTree = this.lastBlackTree;				 				 
			 }
		 }
		
		//Here search for the last chessboard log entries that happened since the last computer move
		ArrayDeque<List<LogContainer>> intermediateLog = new ArrayDeque<>();
		if(lastTree != null &&
				(chessBoard.getLogSize() > lastInvokation) &&
				(chessBoard.getLogSize() - lastInvokation) < (depth - 1)) {

			
			while(chessBoard.getLogSize() > lastInvokation) {
				//Because we are removing the last elements from the chessboard log, we need to add them later on!
				intermediateLog.addLast(chessBoard.getLog().pollLast());
			}
			
			//In the right log entry search for the fromField and toField
				//Maybe there is a specific order in which the log entries are prepared. In that case this part 
				//can be replaced by a section calling the coresponding indexes (first or last)
			List<LogContainer> logEntry;
			Field searchFromField = null;
			Field searchToField = null;
			while((logEntry = intermediateLog.pollLast()) != null) {
				boolean logEntryWasWhiteTurn = (intermediateLog.size() % 2 == 0) ? !chessBoard.isWhiteTurn() : chessBoard.isWhiteTurn();
				if (logEntry.size() == 2) {
					//It is a normal move
					if (logEntry.get(0).getPiece() != null && logEntry.get(0).getPiece().isWhite() == logEntryWasWhiteTurn){
						searchFromField = chessBoard.getFields()[logEntry.get(0).getCol()][logEntry.get(0).getRow()];
						searchToField = chessBoard.getFields()[logEntry.get(1).getCol()][logEntry.get(1).getRow()];
					}else {
						searchFromField = chessBoard.getFields()[logEntry.get(1).getCol()][logEntry.get(1).getRow()];
						searchToField = chessBoard.getFields()[logEntry.get(0).getCol()][logEntry.get(0).getRow()];						
					}
					
				}else if(logEntry.size() == 3) {
					//It is an enpassant move
					for (LogContainer logEntryComponent : logEntry) {
						if (logEntryComponent.getPiece() != null && logEntryComponent.getPiece().isWhite() == logEntryWasWhiteTurn) {
							searchFromField = chessBoard.getFields()[logEntryComponent.getCol()][logEntryComponent.getRow()];
						}else if(logEntryComponent.getPiece() == null) {
							searchToField = chessBoard.getFields()[logEntryComponent.getCol()][logEntryComponent.getRow()];
						}
					}
				}else if (logEntry.size() == 4){
					//It is a castle move
					for (LogContainer logEntryComponent : logEntry) {
						//Search for King field
						if(logEntryComponent.getPiece() != null && logEntryComponent.getPiece() instanceof King) {
							searchFromField = chessBoard.getFields()[logEntryComponent.getCol()][logEntryComponent.getRow()];
							break;
						}
					}
					for (LogContainer logEntryComponent : logEntry) {
						//Search for castle field
						if (logEntryComponent.getCol() == 2 || logEntryComponent.getCol() == 6) {
							searchToField = chessBoard.getFields()[logEntryComponent.getCol()][logEntryComponent.getRow()];							
							break;
						}
					}
				}
				
				//set lastTree to the relevant part of the previous tree
				//Last tree has to exist, if it does not, it is an error in programming
				for (ChessMove cm : lastTree) {
					if((cm.getFrom().hasSamePositionAs(searchFromField)) &&
							(cm.getTo().hasSamePositionAs(searchToField))) {
						lastTree = cm.getTree();
						break;
					}
				}
				//Important add logEntry back to the chessboardLog
				chessBoard.getLog().addLast(logEntry);
				//repeat these steps
			}
			
			
			//Now that we have identified the relevant game tree, we can call the alphabeta function with it
			//Using multithreading
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			List<Future<ChessMove>> possibleMoveFutures = new ArrayList<>(64);		

			for (ChessMove playMove : lastTree) {
				//Ensure that the returned fields are on the original ChessBoard
				playMove.setFrom(chessBoard.getFields()[playMove.getFrom().getCol()][playMove.getFrom().getRow()]);
				playMove.setTo(chessBoard.getFields()[playMove.getTo().getCol()][playMove.getTo().getRow()]);
				
				playMove.getFrom().onClick();
				playMove.getTo().onClick();
				
				ChessBoard copyBoard = new ChessBoard(chessBoard.getWhitePieces(),
						chessBoard.getBlackPieces(),
						chessBoard.isWhiteTurn());
				
				chessBoard.returnToLastMove();
				
				possibleMoveFutures.add(executor.submit(() -> {
					List<ChessMove> moves = this.alphaBetaWithLog(copyBoard,
							depth - 1,
							this.globalAlpha,
							this.globalBeta,
							chessBoard.isWhiteTurn(),
							playMove.getTree());
					//It should not be a problem, to operate on the original tree, because all 
					//threads work on separate branches		
					
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
					
					playMove.setValue(value);
					playMove.setTree(moves);
					
					return playMove;
				}));
			}
			
			// Now collect results from futures
			possibleMoves = possibleMoveFutures.stream().map(futureMove -> {
				try {
					return futureMove.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					return null;
				}
			}).filter(move -> move != null).collect(Collectors.toList());

			executor.shutdown();
			
		
			Collections.sort(possibleMoves);
			//Check if the reordering is really necessary, or does any harm
			if(chessBoard.isWhiteTurn()) {
				Collections.reverse(possibleMoves);
			}
		
		}else {
			//Fall back to the GlobalABThreadingLocalMoveOrder engine, becuase it is the same without moveLog
			ChessEngine alternativeEngine = new GlobalABThreadingLocalMoveOrder(this.whiteEvaluator, this.blackEvaluator);
			possibleMoves = alternativeEngine.computerMove(chessBoard, depth);
		}
		

		//Debugging: Flush JSON to file (costs approx 3 sec per invokation)
//		try(BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Thies\\Desktop\\Delete\\" 
//				 + String.valueOf(System.currentTimeMillis()) + ".json"))){
//			writer.append(possibleMoves.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		//Save the resulting tree for next invokation
		if (chessBoard.isWhiteTurn()) {
			this.lastWhiteInvokation = chessBoard.getLogSize();
			this.lastWhiteTree = possibleMoves;			
		}else {
			this.lastBlackInvokation = chessBoard.getLogSize();
			this.lastBlackTree = possibleMoves;						
		}

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
					//Important, here the depth=0 level can prune the tree
					//so it is not a reliable source for future usage of 
					//the tree, because it might not be complete
					if (chessBoard.isWhiteTurn()) {
						if (value > beta) {
							break tryFields;
						}
					} else {
						if (alpha > value) {
							break tryFields;
						}
					}
					
					//I believe that globalAlpha and globalBeta are not
					//used on this level, because this if block would be invoked
					//very often. And I wnated to save that time, because when this
					//method returns to a higher level than 0, alpha or beta
					//is updated anyway
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

				move.setTree(this.alphaBeta(chessBoard, depth - 1, alpha, beta, globalPlayerIsMaximizing));
				int value = move.getTree().get(0).getValue();
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
				// Check if globalAlpha or globalBeta have been improved by another thread
				if (globalPlayerIsMaximizing && alpha < this.globalAlpha) {
					alpha = this.globalAlpha;
					if (alpha > beta) {
						break tryFields;
					}
				} else if (!globalPlayerIsMaximizing && beta > this.globalBeta) {
					beta = this.globalBeta;
					if (alpha > beta) {
						break tryFields;
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
	
	private List<ChessMove> alphaBetaWithLog(ChessBoard chessBoard, int depth, int alpha,
			int beta, boolean globalPlayerIsMaximizing, List<ChessMove> orderedCalcMoves) {
		if (orderedCalcMoves == null || orderedCalcMoves.get(0).getTree() == null) {
			//In case the ordered List is empty, use the standard function
			//This might happen due to error or this being a pruned branch in the lastTree
			//Also in case of the orderedCalcMoves being a leaf node of the previous invokation
			//(when .getTree()==null), call the standard function, because leaf nodes can be pruned
			//and thus the list might not be complete
			return this.alphaBeta(chessBoard, depth, alpha, beta, globalPlayerIsMaximizing);
		} else {
			//Find new values and trees for the orderedCalcMoves
			//For this to work, it is important, that the list of orderedCalcMoves is complete

			tryFields: for (ChessMove move : orderedCalcMoves) {
				//Ensure that the moves are done on the right chessboard
				chessBoard.getFields()[move.getFrom().getCol()][move.getFrom().getRow()].onClick();
				chessBoard.getFields()[move.getTo().getCol()][move.getTo().getRow()].onClick();
				
				//This is never at depth 0, because the lastTree can not be bigger then the current searchdepth
				move.setTree(this.alphaBetaWithLog(chessBoard, depth - 1, alpha, beta, globalPlayerIsMaximizing, move.getTree()));
				int value = move.getTree().get(0).getValue();
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
				// Check if globalAlpha or globalBeta have been improved by another thread
				if (globalPlayerIsMaximizing && alpha < this.globalAlpha) {
					alpha = this.globalAlpha;
					if (alpha > beta) {
						break tryFields;
					}
				} else if (!globalPlayerIsMaximizing && beta > this.globalBeta) {
					beta = this.globalBeta;
					if (alpha > beta) {
						break tryFields;
					}
				}
			}
		}
		Collections.sort(orderedCalcMoves);
		if (chessBoard.isWhiteTurn()) {
			Collections.reverse(orderedCalcMoves);
		}
		return orderedCalcMoves;
	}
}