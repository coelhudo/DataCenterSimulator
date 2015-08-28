package simulator.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.am.InteractiveUserAM;
import simulator.jobs.InteractiveJob;
import simulator.physical.BladeServer;
import simulator.schedulers.Scheduler;

public class InteractiveUser {

    private static final Logger LOGGER = Logger.getLogger(InteractiveSystem.class.getName());
    private int arrivalTime;
    private int maxProc = 0;
    private int minProc = 0;
    private int maxExpectedResTime = 0;
    private double duration;
    private double remain;
    private int id = 0;
    private String logFileName;
    private File logFile = null;
    private List<BladeServer> computeNodeList;
    private List<Integer> computeNodeIndex;
    private List<InteractiveJob> queueWL;
    private List<ResponseTime> responseList;
    // jobPlacement placement;
    private BufferedReader bis = null;
    // SLA
    private int slaViolation = 0;
    private InteractiveUserAM AM;
    private int usedNode = 0;
    private int maxNumberOfRequest = 0; // # of Request can be handled by number
    // of basic node which for 100% CPU
    // utilization
    private int numberofBasicNode = 0;
    private GeneralSystem parent;
    private Environment environment;

    public InteractiveUser(GeneralSystem parent, Environment environment) {
        this.environment = environment;
        setComputeNodeList(new ArrayList<BladeServer>());
        setComputeNodeIndex(new ArrayList<Integer>());
        setQueueWL(new ArrayList<InteractiveJob>());
        setResponseList(new ArrayList<ResponseTime>());
        setLogFileName(new String());
        // placement=new jobPlacement(ComputeNodeList);
        setAM(new InteractiveUserAM((InteractiveSystem) parent, this, environment));
        this.parent = parent;
    }

    public void addCompNodetoBundle(BladeServer b) {
        b.restart();
        getComputeNodeList().add(b);
    }
    /*
     * Return Values: 1: read successfully 0:put in waiting list -1: end of file
     * or error
     */

    int readingLogFile() {
        if (bis == null) {
            try {
                logFile = new File(getLogFileName());
                bis = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
            } catch (IOException e) {
                LOGGER.warning("Uh oh, got an IOException error!" + e.getMessage());
            }
        }
        try {
            String line = bis.readLine();
            if (line == null) {
                return -2;
            }
            line = line.replace("\t", " ");
            String[] numbers = new String[2];
            numbers = line.trim().split(" ");
            if (numbers.length < 2) {
                return -2;
            }
            InteractiveJob j = new InteractiveJob();
            j.setArrivalTimeOfJob(Integer.parseInt(numbers[0]));
            j.setNumberOfJob(Integer.parseInt(numbers[1]) * 50);
            getQueueWL().add(j);
            return 1;
            // LOGGER.info("Readed inputTime= " + inputTime + " Job
            // Reqested Time=" + j.startTime+" Total job so far="+ total);
        } catch (IOException ex) {
            LOGGER.warning("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
    }
    // reset all working node ready flag and CPU utilization

    void resetReadyFlag() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.setStatusAsRunningNormal();
        }
    }

    int readWebJob() {
        int retReadLogfile = readingLogFile();
        if (!getQueueWL().isEmpty()) {
            if (getQueueWL().get(0).getArrivalTimeOfJob() <= environment.getCurrentLocalTime()) {
                return 1;
            } else {
                return 0;
            }
        }
        // ending condition means there is no job in the logfile
        // LOGGER.info(" One dispacher !!! in the readWebJob interactive
        // "+retReadLogfile);
        return retReadLogfile;
    }

