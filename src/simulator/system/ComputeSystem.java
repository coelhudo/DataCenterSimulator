package simulator.system;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Violation;
import simulator.am.GeneralAM;
import simulator.jobs.BatchJob;
import simulator.jobs.JobProducer;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerCollectionOperations;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public class ComputeSystem extends GeneralSystem {

	private static final Logger LOGGER = Logger.getLogger(ComputeSystem.class.getName());

	private Violation violationType;
	private List<BatchJob> waitingList;
	private int totalJob = 0;
	private boolean blocked = false;
	private Environment environment;
	private SLAViolationLogger slaViolationLogger;
	private JobProducer jobProducer;

	@Inject
	public ComputeSystem(@Assisted SystemPOD systemPOD, Environment environment,
			@Named("ComputeSystem") Scheduler scheduler, @Named("ComputeSystem") ResourceAllocation resourceAllocation,
			@Named("ComputeSystem") GeneralAM generalAM, SLAViolationLogger slaViolationLogger) {
		super(systemPOD, scheduler, resourceAllocation, generalAM);
		this.jobProducer = ((ComputeSystemPOD) systemPOD).getJobProducer();
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
		LOGGER.fine("Compute System running a cycle");
		resetNumberOfSLAViolation();
		int numberOfFinishedJob = 0;
		loadJobsIntoWaitingQueue();
		if (!isBlocked()) {
			moveWaitingJobsToBladeServer();
			LOGGER.fine(String.format("Running %d servers", getComputeNodeList().size()));
			BladeServerCollectionOperations.runAll(getComputeNodeList());
			final int currentFinishedJobs = BladeServerCollectionOperations.totalFinishedJob(getComputeNodeList());
			LOGGER.fine(String.format("Batch Jobs finished: %d ", currentFinishedJobs));
			numberOfFinishedJob += currentFinishedJobs;
		}
		
		if (isBlocked() && !BladeServerCollectionOperations.allIdle(getComputeNodeList())) {
			makeSystemaBlocked();
		}

		if (!isBlocked()) {
			getAM().monitor();
			getAM().analysis();
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
			LOGGER.finer("Loading jobs into queue");
			BatchJob batchJob = (BatchJob) jobProducer.next();
			waitingList.add(batchJob);
			totalJob++;
			if (batchJob.getStartTime() > environment.getCurrentLocalTime()) {
				break;
			}
		} while (jobProducer.hasNext());
		LOGGER.fine(String.format("Queue length %d", waitingList.size()));
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
		setSLAViolationType(Violation.NOTHING);
		if (waitingList.isEmpty()) {
			return;
		}
		BatchJob job = (BatchJob) (getScheduler().nextJob(waitingList));
		while (job.getStartTime() <= environment.getCurrentLocalTime()) {
			List<BladeServer> allocatedServers = getResourceAllocation().allocateSystemLevelServer(getComputeNodeList(),
					job.getNumOfNode());
			if (allocatedServers == null) {
				setSLAViolationType(Violation.COMPUTE_NODE_SHORTAGE);
				return;
			}
			job.setListOfServer(allocatedServers);
			for (BladeServer bladeServer : allocatedServers) {
				bladeServer.feedWork(job);
			}

			if (environment.getCurrentLocalTime() - job.getStartTime() > job.getDeadline()) {
				setSLAViolationType(Violation.DEADLINE_PASSED);
			}

			waitingList.remove(job);
			if (waitingList.isEmpty()) {
				return;
			}
			job = (BatchJob) (getScheduler().nextJob(waitingList));
		}
	}

	void setSLAViolationType(Violation flag) {
		if (flag == Violation.NOTHING) {
			violationType = Violation.NOTHING;
			return;
		}
		
		LOGGER.fine(String.format("Setting violation: %s", flag.toString()));
		
		violationType = flag;
		slaViolationLogger.logHPCViolation(getName(), violationType);
		setNumberOfSLAViolation(getNumberOFSLAViolation() + 1);
		increaseAccumulatedViolation();
	}

	public int numberOfRunningNode() {
		return BladeServerCollectionOperations.countRunning(getComputeNodeList());
	}

	public int numberOfIdleNode() {
		return BladeServerCollectionOperations.countIdle(getComputeNodeList());
	}

	@SuppressWarnings("unused")
	private void activeOneNode() {
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

	public boolean isBlocked() {
		return blocked;
	}

	public void block() {
		this.blocked = true;
	}

	public void unblock() {
		this.blocked = false;
	}

	@Override
	public void finish() {
		slaViolationLogger.finish();
	}
}
