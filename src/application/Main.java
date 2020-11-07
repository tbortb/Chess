package application;

import Engines.AlphaBetaPrune;
import Engines.AlphaBetaPruneMultiThread;
import Engines.AlphaBetaPruneMultiThreadRootCom;
import Engines.ChessEngine;
import Engines.ChessMove;
import Engines.MiniMaxEngine;
import Evaluators.MaxPossibleMoves;
import Evaluators.PiecesAndCenter;
import Pieces.King;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
	private static final int searchDeepth = 5;

	@Override
	public void start(Stage primaryStage) {
		try {
			Scene scene = new Scene(this.createRoot(), 750, 750);
			scene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Parent createRoot() {
		BorderPane borderPane = new BorderPane();
		ChessBoard chessBoard = new ChessBoard();

		borderPane.setCenter(this.createPlayingField(chessBoard));

		borderPane.setTop(this.createComputerMoveView(chessBoard));

		return borderPane;
	}

	private Node createComputerMoveView(ChessBoard chessBoard) {
		HBox hbox = new HBox();
		Button computerGameBtn = new Button("Computer Game");
		Button computerMoveBtn = new Button("Computer Move");
		Button backButton = new Button("Ctrl + Z");
		backButton.setOnAction(event -> chessBoard.setupPreviousBoardState());

		ChessEngine engine = new AlphaBetaPruneMultiThreadRootCom(new PiecesAndCenter(), new MaxPossibleMoves());
		computerGameBtn.setOnAction(e -> {
				final long startTime = System.currentTimeMillis();
				ChessMove computerMove;
				try {					
				computerMove = engine.computerMove(chessBoard, searchDeepth);
				}catch (RuntimeException ex) {
					ex.printStackTrace();
					System.out.println("Log: " + chessBoard.getLog());
					throw new RuntimeException("Something went wrong");
				}
					computerMove.getFrom().fire();
					computerMove.getTo().fire();
					if (chessBoard.getActivePlayerPieces().stream()
							.anyMatch(p -> p instanceof King && chessBoard.getLogSize() < 100)) {
						Platform.runLater(() -> computerGameBtn.fire());
					}
					
				System.out.println(computerMove + " took " + String.valueOf(System.currentTimeMillis() - startTime)
						+ " milisecondss (" + engine.getEvaluatorCalls() + " evaluations)");
		});

		computerMoveBtn.setOnAction(e -> {
			final long startTime = System.currentTimeMillis();
			ChessMove computerMove = engine.computerMove(chessBoard, searchDeepth);
			computerMove.getFrom().fire();
			computerMove.getTo().fire();
			System.out.println(computerMove + " took " + String.valueOf(System.currentTimeMillis() - startTime)
					+ " milisecondss (" + engine.getEvaluatorCalls() + " evaluations)");
		});

		hbox.getChildren().addAll(computerGameBtn, computerMoveBtn, backButton);
		return hbox;
	}

	private Node createPlayingField(ChessBoard chessBoard) {
		GridPane outerPlayingField = new GridPane();

		this.setLabels(outerPlayingField);

		this.createInnerPlayingField(outerPlayingField, chessBoard);

		return outerPlayingField;
	}

	private void createInnerPlayingField(GridPane outerPlayingField, ChessBoard chessBoard) {
		for (int i = 0; i < 64; i++) {
			outerPlayingField.add(chessBoard.getFields()[i / 8][i % 8], i / 8 + 1, i % 8 + 1);
		}
	}

	private void setLabels(GridPane outerPlayingField) {
		char[] colLabels = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
		int[] rowLabels = { 8, 7, 6, 5, 4, 3, 2, 1 };

		for (int i = 0; i <= 7; i++) {
			outerPlayingField.add(new ColumnLabel(colLabels[i]), i + 1, 0);
			outerPlayingField.add(new ColumnLabel(colLabels[i]), i + 1, 9);
			outerPlayingField.add(new RowLabel(rowLabels[i]), 0, i + 1);
			outerPlayingField.add(new RowLabel(rowLabels[i]), 9, i + 1);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}