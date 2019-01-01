package studio.instrument;

import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import studio.oscillators.WaveOscillator;
import utils.Constants;
import utils.exceptions.ExceptionMessages;
import utils.exceptions.OscillatorInstantiationException;
import utils.exceptions.XmlParseException;

import javax.sound.sampled.*;
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
        return new MusicalInstrument(blueprint.name, newStrings(blueprint));
    }

    private List<WaveOscillator> newOscillatorList(Blueprint blueprint) {
        List<WaveOscillator> oscillators = new ArrayList<>();
        try {
            Class<?> oscClass = Class.forName(waveOscillatorClassName(blueprint.oscType));
            for (Float harmonicAmp : blueprint.getHarmonicAmplitudes()) {
                WaveOscillator osc = (WaveOscillator) oscClass.getDeclaredConstructor().newInstance();
                osc.setAmplitude(harmonicAmp);
                oscillators.add(osc);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            throw new OscillatorInstantiationException(ExceptionMessages.OSC_COULD_NOT_BE_INSTANTIATED);
        }
        return Collections.unmodifiableList(oscillators);
    }

    private SourceDataLine newOutput() {
        AudioFormat audioFormat = new AudioFormat(Constants.SAMPLE_RATE, Constants.BIT_DEPTH, Constants.NR_OF_CHANNELS,
                Constants.AUDIO_FORMAT_IS_SIGNED, Constants.AUDIO_FORMAT_IS_BIG_ENDIAN);
        SourceDataLine output = null;
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            output = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            output.open(audioFormat);
            output.start();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
        return output;
    }

    private List<InstrumentString> newStrings(Blueprint blueprint) {
        List<InstrumentString> strings = new ArrayList<>();
        for (int i = 0; i < blueprint.polyphony; i++) {
            InstrumentString string = new InstrumentString(newOutput(), newOscillatorList(blueprint));
            string.setDaemon(true);
            string.start();
            strings.add(string);
        }
        return Collections.unmodifiableList(strings);
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
            NodeList harmonicNodes = doc.getElementsByTagName("harmonic");
            if (nameNodes.getLength() != 1 || oscNodes.getLength() != 1 || poliphonyNodes.getLength() != 1
                    || harmonicNodes.getLength() < 1) {
                throw new XmlParseException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
            }

            blueprint.name = nameNodes.item(0).getTextContent();
            blueprint.oscType = oscNodes.item(0).getTextContent();
            if (blueprint.name == null || blueprint.oscType == null) {
                throw new XmlParseException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
            }
            try {
                blueprint.polyphony = Byte.parseByte(poliphonyNodes.item(0).getTextContent());

                List<Float> harmonicAmplitudes = new ArrayList<>();
                for (int i = 0; i < harmonicNodes.getLength(); i++) {
                    float harmonicAmplitude = Float.parseFloat(harmonicNodes.item(i).getAttributes().getNamedItem("amplitude").getNodeValue());
                    harmonicAmplitudes.add(harmonicAmplitude);
                }
                blueprint.harmonicAmplitudes = harmonicAmplitudes;
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
        private List<Float> harmonicAmplitudes;
    }
}
