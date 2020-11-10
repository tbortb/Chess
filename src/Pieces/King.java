package Pieces;

import java.util.HashSet;
import java.util.Set;

import application.Field;
import javafx.scene.image.Image;

public class King extends ChessPiece {
	private static Image whiteImg = new Image(King.class.getResourceAsStream("../res/ChessPieces/WhiteKing.png"));
	private static Image blackImg = new Image(King.class.getResourceAsStream("../res/ChessPieces/BlackKing.png"));
	
	public King(boolean isWhite, Integer id) {
		super(isWhite, 100, id);
	}
	
	public King(King other) {//CopyConstructor
		super(other);
		this.field.getBoard().endGameProperty().set(false);
	}
	
	@Override
	public Image getImage() {
		return this.isWhite ? whiteImg : blackImg;
	}

	@Override
	public Set<Field> calcLegalMoves() {
		Set<Field> legalMoves = this.checkLinearMoves(1, 1, 1);
		legalMoves.addAll(this.checkLinearMoves(1, -1, 1));
		legalMoves.addAll(this.checkLinearMoves(-1, 1, 1));
		legalMoves.addAll(this.checkLinearMoves(-1, -1, 1));
		legalMoves.addAll(this.checkLinearMoves(-1, 0, 1));
		legalMoves.addAll(this.checkLinearMoves(0, -1, 1));
		legalMoves.addAll(this.checkLinearMoves(1, 0, 1));
		legalMoves.addAll(this.checkLinearMoves(0, 1, 1));

		// Remove any fields if king would be checkmate
		Set<Field> oponentPossibleMoves = new HashSet<>();
		//Only the active player`s King should be afraid to go into chess
		//Otherwise circular reference because both kings check all other pieces, including the other king...
		if(this.isWhite == this.field.getBoard().isWhiteTurn()) {
			for (ChessPiece oponentPiece : this.isWhite ? this.field.getBoard().getBlackPieces() : this.field.getBoard().getWhitePieces()) {
				oponentPossibleMoves.addAll(oponentPiece.getLegalMoves());
			}			
			legalMoves.removeAll(oponentPossibleMoves);
		
			// Add rochade
			if (!this.hasMoved) {
				this.getPossibleRochadeField(legalMoves, oponentPossibleMoves, this.field.getBoard().getFields()[7][this.field.getRow()]);
				this.getPossibleRochadeField(legalMoves, oponentPossibleMoves, this.field.getBoard().getFields()[0][this.field.getRow()]);
			}
		}

		return legalMoves;
	}

	private void getPossibleRochadeField(Set<Field> legalMoves, Set<Field> oponentPossibleMoves, Field rookField) {
		if ((rookField.getPiece() instanceof Rook) && !rookField.getPiece().hasMoved() && rookField.getPiece().isWhite == this.isWhite) {
			for (int col = rookField.getCol() == 0 ? 0 : 4; col <= (rookField.getCol() == 0 ? 4 : 7); col++) {
				if (oponentPossibleMoves.contains(this.field.getBoard().getFields()[col][rookField.getRow()])){
					return;
				}
				if(col != 0 && col != 4 && col != 7 && this.field.getBoard().getFields()[col][rookField.getRow()].getPiece() != null) {
					return;
				}
			}
			legalMoves.add(this.field.getBoard().getFields()[rookField.getCol() == 0 ? 2 : 6][rookField.getRow()]);
		}
	}

}
