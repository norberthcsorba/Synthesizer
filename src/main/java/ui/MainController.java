package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;
import se.europeanspallationsource.javafx.control.knobs.Knob;
import se.europeanspallationsource.javafx.control.knobs.KnobBuilder;
import studio.PianoKeyboardMapper;
import studio.instruments.IMusicalInstrument;
import studio.instruments.MusicalInstrumentFactory;

import java.io.File;
import java.util.List;

public class MainController {
    private IMusicalInstrument instrument;
    private PianoKeyboardMapper keyboardMapper;
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
        instrument = MusicalInstrumentFactory.createFromBlueprint(new File("src/main/resources/instruments/sine-synth.xml"));
        keyboardMapper = new PianoKeyboardMapper(new File("src/main/resources/piano-keyboard-mapping.xml"));
        initPianoView();
        initInstrumentPane();
    }

    private void initInstrumentPane() {
        initChooseInstrumentSubPane();
        initEnvelopeSubPane();
    }

    private void initChooseInstrumentSubPane() {
        List<MusicalInstrumentFactory.Blueprint> availableInstruments = MusicalInstrumentFactory.getAvailableInstruments();
        combo_availableInstruments.setConverter(new StringConverter<MusicalInstrumentFactory.Blueprint>() {
            @Override
            public String toString(MusicalInstrumentFactory.Blueprint object) {
                if(object != null){
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
    }

    private void initEnvelopeSubPane() {
        Knob attackKnob = KnobBuilder.create().gradientStops(new Stop(0, Color.BLACK))
                .currentValueColor(Color.GREEN)
                .currentValue(100)
                .targetValue(100)
                .maxValue(1000)
                .onTargetSet(event -> {
                    Knob knob1 = (Knob) event.getSource();
                    knob1.setCurrentValue(knob1.getTargetValue());
                })
                .unit("ms")
                .build();
        Knob decayKnob = KnobBuilder.create().gradientStops(new Stop(0, Color.BLACK))
                .currentValueColor(Color.GREEN)
                .currentValue(200)
                .targetValue(200)
                .maxValue(1000)
                .onTargetSet(event -> {
                    Knob knob1 = (Knob) event.getSource();
                    knob1.setCurrentValue(knob1.getTargetValue());
                })
                .unit("ms")
                .build();
        Knob sustainKnob = KnobBuilder.create().gradientStops(new Stop(0, Color.BLACK))
                .currentValueColor(Color.GREEN)
                .currentValue(90)
                .targetValue(90)
                .onTargetSet(event -> {
                    Knob knob1 = (Knob) event.getSource();
                    knob1.setCurrentValue(knob1.getTargetValue());
                })
                .unit("%")
                .build();
        Knob releaseKnob = KnobBuilder.create().gradientStops(new Stop(0, Color.BLACK))
                .currentValueColor(Color.GREEN)
                .currentValue(400)
                .targetValue(400)
                .maxValue(1000)
                .onTargetSet(event -> {
                    Knob knob1 = (Knob) event.getSource();
                    knob1.setCurrentValue(knob1.getTargetValue());
                })
                .unit("ms")
                .build();
        vbox_attackControl.getChildren().add(attackKnob);
        vbox_decayControl.getChildren().add(decayKnob);
        vbox_sustainControl.getChildren().add(sustainKnob);
        vbox_releaseControl.getChildren().add(releaseKnob);
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
    private void handleOnChange_ComboAvailableInstruments(){
        MusicalInstrumentFactory.Blueprint instrumentToLoad = combo_availableInstruments.getValue();
        if(instrumentToLoad != null){
            System.out.println(instrumentToLoad.getFilePath());
            File xmlConfigFile = new File(instrumentToLoad.getFilePath());
            this.instrument = MusicalInstrumentFactory.createFromBlueprint(xmlConfigFile);
        }
    }


}
