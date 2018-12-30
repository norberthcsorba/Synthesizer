package studio.instruments;

import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import studio.oscillators.WaveOscillator;
import utils.Constants;
import utils.exceptions.ExceptionMessages;
import utils.exceptions.OscillatorInstantiationException;
import utils.exceptions.XmlParseException;

import javax.sound.sampled.AudioFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicalInstrumentFactory {

    @Getter
    private List<Blueprint> availableInstruments;

    public MusicalInstrumentFactory() {
        loadAvailableInstruments();
    }

    public MusicalInstrument createFromBlueprint(Blueprint blueprint) {
        AudioFormat format = new AudioFormat(Constants.SAMPLE_RATE, Constants.BIT_DEPTH, Constants.NR_OF_CHANNELS,
                Constants.AUDIO_FORMAT_IS_SIGNED, Constants.AUDIO_FORMAT_IS_BIG_ENDIAN);
        List<WaveOscillator> oscillators = new ArrayList<>();
        try {
            Class<?> oscClass = Class.forName(waveOscillatorClassName(blueprint.oscType));
            for (int i = 0; i < blueprint.polyphony; i++) {
                WaveOscillator osc = (WaveOscillator) oscClass.getDeclaredConstructor(AudioFormat.class).newInstance(format);
                oscillators.add(osc);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            throw new OscillatorInstantiationException(ExceptionMessages.OSC_COULD_NOT_BE_INSTANTIATED);
        }
        return new MusicalInstrument(blueprint.name, Collections.unmodifiableList(oscillators));

    }

    private void loadAvailableInstruments() {
        this.availableInstruments = new ArrayList<>();
        File instrumentsFolder = new File(Constants.INSTRUMENTS_FOLDER_PATH);
        for (File file : instrumentsFolder.listFiles()) {
            Blueprint blueprint = parseXmlBlueprintFile(file);
            if (blueprint != null) {
                this.availableInstruments.add(blueprint);
            }
        }
    }

    private Blueprint parseXmlBlueprintFile(File xmlBlueprintFile) {
        try {
            Blueprint blueprint = new Blueprint();
            blueprint.filePath = xmlBlueprintFile.getPath();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(xmlBlueprintFile);
            doc.getDocumentElement().normalize();
            NodeList nameNodes = doc.getElementsByTagName("name");
            NodeList oscNodes = doc.getElementsByTagName("oscillator");
            NodeList poliphonyNodes = doc.getElementsByTagName("polyphony");
            if (nameNodes.getLength() != 1 || oscNodes.getLength() != 1 || poliphonyNodes.getLength() != 1) {
                throw new XmlParseException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
            }
            blueprint.name = nameNodes.item(0).getTextContent();
            blueprint.oscType = oscNodes.item(0).getTextContent();
            if (blueprint.name == null || blueprint.oscType == null) {
                throw new XmlParseException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
            }
            try {
                blueprint.polyphony = Byte.parseByte(poliphonyNodes.item(0).getTextContent());
            } catch (NumberFormatException ex) {
                throw new XmlParseException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
            }
            return blueprint;
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String waveOscillatorClassName(String oscType) {
        StringBuilder className = new StringBuilder(oscType);
        Character firstLetter = className.charAt(0);
        className.replace(0, 1, firstLetter.toString().toUpperCase());
        className.append("WaveOscillator");
        className.insert(0, "studio.oscillators.");
        return className.toString();
    }

    @Getter
    public static class Blueprint {
        private String name;
        private String oscType;
        private byte polyphony;
        private String filePath;
    }
}
