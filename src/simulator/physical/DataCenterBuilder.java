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

import simulator.Environment;

public class DataCenterBuilder {

    private static final Logger LOGGER = Logger.getLogger(DataCenterBuilder.class.getName());

    private List<BladeServerPOD> bladeServerPODs = new ArrayList<BladeServerPOD>();
    private List<Chassis> chassis = new ArrayList<Chassis>();

    private int numbOfSofarChassis = 0;
    private int numOfServerSoFar = 0;
    private Environment environment;
    private DataCenterPOD dataCenterPOD;

    public DataCenterBuilder(String config, Environment environment) {
        this.environment = environment;
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
        dataCenterPOD.clearChassis();
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
                    Chassis chs = new Chassis(chassisPOD, -1);
                    setUpChassis(chs);
                    chassis.add(chs);
                }
            }
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if ("Rack".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    setUpRack(childNodes.item(i));

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
        String[] s = null;

        int tedad = 0;
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
                    tedad = number.length;
                }
                if ("BladeType".equalsIgnoreCase(childNodes.item(i).getNodeName())) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();

                    String[] split = str.split(",");
                    s = new String[split.length];
                    System.arraycopy(split, 0, s, 0, split.length);
                }
            }
        }
        for (int j = 0; j < tedad; j++) {
            for (int k = 0; k < number[j]; k++) {
                BladeServerPOD bladeServerPOD = new BladeServerPOD();
                bladeServerPOD.setBladeType(s[j].trim());
                chassisPOD.appendServer(new BladeServer(bladeServerPOD, -1, environment));
            }
        }

        // if(childNodes.item(i).getNodeName().equalsIgnoreCase("bladeServer"))
        // {
        // BladeServer bladeServer = new BladeServer(chassisID);
        //
        // bladeServer.readFromNode(childNodes.item(i));
        // servers.add(bladeServer);
        // }

        return chassisPOD;
    }

    void setUpRack(Node node) {
        NodeList childNodes = node.getChildNodes();
        int k = 0, rackID = 0;
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
                }
            }
        }
        int kk = 0;
        for (int loop = 0; loop < tedad; loop++) {
            for (kk = 0; kk < tedadinRack[loop]; kk++) {
                for (k = 0; k < chassis.size(); k++) {
                    if (s[loop].equalsIgnoreCase(chassis.get(k).getChassisType())) {
                        break;
                    }
                }
                if (k == chassis.size()) {
                    LOGGER.info("ERORE IN CONFIG FILE DATACENTE.java");
                }
                ChassisPOD chassisPOD = new ChassisPOD();
                chassisPOD.setChassisType(chassis.get(k).getChassisType());
                Chassis ch1 = new Chassis(chassisPOD, numbOfSofarChassis + kk);
                cloneChassis(ch1, chassis.get(k));
                ch1.setRackID(rackID);
                for (BladeServer bladeServer : ch1.getServers()) {
                    bladeServer.setChassisID(numbOfSofarChassis + kk);
                    bladeServer.setRackId(rackID);
                }
                dataCenterPOD.appendChassis(ch1);
            }
            numbOfSofarChassis += kk;
        }
    }

    void setUpChassis(Chassis ch) {
        for (int j = 0; j < ch.getServers().size(); j++) {
            for (BladeServerPOD bladeServerPOD : bladeServerPODs) {
                if (ch.getServers().get(j).getBladeType().trim()
                        .equalsIgnoreCase(bladeServerPOD.getBladeType().trim())) {
                    bladeServerPOD.setServerID(j);
                    ch.getServers().get(j).changeInternals(bladeServerPOD);
                    break;
                }
            }
        }
    }

    void cloneChassis(Chassis destiny, Chassis source) {
        for (BladeServer bladeServer : source.getServers()) {
            BladeServerPOD bladeServerPOD = new BladeServerPOD();
            bladeServerPOD.setFrequencyLevel(new double[bladeServer.getNumberOfFrequencyLevel()]);
            bladeServerPOD.setPowerBusy(new double[bladeServer.getNumberOfPowerBusy()]);
            bladeServerPOD.setPowerIdle(new double[bladeServer.getNumberOfPowerIdle()]);
            int numberOfMIPSlevels = bladeServer.getNumberOfFrequencyLevel();

            for (int p = 0; p < numberOfMIPSlevels; p++) {
                bladeServerPOD.setFrequencyLevelAt(p, bladeServer.getFrequencyLevelAt(p));
                bladeServerPOD.setPowerBusyAt(p, bladeServer.getPowerBusyAt(p));
                bladeServerPOD.setPowerIdleAt(p, bladeServer.getPowerIdleAt(p));
            }
            bladeServerPOD.setIdleConsumption(bladeServer.getIdleConsumption());
            bladeServerPOD.setBladeType(bladeServer.getBladeType());
            bladeServerPOD.setServerID(numOfServerSoFar);

            BladeServer a = new BladeServer(bladeServerPOD, -1, environment);
            destiny.getServers().add(a);

            numOfServerSoFar++;
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

        final int numberOfChassis = dataCenterPOD.getNumberOfChassis();
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

    static BladeServerPOD bladeServerParser(Node node) {
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                // if(childNodes.item(i).getNodeName().equalsIgnoreCase("ID"))
                // {
                // serverID =
                // Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                // }
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
