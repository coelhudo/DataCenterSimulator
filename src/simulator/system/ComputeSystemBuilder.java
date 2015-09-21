package simulator.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulator.jobs.BatchJobProducer;
import simulator.jobs.JobProducer;
import simulator.physical.DataCenterEntityID;

public class ComputeSystemBuilder extends SystemBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(ComputeSystemBuilder.class.getName());
    public ComputeSystemBuilder(String configurationFile, String name) {
        super(configurationFile, name);
    }

    protected SystemPOD readFromNode(Node node, String path) {
        ComputeSystemPOD systemPOD = new ComputeSystemPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("ResourceAllocationAlg".equalsIgnoreCase(childNodes.item(i).getNodeName()))
                    ;
                if ("Scheduler".equalsIgnoreCase(childNodes.item(i).getNodeName()))
                    ; // TODO
                if ("Workload".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
                        JobProducer jobProducer = new BatchJobProducer(bufferedReader);
                        jobProducer.loadJobs();
                        systemPOD.setJobProducer(jobProducer);
                    } catch (IOException e) {
                        LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
                    }
                }
                if ("ComputeNode".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    systemPOD.setNumberofNode(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if ("Rack".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        systemPOD.appendRackID(Integer.parseInt(split[j]));
                        systemPOD.appendRackID(DataCenterEntityID.createRackID(Integer.parseInt(split[j]) + 1));
                    }
                }
            }
        }
        
        return systemPOD;
    }
}
