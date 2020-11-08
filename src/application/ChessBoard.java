package application;

/*Ideas to make it faster:
 * - Try to implement Feldmann's Young Brothers Wait Concept https://www.chessprogramming.org/Parallel_Search
 * -Cache results of getLegalMoves
 * -Have specific order when checking possible moves, so that best moves come first, and alphabetapruning is applied more often*/

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Model.LogContainer;
import Pieces.Bishop;
import Pieces.ChessPiece;
import Pieces.King;
import Pieces.Knight;
import Pieces.Pawn;
import Pieces.Queen;
import Pieces.Rook;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ChessBoard {

	private final ArrayDeque<List<LogContainer>> log = new ArrayDeque<>();
	private final Field[][] fields = new Field[8][8];
	private final Set<ChessPiece> whitePieces = new HashSet<>();
	private final Set<ChessPiece> blackPieces = new HashSet<>();
	private BooleanProperty gameEndedProperty = new SimpleBooleanProperty(false);

	private boolean whiteTurn = true;
	private Field selectedField;
	private List<LogContainer> currentMoveLog = new ArrayList<>();
	private boolean isPlayerGame = false;

	public ChessBoard() {
		this.isPlayerGame = true; // Needs to be true in the constructor, so the field shows the pieces with
									// ImageView
		boolean fieldIsWhite;
		boolean pieceIsWhite;
		int col;
		int row;
		List<Integer> rookLacations = new ArrayList<>(Arrays.asList(0, 56, 7, 63));
		List<Integer> knightLacations = new ArrayList<>(Arrays.asList(8, 48, 15, 55));
		List<Integer> bishopLacations = new ArrayList<>(Arrays.asList(16, 40, 23, 47));
		List<Integer> queenLacations = new ArrayList<>(Arrays.asList(24, 31));
		List<Integer> kingLacations = new ArrayList<>(Arrays.asList(32, 39));
		List<Integer> pawnLacations = new ArrayList<>(
				Arrays.asList(1, 9, 17, 25, 33, 41, 49, 57, 6, 14, 22, 30, 38, 46, 54, 62));

		for (int i = 0; i < 64; i++) {
			fieldIsWhite = (i + i / 8) % 2 == 0;
			pieceIsWhite = i % 8 > 4;
			col = i / 8;
			row = i % 8;

			if (rookLacations.contains(i)) {
				this.fields[col][row] = new Field(this, col, row, fieldIsWhite, new Rook(pieceIsWhite));
				this.addToPlayerPiecesSet(this.fields[col][row].getPiece());
			} else if (knightLacations.contains(i)) {
				this.fields[col][row] = new Field(this, col, row, fieldIsWhite, new Knight(pieceIsWhite));
				this.addToPlayerPiecesSet(this.fields[col][row].getPiece());
			} else if (bishopLacations.contains(i)) {
				this.fields[col][row] = new Field(this, col, row, fieldIsWhite, new Bishop(pieceIsWhite));
				this.addToPlayerPiecesSet(this.fields[col][row].getPiece());
			} else if (queenLacations.contains(i)) {
				this.fields[col][row] = new Field(this, col, row, fieldIsWhite, new Queen(pieceIsWhite));
				this.addToPlayerPiecesSet(this.fields[col][row].getPiece());
			} else if (kingLacations.contains(i)) {
				this.fields[col][row] = new Field(this, col, row, fieldIsWhite, new King(pieceIsWhite));
				this.addToPlayerPiecesSet(this.fields[col][row].getPiece());
			} else if (pawnLacations.contains(i)) {
				this.fields[col][row] = new Field(this, col, row, fieldIsWhite, new Pawn(pieceIsWhite));
				this.addToPlayerPiecesSet(this.fields[col][row].getPiece());
			} else {
				this.fields[col][row] = new Field(this, col, row, fieldIsWhite);
			}
		}
		this.isPlayerGame = false; // Is only set to true when a field is clicked on
	}

//This constructor should be used as a copyConstructor 
	public ChessBoard(Set<ChessPiece> whitePieces, Set<ChessPiece> blackPieces, boolean whiteTurn) {
		// This constructor creates a copy of the input pieces and field and does NOT
		// operate on the original references
		this.whiteTurn = whiteTurn;
		int col;
		int row;
		boolean fieldIsWhite;

		for (int i = 0; i < 64; i++) {
			fieldIsWhite = (i + i / 8) % 2 == 0;
			col = i / 8;
			row = i % 8;
			this.fields[col][row] = new Field(this, col, row, fieldIsWhite);
		}

		for (ChessPiece whitePiece : whitePieces) {
			this.addPiece(whitePiece);
		}

		for (ChessPiece blackPiece : blackPieces) {
			this.addPiece(blackPiece);
		}
	}

	private void addPiece(ChessPiece piece) {
		try {
			ChessPiece newPiece = piece.getClass().getConstructor(piece.getClass()).newInstance(piece);
			this.fields[piece.getField().getCol()][piece.getField().getRow()].setPiece(newPiece);
			this.addToPlayerPiecesSet(newPiece);
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public boolean isPlayerGame() {
		return this.isPlayerGame;
	}

	public void setPlayerGame(boolean isPlayerGame) {
		this.isPlayerGame = isPlayerGame;
	}

	public BooleanProperty endGameProperty() {
		return this.gameEndedProperty;
	}

	public Field[][] getFields() {
		return this.fields;
	}

	public Set<ChessPiece> getWhitePieces() {
		return this.whitePieces;
	}

	public Set<ChessPiece> getBlackPieces() {
		return this.blackPieces;
	}

	public Set<ChessPiece> getActivePlayerPieces() {
		return this.whiteTurn ? this.getWhitePieces() : this.getBlackPieces();
	}

	public Set<ChessPiece> getInactivePlayerPieces() {
		return this.whiteTurn ? this.getBlackPieces() : this.getWhitePieces();
	}

	public ArrayDeque<List<LogContainer>> getLog() {
		return this.log;
	}

	public int getLogSize() {
		return this.log.size();
	}

	public Field getSelectedField() {
		return this.selectedField;
	}

	public void selectField(Field field) {
		this.selectedField = field;
		if (this.isPlayerGame) {
			field.highlightField();
			this.highlightPossibleMoves();
		}
	}

	public void unselectField(Field newField) {
		this.selectedField = null;
		if (this.isPlayerGame) {
			this.unHighlightAllFields();
		}
	}

	private void unHighlightAllFields() {
		for (Field[] col : this.fields) {
			for (Field field : col) {
				field.unHighlightField();
			}
		}
	}

	private void highlightPossibleMoves() {
		for (Field field : this.selectedField.getPiece().getLegalMoves()) {
			field.highlightField();
		}
	}

	public void addToPlayerPiecesSet(ChessPiece piece) {
		if (piece.isWhite()) {
			this.whitePieces.add(piece);
		} else {
			this.blackPieces.add(piece);
		}

		// Debuggung
//		for(ChessPiece p : this.whitePieces) {
//			if(p.getField() == null || p.getField().getPiece() == null || p.getField().getPiece() != p) {
//				System.err.println("No Piece on Feld: " + p.getField());
//				throw new RuntimeException("Piece not on Field");
//			}
//		}
//		for(ChessPiece p : this.blackPieces) {
//			if(p.getField() == null || p.getField().getPiece() == null || p.getField().getPiece() != p) {
//				System.err.println("No Piece on Feld: " + p.getField());
//				throw new RuntimeException("Piece not on Field");
//			}
//		}
	}

	public void removeFromPlayerPiecesSet(ChessPiece piece) {
		if (piece.isWhite()) {
			if (!this.whitePieces.remove(piece)) {
				// In case the reference to remove is not present in the playsers set,
				// remove the chesspiece that is of the same type and has the same location
//				this.whitePieces.removeAll(this.whitePieces.stream().filter(pieceInSet -> pieceInSet.getField().getRow() == piece.getField().getRow() &&
//						pieceInSet.getField().getCol() == piece.getField().getCol() &&
//						pieceInSet.getClass() == piece.getClass()).collect(Collectors.toList()));
			}
		} else {
			if (!this.blackPieces.remove(piece)) {
				// In case the reference to remove is not present in the playsers set,
				// remove the chesspiece that is of the same type and has the same location
//				this.blackPieces.removeAll(this.blackPieces.stream().filter(pieceInSet -> pieceInSet.getField().getRow() == piece.getField().getRow() &&
//						pieceInSet.getField().getCol() == piece.getField().getCol() &&
//						pieceInSet.getClass() == piece.getClass()).collect(Collectors.toList()));
			}
		}
	}

	public boolean isWhiteTurn() {
		return this.whiteTurn;
	}

	public void switchPlayer() {
		this.whiteTurn = !this.whiteTurn;
	}

	public void currentMoveToLog() {
		this.log.addLast(this.currentMoveLog);
		this.currentMoveLog = new ArrayList<>();
	}

	public void writeCurrentMove(Field field){
		try {
			ChessPiece piece = field.getPiece();
			LogContainer container =  new LogContainer(field.getCol(),
					field.getRow(),
					field.getPiece() == null ? null : piece.getClass().getConstructor(piece.getClass()).newInstance(piece));
			this.currentMoveLog.add(container);
		}catch (Exception e) {
			e.printStackTrace();
		}
    }

	public void setupPreviousBoardState() {
		this.isPlayerGame = true;
		this.returnToLastMove();
		this.isPlayerGame = false;
	}

	public void returnToLastMove() {
//		if (this.selectedField != null) {
//			this.selectedField.onClick();//Deselect
//			System.err.println("Selected field automatically unselected");
//		}
//		System.out.println("returnToLastMove, Logsize: " + this.log.size());
		if (this.log.size() == 0) {

			System.err.println("Log is empty! You can not go further back");

			// Debugging
			throw new RuntimeException("LogError");

		}
		for (LogContainer container : this.log.removeLast()) {
			final Field currentField = this.fields[container.getCol()][container.getRow()];
			// Update of the corresponding players set of ChessPieces
			if (currentField.getPiece() != null) {
				this.removeFromPlayerPiecesSet(currentField.getPiece());
			}
			if (container.getPiece() != null) {
				this.addToPlayerPiecesSet(container.getPiece());
			}
			
			currentField.setPiece(container.getPiece());
		}

		this.switchPlayer();
	}
}