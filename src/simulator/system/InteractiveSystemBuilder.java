package simulator.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.physical.DataCenter;

public class InteractiveSystemBuilder extends SystemBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(InteractiveSystemBuilder.class.getName());
    
    public InteractiveSystemBuilder(String configurationFile) {
        super(configurationFile);
    }

    protected SystemPOD readFromNode(Node node, String path) {
        SystemPOD systemPOD = new InteractiveSystemPOD();
        //getComputeNodeList().clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    systemPOD.setNumberofNode(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                    ((InteractiveSystemPOD) systemPOD).setNumberofIdleNode(systemPOD.getNumberOfNode());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        systemPOD.appendRackID(Integer.parseInt(split[j]));
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg"))
                    ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler"))
                    ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("WorkLoad")) {
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

    @Override
    public GeneralSystem build(String name, DataCenter dataCenter, Environment environment, SLAViolationLogger slaViolationLogger) {
        SystemPOD interactiveSystemPOD = getSystemPOD();                
        GeneralSystem interactiveSystem = InteractiveSystem.Create(interactiveSystemPOD, environment, dataCenter,
                slaViolationLogger);
        interactiveSystem.setName(name);
        return interactiveSystem;
    }

}
