package net.hermite.ui;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class PaneHover extends Pane {

    private ScaleTransition scaleTransition;
    private SimpleDoubleProperty expandToMaxProperty;

    public PaneHover() {
        scaleTransition =
                new ScaleTransition(Duration.millis(200), this);
        scaleTransition.setCycleCount(1);
        scaleTransition
                .setInterpolator(Interpolator.EASE_BOTH);

        expandToMaxProperty = new SimpleDoubleProperty(1.05);

        setOnMouseEntered((MouseEvent t) -> {
            scaleTransition.setFromX(getScaleX());
            scaleTransition.setFromY(getScaleY());
            scaleTransition.setToX(expandToMaxProperty.get());
            scaleTransition.setToY(expandToMaxProperty.get());
            scaleTransition.playFromStart();
        });

        setOnMouseExited((MouseEvent t) -> {
            scaleTransition.setFromX(getScaleX());
            scaleTransition.setFromY(getScaleY());
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.playFromStart();
        });
    }
}
