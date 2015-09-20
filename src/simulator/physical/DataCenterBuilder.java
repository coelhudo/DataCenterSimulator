package simulator.physical;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class DataCenterBuilder {

    private static final Logger LOGGER = Logger.getLogger(DataCenterBuilder.class.getName());

    private List<BladeServerPOD> bladeServerPODs = new ArrayList<BladeServerPOD>();
    private List<ChassisPOD> chassisPODs = new ArrayList<ChassisPOD>();

    private int numberOfServersSoFar = 0;
    private int numbOfSofarChassis = 0;
    private DataCenterPOD dataCenterPOD;

    public DataCenterBuilder(String config) {
        dataCenterPOD = new DataCenterPOD();
        parseXmlConfig(config);
    }

    void parseXmlConfig(String config) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final File file = new File(config);
            Document doc = docBuilder.parse(file);
            String path = file.getParent();
            // normalize text representation
            doc.getDocumentElement().normalize();
            parseDataCenter(doc.getDocumentElement(), path);

        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, this.getClass().getName(), ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, this.getClass().getName(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, this.getClass().getName(), ex);
        }
    }

    void parseDataCenter(Node node, String path) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("BladeServer".equalsIgnoreCase(childNodes.item(i).getNodeName())) {

                    BladeServerPOD bladeServerPOD = bladeServerParser(childNodes.item(i));
                    bladeServerPODs.add(bladeServerPOD);
                }
            }
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("Chassis".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    ChassisPOD chassisPOD = parseChassis(childNodes.item(i));
                    loadBladeServersIntoChassis(chassisPOD);
                    chassisPODs.add(chassisPOD);
                }
            }
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("Rack".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    loadChassisIntoRack(childNodes.item(i));

                }
                if ("ThermalModel".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String thermalModelFileName = path + "/"
                            + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    getDmatrix(thermalModelFileName);
                }
                if ("RedTemperature".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    dataCenterPOD.setRedTemperature(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
            }
        }
    }

    public ChassisPOD parseChassis(Node node) {
        ChassisPOD chassisPOD = new ChassisPOD();
        int[] number = null;

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("ChassisType".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    chassisPOD.setChassisType(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if ("numberOfBladeServer".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    number = new int[split.length];
                    for (int j = 0; j < split.length; j++) {
                        number[j] = Integer.parseInt(split[j]);
                    }
                }
                if ("BladeType".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String bladeType = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    chassisPOD.setBladeType(bladeType);
                }
            }
        }

        return chassisPOD;
    }

    void loadChassisIntoRack(Node node) {
        RackPOD rackPOD = new RackPOD();
        NodeList childNodes = node.getChildNodes();
        int rackID = 0;
        int tedad = 0;
        int[] tedadinRack = null;
        String[] s = null;
        int i = 0;
        for (i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("ChassisType".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    s = new String[split.length];
                    System.arraycopy(split, 0, s, 0, split.length);
                }
                if ("NumberOfChassis".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    tedadinRack = new int[split.length];
                    for (int j = 0; j < split.length; j++) {
                        tedadinRack[j] = Integer.parseInt(split[j]);
                    }
                    tedad = tedadinRack.length;
                }
                if ("Rack_ID".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    rackID = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                    rackPOD.setID(rackID);
                }
            }
        }
        int kk = 0;
        for (int loop = 0; loop < tedad; loop++) {
            for (kk = 0; kk < tedadinRack[loop]; kk++) {
                for (ChassisPOD currentChassisPOD :  chassisPODs) {
                    if (s[loop].equalsIgnoreCase(currentChassisPOD.getChassisType())) {
                        loadChassisIntoDataCenter(currentChassisPOD, rackPOD, kk);
                    }
                }

            }
            numbOfSofarChassis += kk;
        }
        dataCenterPOD.appendRack(rackPOD);
    }
    
    void loadChassisIntoDataCenter(ChassisPOD currentChassisPOD, RackPOD rackPOD, int kk) {
        ChassisPOD chassisPOD = new ChassisPOD(currentChassisPOD);
        chassisPOD.setID(numbOfSofarChassis + kk);
        chassisPOD.setRackID(rackPOD.getRackID());
        for(BladeServerPOD bladeServerPOD : chassisPOD.getServerPODs()) {
            bladeServerPOD.setServerID(numberOfServersSoFar);
            numberOfServersSoFar++;
        }
        rackPOD.appendChassis(chassisPOD);
        dataCenterPOD.appendChassis(chassisPOD);
    }

    void loadBladeServersIntoChassis(ChassisPOD chassis) {
        for (BladeServerPOD bladeServerPOD : bladeServerPODs) {
            if (chassis.getBladeType().trim().equalsIgnoreCase(bladeServerPOD.getBladeType().trim())) {
                chassis.appendServerPOD(bladeServerPOD);
                break;
            }
        }

    }

    boolean getDmatrix(String matrixDFileName) {
        BufferedReader bis = null;
        try {
            File f = new File(matrixDFileName);
            bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, this.getClass().getName(), e);
        }

        final int numberOfChassis = countChassis();
        for (int k = 0; k < numberOfChassis; k++) {
            try {
                String line = bis.readLine();
                if (line == null) {
                    return false;
                }
                String[] numbers = line.split("\t");
                if (numbers.length < numberOfChassis) {
                    return false;
                }
                for (int i = 0; i < numberOfChassis; i++) {
                    if (Double.parseDouble(numbers[i]) > 0) {
                        dataCenterPOD.setD(k, i, 13 * Double.parseDouble(numbers[i]));
                    } else {
                        dataCenterPOD.setD(k, i, 0);
                    }
                }
            } catch (IOException ex) {
                LOGGER.info("readJOB EXC readJOB false ");
                LOGGER.log(Level.SEVERE, this.getClass().getName(), ex);
                return false;
            }
        }
        return true;
    }
    
    private int countChassis() {
        int count = 0;
        for(RackPOD rackPOD : dataCenterPOD.getRackPODs()) {
            count += rackPOD.getChassisPODs().size();
        }
        
        return count;
    }

    static BladeServerPOD bladeServerParser(Node node) {
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("BladeType".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    bladeServerPOD.setBladeType(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if ("MIPS".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    bladeServerPOD.setFrequencyLevel(new double[split.length]);
                    for (int j = 0; j < split.length; j++) {
                        bladeServerPOD.setFrequencyLevelAt(j, Double.parseDouble(split[j]));
                    }
                }
                if ("FullyLoaded".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    bladeServerPOD.setPowerBusy(new double[split.length]);
                    for (int j = 0; j < split.length; j++) {
                        bladeServerPOD.setPowerBusyAt(j, Double.parseDouble(split[j]));
                    }
                }
                if ("Idle".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    bladeServerPOD.setPowerIdle(new double[split.length]);
                    for (int j = 0; j < split.length; j++) {
                        bladeServerPOD.setPowerIdleAt(j, Double.parseDouble(split[j]));
                    }
                }
                if ("Standby".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    bladeServerPOD.setIdleConsumption(
                            Double.parseDouble(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));

                }
            }
        }

        return bladeServerPOD;
    }

    public DataCenterPOD getDataCenterPOD() {
        return dataCenterPOD;
    }
}
