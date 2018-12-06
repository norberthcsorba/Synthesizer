package studio.instruments;

import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import studio.oscillators.*;
import utils.AppException;

import javax.sound.sampled.AudioFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicalInstrumentFactory {

    public static MusicalInstrument createFromBlueprint(File xmlBlueprintFile) {
        Blueprint blueprint = parseXmlBlueprintFile(xmlBlueprintFile);
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        WaveOscillator[] oscillators = new WaveOscillator[blueprint.polyphony];
        for (int i = 0; i < oscillators.length; i++) {
            switch (blueprint.oscType) {
                case "sine":
                    oscillators[i] = new SineWaveOscillator(format);
                    break;
                case "square":
                    oscillators[i] = new SquareWaveOscillator(format);
                    break;
                case "triangle":
                    oscillators[i] = new TriangleWaveOscillator(format);
                    break;
                case"saw":
                    oscillators[i] = new SawWaveOscillator(format);
            }
        }
        return new MusicalInstrument(blueprint.name, oscillators);

    }

    public static List<Blueprint> getAvailableInstruments(){
        List<Blueprint> availableInstruments = new ArrayList<>();
        File instrumentsFolder = new File("src/main/resources/instruments");
        for(File file : instrumentsFolder.listFiles()){
            Blueprint blueprint = parseXmlBlueprintFile(file);
            if(blueprint != null){
                availableInstruments.add(blueprint);
            }
        }
        return availableInstruments;
    }

    private static Blueprint parseXmlBlueprintFile(File xmlBlueprintFile) {
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
                throw new AppException("Xml file is not well formed");
            }
            blueprint.name = nameNodes.item(0).getTextContent();
            blueprint.oscType = oscNodes.item(0).getTextContent();
            if (blueprint.name == null || blueprint.oscType == null) {
                throw new AppException("Xml file is not well formed");
            }
            try {
                blueprint.polyphony = Byte.parseByte(poliphonyNodes.item(0).getTextContent());
            } catch (NumberFormatException ex) {
                throw new AppException("Xml file is not well formed");
            }
            return blueprint;
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Getter
    public static class Blueprint {
        private String name;
        private String oscType;
        private byte polyphony;
        private String filePath;
    }
}
