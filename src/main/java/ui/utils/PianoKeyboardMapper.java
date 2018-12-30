package ui.utils;

import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PianoKeyboardMapper {

    private List<KeyMapping> mappingList = new ArrayList<>();
    private Map<String, KeyMapping> mappingTable = new HashMap<>();

    public PianoKeyboardMapper(File xmlMappingFile) {
        loadFromFile(xmlMappingFile);
    }

    private void loadFromFile(File xmlMappingFile) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(xmlMappingFile);
            doc.getDocumentElement().normalize();
            NodeList mappings = doc.getElementsByTagName("mapping");
            for (int i = 0; i < mappings.getLength(); i++) {
                Node node = mappings.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element mapping = (Element) node;
                    String note = mapping.getAttribute("note");
                    float pitch = Float.parseFloat(mapping.getAttribute("pitch"));
                    String key = mapping.getAttribute("key");
                    KeyLayout layout = KeyLayout.valueOf(mapping.getAttribute("layout"));
                    KeyMapping keyMapping = new KeyMapping(note, pitch, key, layout);
                    this.mappingList.add(keyMapping);
                    if (!key.equals("")) {
                        this.mappingTable.put(key, keyMapping);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Getter
    public class KeyMapping {
        private String note;
        private float pitch;
        private String key;
        private KeyLayout layout;

        public KeyMapping(String note, float pitch, String key, KeyLayout layout) {
            this.note = note;
            this.pitch = pitch;
            this.key = key;
            this.layout = layout;
        }
    }

    @Getter
    public enum KeyLayout {
        NATURAL("M0,0 L17,0 L17,100 L0,100 z", "natural"),
        NATURAL_LEFT("M0,0 L12,0 L12,57 L17,57 L17,100 L0,100 z", "natural"),
        NATURAL_MIDDLE("M5,0 L12,0 L12,57 L17,57 L17,100 L0,100 L0,57 L5,57 z", "natural"),
        NATURAL_RIGHT("M5,0 L17,0 L17,100 L0,100 L0,57 L5,57 z", "natural"),
        ALTERED("M0,0 L10,0 L10,57 L0,57 L0,0", "altered");

        private String svgPath;
        private String cssClass;

        KeyLayout(String svgPath, String cssClass) {
            this.svgPath = svgPath;
            this.cssClass = cssClass;
        }

    }
}