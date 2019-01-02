package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;
import lombok.Builder;
import lombok.Getter;
import studio.instrument.IMusicalInstrument;
import studio.instrument.MusicalInstrumentFactory;
import utils.exceptions.OscillatorInstantiationException;

import java.util.List;

class InstrumentPedalController {

    private MusicalInstrumentFactory instrumentFactory;
    @Getter
    private IMusicalInstrument instrument;

    private ToggleButton btn_bypass;
    private ComboBox<MusicalInstrumentFactory.Blueprint> combo_availableInstruments;


    @Builder
    InstrumentPedalController(ToggleButton btn_bypass, ComboBox<MusicalInstrumentFactory.Blueprint> combo_availableInstruments){
        this.btn_bypass = btn_bypass;
        this.combo_availableInstruments = combo_availableInstruments;
    }

    void initialize(){
        instrumentFactory = new MusicalInstrumentFactory();
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
            handleOnChange_comboAvailableInstruments();
        }
    }

    void handleOnChange_comboAvailableInstruments(){
        MusicalInstrumentFactory.Blueprint instrumentToLoad = combo_availableInstruments.getValue();
        if (instrumentToLoad != null) {
            try {
                this.instrument = instrumentFactory.createFromBlueprint(instrumentToLoad);
            } catch (OscillatorInstantiationException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        }
    }
}
