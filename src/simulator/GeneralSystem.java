package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import simulator.am.GeneralAM;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

/**
 *
 * @author fnorouz
 */
public class GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(GeneralSystem.class.getName());
    
    private String name;
    private ResourceAllocation resourceAllocation;
    private Scheduler scheduler;
    private int numberofIdleNode = 0; // idle is change in allocation function
    private int numberofNode;
    private List<Integer> rackId = new ArrayList<Integer>();
    private List<BladeServer> ComputeNodeList;
    private List<Integer> ComputeNodeIndex;
    private BufferedReader bis = null;
    protected int SLAviolation;
    private boolean sysIsDone = false;
    private double power = 0;
    private GeneralAM am;
    private int accumolatedViolation = 0;
    private int numberOfActiveServ = 0;

    public void addComputeNodeToSys(BladeServer b) {
        b.restart();
        getComputeNodeList().add(b);
    }

    void readFromNode(Node node, String path) {
    }

    void calculatePower() {
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            setPower(getPower() + getComputeNodeList().get(i).getPower());
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
            LOGGER.severe(ex.getMessage());
        } catch (SAXException ex) {
            LOGGER.severe(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceAllocation getResourceAllocation() {
        return resourceAllocation;
    }

    public void setResourceAllocation(ResourceAllocation resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public int getNumberofIdleNode() {
        return numberofIdleNode;
    }

    public void setNumberofIdleNode(int numberofIdleNode) {
        this.numberofIdleNode = numberofIdleNode;
    }

    public int getNumberofNode() {
        return numberofNode;
    }

    public void setNumberofNode(int numberofNode) {
        this.numberofNode = numberofNode;
    }

    public List<Integer> getRackId() {
        return rackId;
    }

    public void setRackId(ArrayList<Integer> rackId) {
        this.rackId = rackId;
    }

    public List<BladeServer> getComputeNodeList() {
        return ComputeNodeList;
    }

    public void setComputeNodeList(ArrayList<BladeServer> computeNodeList) {
        ComputeNodeList = computeNodeList;
    }

    public List<Integer> getComputeNodeIndex() {
        return ComputeNodeIndex;
    }

    public void setComputeNodeIndex(ArrayList<Integer> computeNodeIndex) {
        ComputeNodeIndex = computeNodeIndex;
    }

    public BufferedReader getBis() {
        return bis;
    }

    public void setBis(BufferedReader bis) {
        this.bis = bis;
    }

    public int getSLAviolation() {
        return SLAviolation;
    }

    public void setSLAviolation(int sLAviolation) {
        SLAviolation = sLAviolation;
    }

    public boolean isDone() {
        return sysIsDone;
    }

    public void markAsDone() {
        this.sysIsDone = true;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public GeneralAM getAM() {
        return am;
    }

    public void setAM(GeneralAM am) {
        this.am = am;
    }

    public int getAccumolatedViolation() {
        return accumolatedViolation;
    }

    public void setAccumolatedViolation(int accumolatedViolation) {
        this.accumolatedViolation = accumolatedViolation;
    }

    public int getNumberOfActiveServ() {
        return numberOfActiveServ;
    }

    public void setNumberOfActiveServ(int numberOfActiveServ) {
        this.numberOfActiveServ = numberOfActiveServ;
    }
}
