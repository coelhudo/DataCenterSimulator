package simulator.system;

import java.io.BufferedReader;
import java.util.List;

import simulator.am.GeneralAM;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

/**
 *
 * @author fnorouz
 */
public class GeneralSystem {

    private String name;
    private ResourceAllocation resourceAllocation;
    private Scheduler scheduler;
    private int numberofIdleNode = 0; // idle is change in allocation function
    private int numberOfNode;
    private List<Integer> rackIDs;
    private List<BladeServer> computeNodeList;
    private List<Integer> computeNodeIndex;
    private BufferedReader bis = null;
    protected int slaViolation;
    private boolean sysIsDone = false;
    private double power = 0;
    private GeneralAM am;
    private int accumolatedViolation = 0;
    private int numberOfActiveServ = 0;

    public GeneralSystem(SystemPOD systemPOD) {
        rackIDs = systemPOD.getRackIDs();
        name = systemPOD.getName();
    }

    public void addComputeNodeToSys(BladeServer bladeServer) {
        bladeServer.restart();
        appendBladeServerIntoComputeNodeList(bladeServer);
    }

    void calculatePower() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            setPower(getPower() + bladeServer.getPower());
        }

    }

    public String getName() {
        return name;
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

    public int getNumberOfNode() {
        return numberOfNode;
    }

    protected void setNumberOfNode(int numberOfNode) {
        this.numberOfNode = numberOfNode;
    }

    public List<BladeServer> getComputeNodeList() {
        return computeNodeList;
    }

    public void setComputeNodeList(List<BladeServer> computeNodeList) {
        this.computeNodeList = computeNodeList;
    }

    public void appendBladeServerIntoComputeNodeList(BladeServer bladeServer) {
        computeNodeList.add(bladeServer);
    }

    public List<Integer> getComputeNodeIndex() {
        return computeNodeIndex;
    }

    public void setComputeNodeIndex(List<Integer> computeNodeIndex) {
        this.computeNodeIndex = computeNodeIndex;
    }

    public void appendBladeServerIndexIntoComputeNodeIndex(Integer index) {
        computeNodeIndex.add(index);
    }

    public BufferedReader getBis() {
        return bis;
    }

    public void setBis(BufferedReader bis) {
        this.bis = bis;
    }

    public int getSLAviolation() {
        return slaViolation;
    }

    public void setSLAviolation(int slaViolation) {
        this.slaViolation = slaViolation;
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

    public List<Integer> getRackIDs() {
        return rackIDs;
    }

    protected void setRackIDs(List<Integer> rackIDs) {
        this.rackIDs = rackIDs;
    }
}
