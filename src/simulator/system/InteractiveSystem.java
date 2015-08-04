package simulator.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.am.InteractiveSystemAM;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.schedulers.FifoScheduler;
import simulator.schedulers.Scheduler;

public class InteractiveSystem extends GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(InteractiveSystem.class.getName());
    
    private List<InteractiveUser> UserList;
    private List<InteractiveUser> waitingQueueWL;
    private Environment environment;
    private SLAViolationLogger slaViolationLogger;

    public InteractiveSystem(SystemPOD systemPOD, Environment environment, DataCenter dataCenter, SLAViolationLogger slaViolationLogger) {
        super(systemPOD);
        this.environment = environment;
        this.slaViolationLogger = slaViolationLogger;
        setComputeNodeList(new ArrayList<BladeServer>());
        setComputeNodeIndex(new ArrayList<Integer>());
        setUserList(new ArrayList<InteractiveUser>());
        setWaitingQueueWL(new ArrayList<InteractiveUser>());
        setResourceAllocation(new MHR(this.environment, dataCenter));
        setScheduler(new FifoScheduler());
        setSLAviolation(0);
        setNumberOfNode(systemPOD.getNumberOfNode());
        setNumberofIdleNode(systemPOD.getNumberOfNode());
        setBis(systemPOD.getBis());
        setRackIDs(systemPOD.getRackIDs());
    }

    public int numberofAvailableNodetoAlocate() {
        int n = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -2) {
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

    boolean runAcycle() throws IOException {
        if (!getUserList().isEmpty() & checkForViolation()) // &
        // Main.localTime%Main.epochSys==0)
        {
            // AM.monitor();
            // AM.analysis(SLAviolation);
            // AM.planning();
            // AM.execution();
        }
        int finishedBundle = 0;
        for (int i = 0; i < getUserList().size(); i++) {
            // TODO: if each bundle needs some help should ask and here
            // resourceallocation should run
            if (getUserList().get(i).runAcycle() == false) // return false if
            // bundle set jobs
            // are done, we need
            // to
            // re-resourcealocation
            {
                finishedBundle++;
                setNumberofIdleNode(getUserList().get(i).getComputeNodeList().size() + getNumberofIdleNode());
                getUserList().get(i).destroyWLBundle();// restart its servers
                getUserList().remove(i);
            }
        }
        violationCheckandSet();
        /// TODO : some decisiones needed based on SLAviolation
        if (finishedBundle > 0) {
            getResourceAllocation().resourceAloc(this);
        }
        if (getUserList().isEmpty() && getWaitingQueueWL().isEmpty()) {
            markAsDone();
            return true;
        } else {
            return false;
        }
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
            if (getWaitingQueueWL().get(0).getArrivalTime() == environment.getCurrentLocalTime()
                    | getWaitingQueueWL().get(0).getArrivalTime() < environment.getCurrentLocalTime()) {
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

    void violationCheckandSet() throws IOException {
        setSLAviolation(0);
        for (InteractiveUser interactiveUser : getUserList()) {
            setSLAviolation(+interactiveUser.getSLAviolation()); //FIXME: += instead of just +. Before was =+
        }
        if (getSLAviolation() > 0) {
            slaViolationLogger.logInteractiveViolation(getName(), getSLAviolation());

            setAccumolatedViolation(getAccumolatedViolation() + 1);
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

    public static InteractiveSystem Create(SystemPOD systemPOD, Environment environment, DataCenter dataCenter, SLAViolationLogger slaViolationLogger) {
        InteractiveSystem interactiveSystem = new InteractiveSystem(systemPOD, environment, dataCenter, slaViolationLogger);
        interactiveSystem.getResourceAllocation().initialResourceAlocator(interactiveSystem);
        interactiveSystem.setAM(new InteractiveSystemAM(interactiveSystem, environment));

        return interactiveSystem;
    }
}
