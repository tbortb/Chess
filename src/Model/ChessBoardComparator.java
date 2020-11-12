package Model;

import application.ChessBoard;
import application.Field;

public class ChessBoardComparator implements Comparable<ChessBoardComparator>{
	private ChessBoard board;
	private int value;
	private Field fromFieldBeforeBoardState;
	private Field toFieldBeforeBoardState;
	
	public ChessBoardComparator(ChessBoard board, int value, Field fromField, Field toField) {
		this(board, value);
		this.fromFieldBeforeBoardState = fromField;
		this.toFieldBeforeBoardState = toField;
	}
	
	public ChessBoardComparator(ChessBoard board, int value) {
		super();
		this.board = board;
		this.value = value;
	}

	public ChessBoard getBoard() {
		return board;
	}

	public void setBoard(ChessBoard board) {
		this.board = board;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Field getFromFieldBeforeBoardState() {
		return fromFieldBeforeBoardState;
	}

	public Field getToFieldBeforeBoardState() {
		return toFieldBeforeBoardState;
	}

	@Override
	public int compareTo(ChessBoardComparator otherBoard) {
		return this.value - otherBoard.getValue();
	}
}
