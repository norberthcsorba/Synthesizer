package ui.controllers;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.Builder;
import studio.effects.Distortion;

class DistortionPedalController {

    private ToggleButton btn_bypass;
    private GridPane pane_root;
    private Slider slider_preGain, slider_postGain;
    private TextField field_preGain, field_postGain;
    private VBox vbox_distTypes;

    @Builder
    DistortionPedalController(ToggleButton btn_bypass, GridPane pane_root, Slider slider_preGain,
                              Slider slider_postGain, TextField field_preGain, TextField field_postGain,
                              VBox vbox_distTypes) {
        this.btn_bypass = btn_bypass;
        this.pane_root = pane_root;
        this.slider_preGain = slider_preGain;
        this.slider_postGain = slider_postGain;
        this.field_preGain = field_preGain;
        this.field_postGain = field_postGain;
        this.vbox_distTypes = vbox_distTypes;
    }

    void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        for (Distortion.DistortionType distType : Distortion.DistortionType.values()) {
            RadioButton radioButton = new RadioButton(distType.toString());
            radioButton.setToggleGroup(toggleGroup);
            vbox_distTypes.getChildren().add(radioButton);
        }

        field_preGain.textProperty().bindBidirectional(slider_preGain.valueProperty(), new CustomStringConverter(slider_preGain));
        field_postGain.textProperty().bindBidirectional(slider_postGain.valueProperty(), new CustomStringConverter(slider_postGain));

        slider_preGain.valueProperty().addListener((a, b, c) -> Distortion.setPreGain((float) slider_preGain.getValue()));
        slider_postGain.valueProperty().addListener((a, b, c) -> Distortion.setPostGain((float) slider_postGain.getValue()));
        vbox_distTypes.getChildren().stream()
                .filter(child -> child instanceof RadioButton)
                .map(child -> (RadioButton) child)
                .forEach(radio_distType -> {
                    Distortion.DistortionType distType = Distortion.DistortionType.valueOf(radio_distType.getText().toUpperCase());
                    radio_distType.selectedProperty().addListener((a, b, newValue) -> {
                        if (newValue) {
                            Distortion.setDistortionType(distType);
                        }
                    });
                });

        updateProperties();
        Distortion.setOnPropertiesChange((a) -> updateProperties());

        btn_bypass.setSelected(false);
        handleOnAction_btnBypass();
    }

    private void updateProperties() {
        slider_preGain.setValue(Distortion.getPreGain());
        slider_postGain.setValue(Distortion.getPostGain());
        vbox_distTypes.getChildren().stream()
                .filter(child -> child instanceof RadioButton)
                .map(child -> (RadioButton) child)
                .filter(radioButton -> radioButton.getText().equals(Distortion.getDistortionType().toString()))
                .forEach(radioButton -> radioButton.setSelected(true));
        vbox_distTypes.getChildren().stream()
                .filter(child -> child instanceof RadioButton)
                .map(child -> (RadioButton) child)
                .filter(radioButton -> !radioButton.getText().equals(Distortion.getDistortionType().toString()))
                .forEach(radioButton -> radioButton.setSelected(false));
    }

    boolean handleOnAction_btnBypass() {
        boolean bypass = !btn_bypass.isSelected();
        Distortion.setBypass(bypass);
        pane_root.getChildren().forEach(node -> {
            if (node != btn_bypass.getParent()) {
                node.setDisable(bypass);
            }
        });
        return bypass;
    }


    private class CustomStringConverter extends StringConverter<Number> {
        private final Slider slider;

        private CustomStringConverter(Slider slider) {
            this.slider = slider;
        }

        @Override
        public String toString(Number number) {
            return number.floatValue() + "";
        }

        @Override
        public Number fromString(String string) {
            try {
                return Float.valueOf(string);
            } catch (NumberFormatException ex) {
                return slider.getValue();
            }
        }
    }
}
