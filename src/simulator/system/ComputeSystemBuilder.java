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
import simulator.jobs.BatchJobProducer;

public class ComputeSystemBuilder extends SystemBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(ComputeSystemBuilder.class.getName());
    private Environment environment;
    public ComputeSystemBuilder(String configurationFile, String name, Environment environment) {
        super(configurationFile, name);
        this.environment = environment;
    }

    protected SystemPOD readFromNode(Node node, String path) {
        SystemPOD systemPOD = new ComputeSystemPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg"))
                    ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler"))
                    ; // TODO
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Workload")) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
                        systemPOD.setBis(bufferedReader);
                        systemPOD.setJobProducer(new BatchJobProducer(environment, bufferedReader));
                    } catch (IOException e) {
                        LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    systemPOD.setNumberofNode(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        systemPOD.appendRackID(Integer.parseInt(split[j]));
                    }
                }
            }
        }
        
        return systemPOD;
    }
}
