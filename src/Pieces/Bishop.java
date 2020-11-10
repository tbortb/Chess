package Pieces;


import java.util.Set;

import application.Field;
import javafx.scene.image.Image;

public class Bishop extends ChessPiece {
	private static Image whiteImg = new Image(Bishop.class.getResourceAsStream("../res/ChessPieces/WhiteBishop.png"));
	private static Image blackImg = new Image(Bishop.class.getResourceAsStream("../res/ChessPieces/BlackBishop.png"));
	
	public Bishop(boolean isWhite, Integer id) {
		super(isWhite, 3, id);
	}
	
	public Bishop(Bishop other) {//CopyConstructor
		super(other);
	}
	
	@Override
	public Image getImage() {
		return this.isWhite ? whiteImg : blackImg;
	}

	@Override
	public Set<Field> calcLegalMoves() {
		Set<Field> legalMoves = this.checkLinearMoves(1, 1);
		legalMoves.addAll(this.checkLinearMoves(1, -1));
		legalMoves.addAll(this.checkLinearMoves(-1, 1));
		legalMoves.addAll(this.checkLinearMoves(-1, -1));
		
		return legalMoves;
	}

}