package simulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import simulator.physical.DataCenter;

public class DataCenterBuilder {

    private DataCenter dataCenter;
    private List<InteractiveSystem> interactiveSystems = new ArrayList<InteractiveSystem>();
    private List<EnterpriseSystem> enterpriseSystems = new ArrayList<EnterpriseSystem>();
    private List<ComputeSystem> computeSystems = new ArrayList<ComputeSystem>();
    private Simulator.Environment environment;

    public DataCenterBuilder(Simulator.Environment environment) {
        this.environment = environment;
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
                        dataCenter = new DataCenter(DCLayout, environment);
                    }
                    if (childNodes.item(i).getNodeName().equalsIgnoreCase("System")) {
                        NodeList nodiLst = childNodes.item(i).getChildNodes();
                        systemConfig(nodiLst, path);
                    }
                }
            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
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
                        System.out.println("------------------------------------------");
                        System.out.println("Initialization of Enterprise System Name=" + name);
                        EnterpriseSystem ES1 = EnterpriseSystem.Create(fileName, environment, dataCenter);
                        ES1.setName(name);
                        getEnterpriseSystems().add(ES1);
                        whichSystem = -1;
                        break;
                    case 2:
                        System.out.println("------------------------------------------");
                        System.out.println("Initialization of Interactive System Name=" + name);
                        InteractiveSystem wb1 = InteractiveSystem.Create(fileName, environment, dataCenter);
                        wb1.setName(name);
                        getInteractiveSystems().add(wb1);
                        whichSystem = -1;
                        break;
                    case 3:
                        System.out.println("------------------------------------------");
                        System.out.println("Initialization of HPC System Name=" + name);
                        ComputeSystem CP = ComputeSystem.Create(fileName, environment, dataCenter);
                        CP.setName(name);
                        getComputeSystems().add(CP);
                        whichSystem = -1;
                        break;
                    }
                }
            }
        }
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public List<InteractiveSystem> getInteractiveSystems() {
        return interactiveSystems;
    }

    public List<EnterpriseSystem> getEnterpriseSystems() {
        return enterpriseSystems;
    }

    public List<ComputeSystem> getComputeSystems() {
        return computeSystems;
    }

}
