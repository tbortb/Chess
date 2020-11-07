package application;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class RowLabel extends Label {
	public RowLabel(int text) {
		super(String.valueOf(text));
		this.setPrefHeight(100);
		this.setPrefWidth(20);
		this.setAlignment(Pos.BASELINE_CENTER);
	}
}
