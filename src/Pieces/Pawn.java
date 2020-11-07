package Pieces;

import java.util.HashSet;
import java.util.Set;

import application.Field;
import javafx.scene.image.Image;

public class Pawn extends ChessPiece {
	private static Image whiteImg = new Image(Pawn.class.getResourceAsStream("../res/ChessPieces/WhitePawn.png"));
	private static Image blackImg = new Image(Pawn.class.getResourceAsStream("../res/ChessPieces/BlackPawn.png"));
	
	private int enPassantMove = -3;
	private Field enPassantField;

	public Pawn(boolean isWhite) {
		super(isWhite, 1);
	}
	
	public Pawn(Pawn other) {//CopyConstructor
		super(other);
		this.enPassantField = other.getEnPassantField();
		this.enPassantMove = other.getEnPassantMove();
	}
	
	@Override
	public Image getImage() {
		return this.isWhite ? whiteImg : blackImg;
	}

	@Override
	public Set<Field> getLegalMoves() {
		Set<Field> legalMoves = new HashSet<>();
		final int increase = this.isWhite ? -1 : 1;
		final int col = this.field.getCol();
		final int row = this.field.getRow();

		//Check for straight move
		// We do not need to check whether the pawn can fall off the chess field with
		// straight moves, because this can not happen
		// If it is on the ground line it is not a pawn anymore
		
		//Only check staight move if it is the active player's turn
		if (this.isWhite == this.field.getBoard().isWhiteTurn()) {
			if (this.field.getBoard().getFields()[col][row + increase].getPiece() == null) {
				legalMoves.add(this.field.getBoard().getFields()[col][row + increase]);
				if (!this.hasMoved && this.field.getBoard().getFields()[col][row + 2 * increase].getPiece() == null) {
					legalMoves.add(this.field.getBoard().getFields()[col][row + 2 * increase]);
				}
			}
		}

		//Check for diagonal take
		Field testField;
		for (int i = -1; i <= 1; i += 2) {
			try {
				testField = this.field.getBoard().getFields()[col + i][row + increase];
				//Add diagonal fields, if they exist. The diagonal fields are added when the piece is of another color
				//or, if this is not the active player's piece, because it could hit a piece of the opponent that wants to move there
				if ((testField.getPiece() != null && testField.getPiece().isWhite != this.isWhite)
						|| this.isWhite != this.field.getBoard().isWhiteTurn()) {
					legalMoves.add(testField);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
		}
		
		//Check for en passant
		if(this.enPassantField != null && this.enPassantMove == this.field.getBoard().getLogSize() - 1 &&
			//Last check is important in case there is no Pawn available(can happen when enpassant-enabeling move is taken back)
			this.getField().getBoard().getFields()[this.enPassantField.getCol()][this.getField().getRow()].getPiece() instanceof Pawn) {
			legalMoves.add(this.enPassantField);
			
			//Debugging
//			System.err.println("Added EnPassant to legalmoves");
			
		}

		return legalMoves;
	}

	public int getEnPassantMove() {
		return this.enPassantMove;
	}

	public void setEnPassantMove(int enPassantMove) {
		this.enPassantMove = enPassantMove;
	}

	public Field getEnPassantField() {
		return this.enPassantField;
	}

	public void setEnPassantField(Field enPassantField) {
		this.enPassantField = enPassantField;
	}
}
