package ui.utils;

import javafx.scene.control.Slider;
import javafx.util.StringConverter;

public class EnvelopeShaperStringConverter extends StringConverter<Number> {

    private final Slider slider;

    public EnvelopeShaperStringConverter(Slider slider) {
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