    boolean runAcycle() {
        setSLAviolation(0);
        int readingResult = readWebJob();
        //////// RESET READY FLAGS for all nodes
        resetReadyFlag();
        // need more thought
        if (readingResult == 0) // we have jobs but it is not the time to run
        // them
        {
            return true;
        }
        if (readingResult == -2 & getQueueWL().isEmpty()) // no jobs are in the
        // queue and in
        // logfile
        {
            return false;
        }
        double CPUpercentage = 0;
        int numberofReadyNodes = 0;
        int beenRunJobs = 0; // number of jobs have been run so far
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.isRunningNormal()) {
                CPUpercentage = (100.0 - bladeServer.getCurrentCPU()) * bladeServer.getMips() + CPUpercentage;
                numberofReadyNodes++;
            }
        }
        int capacityOfNode = (int) Math
                .ceil((getMaxNumberOfRequest() * CPUpercentage) / (getNumberofBasicNode() * 100.0));
        int capacityOfNode_COPY = capacityOfNode;
        InteractiveJob jj = new InteractiveJob();
        // jj=queueWL.get(0);
        jj = (InteractiveJob) parent.getScheduler().nextJob(getQueueWL());
        while (capacityOfNode > 0) {
            capacityOfNode = capacityOfNode - jj.getNumberOfJob();
            if (capacityOfNode == 0) {
                addToresponseArray(jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                // LOGGER.info((Main.localTime-wJob.arrivalTimeOfJob+1)*(wJob.numberOfJob)
                // +"\t"+wJob.numberOfJob+"\t q len="+queueLength);
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueWL().remove(jj);
                break;
            }
            if (capacityOfNode < 0) // there are more jobs than capacity
            {
                addToresponseArray(capacityOfNode + jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + capacityOfNode + jj.getNumberOfJob();
                jj.setNumberOfJob(-1 * capacityOfNode);
                // LOGGER.info(1000.0*Mips);
                break;
            }
            if (capacityOfNode > 0) {
                // still we have capacity to run the jobs

                addToresponseArray(jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueWL().remove(jj);
                while (!getQueueWL().isEmpty()) {
                    // jj=queueWL.get(0);
                    jj = (InteractiveJob) parent.getScheduler().nextJob(getQueueWL());
                    int copyTedat = capacityOfNode;
                    capacityOfNode = capacityOfNode - jj.getNumberOfJob();
                    if (capacityOfNode == 0) {
                        addToresponseArray(jj.getNumberOfJob(),
                                (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                        // LOGGER.info(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueWL().remove(0);
                        break;
                    }
                    if (capacityOfNode < 0) {
                        // there are more jobs than 1000.0*MIPS
                        addToresponseArray(copyTedat,
                                (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                        jj.setNumberOfJob(-1 * capacityOfNode);
                        beenRunJobs = beenRunJobs + copyTedat;
                        // LOGGER.info(copyTedat);
                        break;
                    }
                    if (capacityOfNode > 0) {
                        addToresponseArray(jj.getNumberOfJob(),
                                (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                        // LOGGER.info(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueWL().remove(0);
                    }
                }
                break;
            }
        }
        if (capacityOfNode_COPY == beenRunJobs) // we're done all our capacity
        {
            for (BladeServer bladeServer : getComputeNodeList()) {
                bladeServer.setCurrentCPU(100);
                bladeServer.setStatusAsRunningBusy();
            }
            usedNode = usedNode + getComputeNodeList().size();
        } else if (beenRunJobs < 0) {
            LOGGER.warning("it is impossible!!!!  webbased BoN");
        } else if (beenRunJobs > 0) {
            int k = 0;
            for (k = 0; k < numberofReadyNodes; k++) {
                int serID = parent.getResourceAllocation().nextServer(getComputeNodeList());
                if (serID == -2) {
                    LOGGER.info("enterPrise BoN : servID =-2\t " + k + "\t" + numberofReadyNodes);
                    break;
                }
                double CPUspace = (100 - getComputeNodeList().get(serID).getCurrentCPU())
                        * getComputeNodeList().get(serID).getMips();
                int reqSpace = (int) Math.ceil(CPUspace * getMaxNumberOfRequest() / (getNumberofBasicNode() * 100.0));
                getComputeNodeList().get(serID).setCurrentCPU(100);
                getComputeNodeList().get(serID).setStatusAsRunningBusy();
                beenRunJobs = beenRunJobs - reqSpace;
                if (beenRunJobs == 0) {
                    k++;
                    break;
                }
                if (beenRunJobs < 0) {
                    getComputeNodeList().get(serID)
                            .setCurrentCPU(Math.ceil((reqSpace + beenRunJobs) * 100.0 / reqSpace));
                    getComputeNodeList().get(serID).setStatusAsRunningNormal();
                    k++;
                    break;
                }
            }
            // LOGGER.info(k +"\t Running node= "+numberofReadyNodes);
            usedNode = usedNode + k;
        }
        getAM().monitor();
        getAM().analysis(getSLAviolation());
        // AM.planning();
        if (getQueueWL().isEmpty() && readingResult == -2) {
            return false;
        } else {
            return true;
        }
    }

    void addToresponseArray(double num, int time) {
        if (time > getMaxExpectedResTime()) {
            setSLAviolation(getSLAviolation() + 1);
        }
        ResponseTime resTime = new ResponseTime();
        resTime.setNumberOfJob(num);
        resTime.setResponseTime(time);
        getResponseList().add(resTime);
    }

    void setReadyFlag() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (!bladeServer.isIdle()) {
                if (bladeServer.getInteractiveList().isEmpty()) {
                    bladeServer.setStatusAsRunningNormal();
                    bladeServer.setCurrentCPU(0);
                } else {
                    bladeServer.setStatusAsRunningBusy();
                }
            }
        }
    }

    List<Integer> getindexSet() {
        return getComputeNodeIndex();
    }

    void destroyWLBundle() throws IOException {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.restart();
        }

        bis.close();
    }

    public int numberofRunningNode() {
        int cnt = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.isRunning()) {
                cnt++;
            }
        }
        return cnt;
    }

    public int numberofIdleNode() {
        int cnt = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.isIdle()) {
                cnt++;
            }
        }
        return cnt;
    }

    public int myFirstIdleNode() {
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).isIdle()) {
                return i;
            }
        }
        if (getComputeNodeList().size() > 1) {
            return 0;
        }
        return -2;
    }

    public void activeOneNode() {
        int i = 0;
        for (i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).isIdle()) {
                getComputeNodeList().get(i).restart();
                getComputeNodeList().get(i).setStatusAsRunningNormal();
                break;
            }
        }
        LOGGER.info("MIIIIPPPSSS    " + getComputeNodeList().get(i).getMips());
    }

    public int numberOfWaitingJobs() {
        int lenJob = 0;
        for (InteractiveJob job : getQueueWL()) {
            if (job.getArrivalTimeOfJob() <= environment.getCurrentLocalTime()) {
                lenJob = +job.getNumberOfJob();
            }
        }

        return lenJob;
    }

    public double getAverageCPUUtilization() {
        int i = 0;
        double cpu = 0;
        for (i = 0; i < getComputeNodeList().size(); i++) {
            cpu = cpu + getComputeNodeList().get(i).getCurrentCPU();
        }
        cpu = cpu / i; // FIXME: why not list.size()? it will always be (size
                       // -1)
        return cpu;
    }

    public double[] getAveragePwrParam() {
        double[] ret = new double[3];
        for (BladeServer bladeServer : getComputeNodeList()) {
            ret[0] = ret[0] + bladeServer.getPwrParam()[0];
            ret[1] = ret[1] + bladeServer.getPwrParam()[1];
            ret[2] = ret[2] + bladeServer.getPwrParam()[2];
        }
        ret[0] = ret[0] / getComputeNodeList().size();
        ret[1] = ret[1] / getComputeNodeList().size();
        ret[2] = ret[2] / getComputeNodeList().size();
        return ret;
    }

    public int getMaxProc() {
        return maxProc;
    }

    public void setMaxProc(int maxProc) {
        this.maxProc = maxProc;
    }

    public int getMinProc() {
        return minProc;
    }

    public void setMinProc(int minProc) {
        this.minProc = minProc;
    }

    public int getMaxExpectedResTime() {
        return maxExpectedResTime;
    }

    public void setMaxExpectedResTime(int maxExpectedResTime) {
        this.maxExpectedResTime = maxExpectedResTime;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public List<BladeServer> getComputeNodeList() {
        return computeNodeList;
    }

    private void setComputeNodeList(List<BladeServer> computeNodeList) {
        this.computeNodeList = computeNodeList;
    }

    public List<Integer> getComputeNodeIndex() {
        return computeNodeIndex;
    }

    private void setComputeNodeIndex(List<Integer> computeNodeIndex) {
        this.computeNodeIndex = computeNodeIndex;
    }

    public List<InteractiveJob> getQueueWL() {
        return queueWL;
    }

    private void setQueueWL(List<InteractiveJob> queueWL) {
        this.queueWL = queueWL;
    }

    public List<ResponseTime> getResponseList() {
        return responseList;
    }

    private void setResponseList(List<ResponseTime> responseList) {
        this.responseList = responseList;
    }

    public int getSLAviolation() {
        return slaViolation;
    }

    public void setSLAviolation(int slaViolation) {
        this.slaViolation = slaViolation;
    }

    public InteractiveUserAM getAM() {
        return AM;
    }

    public void setAM(InteractiveUserAM aM) {
        AM = aM;
    }

    public int getMaxNumberOfRequest() {
        return maxNumberOfRequest;
    }

    public void setMaxNumberOfRequest(int maxNumberOfRequest) {
        this.maxNumberOfRequest = maxNumberOfRequest;
    }

    public int getNumberofBasicNode() {
        return numberofBasicNode;
    }

    public void setNumberofBasicNode(int numberofBasicNode) {
        this.numberofBasicNode = numberofBasicNode;
    }

    int getArrivalTime() {
        return arrivalTime;
    }

    void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    double getDuration() {
        return duration;
    }

    void setDuration(double duration) {
        this.duration = duration;
    }

    double getRemain() {
        return remain;
    }

    void setRemain(double remain) {
        this.remain = remain;
    }

    String getLogFileName() {
        return logFileName;
    }

    void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }
}
