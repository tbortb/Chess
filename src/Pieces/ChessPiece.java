package Pieces;

import java.util.HashSet;
import java.util.Set;

import application.Field;
import javafx.scene.image.Image;

public abstract class ChessPiece {
	private final Integer id;
	protected final boolean isWhite;
	private final int value;
	protected boolean hasMoved = false;
	protected Field field;
	

	public ChessPiece(boolean isWhite, int value, Integer id) {
		this.isWhite = isWhite;
		this.value = value;
		this.id = id;
	}
	
	public ChessPiece(ChessPiece otherPiece) {//CopyConstructor
		this.isWhite = otherPiece.isWhite();
		this.value = otherPiece.getValue();
		this.hasMoved = otherPiece.hasMoved();
		this.field = otherPiece.getField();
		this.id = otherPiece.getId();
	}
	
	public abstract Set<Field> calcLegalMoves();	

	public Set<Field> getLegalMoves(){
		Set<Field> legalMoves;
		if((legalMoves = this.field.getBoard().getLegalMoveLog(this.id)) != null) {
			return legalMoves;
		}else {
			legalMoves = this.calcLegalMoves();
			//Cache LegalMoves
			this.field.getBoard().addLegalMove(this.id, legalMoves);
			return legalMoves;
		}
//		return legalMoves;
	}

	public boolean isLegalMove(Field destinationFiled) {
		return this.getLegalMoves().contains(destinationFiled);		
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public Field getField() {
		return this.field;
	}
	
	public void setField(Field field) {
		this.field = field;
	}
	
	public abstract Image getImage();
	
	public boolean hasMoved() {
		return this.hasMoved;
	}
	
	public void setMoved() {
		this.hasMoved = true;
	}
	
	public boolean isWhite() {
		return this.isWhite;
	}
	
	protected Set<Field> checkLinearMoves(int colIncrease, int rowIncrease, int maxIncrements){
		Set<Field> possibleFields= new HashSet<>();
		
		int colOffset = colIncrease;
		int rowOffset = rowIncrease;
		int iterationNumber = 0;
		Field testField;
		iterations:
		while (true) {
			try {
				testField = this.field.getBoard().getFields()[this.field.getCol() + colOffset][this.field.getRow() + rowOffset];
			} catch (ArrayIndexOutOfBoundsException s) {
				// This happens when the checked Field is not on the board anymore
				break iterations;
			}

			// Break if there is a piece in the way
			if (testField.getPiece() != null) {
				if (testField.getPiece().isWhite() == this.isWhite()) {
					//This is to check if a piece on the testField is covered
					if(testField.getPiece().isWhite() != this.field.getBoard().isWhiteTurn()) {
						possibleFields.add(testField);						
					}
					break iterations;
				} else {
					// else (it is a piece of the other player) add field to list, then break
					possibleFields.add(testField);
					break iterations;
				}
			}
			// Else it is a legal move
			possibleFields.add(testField);
			colOffset += colIncrease;
			rowOffset += rowIncrease;
			if(++iterationNumber >= maxIncrements) {
				break iterations;
			}
		}
		
		return possibleFields;
	}
	
	protected Set<Field> checkLinearMoves(int colIncrease, int rowIncrease){
		return this.checkLinearMoves(colIncrease, rowIncrease, 8);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
		sb.append(": isWhite: ");
		sb.append(this.isWhite);
		if(this.field != null) {
			sb.append(" Col: ");
			sb.append(this.field.getCol());			
			sb.append(" Row: ");
			sb.append(this.field.getRow());
		}else {
			sb.append(" No Field");
		}
		return sb.toString();
	}
}
