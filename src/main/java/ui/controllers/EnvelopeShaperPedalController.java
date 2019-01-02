package ui.controllers;

import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import lombok.Builder;
import studio.instrument.EnvelopeShaper;

class EnvelopeShaperPedalController {

    private Slider slider_attackTime, slider_decayTime, slider_sustainAmp, slider_releaseTime;
    private TextField field_attackTime, field_decayTime, field_sustainAmp, field_releaseTime;

    @Builder
    EnvelopeShaperPedalController(Slider slider_attackTime, Slider slider_decayTime, Slider slider_sustainAmp,
                                  Slider slider_releaseTime, TextField field_attackTime, TextField field_decayTime,
                                  TextField field_sustainAmp, TextField field_releaseTime) {
        this.slider_attackTime = slider_attackTime;
        this.slider_decayTime = slider_decayTime;
        this.slider_sustainAmp = slider_sustainAmp;
        this.slider_releaseTime = slider_releaseTime;
        this.field_attackTime = field_attackTime;
        this.field_decayTime = field_decayTime;
        this.field_sustainAmp = field_sustainAmp;
        this.field_releaseTime = field_releaseTime;
    }

    void initialize() {
        field_attackTime.textProperty().bindBidirectional(
                slider_attackTime.valueProperty(), new CustomStringConverter(slider_attackTime));
        field_decayTime.textProperty().bindBidirectional(
                slider_decayTime.valueProperty(), new CustomStringConverter(slider_decayTime));
        field_sustainAmp.textProperty().bindBidirectional(
                slider_sustainAmp.valueProperty(), new CustomStringConverter(slider_sustainAmp));
        field_releaseTime.textProperty().bindBidirectional(
                slider_releaseTime.valueProperty(), new CustomStringConverter(slider_releaseTime));

        slider_attackTime.valueProperty().addListener((a, b, c) -> EnvelopeShaper.setAttackTime((short) slider_attackTime.getValue()));
        slider_decayTime.valueProperty().addListener((a, b, c) -> EnvelopeShaper.setDecayTime((short) slider_decayTime.getValue()));
        slider_sustainAmp.valueProperty().addListener((a, b, c) -> EnvelopeShaper.setSustainAmp((short) slider_sustainAmp.getValue()));
        slider_releaseTime.valueProperty().addListener((a, b, c) -> EnvelopeShaper.setReleaseTime((short) slider_releaseTime.getValue()));
    }


    private class CustomStringConverter extends StringConverter<Number> {
        private final Slider slider;

        private CustomStringConverter(Slider slider) {
            this.slider = slider;
        }

        @Override
        public String toString(Number number) {
            return number.shortValue() + "";
        }

        @Override
        public Number fromString(String string) {
            try {
                return Short.valueOf(string);
            } catch (NumberFormatException ex) {
                return slider.getValue();
            }
        }
    }

}
