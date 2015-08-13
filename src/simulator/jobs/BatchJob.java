package simulator.jobs;

import java.util.logging.Logger;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;

public class BatchJob extends Job {

    private static final Logger LOGGER = Logger.getLogger(BatchJob.class.getName());

    private double startTime;
    private double exitTime;
    private double deadline;
    private int isChangedThisTime = 0;
    private double reqTime;
    private double utilization;
    private double[] remain;
    private int numOfNode;
    private int[] listOfServer;
    private Environment environment;
    private DataCenter dataCenter;

    public void setRemainParam(double remainingTime, double utilization, int numberOfNodes, double deadline) {
        if (utilization < 1) {
            setUtilization(1);
        } else {
            setUtilization(utilization / 100);
        }
        setNumOfNode(numberOfNodes);
        setListOfServer(new int[getNumOfNode()]);
        setRemain(new double[getNumOfNode()]);
        for (int i = 0; i < getNumOfNode(); i++) {
            setRemainAt(i, remainingTime);
        }
        setReqTime(remainingTime);
        setDeadline(deadline);
    }

    public BatchJob(Environment environment, DataCenter dataCenter) {
        this.environment = environment;
        this.dataCenter = dataCenter;
        setStartTime(0);
        setExitTime(0);
        setReqTime(0);
        setExitTime(0);
        setNumOfNode(0);
        setDeadline(0);
    }

    public boolean allDone() {
        for (int i = 0; i < getNumOfNode(); i++) {
            if (getRemainAt(i) > 0) {
                return false;
            }
        }
        return true;
    }

    public void jobFinished() {
        setExitTime(environment.getCurrentLocalTime());
        double waitTime = (environment.getCurrentLocalTime() + 1) - getStartTime();
        if (waitTime < 0) {
            LOGGER.info("Alert: Error in BatchJob\t" + waitTime);
        }

        BladeServer server = dataCenter.getServer(getServerIndexAt(0));
        server.setRespTime(waitTime + server.getResponseTime());
        for (int i = 0; i < getNumOfNode(); i++) {
            server = dataCenter.getServer(getServerIndexAt(i));
            server.getBlockedBatchList().remove(this);
        }
    }

    public int getThisNodeIndex(int serverIndex) {
        for (int ki = 0; ki < getRemainLength(); ki++) {
            if (getServerIndexAt(ki) == serverIndex) {
                return ki;
            }
        }
        return -1;
    }

    public int getRemainLength() {
        return remain.length;
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

    public int getIsChangedThisTime() {
        return isChangedThisTime;
    }

    public void setIsChangedThisTime(int isChangedThisTime) {
        this.isChangedThisTime = isChangedThisTime;
    }

    public double getReqTime() {
        return reqTime;
    }

    public void setReqTime(double reqTime) {
        this.reqTime = reqTime;
    }

    public double getUtilization() {
        return utilization;
    }

    private void setUtilization(double utilization) {
        this.utilization = utilization;
    }

    public double getRemainAt(int index) {
        return remain[index];
    }
    
    public void setRemainAt(int index, double remainValue) {
        remain[index] = remainValue;
    }

    private void setRemain(double[] remain) {
        this.remain = remain;
    }

    public int getNumOfNode() {
        return numOfNode;
    }

    private void setNumOfNode(int numOfNode) {
        this.numOfNode = numOfNode;
    }

    public int getServerIndexAt(int index) {
        assert(listOfServer != null && listOfServer.length > index);
        return listOfServer[index];
    }

    public void setListOfServer(int[] listOfServer) {
        this.listOfServer = listOfServer;
    }
}
