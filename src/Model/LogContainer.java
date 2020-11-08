package Model;

import Pieces.ChessPiece;

public class LogContainer {
	private int col;
	private int row;
	private ChessPiece piece;
	
	public LogContainer(int col, int row, ChessPiece piece) {
		this.col = col;
		this.row = row;
		this.piece = piece;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	public ChessPiece getPiece() {
		return piece;
	}

}
