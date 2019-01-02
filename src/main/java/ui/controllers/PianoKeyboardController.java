package ui.controllers;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import lombok.Builder;
import ui.utils.PianoKeyboardMapper;
import utils.Constants;
import utils.exceptions.XmlParseException;

import java.io.File;

class PianoKeyboardController {

    private PianoKeyboardMapper keyboardMapper;
    private InstrumentPedalController instrumentPedalController;

    private Scene crtScene;
    private HBox hbox_pianoKeyboard;

    @Builder
    public PianoKeyboardController(InstrumentPedalController instrumentPedalController, Scene crtScene,
                                   HBox hbox_pianoKeyboard) {
        this.instrumentPedalController = instrumentPedalController;
        this.crtScene = crtScene;
        this.hbox_pianoKeyboard = hbox_pianoKeyboard;
    }

    void initialize() {
        try {
            keyboardMapper = new PianoKeyboardMapper(new File(Constants.PIANO_KEYBOARD_MAPPING_FILE_PATH));
            initPianoView();
            crtScene.setOnKeyPressed(this::handleKeyPressed);
            crtScene.setOnKeyReleased(this::handleKeyReleased);
            crtScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShiftMapping);
        } catch (XmlParseException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
        }
    }

    private void initPianoView() {
        double translateX = 0;
        for (PianoKeyboardMapper.KeyMapping note : keyboardMapper.getMappingList()) {
            SVGPath pianoKey = new SVGPath();
            pianoKey.setContent(note.getLayout().getSvgPath());
            pianoKey.getStyleClass().addAll("piano-key", note.getLayout().getCssClass());
            if (note.getKey().equals("")) {
                pianoKey.getStyleClass().add("unmapped");
            }
            if (note.getLayout() == PianoKeyboardMapper.KeyLayout.ALTERED) {
                translateX -= 6;
            }
            pianoKey.setTranslateX(translateX);
            if (note.getLayout() == PianoKeyboardMapper.KeyLayout.ALTERED) {
                translateX -= 6;
            }
            if (note.getLayout() == PianoKeyboardMapper.KeyLayout.NATURAL_RIGHT) {
                translateX -= 2;
            }
            hbox_pianoKeyboard.getChildren().add(pianoKey);
            hbox_pianoKeyboard.getParent().scaleXProperty().set(0.877);
            hbox_pianoKeyboard.getParent().setTranslateX(-85);
            hbox_pianoKeyboard.setScaleX(1.35);
            hbox_pianoKeyboard.setScaleY(1.22);
            hbox_pianoKeyboard.setTranslateX(290);
            hbox_pianoKeyboard.setTranslateY(20);
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        String keyPressed = event.getCode().getChar().toLowerCase();
        PianoKeyboardMapper.KeyMapping mapping = keyboardMapper.getMappingTable().get(keyPressed);
        if (mapping != null) {
            float pitchToPlay = mapping.getPitch();
            boolean pitchCouldBePlayed = instrumentPedalController.getInstrument().startPlaying(pitchToPlay);
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
    }

    private void handleKeyReleased(KeyEvent event) {
        System.out.println(event.getCode() + " (released)");
        String keyPressed = event.getCode().getChar().toLowerCase();
        PianoKeyboardMapper.KeyMapping mapping = keyboardMapper.getMappingTable().get(keyPressed);
        if (mapping != null) {
            float pitchToPlay = mapping.getPitch();
            boolean pitchCouldBePlayed = instrumentPedalController.getInstrument().stopPlaying(pitchToPlay);
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
    }

    private void handleShiftMapping(KeyEvent event) {
        switch (event.getCode()) {
            case F10:
                keyboardMapper.shiftMapping(-Constants.OCTAVE_SIZE);
                break;
            case F11:
                keyboardMapper.shiftMapping(Constants.OCTAVE_SIZE);
                break;
        }
        hbox_pianoKeyboard.getChildren().forEach(note -> {
            note.getStyleClass().remove("unmapped");
        });
        for (int i = 0; i < keyboardMapper.getMappingList().size(); i++) {
            if (keyboardMapper.getMappingList().get(i).getKey().equals("")) {
                hbox_pianoKeyboard.getChildren().get(i).getStyleClass().add("unmapped");
            }
        }
    }

}
