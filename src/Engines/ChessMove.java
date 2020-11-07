package Engines;

import application.Field;

public class ChessMove implements Comparable<ChessMove>{
	private Field from;
	private Field to;
	private int value;
	
	public ChessMove(Field from, Field to, int value) {
		this.from = from;
		this.to = to;
		this.value = value;
	}

	public Field getFrom() {
		return this.from;
	}

	public Field getTo() {
		return this.to;
	}

	public int getValue() {
		return this.value;
	}
	
	public void setValue(int newValue) {
		this.value = newValue;
	}

	@Override
	public int compareTo(ChessMove otherMove) {
		return this.value - otherMove.getValue();
	}

	@Override
	public String toString() {
		return "ChessMove [fromField=" + this.from.getCol() + "-" + this.from.getRow() + ", toField=" +
				this.to.getCol() + "-" + this.to.getRow() + ", value=" + this.value + "]";
	}
	
	
	
}
