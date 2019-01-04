package studio.instrument;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import studio.effects.EnvelopeShaper;
import utils.exceptions.ExceptionMessages;
import utils.exceptions.ObjectInstantiationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MusicalInstrumentBlueprintParser {

    public static Blueprint parseXmlBlueprintFile(File xmlBlueprintFile) {
        Document doc = newXmlParser(xmlBlueprintFile);
        return Blueprint.builder()
                .name(parseInstrumentName(doc))
                .oscType(parseInstrumentOscillatorType(doc))
                .polyphony(parseInstrumentPolyphony(doc))
                .harmonicAmplitudes(parseInstrumentHarmonicAmplitudes(doc))
                .envelope(parseInstrumentEnvelope(doc))
                .build();
    }

    private static Document newXmlParser(File xmlFile) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String parseInstrumentName(Document doc) {
        NodeList nameNodes = doc.getElementsByTagName("name");
        if (nameNodes.getLength() != 1) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        String name = nameNodes.item(0).getTextContent();
        if (name == null) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        return name;
    }

    private static String parseInstrumentOscillatorType(Document doc) {
        NodeList oscNodes = doc.getElementsByTagName("oscillator");
        if (oscNodes.getLength() != 1) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        String oscType = oscNodes.item(0).getTextContent();
        if (oscType == null || (!oscType.equals("sine") && !oscType.equals("square")
                && !oscType.equals("saw") && !oscType.equals("triangle"))) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        return oscType;
    }

    private static byte parseInstrumentPolyphony(Document doc) {
        NodeList poliphonyNodes = doc.getElementsByTagName("polyphony");
        if (poliphonyNodes.getLength() != 1) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        try {
            return Byte.parseByte(poliphonyNodes.item(0).getTextContent());
        } catch (NumberFormatException ex) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
    }

    private static List<Float> parseInstrumentHarmonicAmplitudes(Document doc) {
        NodeList harmonicNodes = doc.getElementsByTagName("harmonic");
        if (harmonicNodes.getLength() < 1) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        List<Float> harmonicAmplitudes = new ArrayList<>();
        try {
            for (int i = 0; i < harmonicNodes.getLength(); i++) {
                float harmonicAmplitude = Float.parseFloat(harmonicNodes.item(i).getAttributes().getNamedItem("amplitude").getNodeValue());
                harmonicAmplitudes.add(harmonicAmplitude);
            }
        } catch (NumberFormatException ex) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        return harmonicAmplitudes;
    }

    private static EnvelopeShaper.Envelope parseInstrumentEnvelope(Document doc) {
        NodeList envelopeNodes = doc.getElementsByTagName("envelope");
        if (envelopeNodes.getLength() != 1) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
        final String attackTime = envelopeNodes.item(0).getAttributes().getNamedItem("attack-time").getNodeValue();
        final String decayTime = envelopeNodes.item(0).getAttributes().getNamedItem("decay-time").getNodeValue();
        final String sustainAmp = envelopeNodes.item(0).getAttributes().getNamedItem("sustain-amp").getNodeValue();
        final String releaseTime = envelopeNodes.item(0).getAttributes().getNamedItem("release-time").getNodeValue();
        final String hasDecayAndSustain = envelopeNodes.item(0).getAttributes().getNamedItem("has-decay-and-sustain").getNodeValue();
        try {
            return EnvelopeShaper.Envelope.builder()
                    .attackTime(Short.parseShort(attackTime))
                    .decayTime(Short.parseShort(decayTime))
                    .sustainAmp(Float.parseFloat(sustainAmp))
                    .sustainAmp(Short.parseShort(releaseTime))
                    .hasDecayAndSustain(Boolean.parseBoolean(hasDecayAndSustain))
                    .build();
        } catch (NumberFormatException ex) {
            throw new ObjectInstantiationException(ExceptionMessages.BLUEPRINT_IS_NOT_WELL_FORMED);
        }
    }

}
