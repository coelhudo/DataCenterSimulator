package simulator.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Violation;
import simulator.am.ComputeSystemAM;
import simulator.jobs.BatchJob;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerCollectionOperations;
import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.schedulers.LeastRemainFirstScheduler;
import simulator.schedulers.Scheduler;

public class ComputeSystem extends GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(ComputeSystem.class.getName());

    private Violation SLAViolationType;
    private List<BatchJob> waitingList;
    private int totalJob = 0;
    private double inputTime;
    private boolean blocked = false;
    private Environment environment;
    private SLAViolationLogger slaViolationLogger;
    private DataCenter dataCenter;

    private ComputeSystem(SystemPOD systemPOD, Environment environment, DataCenter dataCenter,
            SLAViolationLogger slaViolationLogger) {
        super(systemPOD);
        this.environment = environment;
        this.dataCenter = dataCenter;
        this.slaViolationLogger = slaViolationLogger;
        setComputeNodeList(new ArrayList<BladeServer>());
        waitingList = new ArrayList<BatchJob>();
        setComputeNodeIndex(new ArrayList<Integer>());
        setScheduler(new LeastRemainFirstScheduler());
        setBis(systemPOD.getBis());
        setNumberOfNode(systemPOD.getNumberOfNode());
        setRackIDs(systemPOD.getRackIDs());
        setResourceAllocation(new MHR(this.environment, this.dataCenter));
        totalJob = 0;
    }

    public boolean runAcycle() {
        setSLAviolation(0);
        int numberOfFinishedJob = 0;
        BatchJob j = new BatchJob(dataCenter);
        // reads all jobs with arrival time less than Localtime
        while (readJob(j)) {
            if (inputTime > environment.getCurrentLocalTime()) {
                break;
            }
            j = new BatchJob(dataCenter);
        }
        if (!isBlocked()) {
            moveWaitingJobsToBladeServer();
            BladeServerCollectionOperations.runAllServers(getComputeNodeList());
            numberOfFinishedJob += BladeServerCollectionOperations.totalFinishedJob(getComputeNodeList());
        }
        // if is blocked and was not belocked before make it blocked
        if (isBlocked() && !allNodesAreBlocked()) {
            makeSystemaBlocked();
        }

        if (!isBlocked()) {
            getAM().monitor();
            getAM().analysis(0);
        }

        if (numberOfFinishedJob == totalJob) {
            markAsDone();
            return true;
        }
        return false;

    }
    /// returns true if all nodes are blocked

    boolean allNodesAreBlocked() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() != -1) {
                return false;
            }
        }
        return true;
    }

    void makeSystemaBlocked() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.saveStatus();
            bladeServer.setStatusAsNotAssignedToAnyApplication();
        }
    }

    public void makeSystemaUnBlocked() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.restoreStatus();
        }
    }

    void moveWaitingJobsToBladeServer() {
        setSLAviolation(Violation.NOTHING);
        if (waitingList.isEmpty()) {
            return;
        }
        BatchJob job = (BatchJob) (getScheduler().nextJob(waitingList));
        while (job.getStartTime() <= environment.getCurrentLocalTime()) {
            int[] indexes = new int[job.getNumOfNode()]; // number of node the
            // last job wants
            if (getResourceAllocation().allocateSystemLevelServer(getComputeNodeList(), indexes)[0] == -2) {
                setSLAviolation(Violation.COMPUTE_NODE_SHORTAGE);
                return; // can not find the bunch of requested node for the job
            }
            int[] listServer = makeListofServer(indexes);
            job.setListOfServer(listServer);
            for (int i = 0; i < indexes.length; i++) {
                getComputeNodeList().get(indexes[i]).feedWork(job);
            }

            if (environment.getCurrentLocalTime() - job.getStartTime() > job.getDeadline()) {
                setSLAviolation(Violation.DEADLINE_PASSED);
            }

            waitingList.remove(job);
            if (waitingList.isEmpty()) {
                return;
            }
            job = (BatchJob) (getScheduler().nextJob(waitingList));
        }
    }

    int[] makeListofServer(int[] list) {
        int[] retList = new int[list.length];
        for (int i = 0; i < list.length; i++) {
            retList[i] = getComputeNodeList().get(list[i]).getServerID();
        }
        return retList;
    }

    void setSLAviolation(Violation flag) {
        SLAViolationType = Violation.NOTHING;
        if (flag == Violation.COMPUTE_NODE_SHORTAGE) // means there is not
        // enough compute nodes for
        // job, this function is
        // called from
        // resourceIsAvailable
        {
            SLAViolationType = Violation.COMPUTE_NODE_SHORTAGE;
            slaViolation++;
        }
        if (flag == Violation.DEADLINE_PASSED) {
            SLAViolationType = Violation.DEADLINE_PASSED;
            slaViolation++;
        }
        if (SLAViolationType != Violation.NOTHING) {
            slaViolationLogger.logHPCViolation(getName(), SLAViolationType);
            setAccumolatedViolation(getAccumolatedViolation() + 1);
        }
    }

    boolean readJob(BatchJob batchJob) {
        try {
            String line = getBis().readLine();
            if (line == null) {
                return false;
            }
            line = line.replace("\t", " ");
            String[] numbers = line.split(" ");
            if (numbers.length < 5) {
                return false;
            }
            // Input log format: (time, requiertime, CPU utilization, number of
            // core, dealine for getting to a server buffer)
            inputTime = Double.parseDouble(numbers[0]);
            batchJob.setRemainParam(Double.parseDouble(numbers[1]), Double.parseDouble(numbers[2]),
                    Integer.parseInt(numbers[3]), Integer.parseInt(numbers[4]));
            batchJob.setStartTime(inputTime);
            final boolean added = waitingList.add(batchJob);
            // number of jobs which are copied on # of requested nodes
            totalJob = totalJob + 1;
            return added;
        } catch (IOException ex) {
            LOGGER.info("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    List<Integer> getIndexSet() {
        return getComputeNodeIndex();
    }

    public int numberOfRunningNode() {
        int cnt = 0;
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).getReady() > -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public int numberOfIdleNode() {
        int cnt = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public void activeOneNode() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -1) {
                bladeServer.restart();
                bladeServer.setStatusAsRunningNormal();
                LOGGER.info("activeone node in compuet system MIIIIPPPSSS    " + bladeServer.getMips());
                break;
            }
        }
    }

    double finalized() {
        try {
            getBis().close();
        } catch (IOException ex) {
            Logger.getLogger(EnterpriseApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        double totalResponsetime = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            totalResponsetime = totalResponsetime + bladeServer.getResponseTime();

        }
        return totalResponsetime;
    }

    public static ComputeSystem Create(SystemPOD systemPOD, Environment environment, DataCenter dataCenter,
            SLAViolationLogger slaViolationLogger) {
        ComputeSystem computeSystem = new ComputeSystem(systemPOD, environment, dataCenter, slaViolationLogger);
        computeSystem.getResourceAllocation().initialResourceAloc(computeSystem);
        computeSystem.setAM(new ComputeSystemAM(computeSystem, environment));
        return computeSystem;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
