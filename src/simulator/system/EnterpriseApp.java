package simulator.system;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.am.ApplicationAM;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.JobProducer;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public class EnterpriseApp {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseApp.class.getName());

    private int id = 0;
    private int maxProc = 0;
    private int minProc = 0;
    private int maxExpectedResTime = 0;
    private List<BladeServer> computeNodeList;
    private List<EnterpriseJob> queueApp;
    private List<ResponseTime> responseList;
    private int timeTreshold = 0;
    private int slaPercentage;
    private int slaViolation = 0;
    private int numOfViolation = 0;
    private ApplicationAM am;
    private int maxNumberOfRequest = 0; // # of Request can be handled by number
    // of basic node which for 100% CPU
    // utilization
    private int numberofBasicNode = 0;
    private Scheduler scheduler;
    private ResourceAllocation resourceAllocation;
    private Environment environment;
    private JobProducer jobProducer;

    public EnterpriseApp(EnterpriseApplicationPOD enterpriseApplicationPOD, Scheduler scheduler,
            ResourceAllocation resourceAllocation, Environment environment) {
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
        jobProducer = enterpriseApplicationPOD.getJobProducer();
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

    int readWebJob() {
        if(jobProducer.hasNext()) {
            getQueueApp().add((EnterpriseJob) jobProducer.next());
        }
        
        if (getQueueApp().isEmpty()) {
            return -2;
        }

        if (getQueueApp().get(0).getArrivalTimeOfJob() <= environment.getCurrentLocalTime()) {
            return 1;
        }
        
        return 0;
    }

    /**
     * Reset all working node ready flag and CPU utilization.
     * 
     * Legacy Obs.: If it is idle do not change it! it is responsibility of its
     * AM to change it.
     */
    void resetReadyFlagAndCPU() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (!bladeServer.isIdle()) {
                bladeServer.setCurrentCPU(0);
                bladeServer.setStatusAsRunningNormal();
            }
        }
    }
    // False: logfile is finished and no remain job

    public boolean runAcycle() {
        int readingResult = readWebJob();
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
        double cpuPercentage = 0;
        int numberofReadyNodes = 0;
        int beenRunJobs = 0; // number of jobs have been run so far
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.isRunningNormal()) {
                cpuPercentage = (100.0 - bladeServer.getCurrentCPU()) * bladeServer.getMips() + cpuPercentage;
                numberofReadyNodes++;
            }
        }
        int capacityOfNode = (int) Math
                .ceil((getMaxNumberOfRequest() * cpuPercentage) / (getNumberofBasicNode() * 100.0));
        int capacityOfNode_COPY = capacityOfNode;
        EnterpriseJob jj = (EnterpriseJob) scheduler.nextJob(getQueueApp());
        while (capacityOfNode > 0) {
            capacityOfNode = capacityOfNode - jj.getNumberOfJob();
            final int time = environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1;
            if (capacityOfNode == 0) {
                addToresponseArray(jj.getNumberOfJob(), time);
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueApp().remove(jj);
                break;
            }
            if (capacityOfNode < 0) {
                // there are more jobs than capacity
                addToresponseArray(capacityOfNode + jj.getNumberOfJob(), time);
                beenRunJobs = beenRunJobs + capacityOfNode + jj.getNumberOfJob();
                jj.setNumberOfJob(-1 * capacityOfNode);
                break;
            }
            if (capacityOfNode > 0) {
                // still we have capacity to run the jobs
                addToresponseArray(jj.getNumberOfJob(), time);
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueApp().remove(jj);
                while (!getQueueApp().isEmpty()) {
                    jj = (EnterpriseJob) scheduler.nextJob(getQueueApp());
                    int copyTedat = capacityOfNode;
                    capacityOfNode = capacityOfNode - jj.getNumberOfJob();
                    final int responseTime = environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1;
                    if (capacityOfNode == 0) {
                        addToresponseArray(jj.getNumberOfJob(), responseTime);
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueApp().remove(0);
                        break;
                    }
                    if (capacityOfNode < 0) {
                        // there are more jobs than 1000.0*MIPS
                        addToresponseArray(copyTedat, responseTime);
                        jj.setNumberOfJob(-1 * capacityOfNode);
                        beenRunJobs = beenRunJobs + copyTedat;
                        break;
                    }
                    if (capacityOfNode > 0) {
                        addToresponseArray(jj.getNumberOfJob(), responseTime);
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

    public void destroyApplication() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.restart();
            bladeServer.setStatusAsNotAssignedToAnyApplication();
        }
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

    public static EnterpriseApp create(EnterpriseApplicationPOD enterpriseApplicationPOD, Scheduler scheduler,
            ResourceAllocation resourceAllocation, Environment environment, ApplicationAM applicationAM) {
        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, scheduler, resourceAllocation,
                environment);
        enterpriseApplication.setAM(applicationAM);
        return enterpriseApplication;
    }
}
