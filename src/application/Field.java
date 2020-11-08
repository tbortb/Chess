package application;

import Pieces.ChessPiece;
import Pieces.King;
import Pieces.Pawn;
import Pieces.Queen;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Field extends Button {
	private static final float SQUARE_LENGTH = 80;
	private static final String BLACK_COLOR = "-fx-background-color: #999999;";
	private static final String WHITE_COLOR = "-fx-background-color: #ffffff;";
	private static final String HIGHLIGHT_COLOR = "-fx-background-color: #555500;";

	private final int col;
	private final int row;
	private final boolean isWhite;
	private final ChessBoard board;

	private ChessPiece piece;

	public Field(Field otherField) { // CopyConstructor
		this(otherField.getBoard(), otherField.getCol(), otherField.getRow(), otherField.isWhite());
	}

	public Field(ChessBoard board, int col, int row, boolean isWhite, ChessPiece piece) {
		this(board, col, row, isWhite);
		this.setPiece(piece);
	}

	public Field(ChessBoard board, int col, int row, boolean isWhite) {
		super();
		this.row = row;
		this.col = col;
		this.isWhite = isWhite;
		this.board = board;

		this.unHighlightField();
		this.setPrefHeight(SQUARE_LENGTH);
		this.setMaxHeight(SQUARE_LENGTH);
		this.setMinHeight(SQUARE_LENGTH);
		this.setPrefWidth(SQUARE_LENGTH);
		this.setMaxWidth(SQUARE_LENGTH);
		this.setMinWidth(SQUARE_LENGTH);
		this.setAlignment(Pos.BASELINE_CENTER);

		ImageView imageView = new ImageView();
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(SQUARE_LENGTH / 1.5);
		imageView.setFitWidth(SQUARE_LENGTH / 1.5);
		this.setGraphic(imageView);

		this.setOnAction(event -> {
			this.board.setPlayerGame(true);
			this.onClick();
			this.board.setPlayerGame(false);
		});
	}

	public void onClick() {
		if (this.board.getSelectedField() == null) {
			// If this field does not have a piece, or is not from active player, do nothing
			if (this.getPiece() != null) {
				if (this.getPiece().isWhite() == this.board.isWhiteTurn()) {
					this.board.selectField(this);
				}
			}
		} else if (this.board.getSelectedField() == this) {
			this.board.unselectField(this);
		} else if (this.board.getSelectedField().getPiece().isLegalMove(this)) {
			this.board.writeCurrentMove(this.board.getSelectedField());
			this.board.writeCurrentMove(this);
			this.setPiece(this.board.getSelectedField().getPiece());
			this.doSpecialMoves(); // Castle, EnPassant, PawnConversion
			this.getPiece().setMoved(); // Has to be done after doSpecialMove()
			// Folgende Zeile unhighlighted leider nicht alle Gehighlighteten Felder
//				this.board.getSelectedField().getPiece().getLegalMoves().forEach(legalField -> legalField.unHighlightField());
//				this.board.getSelectedField().unHighlightField();
			this.board.getSelectedField().setPiece(null);

			// Warum muss dieses Feld unselected werden? Das selected Field ist doch das mit
			// dem ersten klick
			this.board.unselectField(this);
			this.board.currentMoveToLog();

			this.board.switchPlayer();
		} else {
			System.err.println("Piece can not move from: " + this.getBoard().getSelectedField().toString() + " to "
					+ this.toString());
//			throw new RuntimeException("Piece can not move from: " + this.getBoard().getSelectedField().toString() + " to " + this.toString());
		}
	}

	private void doSpecialMoves() {
		if (this.getPiece() instanceof King && this.board.getSelectedField().getCol() == 4) {
			if (this.getCol() == 6) {// Small Castle
				int row = this.getPiece().isWhite() ? 7 : 0;

				this.performRochade(this.board.getFields()[5][row], this.board.getFields()[7][row]);
				return;
			}
			if (this.getCol() == 2) {// Big Castle8
				int row = this.getPiece().isWhite() ? 7 : 0;

				this.performRochade(this.board.getFields()[3][row], this.board.getFields()[0][row]);
				return;
			}
		}
		if (this.getPiece() instanceof Pawn) {
			Pawn pawn = (Pawn) this.getPiece();
			if (this.getRow() == (this.board.isWhiteTurn() ? 4 : 3) && !pawn.hasMoved()) { // Set en passant move
				for (int offset = -1; offset <= 1; offset += 2) {
					if (this.getCol() + offset > 7 || this.getCol() + offset < 0) {
						continue; // Index would be out of bounds
					}
					ChessPiece otherPiece = this.board.getFields()[this.getCol() + offset][this.getRow()].getPiece();
					if (otherPiece != null && otherPiece instanceof Pawn && otherPiece.isWhite() != pawn.isWhite()) {
						this.board.writeCurrentMove(otherPiece.getField());
						((Pawn) otherPiece).setEnPassantField(
								this.board.getFields()[this.getCol()][this.board.isWhiteTurn() ? 5 : 2]);
						((Pawn) otherPiece).setEnPassantMove(this.board.getLogSize());
					}
				}
				return;
			}
			if (pawn.getEnPassantField() == this && pawn.getEnPassantMove() == this.board.getLogSize() - 1) {// It is en
																												// passant
				int killPawnCol = this.getCol();
				int killPawnRow = this.board.isWhiteTurn() ? 3 : 4;

				Field killPawnField = this.board.getFields()[killPawnCol][killPawnRow];
				if (!(killPawnField.getPiece() instanceof Pawn)) {

					// Debugging
					System.err.println("Tried enpassant, but there wars no pawn to hit");

					return;// There must be a pawn to kill with enpassant
				}
				this.board.writeCurrentMove(killPawnField);
				this.board.removeFromPlayerPiecesSet(this.board.getFields()[killPawnCol][killPawnRow].getPiece());
				this.board.getFields()[killPawnCol][killPawnRow].setPiece(null);
				return;
			}

			if (this.getRow() == (this.board.isWhiteTurn() ? 0 : 7)) {// Pawn on base line
				Queen newQueen = new Queen(this.board.isWhiteTurn());
				this.setPiece(newQueen);
				this.board.addToPlayerPiecesSet(newQueen);
				return;
			}
		}
	}

	private void performRochade(Field newRookField, Field oldRookField) {
		this.board.writeCurrentMove(oldRookField);
		this.board.writeCurrentMove(newRookField);
		newRookField.setPiece(oldRookField.getPiece());
		oldRookField.setPiece(null);
		return;
	}

	public ChessBoard getBoard() {
		return this.board;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}

	public boolean isWhite() {
		return this.isWhite;
	}

	public ChessPiece getPiece() {
		return this.piece;
	}

	public void setPiece(ChessPiece piece) {
		if (this.piece != null && piece != null) {
			this.board.removeFromPlayerPiecesSet(this.piece);
		}
		this.piece = piece;
		if (piece == null) {
			if (this.board.isPlayerGame()) {
				this.setImage(null);
			}
		} else {
			this.piece.setField(this);
			if (this.board.isPlayerGame()) {
				this.setImage(this.piece.getImage());
			}
		}
	}

	private void setImage(Image newImage) {
		((ImageView) this.graphicProperty().getValue()).setImage(newImage);
	}

	public void highlightField() {
		this.setStyle(HIGHLIGHT_COLOR);
	}

	public void unHighlightField() {
		this.setStyle(this.isWhite ? WHITE_COLOR : BLACK_COLOR);
	}

	@Override
	public String toString() {
		return "Field [col=" + this.col + ", row=" + this.row + ", piece=" + (this.piece == null ? "null"
				: this.piece.getClass().getSimpleName() + ", isWhite=" + this.piece.isWhite()) + "]";
	}
}
