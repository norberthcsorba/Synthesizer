package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import studio.instrument.Blueprint;

public class MainController {


    //PianoKeyboardController
    private PianoKeyboardController pianoKeyboardController;
    @FXML
    private HBox hbox_pianoKeyboard;

    //InstrumentPedalController
    private InstrumentPedalController instrumentPedalController;
    @FXML
    private GridPane grid_instrumentPedal;
    @FXML
    private ToggleButton btn_bypassInstrumentPedal;
    @FXML
    private ComboBox<Blueprint> combo_availableInstruments;

    //EnvelopePedal
    private EnvelopeShaperPedalController envelopeShaperPedalController;
    @FXML
    private CheckBox check_hasDecayAndSustain;
    @FXML
    private Slider slider_attackTime, slider_decayTime, slider_sustainAmp, slider_releaseTime;
    @FXML
    private TextField field_attackTime, field_decayTime, field_sustainAmp, field_releaseTime;

    //DistortionPedal
    private DistortionPedalController distortionPedalController;
    @FXML
    private ToggleButton btn_bypassDistPedal;
    @FXML
    private GridPane grid_distortionPedal;
    @FXML
    private Slider slider_distPreGain, slider_distPostGain;
    @FXML
    private TextField field_distPreGain, field_distPostGain;
    @FXML
    private VBox vbox_distTypes;


    public void initialize() {
        instrumentPedalController = InstrumentPedalController.builder()
                .pane_root(grid_instrumentPedal)
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
                .check_hasDecayAndSustain(check_hasDecayAndSustain)
                .build();
        envelopeShaperPedalController.initialize();

        distortionPedalController = DistortionPedalController.builder()
                .pane_root(grid_distortionPedal)
                .btn_bypass(btn_bypassDistPedal)
                .slider_preGain(slider_distPreGain)
                .slider_postGain(slider_distPostGain)
                .field_preGain(field_distPreGain)
                .field_postGain(field_distPostGain)
                .vbox_distTypes(vbox_distTypes)
                .build();
        distortionPedalController.initialize();
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

    @FXML
    private void handleOnAction_btnBypassInstrumentPedal() {
        boolean bypass = instrumentPedalController.handleOnAction_btnBypass();
        hbox_pianoKeyboard.setDisable(bypass);
    }

    @FXML
    private void handleOnAction_btnBypassDistortionPedal() {
        distortionPedalController.handleOnAction_btnBypass();
    }

    public void cleanUp() {
        instrumentPedalController.cleanUp();
    }
}
