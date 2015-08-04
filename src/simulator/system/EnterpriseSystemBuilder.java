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
    
    public EnterpriseSystemBuilder(String configurationFile) {
        super(configurationFile);
    }
    
    protected SystemPOD readFromNode(Node node, String path) {
        SystemPOD systemPOD = new EnterpriseSystemPOD();
        //getComputeNodeList().clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
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
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg"))
                    ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler"))
                    ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplication")) {
                    EnterpriseApplicationPOD enterpriseApplicationPOD = getEnterpriseApplicationPOD(childNodes.item(i), path);
                    ((EnterpriseSystemPOD) systemPOD).appendEnterpriseApplicationPOD(enterpriseApplicationPOD);
                }
            }
        }
        
        return systemPOD;
    }
    
    EnterpriseApplicationPOD getEnterpriseApplicationPOD(Node node, String path) {
        //getComputeNodeList().clear();
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("id")) {
                    enterpriseApplicationPOD.setID(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); // Id
                    // of
                    // the
                    // application
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplicationWorkLoad")) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        enterpriseApplicationPOD.setBIS(new BufferedReader(new InputStreamReader(new FileInputStream(logFile))));
                    } catch (IOException e) {
                        LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("MaxNumberOfRequest")) {
                    enterpriseApplicationPOD.setMaxNumberOfRequest(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("NumberofBasicNode")) {
                    enterpriseApplicationPOD.setNumberofBasicNode(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("timeTreshold")) {
                    enterpriseApplicationPOD.setTimeTreshold(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                    enterpriseApplicationPOD.setMaxExpectedResTime(enterpriseApplicationPOD.getTimeTreshold());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Percentage")) {
                    enterpriseApplicationPOD.setSLAPercentage(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                } // We dont have server list now but may be in future we had
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("minProcessor")) {
                    enterpriseApplicationPOD.setMinProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("maxProcessor")) {
                    enterpriseApplicationPOD.setMaxProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }

            }
        }
        
        return enterpriseApplicationPOD;
    }

}
