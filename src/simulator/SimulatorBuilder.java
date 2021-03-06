package simulator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import simulator.physical.DataCenterBuilder;
import simulator.physical.DataCenterPOD;
import simulator.system.ComputeSystemBuilder;
import simulator.system.ComputeSystemPOD;
import simulator.system.EnterpriseSystemBuilder;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.InteractiveSystemBuilder;
import simulator.system.InteractiveSystemPOD;
import simulator.system.SystemBuilder;
import simulator.system.SystemsPOD;

public class SimulatorBuilder {

    private static final Logger LOGGER = Logger.getLogger(SimulatorBuilder.class.getName());

    private String configurationFile;

    public SimulatorBuilder(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    public SimulatorPOD build() {
        SimulatorPOD simulatorPOD = new SimulatorPOD();
        SystemsPOD systemsPOD = new SystemsPOD();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final File file = new File(configurationFile);
            Document doc = docBuilder.parse(file);
            String path = file.getParent();
            // normalize text representation
            doc.getDocumentElement().normalize();
            Node node = doc.getDocumentElement();
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    if ("layout".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                        String dataCenterLayout = path + "/"
                                + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder(dataCenterLayout);
                        DataCenterPOD dataCenterPOD = dataCenterBuilder.getDataCenterPOD();
                        simulatorPOD.setDataCenterPOD(dataCenterPOD);
                    }
                    if ("System".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                        NodeList nodiLst = childNodes.item(i).getChildNodes();
                        systemConfig(nodiLst, path, systemsPOD);
                    }
                }
            }
        } catch (ParserConfigurationException ex) {
            LOGGER.severe(ex.getMessage());
        } catch (SAXException ex) {
            LOGGER.severe(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }

        simulatorPOD.setSystemsPOD(systemsPOD);
        return simulatorPOD;
    }

    public void systemConfig(NodeList nodiLst, String path, SystemsPOD systemsPOD) {
        int whichSystem = -1;
        // whichSystem=1 means Enterprise
        // whichSystem=2 means Interactive
        // whichSystem=3 means HPC
        String name = new String();
        for (int i = 0; i < nodiLst.getLength(); i++) {
            if (nodiLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("type")) {
                    String systemType = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                    if ("Enterprise".equalsIgnoreCase(systemType)) {
                        whichSystem = 1;
                    } else if ("Interactive".equalsIgnoreCase(systemType)) {
                        whichSystem = 2;
                    } else if ("HPC".equalsIgnoreCase(systemType)) {
                        whichSystem = 3;
                    }
                }
                if ("name".equalsIgnoreCase(nodiLst.item(i).getNodeName())) {
                    name = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                }
                if ("configFile".equalsIgnoreCase(nodiLst.item(i).getNodeName())) {
                    String fileName = path + "/" + nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                    switch (whichSystem) {
                    case 1:
                        LOGGER.info("Initialization of Enterprise System Name=" + name);
                        SystemBuilder enterpriseSystemBuilder = new EnterpriseSystemBuilder(fileName, name);
                        systemsPOD.appendEnterprisePOD((EnterpriseSystemPOD) enterpriseSystemBuilder.getSystemPOD());
                        break;
                    case 2:
                        LOGGER.info("Initialization of Interactive System Name=" + name);
                        SystemBuilder interactiveSystemBuilder = new InteractiveSystemBuilder(fileName, name);
                        systemsPOD.appendInteractivePOD((InteractiveSystemPOD) interactiveSystemBuilder.getSystemPOD());
                        break;
                    case 3:
                        LOGGER.info("Initialization of HPC System Name=" + name);
                        SystemBuilder computeSystemBuilder = new ComputeSystemBuilder(fileName, name);
                        systemsPOD.appendComputeSystemPOD((ComputeSystemPOD) computeSystemBuilder.getSystemPOD());
                        break;
                    }
                    whichSystem = -1;
                }
            }
        }
    }
}
