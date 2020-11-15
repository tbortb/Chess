package Engines;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Model.ChessMove;
import Model.LogContainer;
import Pieces.King;
import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import application.Field;

public class AlphaBetaPruneLocalMoveOrderMoveLog extends ChessEngine {
	// alpha = minimum value that a maximizing player is guaranteed to get
	// beta = maximum value that a minimizing player is guaranteed to get
	
	private int lastWhiteInvokation = -1;
	private List<ChessMove> lastWhiteTree;
	private int lastBlackInvokation = -1;
	private List<ChessMove> lastBlackTree;

	public AlphaBetaPruneLocalMoveOrderMoveLog(ChessBoardEvaluator evaluator) {
		super(evaluator);
	}

	public AlphaBetaPruneLocalMoveOrderMoveLog(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		super(whiteEvaluator, blackEvaluator);
	}

	@Override
	public List<ChessMove> computerMove(ChessBoard chessBoard, int depth) {
		this.evaluateCalls.set(0);
		this.useWhiteEval = chessBoard.isWhiteTurn();
		List<ChessMove> output;
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
		if(lastTree != null && (chessBoard.getLogSize() > lastInvokation) && (chessBoard.getLogSize() - lastInvokation) < depth) {
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
				for (ChessMove cm : lastTree) {
					if((cm.getFrom() == searchFromField) && (cm.getTo() == searchToField)) {
						lastTree = cm.getTree();
						break;
					}
				}
				//Important add logEntry back to the chessboardLog
				chessBoard.getLog().addLast(logEntry);
				//repeat these steps
			}
			
			//Now that we have identified the relevant game tree, we can call the alphabeta function with it
			output = this.alphaBetaWithLog(chessBoard,
					depth,
					Integer.MIN_VALUE,
					Integer.MAX_VALUE,
					lastTree);
		
		
		}else {
			output = this.alphaBeta(chessBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);			
		}
		

		//Flush JSON to file (costs approx 3 sec per invokation)
//		try(BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Thies\\Desktop\\Delete\\" 
//				 + String.valueOf(System.currentTimeMillis()) + ".json"))){
//			writer.append(output.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		if (chessBoard.isWhiteTurn()) {
			this.lastWhiteInvokation = chessBoard.getLogSize();
			this.lastWhiteTree = output;			
		}else {
			this.lastBlackInvokation = chessBoard.getLogSize();
			this.lastBlackTree = output;						
		}
		
		return output;
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

				move.setTree(this.alphaBeta(chessBoard, depth - 1, alpha, beta));
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
			}
		}
		Collections.sort(possibleMoves);
		if (chessBoard.isWhiteTurn()) {
			Collections.reverse(possibleMoves);
		}
		return possibleMoves;
	}
	
	private List<ChessMove> alphaBetaWithLog(ChessBoard chessBoard, int depth, int alpha,
			int beta, List<ChessMove> orderedCalcMoves) {
		if (orderedCalcMoves == null || orderedCalcMoves.get(0).getTree() == null) {
			//In case the ordered List is empty, use the standard function
			//This might happen due to error or this being a pruned branch in the lastTree
			//Also in case of the orderedCalcMoves being a leaf node of the previous invokation
			//(when .getTree()==null), call the standard function, because leaf nodes can be pruned
			//and thus the list might not be complete
			return this.alphaBeta(chessBoard, depth, alpha, beta);
		} else {
			//Find new values and trees for the orderedCalcMoves
			//For this to work, it is important, that the list is complete

			tryFields: for (ChessMove move : orderedCalcMoves) {
				move.getFrom().onClick(); // mouse click on chess piece's field
				move.getTo().onClick(); // Move chosen ChessPiece to a field

				//This is never at depth 0, because the lastTree can not be bigger then the current searchdepth
				move.setTree(this.alphaBetaWithLog(chessBoard, depth - 1, alpha, beta, move.getTree()));
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
			}
		}
		Collections.sort(orderedCalcMoves);
		if (chessBoard.isWhiteTurn()) {
			Collections.reverse(orderedCalcMoves);
		}
		return orderedCalcMoves;
	}
}