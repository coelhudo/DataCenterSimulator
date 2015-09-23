package simulator.system;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Violation;
import simulator.am.ComputeSystemAM;
import simulator.jobs.BatchJob;
import simulator.jobs.JobProducer;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerCollectionOperations;
import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.schedulers.LeastRemainFirstScheduler;

public class ComputeSystem extends GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(ComputeSystem.class.getName());

    private Violation SLAViolationType;
    private List<BatchJob> waitingList;
    private int totalJob = 0;
    private boolean blocked = false;
    private Environment environment;
    private SLAViolationLogger slaViolationLogger;
    private JobProducer jobProducer;

    private ComputeSystem(SystemPOD systemPOD, Environment environment, DataCenter dataCenter,
            SLAViolationLogger slaViolationLogger) {
        super(systemPOD, new LeastRemainFirstScheduler(), new MHR(environment, dataCenter));
        this.jobProducer = ((ComputeSystemPOD)systemPOD).getJobProducer();
        this.environment = environment;
        this.slaViolationLogger = slaViolationLogger;
        setComputeNodeList(new ArrayList<BladeServer>());
        waitingList = new ArrayList<BatchJob>();
        setBis(systemPOD.getBis());
        setNumberOfNode(systemPOD.getNumberOfNode());
        totalJob = 0;
    }

    @Override
    public boolean runAcycle() {
        resetNumberOfSLAViolation();
        int numberOfFinishedJob = 0;
        loadJobsIntoWaitingQueue();
        if (!isBlocked()) {
            moveWaitingJobsToBladeServer();
            BladeServerCollectionOperations.runAll(getComputeNodeList());
            numberOfFinishedJob += BladeServerCollectionOperations.totalFinishedJob(getComputeNodeList());
        }
        // if is blocked and was not belocked before make it blocked
        if (isBlocked() && !BladeServerCollectionOperations.allIdle(getComputeNodeList())) {
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

    private void loadJobsIntoWaitingQueue() {
        if (!jobProducer.hasNext()) {
            return;
        }

        do {
            BatchJob batchJob = (BatchJob) jobProducer.next();
            waitingList.add(batchJob);
            totalJob++;
            if (batchJob.getStartTime() > environment.getCurrentLocalTime()) {
                break;
            }
        } while (jobProducer.hasNext());
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
            List<BladeServer> allocatedServers = getResourceAllocation().allocateSystemLevelServer(getComputeNodeList(), job.getNumOfNode());
            if (allocatedServers == null) {
                setSLAviolation(Violation.COMPUTE_NODE_SHORTAGE);
                return;
            }
            job.setListOfServer(allocatedServers);
            for (BladeServer bladeServer : allocatedServers) {
                bladeServer.feedWork(job);
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

    void setSLAviolation(Violation flag) {
        SLAViolationType = Violation.NOTHING;
        if (flag == Violation.COMPUTE_NODE_SHORTAGE) {
            SLAViolationType = Violation.COMPUTE_NODE_SHORTAGE;
            setSLAviolation(getSLAviolation()+1);
        }
        if (flag == Violation.DEADLINE_PASSED) {
            SLAViolationType = Violation.DEADLINE_PASSED;
            setSLAviolation(getSLAviolation()+1);
        }
        if (SLAViolationType != Violation.NOTHING) {
            slaViolationLogger.logHPCViolation(getName(), SLAViolationType);
            setAccumolatedViolation(getAccumolatedViolation() + 1);
        }
    }

    public int numberOfRunningNode() {
        return BladeServerCollectionOperations.countRunning(getComputeNodeList());
    }

    public int numberOfIdleNode() {
        return BladeServerCollectionOperations.countIdle(getComputeNodeList());
    }

    public void activeOneNode() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.isIdle()) {
                bladeServer.restart();
                bladeServer.setStatusAsRunningNormal();
                LOGGER.info("activeone node in compuet system MIIIIPPPSSS    " + bladeServer.getMips());
                break;
            }
        }
    }

    double finalized() {
        return BladeServerCollectionOperations.totalResponseTime(getComputeNodeList());
    }

    public static ComputeSystem create(SystemPOD systemPOD, Environment environment, DataCenter dataCenter,
            SLAViolationLogger slaViolationLogger) {
        ComputeSystem computeSystem = new ComputeSystem(systemPOD, environment, dataCenter, slaViolationLogger);
        computeSystem.getResourceAllocation().initialResourceAloc(computeSystem);
        computeSystem.setAM(new ComputeSystemAM(environment));
        return computeSystem;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
