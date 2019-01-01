package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;
import studio.instrument.EnvelopeShaper;
import studio.instrument.IMusicalInstrument;
import studio.instrument.MusicalInstrumentFactory;
import ui.utils.EnvelopeShaperStringConverter;
import ui.utils.PianoKeyboardMapper;
import utils.Constants;
import utils.exceptions.OscillatorInstantiationException;
import utils.exceptions.XmlParseException;

import java.io.File;
import java.util.List;

public class MainController {
    private IMusicalInstrument instrument;
    private PianoKeyboardMapper keyboardMapper;
    private MusicalInstrumentFactory instrumentFactory;
    private Scene crtScene;
    @FXML
    private HBox hbox_pianoKeyboard;
    @FXML
    private ComboBox<MusicalInstrumentFactory.Blueprint> combo_availableInstruments;
    @FXML
    private Slider slider_attackTime, slider_decayTime, slider_sustainAmp, slider_releaseTime;
    @FXML
    private TextField field_attackTime, field_decayTime, field_sustainAmp, field_releaseTime;

    public void initialize() {
        try {
            keyboardMapper = new PianoKeyboardMapper(new File(Constants.PIANO_KEYBOARD_MAPPING_FILE_PATH));
            instrumentFactory = new MusicalInstrumentFactory();
            initPianoView();
            initPedalboard();
        } catch (XmlParseException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
        }
    }

    private void initPedalboard() {
        initInstrumentPedal();
        initEnvelopeShaperPedal();
    }

    private void initInstrumentPedal() {
        List<MusicalInstrumentFactory.Blueprint> availableInstruments = instrumentFactory.getAvailableInstruments();
        combo_availableInstruments.setConverter(new StringConverter<>() {
            @Override
            public String toString(MusicalInstrumentFactory.Blueprint object) {
                if (object != null) {
                    return object.getName();
                }
                return null;
            }

            @Override
            public MusicalInstrumentFactory.Blueprint fromString(String string) {
                return combo_availableInstruments.getItems().stream()
                        .filter(instrument -> instrument.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });
        ObservableList<MusicalInstrumentFactory.Blueprint> observableList = FXCollections.observableArrayList();
        observableList.addAll(availableInstruments);
        combo_availableInstruments.setItems(observableList);
        if (availableInstruments.size() > 0) {
            combo_availableInstruments.setValue(availableInstruments.get(0));
            handleOnChange_ComboAvailableInstruments();
        }
    }

    private void initEnvelopeShaperPedal() {
        field_attackTime.textProperty().bindBidirectional(
                slider_attackTime.valueProperty(), new EnvelopeShaperStringConverter(slider_attackTime));
        field_decayTime.textProperty().bindBidirectional(
                slider_decayTime.valueProperty(), new EnvelopeShaperStringConverter(slider_decayTime));
        field_sustainAmp.textProperty().bindBidirectional(
                slider_sustainAmp.valueProperty(), new EnvelopeShaperStringConverter(slider_sustainAmp));
        field_releaseTime.textProperty().bindBidirectional(
                slider_releaseTime.valueProperty(), new EnvelopeShaperStringConverter(slider_releaseTime));

        slider_attackTime.valueProperty().addListener((a,b,c) -> EnvelopeShaper.setAttackTime((short) slider_attackTime.getValue()));
        slider_decayTime.valueProperty().addListener((a,b,c) -> EnvelopeShaper.setDecayTime((short) slider_decayTime.getValue()));
        slider_sustainAmp.valueProperty().addListener((a,b,c) -> EnvelopeShaper.setSustainAmp((short) slider_sustainAmp.getValue()));
        slider_releaseTime.valueProperty().addListener((a,b,c) -> EnvelopeShaper.setReleaseTime((short) slider_releaseTime.getValue()));
    }

    private void initPianoView() {
        double translateX = 0;
        for (PianoKeyboardMapper.KeyMapping note : keyboardMapper.getMappingList()) {
            SVGPath pianoKey = new SVGPath();
            pianoKey.setContent(note.getLayout().getSvgPath());
            pianoKey.getStyleClass().addAll("piano-key", note.getLayout().getCssClass());
            if (note.getLayout() == PianoKeyboardMapper.KeyLayout.ALTERED) {
                translateX -= 5;
            }
            pianoKey.setTranslateX(translateX);
            if (note.getLayout() == PianoKeyboardMapper.KeyLayout.ALTERED) {
                translateX -= 5;
            }
            hbox_pianoKeyboard.getChildren().add(pianoKey);
            hbox_pianoKeyboard.getParent().scaleXProperty().set(0.855);
            hbox_pianoKeyboard.getParent().setTranslateX(-102);
            hbox_pianoKeyboard.setScaleX(1.4);
            hbox_pianoKeyboard.setScaleY(1.22);
            hbox_pianoKeyboard.setTranslateX(250);
            hbox_pianoKeyboard.setTranslateY(20);
        }
    }

    private void mapPianoToKeyboard() {
        crtScene.setOnKeyPressed(event -> {
            String keyPressed = event.getCode().getChar().toLowerCase();
            PianoKeyboardMapper.KeyMapping mapping = keyboardMapper.getMappingTable().get(keyPressed);
            if (mapping != null) {
                float pitchToPlay = mapping.getPitch();
                boolean pitchCouldBePlayed = instrument.play(pitchToPlay);
                if (!pitchCouldBePlayed) {
                    return;
                }
                for (int i = 0; i < keyboardMapper.getMappingList().size(); i++) {
                    PianoKeyboardMapper.KeyMapping note = keyboardMapper.getMappingList().get(i);
                    if (note.getKey().equals(keyPressed)) {
                        hbox_pianoKeyboard.getChildren().get(i).getStyleClass().add("pressed");
                        break;
                    }
                }
            }
        });
        crtScene.setOnKeyReleased(event -> {
            String keyPressed = event.getCode().getChar().toLowerCase();
            PianoKeyboardMapper.KeyMapping mapping = keyboardMapper.getMappingTable().get(keyPressed);
            if (mapping != null) {
                float pitchToPlay = mapping.getPitch();
                boolean pitchCouldBePlayed = instrument.stop(pitchToPlay);
                if (!pitchCouldBePlayed) {
                    return;
                }
                for (int i = 0; i < keyboardMapper.getMappingList().size(); i++) {
                    PianoKeyboardMapper.KeyMapping note = keyboardMapper.getMappingList().get(i);
                    if (note.getKey().equals(keyPressed)) {
                        hbox_pianoKeyboard.getChildren().get(i).getStyleClass().removeAll("pressed");
                        break;
                    }
                }
            }
        });
    }

    public void setCrtScene(Scene crtScene) {
        this.crtScene = crtScene;
        mapPianoToKeyboard();
    }

    @FXML
    private void handleOnChange_ComboAvailableInstruments() {
        MusicalInstrumentFactory.Blueprint instrumentToLoad = combo_availableInstruments.getValue();
        if (instrumentToLoad != null) {
            try {
                this.instrument = instrumentFactory.createFromBlueprint(instrumentToLoad);
            } catch (OscillatorInstantiationException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        }
    }

    public void cleanUp() {
        instrument.cleanUp();
    }

}
