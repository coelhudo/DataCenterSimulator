package simulator;

import simulator.physical.BladeServer;
import simulator.physical.DataCenter;
import simulator.am.GeneralAM;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author fnorouz
 */
public class GeneralSystem {

    public String name;
    public ResourceAllocation rc;
    public Scheduler schdler;
    public int numberofIdleNode = 0, numberofNode; // idle is change in allocation function
    public ArrayList<Integer> rackId = new ArrayList<Integer>();
    public ArrayList<BladeServer> ComputeNodeList;
    public ArrayList<Integer> ComputeNodeIndex;
    public BufferedReader bis = null;
    public int SLAviolation;
    public boolean sysIsDone = false;
    public double pwr = 0;
    public GeneralAM am;
    public int accumolatedViolation = 0;
    public int numberOfActiveServ = 0;

    public void addComputeNodeToSys(BladeServer b) {
        b.restart();
        ComputeNodeList.add(b);
    }

    void readFromNode(Node node, String path) {
    }

    void calculatePwr() {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            pwr = pwr + ComputeNodeList.get(i).getPower();
        }

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
}
