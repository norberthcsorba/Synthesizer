package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;
import se.europeanspallationsource.javafx.control.knobs.Knob;
import se.europeanspallationsource.javafx.control.knobs.KnobBuilder;
import studio.instrument.EnvelopeShaper;
import studio.instrument.IMusicalInstrument;
import studio.instrument.MusicalInstrumentFactory;
import ui.utils.PianoKeyboardMapper;
import utils.Constants;
import utils.exceptions.OscillatorInstantiationException;
import utils.exceptions.XmlParseException;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class MainController {
    private IMusicalInstrument instrument;
    private PianoKeyboardMapper keyboardMapper;
    private MusicalInstrumentFactory instrumentFactory;
    private Scene crtScene;
    @FXML
    private HBox hbox_pianoKeyboard;
    @FXML
    private HBox hbox_instrument;
    @FXML
    private VBox vbox_attackControl, vbox_decayControl, vbox_sustainControl, vbox_releaseControl;
    @FXML
    private AreaChart chart_envelope;
    @FXML
    private ComboBox<MusicalInstrumentFactory.Blueprint> combo_availableInstruments;
    @FXML
    private Button btn_browseInstrument, btn_saveInstrument;

    public void initialize() {
        try {
            keyboardMapper = new PianoKeyboardMapper(new File(Constants.PIANO_KEYBOARD_MAPPING_FILE_PATH));
            instrumentFactory = new MusicalInstrumentFactory();
            initPianoView();
            initInstrumentPane();
        } catch (XmlParseException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
        }
    }

    private void initInstrumentPane() {
        initChooseInstrumentSubPane();
        initEnvelopeSubPane();
    }

    private void initChooseInstrumentSubPane() {
        List<MusicalInstrumentFactory.Blueprint> availableInstruments = instrumentFactory.getAvailableInstruments();
        combo_availableInstruments.setConverter(new StringConverter<MusicalInstrumentFactory.Blueprint>() {
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

    private void initEnvelopeSubPane() {
        Knob attackKnob = newKnob((short)0, (short)500, EnvelopeShaper.getAttackTime(), "ms", EnvelopeShaper::setAttackTime);
        Knob decayKnob = newKnob((short)0, (short)500, EnvelopeShaper.getDecayTime(), "ms", EnvelopeShaper::setDecayTime);
        Knob sustainKnob =  newKnob((short)-10, (short)-1, (short)EnvelopeShaper.getSustainAmp(), "db", EnvelopeShaper::setSustainAmp);
        Knob releaseKnob = newKnob((short)0, (short)500, EnvelopeShaper.getReleaseTime(), "ms", EnvelopeShaper::setReleaseTime);
        vbox_attackControl.getChildren().add(attackKnob);
        vbox_decayControl.getChildren().add(decayKnob);
        vbox_sustainControl.getChildren().add(sustainKnob);
        vbox_releaseControl.getChildren().add(releaseKnob);
    }

    private Knob newKnob(short minValue, short maxValue, short initialValue, String unit, Consumer<Short> callback){
        return KnobBuilder.create()
                .gradientStops(new Stop(0, Color.BLACK))
                .currentValueColor(Color.GREEN)
                .currentValue(initialValue)
                .targetValue(initialValue)
                .minValue(minValue)
                .maxValue(maxValue)
                .decimals(0)
                .onTargetSet(event -> {
                    Knob knob1 = (Knob) event.getSource();
                    knob1.setCurrentValue(knob1.getTargetValue());
                    callback.accept((short)knob1.getTargetValue());
                })
                .unit(unit)
                .build();
    }

    private void initPianoView() {
        hbox_pianoKeyboard.getStyleClass().add("piano-keyboard");
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
        }
    }

    private void mapPianoToKeyboard() {
        crtScene.setOnKeyPressed(event -> {
            String keyPressed = event.getCode().getChar().toLowerCase();
            PianoKeyboardMapper.KeyMapping mapping = keyboardMapper.getMappingTable().get(keyPressed);
            if (mapping != null) {
                float pitchToPlay = mapping.getPitch();
                instrument.play(pitchToPlay);
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
                instrument.stop(pitchToPlay);
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
