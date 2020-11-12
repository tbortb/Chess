package Engines;

import java.util.concurrent.atomic.AtomicInteger;

import Evaluators.ChessBoardEvaluator;
import application.ChessBoard;
import Model.ChessMove;

public abstract class ChessEngine implements ChessBoardEvaluator {
	private ChessBoardEvaluator whiteEvaluator;
	private ChessBoardEvaluator blackEvaluator;
	protected volatile AtomicInteger evaluateCalls = new AtomicInteger(0);
	protected boolean useWhiteEval = true;//default to true, in case the subclass does not set it
	
	public ChessEngine(ChessBoardEvaluator evaluator) {
		this.whiteEvaluator = evaluator;
		this.blackEvaluator = evaluator;
	}
	public ChessEngine(ChessBoardEvaluator whiteEvaluator, ChessBoardEvaluator blackEvaluator) {
		this.whiteEvaluator = whiteEvaluator;
		this.blackEvaluator = blackEvaluator;
	}
	
	public abstract ChessMove computerMove(ChessBoard chessBoard, int deepth);
	
	@Override
	public int evaluatePosition(ChessBoard chessBoard) {
		this.evaluateCalls.getAndIncrement();
		return this.useWhiteEval ? this.whiteEvaluator.evaluatePosition(chessBoard) : this.blackEvaluator.evaluatePosition(chessBoard);
	}
	
	public AtomicInteger getEvaluatorCalls() {
		return this.evaluateCalls;
	}
}