package Pieces;

import java.util.Set;

import application.Field;
import javafx.scene.image.Image;

public class Queen extends ChessPiece {
	private static Image whiteImg = new Image(Queen.class.getResourceAsStream("../res/ChessPieces/WhiteQueen.png"));
	private static Image blackImg = new Image(Queen.class.getResourceAsStream("../res/ChessPieces/BlackQueen.png"));
	
	public Queen(boolean isWhite) {
		super(isWhite, 10);
	}
	
	public Queen(Queen other) {//CopyConstructor
		super(other);
	}
	
	@Override
	public Image getImage() {
		return this.isWhite ? whiteImg : blackImg;
	}

	@Override
	public Set<Field> getLegalMoves() {
		Set<Field> legalMoves = this.checkLinearMoves(1, 1);
		legalMoves.addAll(this.checkLinearMoves(1, -1));
		legalMoves.addAll(this.checkLinearMoves(-1, 1));
		legalMoves.addAll(this.checkLinearMoves(-1, -1));
		legalMoves.addAll(this.checkLinearMoves(-1, 0));
		legalMoves.addAll(this.checkLinearMoves(0, -1));
		legalMoves.addAll(this.checkLinearMoves(1, 0));
		legalMoves.addAll(this.checkLinearMoves(0, 1));
		
		return legalMoves;
	}

}
