package simulator.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.am.ApplicationAM;
import simulator.jobs.EnterpriseJob;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public class EnterpriseApp {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseApp.class.getName());

    private int id = 0;
    // int usedNode=0;
    private int maxProc = 0;
    private int minProc = 0;
    private int maxExpectedResTime = 0;
    private List<BladeServer> computeNodeList;
    // ArrayList <Integer> ComputeNodeIndex;
    private List<EnterpriseJob> queueApp;
    private List<ResponseTime> responseList;
    // jobPlacement placement;
    private int timeTreshold = 0;
    private int slaPercentage;
    private int slaViolation = 0;
    private int numOfViolation = 0;
    private BufferedReader bis = null;
    private ApplicationAM am;
    // EnterpriseSystem mySys; //Application knows in which Sys it is located.
    // initialize in EnterpriseSystem
    private int maxNumberOfRequest = 0; // # of Request can be handled by number
    // of basic node which for 100% CPU
    // utilization
    private int numberofBasicNode = 0;
    private Scheduler scheduler;
    private ResourceAllocation resourceAllocation;
    private Environment environment;

    public EnterpriseApp(EnterpriseApplicationPOD enterpriseApplicationPOD, Scheduler scheduler, ResourceAllocation resourceAllocation,
            Environment environment) {
        this.scheduler = scheduler;
        this.resourceAllocation = resourceAllocation;
        this.environment = environment;
        setComputeNodeList(new ArrayList<BladeServer>());
        setQueueApp(new ArrayList<EnterpriseJob>());
        setResponseList(new ArrayList<ResponseTime>());
        id = enterpriseApplicationPOD.getID();
        minProc = enterpriseApplicationPOD.getMinProc();
        timeTreshold = enterpriseApplicationPOD.getTimeTreshold();
        slaPercentage = enterpriseApplicationPOD.getSLAPercentage();
        maxNumberOfRequest = enterpriseApplicationPOD.getMaxNumberOfRequest();
        numberofBasicNode = enterpriseApplicationPOD.getNumberofBasicNode();
        maxExpectedResTime = enterpriseApplicationPOD.getMaxExpectedResTime();
        bis = enterpriseApplicationPOD.getBIS();
        configSLAallcomputingNode();
    }

    public double numberOfWaitingJobs() {
        double lenJob = 0;
        for (EnterpriseJob job : getQueueApp()) {
            if (job.getArrivalTimeOfJob() <= environment.getCurrentLocalTime()) {
                lenJob = +job.getNumberOfJob();
            }
        }

        return lenJob;
    }

    public void configSLAallcomputingNode() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.configSLAparameter(getTimeTreshold(), getSLAPercentage());
        }
    }

    public void addCompNodetoBundle(BladeServer bladeServer) {
        bladeServer.restart();
        getComputeNodeList().add(bladeServer);
    }

    public void removeCompNodeFromBundle(BladeServer bladeServer) {
        bladeServer.restart();
        bladeServer.setStatusAsNotAssignedToAnyApplication();
        getComputeNodeList().remove(bladeServer);
    }
    /*
     * Return Values: 1: read successfully 0:put in waiting list -1: end of file
     * or error
     */

    int readingLogFile() {
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
            EnterpriseJob j = new EnterpriseJob();
            j.setArrivalTimeOfJob(Integer.parseInt(numbers[0]));
            j.setNumberOfJob(Integer.parseInt(numbers[1]));
            getQueueApp().add(j);
            return 1;
            // LOGGER.info("Readed inputTime= " + inputTime + " Job
            // Reqested Time=" + j.startTime+" Total job so far="+ total);
        } catch (IOException ex) {
            LOGGER.info("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
    }

    int readWebJob() {
        int retReadLogfile = readingLogFile();
        if (!getQueueApp().isEmpty()) {
            if (getQueueApp().get(0).getArrivalTimeOfJob() <= environment.getCurrentLocalTime()) {
                return 1;
            } else {
                return 0;
            }
        }
        // ending condition means there is no job in the logfile
        // LOGGER.info(" One dispacher !!! in the readWebJob enterprise
        // "+retReadLogfile);
        return retReadLogfile;
    }
    // reset all working node ready flag and CPU utilization

    void resetReadyFlagAndCPU() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (!bladeServer.isIdle()) { // if it is idle
                // dont
                // change it! it
                // is
                // responsibility
                // of
                // its AM to
                // change
                // it
                bladeServer.setCurrentCPU(0);
                bladeServer.setStatusAsRunningNormal();
            }
        }
    }
    // False: logfile is finished and no remain job

    public boolean runAcycle() {
        int readingResult = readWebJob();
        //////// RESET READY FLAGS for all nodes
        resetReadyFlagAndCPU();
        // need more thought
        if (readingResult == 0) {
            // we have jobs but it is not the time to run them
            return true;
        }
        if (readingResult == -2 & getQueueApp().isEmpty()) {
            // no jobs are in the queue and in logfile
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
        EnterpriseJob jj = (EnterpriseJob) scheduler.nextJob(getQueueApp());
        while (capacityOfNode > 0) {
            capacityOfNode = capacityOfNode - jj.getNumberOfJob();
            if (capacityOfNode == 0) {
                addToresponseArray(jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueApp().remove(jj);
                break;
            }
            if (capacityOfNode < 0) {
                // there are more jobs than capacity
                addToresponseArray(capacityOfNode + jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + capacityOfNode + jj.getNumberOfJob();
                jj.setNumberOfJob(-1 * capacityOfNode);
                break;
            }
            if (capacityOfNode > 0) {
                // still we have capacity to run the jobs
                addToresponseArray(jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueApp().remove(jj);
                while (!getQueueApp().isEmpty()) {
                    jj = (EnterpriseJob) scheduler.nextJob(getQueueApp());
                    int copyTedat = capacityOfNode;
                    capacityOfNode = capacityOfNode - jj.getNumberOfJob();
                    if (capacityOfNode == 0) {
                        addToresponseArray(jj.getNumberOfJob(),
                                (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueApp().remove(0);
                        break;
                    }
                    if (capacityOfNode < 0) {
                        // there are more jobs than 1000.0*MIPS
                        addToresponseArray(copyTedat,
                                (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                        jj.setNumberOfJob(-1 * capacityOfNode);
                        beenRunJobs = beenRunJobs + copyTedat;
                        break;
                    }
                    if (capacityOfNode > 0) {
                        addToresponseArray(jj.getNumberOfJob(),
                                (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueApp().remove(0);
                    }
                }
                break;
            }
        }
        if (capacityOfNode_COPY == beenRunJobs) {
            // we're done all our capacity
            for (BladeServer bladeServer : getComputeNodeList()) {
                if (bladeServer.isRunningNormal()) {
                    bladeServer.setCurrentCPU(100);
                    bladeServer.setStatusAsRunningBusy();
                }
            }
        } else if (beenRunJobs < 0) {
            LOGGER.info("it is impossible!!!!  Enterprise BoN");
        } else if (beenRunJobs > 0) {
            int k = 0;
            for (k = 0; k < numberofReadyNodes; k++) {
                int serID = resourceAllocation.nextServer(getComputeNodeList());
                if (serID == -2) {
                    LOGGER.info("enterPrise BoN : servID =-2\t " + k + "\t" + numberofReadyNodes);
                    break;
                }
                BladeServer bladeServer = getComputeNodeList().get(serID);
                double CPUspace = (100 - bladeServer.getCurrentCPU()) * bladeServer.getMips();
                int reqSpace = (int) Math.ceil(CPUspace * getMaxNumberOfRequest() / (getNumberofBasicNode() * 100.0));
                bladeServer.setCurrentCPU(100);
                bladeServer.setStatusAsRunningBusy();
                beenRunJobs = beenRunJobs - reqSpace;
                if (beenRunJobs == 0) {
                    k++;
                    break;
                }
                if (beenRunJobs < 0) {
                    bladeServer.setCurrentCPU(Math.ceil((reqSpace + beenRunJobs) * 100.0 / reqSpace));
                    bladeServer.setStatusAsRunningNormal();
                    k++;
                    break;
                }
            }
        }
        // AM.monitor();
        // AM.analysis(SLAviolation);
        // AM.planning();
        return !(getQueueApp().isEmpty() && readingResult == -2);
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

    void addToresponseArray(double num, int time) {
        ResponseTime t = new ResponseTime();
        t.setNumberOfJob(num);
        t.setResponseTime(time);
        getResponseList().add(t);
    }

    public double getAverageCPUutil() {
        if (getComputeNodeList().isEmpty()) {
            return 0.0;
        }

        int i = 0;
        double cpu = 0;
        for (i = 0; i < getComputeNodeList().size(); i++) {
            cpu = cpu + getComputeNodeList().get(i).getCurrentCPU();
        }
        cpu = cpu / i;
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

    public void destroyApplication() throws IOException {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.restart();
            bladeServer.setStatusAsNotAssignedToAnyApplication();
        }
        bis.close();
    }

    boolean isThereIdleNode() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.isIdle()) {
                return true;
            }
        }
        return false;
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
    // Check the responseTime of each server for setting the frequency level for
    // the next time slot
    /*
     * double finalized () { try { bis.close(); } catch (IOException ex) {
     * Logger.getLogger(application.class.getName()).log(Level.SEVERE, null,
     * ex); } double meanResponsetime=0; double totalJob=0; for(int
     * i=0;i<Main.responseList.size();i++) { meanResponsetime=meanResponsetime+
     * Main.responseList.get(i).responseTime*Main.responseList.get(i).
     * numberOfJob; totalJob+=Main.responseList.get(i).numberOfJob;
     * //LOGGER.info("respTime="+serverList.get(i).respTime+ "\t TotalJob="
     * +serverList.get(i).totalJob); }
     * 
     * return meanResponsetime;///totalJob; }
     */

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
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

    public List<BladeServer> getComputeNodeList() {
        return computeNodeList;
    }

    public void setComputeNodeList(ArrayList<BladeServer> computeNodeList) {
        this.computeNodeList = computeNodeList;
    }

    public List<EnterpriseJob> getQueueApp() {
        return queueApp;
    }

    public void setQueueApp(ArrayList<EnterpriseJob> queueApp) {
        this.queueApp = queueApp;
    }

    public List<ResponseTime> getResponseList() {
        return responseList;
    }

    public void setResponseList(ArrayList<ResponseTime> responseList) {
        this.responseList = responseList;
    }

    public int getTimeTreshold() {
        return timeTreshold;
    }

    public void setTimeTreshold(int timeTreshold) {
        this.timeTreshold = timeTreshold;
    }

    public int getSLAPercentage() {
        return slaPercentage;
    }

    public void setSLAPercentage(int slaPercentage) {
        this.slaPercentage = slaPercentage;
    }

    public int getNumofViolation() {
        return numOfViolation;
    }

    public void setNumofViolation(int numOfViolation) {
        this.numOfViolation = numOfViolation;
    }

    public int getSLAviolation() {
        return slaViolation;
    }

    public void setSLAviolation(int sLAviolation) {
        slaViolation = sLAviolation;
    }

    public ApplicationAM getAM() {
        return am;
    }

    public void setAM(ApplicationAM am) {
        this.am = am;
        this.am.setApplication(this);
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

    public static EnterpriseApp create(EnterpriseApplicationPOD enterpriseApplicationPOD, Scheduler scheduler, ResourceAllocation resourceAllocation,
            Environment environment, ApplicationAM applicationAM) {
        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, scheduler, resourceAllocation, environment);
        enterpriseApplication.setAM(applicationAM);
        return enterpriseApplication;
    }
}
