package Pieces;

import java.util.Set;

import application.Field;
import javafx.scene.image.Image;

public class Rook extends ChessPiece {
	private static Image whiteImg = new Image(Rook.class.getResourceAsStream("../res/ChessPieces/WhiteRook.png"));
	private static Image blackImg = new Image(Rook.class.getResourceAsStream("../res/ChessPieces/BlackRook.png"));
	
	public Rook(boolean isWhite) {
		super(isWhite, 5);
	}
	
	public Rook(Rook other) {//CopyConstructor
		super(other);
	}
	
	@Override
	public Image getImage() {
		return this.isWhite ? whiteImg : blackImg;
	}

	@Override
	public Set<Field> getLegalMoves() {
		Set<Field> legalMoves = this.checkLinearMoves(1, 0);
		legalMoves.addAll(this.checkLinearMoves(-1, 0));
		legalMoves.addAll(this.checkLinearMoves(0, 1));
		legalMoves.addAll(this.checkLinearMoves(0, -1));
		
		return legalMoves;
	}

}