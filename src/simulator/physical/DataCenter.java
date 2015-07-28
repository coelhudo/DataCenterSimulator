package simulator.physical;

import simulator.am.DataCenterAM;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import simulator.Simulator;

public final class DataCenter {

    private int overRed = 0;
    private double totalPowerConsumption = 0;
    private Cooler cooler1 = new Cooler();
    private List<Chassis> chassisSet = new ArrayList<Chassis>();
    private List<BladeServer> BSTemp = new ArrayList<BladeServer>();
    private List<Chassis> CHSTemp = new ArrayList<Chassis>();
    private int redTemperature;
    private FileOutputStream fos;
    private OutputStreamWriter oos;
    private int numbOfSofarChassis = 0;
    private int numOfServerSoFar = 0;
    private double[][] D;
    private DataCenterAM am;
    ///////////////////////////
    private Simulator.Environment environment;

    public DataCenter(String config, Simulator.Environment environment) {
        // output file for writing total DC power consumption
        am = new DataCenterAM(environment);
        String s = "out_W.txt";
        File destinationFile = new File(s);
        try {
            fos = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.environment = environment;
        oos = new OutputStreamWriter(fos);
        // reading config file to set the parameters
        parseXmlConfig(config);
    }

    int getServerIndex(int i) {
        return i % chassisSet.get(0).getServers().size();
    }

    int getChasisIndex(int i) {
        return i / chassisSet.get(0).getServers().size();
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
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
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

    void setUpChassis(Chassis ch) {
        for (int j = 0; j < ch.getServers().size(); j++) {
            int i;
            for (i = 0; i < BSTemp.size(); i++) {
                if (ch.getServers().get(j).bladeType.trim().equalsIgnoreCase(BSTemp.get(i).bladeType.trim())) {
                    break;
                }
            }
            if (i == BSTemp.size()) {
                System.out.println("DataCenter.java");
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
            ch.getServers().get(j).bladeType = BSTemp.get(i).bladeType;
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
                    if (s[loop].equalsIgnoreCase(CHSTemp.get(k).chassisType)) {
                        break;
                    }
                }
                if (k == CHSTemp.size()) {
                    System.out.println("ERORE IN CONFIG FILE DATACENTE.java");
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
            A.getServers().get(i).bladeType = B.getServers().get(i).bladeType;
            A.getServers().get(i).setServerID(numOfServerSoFar);
            numOfServerSoFar++;
        }
    }

    public void calculatePower() {
        int m = chassisSet.size();
        double computingPower = 0;
        double[] temprature = new double[m];
        double maxTemp = 0;
        for (int i = 0; i < m; i++) {
            double temp = chassisSet.get(i).power();
            // if(chassisSet.get(i).getServers().get(0).currentCPU!=0)
            // System.out.println(chassisSet.get(i).servers.get(0).currentCPU +"
            // \ttime ="+Main.localTime +" \t chassi
            // id="+chassisSet.get(i).chassisID );

            try {
                oos.write((int) temp + "\t");
            } catch (IOException ex) {
                Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
            }
            computingPower = computingPower + temp;
        }
        // System.out.println("in betweeeeeen");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temprature[i] = temprature[i] + D[i][j] * chassisSet.get(j).power();
                // System.out.println(chassis_list[i]);
            }
        }
        maxTemp = temprature[0];
        for (int i = 0; i < m; i++) {
            if (maxTemp < temprature[i]) {
                maxTemp = temprature[i];
            }
        }
        // System.out.println(maxTepm);
        maxTemp = redTemperature - maxTemp;
        if (maxTemp <= 0) {
            // System.out.println("maxTem less than 0000 " + maxTemp);
            am.setSlowDownFromCooler(true);
            overRed++;

        } else {
            am.setSlowDownFromCooler(false);
        }
        double cop = cooler1.getCOP(maxTemp);
        try {
            // System.out.println(((int)(Pcomp*(1+1.0/COP)))+"\t"+(int)Pcomp+"\t"+localTime);
            // oos.write(Integer.toString((int)
            // (Pcomp*(1+1.0/COP)))+"\t"+Integer.toString((int)Pcomp)+"\t"+localTime+"\t"+perc[0]+"\t"+perc[1]+"\t"+perc[2]+"\n");
            oos.write(((int) (computingPower * (1 + 1.0 / cop))) + "\t" + (int) computingPower + "\t"
                    + environment.getCurrentLocalTime() + "\n");
            totalPowerConsumption = totalPowerConsumption + computingPower * (1 + 1.0 / cop);
            // System.out.println(totalPowerConsumption);
        } catch (IOException ex) {
            Logger.getLogger(Package.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void shutDownDC() throws FileNotFoundException, IOException {
        oos.close();
        fos.close();
    }

    boolean getDmatrix(String DFileName) {
        int m = chassisSet.size();
        D = new double[m][m];
        BufferedReader bis = null;
        try {
            File f = new File(DFileName);
            bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (IOException e) {
            System.out.println("Uh oh, got an IOException error!" + e.getMessage());

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
                System.out.println("readJOB EXC readJOB false ");
                Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    public DataCenterAM getAM() {
        return am;
    }

    public int getOverRed() {
        return overRed;
    }

    public BladeServer getServer(int i) {
        return chassisSet.get(getChasisIndex(i)).getServers().get(getServerIndex(i));
    }

    public BladeServer getServer(int indexChassis, int indexServer) {
        return chassisSet.get(indexChassis).getServers().get(indexServer);
    }

    public List<Chassis> getChassisSet() {
        return chassisSet;
    }

    public void setChassisSet(List<Chassis> chassisSet) {
        this.chassisSet = chassisSet;
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }
}
