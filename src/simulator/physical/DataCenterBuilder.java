package simulator.physical;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

    private List<BladeServer> BSTemp = new ArrayList<BladeServer>();
    private List<Chassis> CHSTemp = new ArrayList<Chassis>();

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
            readFromNode(doc.getDocumentElement(), path);

        } catch (ParserConfigurationException ex) {
            LOGGER.severe(ex.getMessage());
        } catch (SAXException ex) {
            LOGGER.severe(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }

    }

    void readFromNode(Node node, String path) {
        dataCenterPOD.clearChassis();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("BladeServer")) {
                    
                    BladeServerPOD bladeServerPOD = bladeServerParser(childNodes.item(i));
                    BladeServer bs = new BladeServer(bladeServerPOD, -1, environment);
                    BSTemp.add(bs);
                }
            }
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Chassis")) {
                    Chassis chs = new Chassis(-1, environment);
                    chs.readFromNode(childNodes.item(i));
                    setUpChassis(chs);
                    CHSTemp.add(chs);
                }
            }
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    setUpRack(childNodes.item(i));

                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ThermalModel")) {
                    String DFileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    getDmatrix(DFileName);
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("RedTemperature")) {
                    dataCenterPOD.setRedTemperature(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
            }
        }
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
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ChassisType")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    s = new String[split.length];
                    System.arraycopy(split, 0, s, 0, split.length);
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("NumberOfChassis")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    tedadinRack = new int[split.length];
                    for (int j = 0; j < split.length; j++) {
                        tedadinRack[j] = Integer.parseInt(split[j]);
                    }
                    tedad = tedadinRack.length;
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack_ID")) {
                    rackID = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
            }
        }
        int kk = 0;
        for (int loop = 0; loop < tedad; loop++) {
            for (kk = 0; kk < tedadinRack[loop]; kk++) {
                for (k = 0; k < CHSTemp.size(); k++) {
                    if (s[loop].equalsIgnoreCase(CHSTemp.get(k).getChassisType())) {
                        break;
                    }
                }
                if (k == CHSTemp.size()) {
                    LOGGER.info("ERORE IN CONFIG FILE DATACENTE.java");
                }
                Chassis ch1 = new Chassis(numbOfSofarChassis + kk, environment);
                cloneChassis(ch1, CHSTemp.get(k));
                ch1.setRackID(rackID);
                for (int inx = 0; inx < ch1.getServers().size(); inx++) {
                    ch1.getServers().get(inx).setChassisID(numbOfSofarChassis + kk);
                    ch1.getServers().get(inx).setRackId(rackID);
                }
                dataCenterPOD.appendChassis(ch1);
            }
            numbOfSofarChassis += kk;
        }
    }

    void setUpChassis(Chassis ch) {
        for (int j = 0; j < ch.getServers().size(); j++) {
            int i;
            for (i = 0; i < BSTemp.size(); i++) {
                if (ch.getServers().get(j).getBladeType().trim()
                        .equalsIgnoreCase(BSTemp.get(i).getBladeType().trim())) {
                    break;
                }
            }
            if (i == BSTemp.size()) {
                LOGGER.info("DataCenter.java");
            }
            BladeServerPOD bladeServerPOD = new BladeServerPOD();
            bladeServerPOD.setFrequencyLevel(new double[BSTemp.get(i).getNumberOfFrequencyLevel()]);
            bladeServerPOD.setPowerBusy(new double[BSTemp.get(i).getNumberOfPowerBusy()]);
            bladeServerPOD.setPowerIdle(new double[BSTemp.get(i).getNumberOfPowerIdle()]);
            for (int p = 0; p < BSTemp.get(i).getNumberOfFrequencyLevel(); p++) {
                bladeServerPOD.setFrequencyLevelAt(p, BSTemp.get(i).getFrequencyLevelAt(p));
                bladeServerPOD.setPowerBusyAt(p, BSTemp.get(i).getPowerBusyAt(p));
                bladeServerPOD.setPowerIdleAt(p, BSTemp.get(i).getPowerIdleAt(p));
            }
            bladeServerPOD.setIdleConsumption(BSTemp.get(i).getIdleConsumption());
            bladeServerPOD.setServerID(j);
            bladeServerPOD.setBladeType(BSTemp.get(i).getBladeType());
            ch.getServers().get(j).changeInternals(bladeServerPOD);
        }

    }
    
    void cloneChassis(Chassis A, Chassis B) // A<--B
    {
        for (int i = 0; i < B.getServers().size(); i++) {
            BladeServerPOD bladeServerPOD = new BladeServerPOD();
            bladeServerPOD.setFrequencyLevel(new double[B.getServers().get(i).getNumberOfFrequencyLevel()]);
            bladeServerPOD.setPowerBusy(new double[B.getServers().get(i).getNumberOfPowerBusy()]);
            bladeServerPOD.setPowerIdle(new double[B.getServers().get(i).getNumberOfPowerIdle()]);
            int numberOfMIPSlevels = B.getServers().get(i).getNumberOfFrequencyLevel();
            //
            
            for (int p = 0; p < numberOfMIPSlevels; p++) {
                bladeServerPOD.setFrequencyLevelAt(p, B.getServers().get(i).getFrequencyLevelAt(p));
                bladeServerPOD.setPowerBusyAt(p, B.getServers().get(i).getPowerBusyAt(p));
                bladeServerPOD.setPowerIdleAt(p, B.getServers().get(i).getPowerIdleAt(p));
            }
            bladeServerPOD.setIdleConsumption(B.getServers().get(i).getIdleConsumption());
            bladeServerPOD.setBladeType(B.getServers().get(i).getBladeType());
            bladeServerPOD.setServerID(numOfServerSoFar);
            
            BladeServer a = new BladeServer(bladeServerPOD, -1, environment);
            A.getServers().add(a);

            numOfServerSoFar++;
        }
    }
    
    boolean getDmatrix(String DFileName) {
        BufferedReader bis = null;
        try {
            File f = new File(DFileName);
            bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (IOException e) {
            LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
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
                LOGGER.severe(ex.getMessage());
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
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("BladeType")) {
                    bladeServerPOD.setBladeType(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("MIPS")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    bladeServerPOD.setFrequencyLevel(new double[split.length]);
                    for (int j = 0; j < split.length; j++) {
                        bladeServerPOD.setFrequencyLevelAt(j, Double.parseDouble(split[j]));
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("FullyLoaded")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    bladeServerPOD.setPowerBusy(new double[split.length]);
                    for (int j = 0; j < split.length; j++) {
                        bladeServerPOD.setPowerBusyAt(j, Double.parseDouble(split[j]));
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Idle")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    bladeServerPOD.setPowerIdle(new double[split.length]);
                    for (int j = 0; j < split.length; j++) {
                        bladeServerPOD.setPowerIdleAt(j, Double.parseDouble(split[j]));
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Standby")) {
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
