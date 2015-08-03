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

    private List<Chassis> chassisSet = new ArrayList<Chassis>();
    private List<BladeServer> BSTemp = new ArrayList<BladeServer>();
    private List<Chassis> CHSTemp = new ArrayList<Chassis>();

    private int numbOfSofarChassis = 0;
    private int numOfServerSoFar = 0;
    private int redTemperature;
    private Environment environment;
    private double[][] D;
    
    public DataCenterBuilder(String config, Environment environment) {
        this.environment = environment;
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
        chassisSet.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("BladeServer")) {
                    BladeServer bs = new BladeServer(-1, environment);
                    bs.readFromNode(childNodes.item(i));
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
                    redTemperature = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
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
                chassisSet.add(ch1);
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
            ch.getServers().get(j).setFrequencyLevel(new double[BSTemp.get(i).getFrequencyLevel().length]);
            ch.getServers().get(j).setPowerBusy(new double[BSTemp.get(i).getPowerBusy().length]);
            ch.getServers().get(j).setPowerIdle(new double[BSTemp.get(i).getPowerIdle().length]);
            for (int p = 0; p < BSTemp.get(i).getFrequencyLevel().length; p++) {
                ch.getServers().get(j).getFrequencyLevel()[p] = BSTemp.get(i).getFrequencyLevel()[p];
                ch.getServers().get(j).getPowerBusy()[p] = BSTemp.get(i).getPowerBusy()[p];
                ch.getServers().get(j).getPowerIdle()[p] = BSTemp.get(i).getPowerIdle()[p];
            }
            ch.getServers().get(j).setIdleConsumption(BSTemp.get(i).getIdleConsumption());
            ch.getServers().get(j).setServerID(j);
            ch.getServers().get(j).setBladeType(BSTemp.get(i).getBladeType());
        }

    }
    
    void cloneChassis(Chassis A, Chassis B) // A<--B
    {
        for (int i = 0; i < B.getServers().size(); i++) {
            BladeServer a = new BladeServer(i, environment);
            //
            a.setFrequencyLevel(new double[B.getServers().get(i).getFrequencyLevel().length]);
            a.setPowerBusy(new double[B.getServers().get(i).getPowerBusy().length]);
            a.setPowerIdle(new double[B.getServers().get(i).getPowerIdle().length]);
            int numberOfMIPSlevels = B.getServers().get(i).getFrequencyLevel().length;
            //
            A.getServers().add(a);

            for (int p = 0; p < numberOfMIPSlevels; p++) {
                A.getServers().get(i).getFrequencyLevel()[p] = B.getServers().get(i).getFrequencyLevel()[p];
                A.getServers().get(i).getPowerBusy()[p] = B.getServers().get(i).getPowerBusy()[p];
                A.getServers().get(i).getPowerIdle()[p] = B.getServers().get(i).getPowerIdle()[p];
            }
            A.getServers().get(i).setIdleConsumption(B.getServers().get(i).getIdleConsumption());
            A.getServers().get(i).setBladeType(B.getServers().get(i).getBladeType());
            A.getServers().get(i).setServerID(numOfServerSoFar);
            numOfServerSoFar++;
        }
    }
    
    boolean getDmatrix(String DFileName) {
        int m = chassisSet.size();
        D = new double[m][m];
        BufferedReader bis = null;
        try {
            File f = new File(DFileName);
            bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (IOException e) {
            LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());

        } finally {
        }
        int i = 0, k = 0;
        for (k = 0; k < m; k++) {
            try {
                String line = bis.readLine();
                if (line == null) {
                    return false;
                }
                String[] numbers = line.split("\t");
                if (numbers.length < m) {
                    return false;
                }
                for (i = 0; i < m; i++) {

                    if (Double.parseDouble(numbers[i]) > 0) {
                        D[k][i] = 13 * Double.parseDouble(numbers[i]);
                    } else {
                        D[k][i] = 0;
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

    
    public List<Chassis> getChassis() {
        return chassisSet;
    }

    public int getRedTemperature() {
        return redTemperature;
    }
    
    public double[][] getD() {
        return D;
    }
}
