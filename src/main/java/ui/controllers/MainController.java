package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import studio.instrument.MusicalInstrumentFactory;

public class MainController {


    //PianoKeyboardController
    private PianoKeyboardController pianoKeyboardController;
    @FXML
    private HBox hbox_pianoKeyboard;

    //InstrumentPedalController
    private InstrumentPedalController instrumentPedalController;
    @FXML
    private ToggleButton btn_bypassInstrumentPedal;
    @FXML
    private ComboBox<MusicalInstrumentFactory.Blueprint> combo_availableInstruments;

    //EnvelopePedal
    private EnvelopeShaperPedalController envelopeShaperPedalController;
    @FXML
    private Slider slider_attackTime, slider_decayTime, slider_sustainAmp, slider_releaseTime;
    @FXML
    private TextField field_attackTime, field_decayTime, field_sustainAmp, field_releaseTime;

    public void initialize() {
        instrumentPedalController = InstrumentPedalController.builder()
                .btn_bypass(btn_bypassInstrumentPedal)
                .combo_availableInstruments(combo_availableInstruments)
                .build();
        instrumentPedalController.initialize();

        envelopeShaperPedalController = EnvelopeShaperPedalController.builder()
                .slider_attackTime(slider_attackTime)
                .field_attackTime(field_attackTime)
                .field_decayTime(field_decayTime)
                .field_sustainAmp(field_sustainAmp)
                .field_releaseTime(field_releaseTime)
                .slider_attackTime(slider_attackTime)
                .slider_decayTime(slider_decayTime)
                .slider_sustainAmp(slider_sustainAmp)
                .slider_releaseTime(slider_releaseTime)
                .build();
        envelopeShaperPedalController.initialize();
    }


    public void setCrtScene(Scene crtScene) {
        pianoKeyboardController = PianoKeyboardController.builder()
                .crtScene(crtScene)
                .instrumentPedalController(instrumentPedalController)
                .hbox_pianoKeyboard(hbox_pianoKeyboard)
                .build();
        pianoKeyboardController.initialize();
    }

    @FXML
    private void handleOnChange_ComboAvailableInstruments() {
        instrumentPedalController.handleOnChange_comboAvailableInstruments();
    }

}
