package simulator.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EnterpriseSystemBuilder extends SystemBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystemBuilder.class.getName());
    
    public EnterpriseSystemBuilder(String configurationFile, String name) {
        super(configurationFile, name);
    }
    
    protected SystemPOD readFromNode(Node node, String path) {
        SystemPOD systemPOD = new EnterpriseSystemPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("ComputeNode".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    systemPOD.setNumberofNode(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
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
                if ("EnterpriseApplication".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    EnterpriseApplicationPOD enterpriseApplicationPOD = getEnterpriseApplicationPOD(childNodes.item(i), path);
                    ((EnterpriseSystemPOD) systemPOD).appendEnterpriseApplicationPOD(enterpriseApplicationPOD);
                }
            }
        }
        
        return systemPOD;
    }
    
    EnterpriseApplicationPOD getEnterpriseApplicationPOD(Node node, String path) {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("id".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    enterpriseApplicationPOD.setID(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); // Id
                    // of
                    // the
                    // application
                }
                if ("EnterpriseApplicationWorkLoad".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        enterpriseApplicationPOD.setBIS(new BufferedReader(new InputStreamReader(new FileInputStream(logFile))));
                    } catch (IOException e) {
                        LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
                    }
                }
                if ("MaxNumberOfRequest".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    enterpriseApplicationPOD.setMaxNumberOfRequest(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if ("NumberofBasicNode".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    enterpriseApplicationPOD.setNumberofBasicNode(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if ("timeTreshold".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    enterpriseApplicationPOD.setTimeTreshold(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                    enterpriseApplicationPOD.setMaxExpectedResTime(enterpriseApplicationPOD.getTimeTreshold());
                }
                if ("Percentage".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    enterpriseApplicationPOD.setSLAPercentage(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                } // We dont have server list now but may be in future we had
                if ("minProcessor".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    enterpriseApplicationPOD.setMinProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if ("maxProcessor".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    enterpriseApplicationPOD.setMaxProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }

            }
        }
        
        return enterpriseApplicationPOD;
    }
}
