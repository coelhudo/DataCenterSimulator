package simulator.system;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class SystemBuilder {

    private static final Logger LOGGER = Logger.getLogger(GeneralSystem.class.getName());
    private String configurationFile;
    protected File logFile;
    
    public SystemBuilder(String configurationFile) {
        this.configurationFile = configurationFile;
    }
    
    public SystemPOD build() {
        SystemPOD systemPOD = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final File file = new File(configurationFile);
            Document doc = docBuilder.parse(file);
            String path = file.getParent();
            // normalize text representation
            doc.getDocumentElement().normalize();
            systemPOD = readFromNode(doc.getDocumentElement(), path);
        } catch (ParserConfigurationException ex) {
            LOGGER.severe(ex.getMessage());
        } catch (SAXException ex) {
            LOGGER.severe(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
        
        assert(systemPOD != null);
        return systemPOD;
    }

    protected abstract SystemPOD readFromNode(Node node, String path);
}
