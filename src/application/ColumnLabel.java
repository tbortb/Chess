package application;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class ColumnLabel extends Label {
	public ColumnLabel(char text) {
		super(String.valueOf(text));
		this.setPrefHeight(20);
		this.setPrefWidth(100);
		this.setAlignment(Pos.BASELINE_CENTER);
	}
}
