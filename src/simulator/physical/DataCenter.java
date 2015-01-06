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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import simulator.Simulator;

public final class DataCenter {

    public int overRed = 0;
    public double totalPowerConsumption = 0;
    public Cooler cooler1 = new Cooler();
    public ArrayList<Chassis> chassisSet = new ArrayList<Chassis>();
    public ArrayList<BladeServer> BSTemp = new ArrayList<BladeServer>();
    public ArrayList<Chassis> CHSTemp = new ArrayList<Chassis>();
    public int redTemperature;
    public FileOutputStream fos;
    public OutputStreamWriter oos;
    public int numbOfSofarChassis = 0;
    public int numOfServerSoFar = 0;
    public double[][] D;
    public DataCenterAM am = new DataCenterAM();
    ///////////////////////////

    public DataCenter(String config) {
        //output file for writing total DC power consumption
        String s = "out_W.txt";
        File destinationFile = new File(s);
        try {
            fos = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        oos = new OutputStreamWriter(fos);
        //reading config file to set the parameters
        parseXmlConfig(config);
    }

    int getServerIndex(int i) {
        return i % chassisSet.get(0).servers.size();
    }

    int getChasisIndex(int i) {
        return i / chassisSet.get(0).servers.size();
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
                    BladeServer bs = new BladeServer(-1);
                    bs.readFromNode(childNodes.item(i));
                    BSTemp.add(bs);
                }
            }
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Chassis")) {
                    Chassis chs = new Chassis(-1);
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
        for (int j = 0; j < ch.servers.size(); j++) {
            int i;
            for (i = 0; i < BSTemp.size(); i++) {
                if (ch.servers.get(j).bladeType.trim().equalsIgnoreCase(BSTemp.get(i).bladeType.trim())) {
                    break;
                }
            }
            if (i == BSTemp.size()) {
                System.out.println("DataCenter.java");
            }
            ch.servers.get(j).frequencyLevel = new double[BSTemp.get(i).frequencyLevel.length];
            ch.servers.get(j).powerBusy = new double[BSTemp.get(i).powerBusy.length];
            ch.servers.get(j).powerIdle = new double[BSTemp.get(i).powerIdle.length];
            for (int p = 0; p < BSTemp.get(i).frequencyLevel.length; p++) {
                ch.servers.get(j).frequencyLevel[p] = BSTemp.get(i).frequencyLevel[p];
                ch.servers.get(j).powerBusy[p] = BSTemp.get(i).powerBusy[p];
                ch.servers.get(j).powerIdle[p] = BSTemp.get(i).powerIdle[p];
            }
            ch.servers.get(j).idleConsumption = BSTemp.get(i).idleConsumption;
            ch.servers.get(j).serverID = j;
            ch.servers.get(j).bladeType = BSTemp.get(i).bladeType;
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
                Chassis ch1 = new Chassis(numbOfSofarChassis + kk);
                cloneChassis(ch1, CHSTemp.get(k));
                ch1.rackId = rackID;
                for (int inx = 0; inx < ch1.servers.size(); inx++) {
                    ch1.servers.get(inx).chassisID = numbOfSofarChassis + kk;
                    ch1.servers.get(inx).rackId = rackID;
                }
                chassisSet.add(ch1);
            }
            numbOfSofarChassis += kk;
        }
    }

    void cloneChassis(Chassis A, Chassis B) //    A<--B
    {
        for (int i = 0; i < B.servers.size(); i++) {
            BladeServer a = new BladeServer(i);
            //
            a.frequencyLevel = new double[B.servers.get(i).frequencyLevel.length];
            a.powerBusy = new double[B.servers.get(i).powerBusy.length];
            a.powerIdle = new double[B.servers.get(i).powerIdle.length];
            int numberOfMIPSlevels = B.servers.get(i).frequencyLevel.length;
            //
            A.servers.add(a);

            for (int p = 0; p < numberOfMIPSlevels; p++) {
                A.servers.get(i).frequencyLevel[p] = B.servers.get(i).frequencyLevel[p];
                A.servers.get(i).powerBusy[p] = B.servers.get(i).powerBusy[p];
                A.servers.get(i).powerIdle[p] = B.servers.get(i).powerIdle[p];
            }
            A.servers.get(i).idleConsumption = B.servers.get(i).idleConsumption;
            A.servers.get(i).bladeType = B.servers.get(i).bladeType;
            A.servers.get(i).serverID = numOfServerSoFar;
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
            //if(chassisSet.get(i).servers.get(0).currentCPU!=0)
            //  System.out.println(chassisSet.get(i).servers.get(0).currentCPU +" \ttime ="+Main.localTime +" \t  chassi id="+chassisSet.get(i).chassisID );

            try {
                oos.write((int) temp + "\t");
            } catch (IOException ex) {
                Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
            }
            computingPower = computingPower + temp;
        }
        //System.out.println("in betweeeeeen");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temprature[i] = temprature[i] + D[i][j] * chassisSet.get(j).power();
                //System.out.println(chassis_list[i]);
            }
        }
        maxTemp = temprature[0];
        for (int i = 0; i < m; i++) {
            if (maxTemp < temprature[i]) {
                maxTemp = temprature[i];
            }
        }
        //System.out.println(maxTepm);
        maxTemp = redTemperature - maxTemp;
        if (maxTemp <= 0) {
            //System.out.println("maxTem less than 0000  " + maxTemp);
            am.SlowDownFromCooler = true;
            overRed++;

        } else {
            am.SlowDownFromCooler = false;
        }
        double cop = cooler1.getCOP(maxTemp);
        try {
            //System.out.println(((int)(Pcomp*(1+1.0/COP)))+"\t"+(int)Pcomp+"\t"+localTime);
            //oos.write(Integer.toString((int) (Pcomp*(1+1.0/COP)))+"\t"+Integer.toString((int)Pcomp)+"\t"+localTime+"\t"+perc[0]+"\t"+perc[1]+"\t"+perc[2]+"\n");
            oos.write(((int) (computingPower * (1 + 1.0 / cop))) + "\t" + (int) computingPower + "\t" + Simulator.getInstance().localTime + "\n");
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
        return chassisSet.get(getChasisIndex(i)).servers.get(getServerIndex(i));
    }

    public BladeServer getServer(int indexChassis, int indexServer) {
        return chassisSet.get(indexChassis).servers.get(indexServer);
    }
}
