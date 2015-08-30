package simulator.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InteractiveSystemBuilder extends SystemBuilder {

    private static final Logger LOGGER = Logger.getLogger(InteractiveSystemBuilder.class.getName());

    public InteractiveSystemBuilder(String configurationFile, String name) {
        super(configurationFile, name);
    }

    protected SystemPOD readFromNode(Node node, String path) {
        SystemPOD systemPOD = new InteractiveSystemPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("ComputeNode".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    systemPOD.setNumberofNode(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                    ((InteractiveSystemPOD) systemPOD).setNumberofIdleNode(systemPOD.getNumberOfNode());
                }
                if ("Rack".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        systemPOD.appendRackID(Integer.parseInt(split[j]));
                    }
                }
                if ("ResourceAllocationAlg".equalsIgnoreCase(childNodes.item(i).getNodeName()))
                    ;
                if ("Scheduler".equalsIgnoreCase(childNodes.item(i).getNodeName()))
                    ;
                if ("WorkLoad".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        systemPOD.setBis(new BufferedReader(new InputStreamReader(new FileInputStream(logFile))));
                    } catch (IOException e) {
                        LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
                    }
                }
            }
        }

        return systemPOD;
    }
}
