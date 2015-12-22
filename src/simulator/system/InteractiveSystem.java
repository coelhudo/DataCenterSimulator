package simulator.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.am.GeneralAM;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public class InteractiveSystem extends GeneralSystem {

	private static final Logger LOGGER = Logger.getLogger(InteractiveSystem.class.getName());

	private List<InteractiveUser> UserList;
	private List<InteractiveUser> waitingQueueWL;
	private Environment environment;
	private SLAViolationLogger slaViolationLogger;

	@Inject
	public InteractiveSystem(@Assisted SystemPOD systemPOD, Environment environment,
			@Named("InteractiveSystem") Scheduler scheduler,
			@Named("InteractiveSystem") ResourceAllocation resourceAllocation, SLAViolationLogger slaViolationLogger,
			@Named("InteractiveSystem") GeneralAM generalAM) {
		super(systemPOD, scheduler, resourceAllocation, generalAM);
		this.environment = environment;
		this.slaViolationLogger = slaViolationLogger;
		setComputeNodeList(new ArrayList<BladeServer>());
		setUserList(new ArrayList<InteractiveUser>());
		setWaitingQueueWL(new ArrayList<InteractiveUser>());
		resetNumberOfSLAViolation();
		setNumberOfNode(systemPOD.getNumberOfNode());
		setNumberOfIdleNode(systemPOD.getNumberOfNode());
		setBis(systemPOD.getBis());
	}

	public int numberofAvailableNodetoAlocate() {
		int n = 0;
		for (BladeServer bladeServer : getComputeNodeList()) {
			if (bladeServer.isNotApplicationAssigned()) {
				n++;
			}
		}
		return n;
	}

	public boolean checkForViolation() {
		for (InteractiveUser interactiveUser : getUserList()) {
			if (interactiveUser.getSLAviolation() > 0) {
				return true;
			}
		}
		return false;
	}
	// Return False means everything is finished!

	@Override
	public boolean runAcycle() {
		if (!getUserList().isEmpty() & checkForViolation()) {
			// AM.monitor();
			// AM.analysis(SLAviolation);
			// AM.planning();
			// AM.execution();
		}
		for (int i = 0; i < getUserList().size(); i++) {
			// TODO: if each bundle needs some help should ask and here
			// resourceallocation should run
			if (!getUserList().get(i).runAcycle()) {
				setNumberOfIdleNode(getUserList().get(i).getComputeNodeList().size() + getNumberOfIdleNode());
				getUserList().get(i).destroyWLBundle();// restart its servers
				getUserList().remove(i);
			}
		}
		/// TODO : some decisiones needed based on SLAviolation
		violationCheckandSet();

		if (getUserList().isEmpty() && getWaitingQueueWL().isEmpty()) {
			markAsDone();
			return true;
		}

		return false;
	}
	// First time resource Allocation

	int forwardingJob() {
		int readingResult = readWL();
		int index;
		while (readingResult == 1) {
			index = getResourceAllocation().initialResourceAloc(this);
			if (index == -1) {
				return index;
			}
			readingResult = readWL();
		}
		return readingResult;
	}

	int readWL() {
		int retReadLogfile = readingLogFile();
		if (!getWaitingQueueWL().isEmpty()) {
			if (getWaitingQueueWL().get(0).getArrivalTime() <= environment.getCurrentLocalTime()) {
				return 1;
			} else {
				return 0;
			}
		}
		return retReadLogfile;
	}
	// In Logfile: arrival time and description of WL (m,k,dur,wl,sla)-->
	// (t,WL(m,k,dur,wl,sla))

	int readingLogFile() {
		try {
			String line = getBis().readLine();
			if (line == null) {
				return -2;
			}
			line = line.replace("\t", " ");
			String[] numbers = new String[6];
			numbers = line.trim().split(" ");
			if (numbers.length < 6) {
				return -2;
			}
			InteractiveUser test = new InteractiveUser(this, environment);
			test.setArrivalTime(Integer.parseInt(numbers[0]));
			test.setMinProc(Integer.parseInt(numbers[1]));
			test.setMaxProc(Integer.parseInt(numbers[2]));
			test.setDuration(Double.parseDouble(numbers[3]));
			test.setRemain(test.getDuration()); // for now I've not used that!
			test.setLogFileName(numbers[4]);
			test.setMaxExpectedResTime(Integer.parseInt(numbers[5]));
			test.setMaxNumberOfRequest(Integer.parseInt(numbers[6]));
			test.setNumberofBasicNode(Integer.parseInt(numbers[7]));
			getWaitingQueueWL().add(test);
			return 1;
			// LOGGER.info("Readed inputTime= " + inputTime + " Job
			// Reqested Time=" + j.startTime+" Total job so far="+ total);
		} catch (IOException ex) {
			LOGGER.info("readJOB EXC readJOB false ");
			Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
			return -2;
		}
	}

	void violationCheckandSet() {
		resetNumberOfSLAViolation();
		for (InteractiveUser interactiveUser : getUserList()) {
			setNumberOfSLAViolation(+interactiveUser.getSLAviolation());
		}
		if (getNumberOFSLAViolation() > 0) {
			slaViolationLogger.logInteractiveViolation(getName(), getNumberOFSLAViolation());
			increaseAccumulatedViolation();
		}
	}
	// void addComputeNodeToSys(BladeServer b){
	// b.restart();
	// ComputeNodeList.add(b);
	// }

	// void parseXmlConfig(String config) {
	// try {
	// DocumentBuilderFactory docBuilderFactory =
	// DocumentBuilderFactory.newInstance();
	// DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	// Document doc = docBuilder.parse(new File(config));
	// // normalize text representation
	// doc.getDocumentElement().normalize();
	// readFromNode(doc.getDocumentElement());
	// } catch (ParserConfigurationException ex) {
	// LOGGER.severe(ex.getMessage());
	// } catch (SAXException ex) {
	// LOGGER.severe(ex.getMessage());
	// } catch (IOException ex) {
	// LOGGER.severe(ex.getMessage());
	// }
	// }

	public List<InteractiveUser> getUserList() {
		return UserList;
	}

	public void setUserList(ArrayList<InteractiveUser> userList) {
		UserList = userList;
	}

	public List<InteractiveUser> getWaitingQueueWL() {
		return waitingQueueWL;
	}

	public void setWaitingQueueWL(ArrayList<InteractiveUser> waitingQueueWL) {
		this.waitingQueueWL = waitingQueueWL;
	}

	@Override
	public void finish() {
		slaViolationLogger.finish();
	}
}
