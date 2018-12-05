package ui;

import javafx.scene.Scene;
import studio.instruments.MusicalInstrumentFactory;
import studio.instruments.IMusicalInstrument;
import studio.PianoKeyboardMapper;

import java.io.File;

public class MainController {
    private IMusicalInstrument instrument;
    private PianoKeyboardMapper keyboardMapper;
    private Scene crtScene;

    public void initialize() {
        instrument = MusicalInstrumentFactory.createFromBlueprint(new File("src/main/resources/instruments/sine-synth.xml"));
        keyboardMapper = new PianoKeyboardMapper(new File("src/main/resources/piano-keyboard-mapping.xml"));
    }

    public void setCrtScene(Scene crtScene){
        this.crtScene = crtScene;
        crtScene.setOnKeyPressed(event -> {
            String keyPressed = event.getCode().getChar().toLowerCase();
            PianoKeyboardMapper.KeyMapping mapping = keyboardMapper.getMappingTable().get(keyPressed);
            if(mapping != null){
                float pitchToPlay = mapping.getPitch();
                instrument.play(pitchToPlay);
            }
        });
        crtScene.setOnKeyReleased(event -> {
            String keyPressed = event.getCode().getChar().toLowerCase();
            PianoKeyboardMapper.KeyMapping mapping = keyboardMapper.getMappingTable().get(keyPressed);
            if(mapping != null) {
                float pitchToPlay = mapping.getPitch();
                instrument.stop(pitchToPlay);
            }
        });
    }


}
