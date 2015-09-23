package simulator.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import simulator.physical.DataCenterEntityID;
import simulator.physical.BladeServer;

public class BatchJob extends Job {

    private static final Logger LOGGER = Logger.getLogger(BatchJob.class.getName());

    private double startTime;
    private double exitTime;
    private double deadline;
    private boolean modified = false;
    private double remainingTime;
    private double utilization;
    private int numOfNode;
    private Map<DataCenterEntityID, BladeServerShare> listOfServer = new HashMap<DataCenterEntityID, BladeServerShare>();

    private class BladeServerShare {
        public BladeServerShare(BladeServer bladeServer, double remainingTime) {
            this.bladeSever = bladeServer;
            this.remainingTime = remainingTime;
        }

        private double remainingTime;
        private BladeServer bladeSever;
    }

    public void setRemainParam(double remainingTime, double utilization, int numberOfNodes, double deadline) {
        if (utilization < 1) {
            setUtilization(1);
        } else {
            setUtilization(utilization / 100);
        }
        setNumOfNode(numberOfNodes);
        setReqTime(remainingTime);
        setDeadline(deadline);
    }

    public BatchJob() {
        setStartTime(0);
        setExitTime(0);
        setReqTime(0);
        setExitTime(0);
        setNumOfNode(0);
        setDeadline(0);
    }

    public boolean allDone() {
        for (BladeServerShare bladeServerShare : listOfServer.values()) {
            if (bladeServerShare.remainingTime > 0) {
                return false;
            }
        }
        return true;
    }

    public double Finish(double timeStamp) {
        setExitTime(timeStamp);
        double waitTime = (timeStamp + 1) - getStartTime();
        if (waitTime < 0) {
            LOGGER.info("Alert: Error in BatchJob\t" + waitTime);
        }

        for (BladeServerShare bladeServerShare : this.listOfServer.values()) {
            bladeServerShare.bladeSever.getBlockedBatchList().remove(this);
        }

        return waitTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getExitTime() {
        return exitTime;
    }

    public void setExitTime(double exitTime) {
        this.exitTime = exitTime;
    }

    public double getDeadline() {
        return deadline;
    }

    private void setDeadline(double deadline) {
        this.deadline = deadline;
    }

    public boolean isModified() {
        return modified;
    }

    public void setAsModified() {
        this.modified = true;
    }

    public void setAsNotModified() {
        this.modified = false;
    }

    public double getReqTime() {
        return remainingTime;
    }

    public void setReqTime(double reqTime) {
        this.remainingTime = reqTime;
    }

    public double getUtilization() {
        return utilization;
    }

    private void setUtilization(double utilization) {
        this.utilization = utilization;
    }

    public double getRemainAt(DataCenterEntityID id) {
        return listOfServer.get(id).remainingTime;
    }

    public void setRemainAt(DataCenterEntityID id, double remainValue) {
        listOfServer.get(id).remainingTime = remainValue;
    }

    public int getNumOfNode() {
        return numOfNode;
    }

    private void setNumOfNode(int numOfNode) {
        this.numOfNode = numOfNode;
    }

    public BladeServer getServerIndexAt(DataCenterEntityID index) {
        return listOfServer.get(index).bladeSever;
    }

    public void setListOfServer(List<BladeServer> listOfServer) {
        if (listOfServer.size() != numOfNode) {
            throw new RuntimeException(
                    "Not expecting amount of servers different from number of nodes requested for this job.");
        }

        for (BladeServer bladeServer : listOfServer) {
            this.listOfServer.put(bladeServer.getID(), new BladeServerShare(bladeServer, remainingTime));
        }
    }
}
