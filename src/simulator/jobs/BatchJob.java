package simulator.jobs;

import simulator.physical.BladeServer;
import simulator.Simulator;

public class BatchJob extends Job {

    public double startTime, exitTime, deadline;
    public int isChangedThisTime = 0;
    public double reqTime, utilization;
    public double[] remain;
    public int numOfNode;
    public int[] listOfServer;

    public void setRemainParam(double exp, double ut, int node, int deadln) {
        if (ut < 1) {
            utilization = 1;
        } else {
            utilization = ut / 100;
        }
        numOfNode = node;
        listOfServer = new int[numOfNode];
        remain = new double[numOfNode];
        for (int i = 0; i < numOfNode; i++) {
            remain[i] = exp;
        }
        reqTime = exp;
        deadline = deadln;
    }

    public BatchJob() {
        startTime = 0;
        exitTime = 0;
        reqTime = 0;
        exitTime = 0;
        numOfNode = 0;
        deadline = 0;
    }

    public boolean allDone() {
        int i;
        for (i = 0; i < numOfNode; i++) {
            if (remain[i] > 0) {
                return false;
            }
        }
        return true;

    }

    public void jobFinished() {
        exitTime = Simulator.getInstance().localTime;
        double waitTime = (Simulator.getInstance().localTime + 1) - startTime;//- (int)(reqTime);
        if (waitTime < 0) {
            System.out.println("Alert: Error in BatchJob\t" + waitTime);
        }

        BladeServer server = Simulator.getInstance().getDatacenter().getServer(listOfServer[0]);
        server.respTime = waitTime + server.respTime;
        for (int i = 0; i < numOfNode; i++) {
            server = Simulator.getInstance().getDatacenter().getServer(listOfServer[i]);
            server.blockedBatchList.remove(this);
        }

        return;
    }

    public int getThisNodeIndex(int serverIndex) {
        int ki;
        for (ki = 0; ki < remain.length; ki++) {
            if (listOfServer[ki] == serverIndex) {
                return ki;
            }
        }
        return -1;
    }
}
