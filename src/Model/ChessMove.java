package Model;

import java.util.List;

import application.Field;

public class ChessMove implements Comparable<ChessMove> {
	private Field from;
	private Field to;
	private int value;
	private List<ChessMove> tree;

	public ChessMove(Field from, Field to, int value, List<ChessMove> principalVariation) {
		this(from, to, value);
		this.tree = principalVariation;
	}

	public ChessMove(Field from, Field to, int value) {
		this(from, to);
		this.value = value;
	}
	
	public ChessMove(Field from, Field to) {
		this.from = from;
		this.to = to;
	}

	public List<ChessMove> getTree() {
		return this.tree;
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
		StringBuilder sb = new StringBuilder("{\"fromField\":\"" + this.from.getCol() + "-" + this.from.getRow()
		+ "\", \"toField\":\"" + this.to.getCol() + "-" + this.to.getRow() + "\", \"value\":\"" 
				+ this.value + "\", \"principalVariation\":");
		if (this.tree != null) {
			sb.append(this.tree);
		} else {
			sb.append("\"NULL\"");
		}
		sb.append("}");
		return sb.toString();		
	}


	public String toShortString() {
		return "ChessMove [fromField=" + this.from.getCol() + "-" + this.from.getRow()
				+ ", toField=" + this.to.getCol() + "-" + this.to.getRow() + ", value=" + this.value + "]"; 
	}

}
