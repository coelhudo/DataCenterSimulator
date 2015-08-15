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

import simulator.physical.ActivitiesLogger;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterBuilder;
import simulator.physical.DataCenterPOD;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemBuilder;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemBuilder;
import simulator.system.InteractiveSystem;
import simulator.system.InteractiveSystemBuilder;
import simulator.system.SystemBuilder;
import simulator.system.Systems;

public class SimulatorBuilder {

    private static final Logger LOGGER = Logger.getLogger(SimulatorBuilder.class.getName());

    private DataCenter dataCenter;
    private Environment environment;
    private Systems systems;
    private SLAViolationLogger slaViolationLogger;

    public SimulatorBuilder(Environment environment, SLAViolationLogger slaViolationLogger) {
        this.environment = environment;
        this.systems = new Systems(this.environment);
        this.slaViolationLogger = slaViolationLogger;
    }

    public void buildLogicalDataCenter(String config) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final File file = new File(config);
            Document doc = docBuilder.parse(file);
            String path = file.getParent();
            // normalize text representation
            doc.getDocumentElement().normalize();
            Node node = doc.getDocumentElement();
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    if (childNodes.item(i).getNodeName().equalsIgnoreCase("layout")) {
                        String DCLayout = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                        DataCenterBuilder dataCenterBuilder = new DataCenterBuilder(DCLayout, environment);
                        DataCenterPOD dataCenterPOD = dataCenterBuilder.getDataCenterPOD();
                        ActivitiesLogger activitiesLogger = new ActivitiesLogger("out_W.txt");
                        dataCenter = new DataCenter(dataCenterPOD, activitiesLogger, environment, systems);
                    }
                    if (childNodes.item(i).getNodeName().equalsIgnoreCase("System")) {
                        NodeList nodiLst = childNodes.item(i).getChildNodes();
                        systemConfig(nodiLst, path);
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
    }

    public void systemConfig(NodeList nodiLst, String path) {
        int whichSystem = -1;
        // whichSystem=1 means Enterprise
        // whichSystem=2 means Interactive
        // whichSystem=3 means HPC
        String name = new String();
        for (int i = 0; i < nodiLst.getLength(); i++) {
            if (nodiLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("type")) {
                    String systemType = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                    if (systemType.equalsIgnoreCase("Enterprise")) {
                        whichSystem = 1;
                    } else if (systemType.equalsIgnoreCase("Interactive")) {
                        whichSystem = 2;
                    } else if (systemType.equalsIgnoreCase("HPC")) {
                        whichSystem = 3;
                    }
                }
                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("name")) {
                    name = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                }
                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("configFile")) {
                    String fileName = path + "/" + nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                    switch (whichSystem) {
                    case 1:
                        LOGGER.info("Initialization of Enterprise System Name=" + name);
                        SystemBuilder enterpriseSystemBuilder = new EnterpriseSystemBuilder(fileName);
                        systems.addEnterpriseSystem((EnterpriseSystem) enterpriseSystemBuilder.build(name, dataCenter,
                                environment, slaViolationLogger));
                        break;
                    case 2:
                        LOGGER.info("Initialization of Interactive System Name=" + name);
                        SystemBuilder interactiveSystemBuilder = new InteractiveSystemBuilder(fileName);
                        systems.addInteractiveSystem((InteractiveSystem) interactiveSystemBuilder.build(name, dataCenter,
                                environment, slaViolationLogger));
                        break;
                    case 3:
                        LOGGER.info("Initialization of HPC System Name=" + name);
                        SystemBuilder computeSystemBuilder = new ComputeSystemBuilder(fileName);
                        systems.addComputeSystem((ComputeSystem) computeSystemBuilder.build(name, dataCenter,
                                environment, slaViolationLogger));
                        break;
                    }
                    whichSystem = -1;
                }
            }
        }
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public Systems getSystems() {
        return systems;
    }

}
