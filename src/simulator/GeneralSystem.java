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

    private String name;
    private ResourceAllocation resourceAllocation;
    private Scheduler scheduler;
    private int numberofIdleNode = 0; // idle is change in allocation function
	private int numberofNode;
    private ArrayList<Integer> rackId = new ArrayList<Integer>();
    private ArrayList<BladeServer> ComputeNodeList;
    private ArrayList<Integer> ComputeNodeIndex;
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

    void calculatePwr() {
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
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
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

	public ArrayList<Integer> getRackId() {
		return rackId;
	}

	public void setRackId(ArrayList<Integer> rackId) {
		this.rackId = rackId;
	}

	public ArrayList<BladeServer> getComputeNodeList() {
		return ComputeNodeList;
	}

	public void setComputeNodeList(ArrayList<BladeServer> computeNodeList) {
		ComputeNodeList = computeNodeList;
	}

	public ArrayList<Integer> getComputeNodeIndex() {
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

	public boolean isSysIsDone() {
		return sysIsDone;
	}

	public void setSysIsDone(boolean sysIsDone) {
		this.sysIsDone = sysIsDone;
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
