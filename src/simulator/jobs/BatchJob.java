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

    public void setRemainParam(double exp, double ut, int node, int deadln) {
        if (ut < 1) {
            setUtilization(1);
        } else {
            setUtilization(ut / 100);
        }
        setNumOfNode(node);
        setListOfServer(new int[getNumOfNode()]);
        setRemain(new double[getNumOfNode()]);
        for (int i = 0; i < getNumOfNode(); i++) {
            getRemain()[i] = exp;
        }
        setReqTime(exp);
        setDeadline(deadln);
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
        int i;
        for (i = 0; i < getNumOfNode(); i++) {
            if (getRemain()[i] > 0) {
                return false;
            }
        }
        return true;

    }

    public void jobFinished() {
        setExitTime(environment.getCurrentLocalTime());
        double waitTime = (environment.getCurrentLocalTime() + 1) - getStartTime();
        // - (int)(reqTime);
        if (waitTime < 0) {
            LOGGER.info("Alert: Error in BatchJob\t" + waitTime);
        }

        BladeServer server = dataCenter.getServer(getListOfServer()[0]);
        server.setRespTime(waitTime + server.getResponseTime());
        for (int i = 0; i < getNumOfNode(); i++) {
            server = dataCenter.getServer(getListOfServer()[i]);
            server.getBlockedBatchList().remove(this);
        }

        return;
    }

    public int getThisNodeIndex(int serverIndex) {
        int ki;
        for (ki = 0; ki < getRemain().length; ki++) {
            if (getListOfServer()[ki] == serverIndex) {
                return ki;
            }
        }
        return -1;
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

    public void setDeadline(double deadline) {
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

    public void setUtilization(double utilization) {
        this.utilization = utilization;
    }

    public double[] getRemain() {
        return remain;
    }

    public void setRemain(double[] remain) {
        this.remain = remain;
    }

    public int getNumOfNode() {
        return numOfNode;
    }

    public void setNumOfNode(int numOfNode) {
        this.numOfNode = numOfNode;
    }

    public int[] getListOfServer() {
        return listOfServer;
    }

    public void setListOfServer(int[] listOfServer) {
        this.listOfServer = listOfServer;
    }
}
