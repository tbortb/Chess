package Pieces;

import java.util.Set;

import application.Field;
import javafx.scene.image.Image;

public class Knight extends ChessPiece {
	private static Image whiteImg = new Image(Knight.class.getResourceAsStream("../res/ChessPieces/WhiteHorse.png"));
	private static Image blackImg = new Image(Knight.class.getResourceAsStream("../res/ChessPieces/BlackHorse.png"));
	
	public Knight(boolean isWhite) {
		super(isWhite, 3);
	}
	
	public Knight(Knight other) {//CopyConstructor
		super(other);
	}
	
	@Override
	public Image getImage() {
		return this.isWhite ? whiteImg : blackImg;
	}

	@Override
	public Set<Field> getLegalMoves() {
		Set<Field> legalMoves = this.checkLinearMoves(2, 1, 1);
		legalMoves.addAll(this.checkLinearMoves(2, -1, 1));
		legalMoves.addAll(this.checkLinearMoves(1, 2, 1));
		legalMoves.addAll(this.checkLinearMoves(1, -2, 1));
		legalMoves.addAll(this.checkLinearMoves(-2, 1, 1));
		legalMoves.addAll(this.checkLinearMoves(-2, -1, 1));
		legalMoves.addAll(this.checkLinearMoves(-1, 2, 1));
		legalMoves.addAll(this.checkLinearMoves(-1, -2, 1));
		
		return legalMoves;
	}

}
