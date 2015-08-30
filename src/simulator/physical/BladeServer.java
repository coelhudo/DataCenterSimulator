//Ready in web based workload is not set correctly, check it out!
package simulator.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;

public class BladeServer {

    private static final Logger LOGGER = Logger.getLogger(BladeServer.class.getName());

    private enum BladeServerStatus {
        NOT_ASSIGNED_TO_ANY_SYSTEM, NOT_ASSIGNED_TO_ANY_APPLICATION, IDLE, RUNNING_NORMAL, RUNNING_BUSY
    }

    private List<ResponseTime> responseList;
    private List<ResponseTime> responseListWeb;
    private double[] frequencyLevel;
    private double[] powerBusy;
    private double[] powerIdle;
    private double mips;
    private double idleConsumption;
    private String bladeType;
    private double respTime = 0;
    private double resTimeEpoch = 0;
    private double currentCPU = 0;
    private int queueLength;
    private int totalJob = 0;
    private double totalJobEpoch = 0;
    private BladeServerStatus ready;
    private BladeServerStatus savedReady;
    private List<BatchJob> activeBatchJobs;
    private List<BatchJob> blockedBatchJobs;
    private List<EnterpriseJob> enterpriseJobs;
    private List<InteractiveJob> interactiveJobs;
    private int chassisID;
    private int totalFinishedJob = 0;
    private int serverID;
    private int rackId;
    // Application Bundle
    private int timeTreshold = 0;
    private int slaPercentage;
    // WorkLoad Bundle
    private int maxExpectedRes = 0;
    private boolean slaViolation;
    private Environment environment;

    public BladeServer(BladeServerPOD bladeServerPOD, Environment environment) {
        this.environment = environment;
        setRespTime(0);
        // if it is -1 means that it is not put in the proper position yet ID
        // should be set
        chassisID = bladeServerPOD.getChassisID();
        rackId = bladeServerPOD.getRackID();
        serverID = bladeServerPOD.getServerID();
        bladeType = bladeServerPOD.getBladeType();
        powerBusy = bladeServerPOD.getPowerBusy();
        powerIdle = bladeServerPOD.getPowerIdle();
        frequencyLevel = bladeServerPOD.getFrequencyLevel();
        idleConsumption = bladeServerPOD.getIdleConsumption();
        setCurrentCPU(0);
        setActiveBatchList(new ArrayList<BatchJob>());
        setBlockedBatchList(new ArrayList<BatchJob>());
        setEnterprizList(new ArrayList<EnterpriseJob>());
        setWebBasedList(new ArrayList<InteractiveJob>());
        setResponseList(new ArrayList<ResponseTime>());
        setResponseListWeb(new ArrayList<ResponseTime>());
        setQueueLength(0);
        // -3 means it is not assigned to any system yet
        // -2: it is in a system but is not assigned to an application
        // -1 idle
        // 0 or 1 is ready or not just the matter of CPU utilization over 100%
        // or not
        setStatusAsNotAssignedToAnySystem();
        setTotalFinishedJob(0);
        setSLAviolation(false);
        setMips(1.4);
    }

    // Transaction system

    public void configSLAparameter(int time, int percentage) {
        setTimeTreshold(time);
        setSLAPercentage(percentage);
    }
    // Interactive System

    public void configSLAparameter(int time) {
        setMaxExpectedRes(time);
    }

    public double[] getPwrParam() {
        double[] ret = new double[3];
        int i = getCurrentFreqLevel();
        ret[0] = powerBusy[i];
        ret[1] = powerIdle[i];
        ret[2] = idleConsumption;
        return ret;
    }

    public double getPower() {
        double pw = 0, w = 0, a = 0, cpu = 0;
        int j;
        cpu = getCurrentCPU();
        if (getMips() == 0) {
            pw = pw + idleConsumption;
            LOGGER.info("MIPS Zero!!!!");
        } else {
            for (j = 0; j < 3; j++) {
                if (frequencyLevel[j] == mips) {
                    break;
                }
            }
            w = powerIdle[j];
            a = powerBusy[j] - w;
            if (isNotSystemAssigned() || isNotApplicationAssigned() || isIdle()) {
                // if the server is in idle state

                a = 0;
                w = idleConsumption;
                // LOGGER.info(Main.localTime);
            }
            pw = pw + a * cpu / 100 + w;

        }
        return pw;
    }

    public void restart() {
        setRespTime(0);
        setCurrentCPU(0);
        setActiveBatchList(new ArrayList<BatchJob>());
        setBlockedBatchList(new ArrayList<BatchJob>());
        setEnterprizList(new ArrayList<EnterpriseJob>());
        setWebBasedList(new ArrayList<InteractiveJob>());
        setQueueLength(0);
        ///// check
        setStatusAsIdle();
        setTotalFinishedJob(0);
        setMips(1.4);
        setResTimeEpoch(0);
        setTotalJob(0);
        setTotalJobEpoch(0);
        setSLAviolation(false);
    }
    // if it blongs to Enterprise system

    public void makeItIdle(EnterpriseJob jj) {
        // System.out.print("\tIdle\t\t\t\t\t@:"+Main.localTime);
        setStatusAsIdle();
        setMips(frequencyLevel[0]);
    }

    public void feedWork(InteractiveJob interactiveJob) {
        int nums = interactiveJob.getNumberOfJob();
        int time = interactiveJob.getArrivalTimeOfJob();
        setQueueLength(getQueueLength() + nums);
        // FIXME: Create a clone method for InteractiveJob
        InteractiveJob wJob = new InteractiveJob();
        wJob.setArrivalTimeOfJob(time);
        wJob.setNumberOfJob(nums);
        getInteractiveList().add(wJob);
        setTotalJob(getTotalJob() + nums);
    }
    // feeding batch type Job to blade server

    public void feedWork(BatchJob batchJob) {
        getActiveBatchList().add(batchJob);
        setReady();
        setTotalJob(getTotalJob() + 1);
    }
    // feeding webbased type Job to blade server

    public void feedWork(EnterpriseJob enterpriseJob) {
        int nums = enterpriseJob.getNumberOfJob();
        int time = enterpriseJob.getArrivalTimeOfJob();
        setQueueLength(getQueueLength() + nums);
        // FIXME: Create a clone method for EnterpriseJob
        EnterpriseJob wJob = new EnterpriseJob();
        wJob.setArrivalTimeOfJob(time);
        wJob.setNumberOfJob(nums);
        getEnterpriseList().add(wJob);
        setTotalJob(nums + getTotalJob());
    }

    public int getCurrentFreqLevel() {
        for (int i = 0; i < frequencyLevel.length; i++) {
            if (getMips() == frequencyLevel[i]) {
                return i; // statrs from 1 not zero!
            }
        }
        LOGGER.info("wrong frequency level !! ");
        return -1;
    }

    public int increaseFrequency() {
        // LOGGER.info("MIIIPPSSS "+Mips);
        if (getCurrentFreqLevel() == 2) {
            return 0; // FIXME: This should be 2 i think. That's my conclusion
                      // based on the behaviour of decreaseFrequency
        } else {
            setMips(frequencyLevel[getCurrentFreqLevel() + 1]); // getCurrentFrequency
            // already
            // increased
            // the
            // freq
            // level
            environment.updateNumberOfMessagesFromDataCenterToSystem();
        }
        if (getMips() == 0) {
            LOGGER.info("Mipss sefr shoodd!!!");
        }
        return 1;
    }

    public int decreaseFrequency() {
        // LOGGER.info("Decreasing frequency");
        if (getCurrentFreqLevel() == 0) {// LOGGER.info("Minimum
            // Frequency Level ~~~ ");
            return 0;
        } else {
            setMips(frequencyLevel[getCurrentFreqLevel() - 1]);
            environment.updateNumberOfMessagesFromDataCenterToSystem();
        }
        if (getMips() == 0) {
            LOGGER.info("Mipss sefr shoodd!!!");
        }
        return 1;
    }

    public int run() {
        int num = getActiveBatchList().size(), index = 0, index_1 = 0, rmpart = 0;
        double extraShare = 0;
        if (num == 0) {
            setStatusAsRunningNormal();
            setCurrentCPU(0);
            return 0;
        }
        double share = getMips() / num;
        final double share_t = share;
        double tempCpu = 0;
        while (index < num) {
            index_1 = index;
            for (int i = 0; i < getActiveBatchList().size(); i++) {
                BatchJob job = getActiveBatchList().get(i);
                if (job.getUtilization() <= share & job.getIsChangedThisTime() == 0) {
                    extraShare = extraShare + share - job.getUtilization();
                    index++;
                    job.setIsChangedThisTime(1);
                    tempCpu = job.getUtilization() + tempCpu;
                    i = i - done(job, share_t);
                }
            }
            for (BatchJob batchJob : getActiveBatchList()) {
                if (batchJob.getIsChangedThisTime() == 0) {
                    rmpart++;
                }
            }
            if (rmpart != 0) {
                share = share + extraShare / rmpart;
            }
            rmpart = 0;
            extraShare = 0;
            if (index == index_1) {
                break;
            }
        }
        for (int i = 0; i < getActiveBatchList().size(); i++) {
            BatchJob job = getActiveBatchList().get(i);
            if (job.getIsChangedThisTime() == 0) {
                final double utilization = job.getUtilization();
                final Double shareUtilizationRatio = share / utilization;
                if (shareUtilizationRatio.isInfinite()) {
                    throw new ArithmeticException("Division by Zero");
                }
                
                if (shareUtilizationRatio > 1) {
                    LOGGER.info("share more than one!\t" + share_t + "\t" + share + "\t" + utilization + "\t"
                            + environment.getCurrentLocalTime());
                }
                job.setIsChangedThisTime(1);
                i = i - done(job, shareUtilizationRatio);
                tempCpu = tempCpu + share;
            }
        }

        for (BatchJob job : getActiveBatchList()) {
            job.setIsChangedThisTime(0);
        }
        // Inja be nazaram /MIPS ham mikhad ke sad beshe fek konam MIPS ro dar
        // nazar nagereftam!
        setCurrentCPU(100.0 * tempCpu / getMips());
        setReady();
        return 1;
    }

    public int done(BatchJob job, double share) {
        // return 1 means: a job has been finished
        if (share == 0) {
            LOGGER.info(
                    "In DONE share== zero00000000000000000000000000000000000000oo,revise the code  need some work!");
            job.setExitTime(environment.getCurrentLocalTime());
            getActiveBatchList().remove(job);
            return 1;
        }
        int ki = job.getThisNodeIndex(getServerID());
        if (ki == -1) {
            LOGGER.info("Blade server is wrong in BladeServer!!!");
        }

        job.setRemainAt(ki, job.getRemainAt(ki) - share);
        if (job.getRemainAt(ki) <= 0) {
            getBlockedBatchList().add(job);
            job.setIsChangedThisTime(0);
            getActiveBatchList().remove(job);// still exsits in other nodes
            if (job.allDone()) {
                
                setRespTime(job.Finish(environment.getCurrentLocalTime()) + getResponseTime());

                setTotalFinishedJob(getTotalFinishedJob() + 1);
                return 1;
            }
        }
        return 0;
    }

    public void setReady() {
        double tmp = 0, treshold = 1;
        int num = 0, i;
        num = getActiveBatchList().size();
        for (i = 0; i < num; i++) {
            tmp = tmp + getActiveBatchList().get(i).getUtilization();
        }
        if (tmp >= treshold) {
            setStatusAsRunningBusy();
        } else {
            setStatusAsRunningNormal();
        }
    }

    // void addToresponseArray(double num,int time)
    // {
    // responseTime t= new responseTime();
    // t.numberOfJob=num;
    // t.responseTime=time;
    // responseList.add(t);
    // }
    // void addToresponseArrayWeb(double num,int time)
    // {
    // if(time>maxExpectedRes)
    // SLAviolation=true;
    // responseTime t= new responseTime();
    // t.numberOfJob=num;
    // t.responseTime=time;
    // responseList.add(t);
    // }
    // int whichServer(int i)
    // {
    // return i%DataCenter.theDataCenter.chassisSet.get(0).servers.size();
    // }
    // int whichChasiss (int i)
    // {
    // return i/DataCenter.theDataCenter.chassisSet.get(0).servers.size();
    // }

    public List<ResponseTime> getResponseList() {
        return responseList;
    }

    public void setResponseList(List<ResponseTime> responseList) {
        this.responseList = responseList;
    }

    public List<ResponseTime> getResponseListWeb() {
        return responseListWeb;
    }

    public void setResponseListWeb(List<ResponseTime> responseListWeb) {
        this.responseListWeb = responseListWeb;
    }

    public double getMips() {
        return mips;
    }

    public void setMips(double mips) {
        this.mips = mips;
    }

    public double getResponseTime() {
        return respTime;
    }

    public void setRespTime(double respTime) {
        this.respTime = respTime;
    }

    public double getResTimeEpoch() {
        return resTimeEpoch;
    }

    public void setResTimeEpoch(double resTimeEpoch) {
        this.resTimeEpoch = resTimeEpoch;
    }

    public double getCurrentCPU() {
        return currentCPU;
    }

    public void setCurrentCPU(double currentCPU) {
        this.currentCPU = currentCPU;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public void setQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }

    public int getTotalJob() {
        return totalJob;
    }

    public void setTotalJob(int totalJob) {
        this.totalJob = totalJob;
    }

    public double getTotalJobEpoch() {
        return totalJobEpoch;
    }

    public void setTotalJobEpoch(double totalJobEpoch) {
        this.totalJobEpoch = totalJobEpoch;
    }
    
    public boolean isNotSystemAssigned() {
        return ready == BladeServerStatus.NOT_ASSIGNED_TO_ANY_SYSTEM;
    }
    
    public boolean isNotApplicationAssigned() {
        return ready == BladeServerStatus.NOT_ASSIGNED_TO_ANY_APPLICATION;
    }
    
    public boolean isIdle() {
        return ready == BladeServerStatus.IDLE;
    }
    
    public boolean isRunningNormal() {
        return ready == BladeServerStatus.RUNNING_NORMAL;
    }
    
    public boolean isRunningBusy() {
        return ready == BladeServerStatus.RUNNING_BUSY;
    }
    
    public boolean isRunning() {
        return ready == BladeServerStatus.RUNNING_BUSY || ready == BladeServerStatus.RUNNING_NORMAL;
    }

    private void setStatusAsNotAssignedToAnySystem() {
        this.ready = BladeServerStatus.NOT_ASSIGNED_TO_ANY_SYSTEM;
    }

    public void setStatusAsNotAssignedToAnyApplication() {
        this.ready = BladeServerStatus.NOT_ASSIGNED_TO_ANY_APPLICATION;
    }

    public void setStatusAsIdle() {
        this.ready = BladeServerStatus.IDLE;
    }

    public void setStatusAsRunningNormal() {
        this.ready = BladeServerStatus.RUNNING_NORMAL;
    }

    public void setStatusAsRunningBusy() {
        this.ready = BladeServerStatus.RUNNING_BUSY;
    }

    public void restoreStatus() {
        this.ready = savedReady;
    }

    public void saveStatus() {
        this.savedReady = this.ready;
    }

    public List<BatchJob> getActiveBatchList() {
        return activeBatchJobs;
    }

    public void setActiveBatchList(List<BatchJob> activeBatchList) {
        this.activeBatchJobs = activeBatchList;
    }

    public List<BatchJob> getBlockedBatchList() {
        return blockedBatchJobs;
    }

    public void setBlockedBatchList(List<BatchJob> blockedBatchList) {
        this.blockedBatchJobs = blockedBatchList;
    }

    public List<EnterpriseJob> getEnterpriseList() {
        return enterpriseJobs;
    }

    public void setEnterprizList(List<EnterpriseJob> enterpriseJobs) {
        this.enterpriseJobs = enterpriseJobs;
    }

    public List<InteractiveJob> getInteractiveList() {
        return interactiveJobs;
    }

    public void setWebBasedList(List<InteractiveJob> webBasedList) {
        this.interactiveJobs = webBasedList;
    }

    public int getTotalFinishedJob() {
        return totalFinishedJob;
    }

    public void setTotalFinishedJob(int totalFinishedJob) {
        this.totalFinishedJob = totalFinishedJob;
    }

    public int getChassisID() {
        return chassisID;
    }

    public void setChassisID(int chassisID) {
        this.chassisID = chassisID;
    }

    public int getServerID() {
        return serverID;
    }

    public int getRackId() {
        return rackId;
    }

    public void setRackId(int rackId) {
        this.rackId = rackId;
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

    public int getMaxExpectedRes() {
        return maxExpectedRes;
    }

    public void setMaxExpectedRes(int maxExpectedRes) {
        this.maxExpectedRes = maxExpectedRes;
    }

    public boolean isSLAviolation() {
        return slaViolation;
    }

    public void setSLAviolation(boolean slaViolation) {
        this.slaViolation = slaViolation;
    }

    public String getBladeType() {
        return bladeType;
    }

    public double getFrequencyLevelAt(int index) {
        return frequencyLevel[index];
    }

    public int getNumberOfFrequencyLevel() {
        return frequencyLevel.length;
    }

    public double getPowerBusyAt(int index) {
        return powerBusy[index];
    }

    public int getNumberOfPowerBusy() {
        return powerBusy.length;
    }

    public double getPowerIdleAt(int index) {
        return powerIdle[index];
    }

    public int getNumberOfPowerIdle() {
        return powerIdle.length;
    }

    public double getIdleConsumption() {
        return idleConsumption;
    }

    /*
     * double getMeanResTimeLastEpoch() {
     * 
     * if (resTimeEpoch == 0) // the first time in { resTimeEpoch = respTime;
     * totalJobEpoch = totalJob - queueLength; LOGGER.info(
     * "First   Last Epoch   " + respTime + totalJobEpoch + "\t" + chassisID);
     * if (totalJobEpoch > 0) return respTime / totalJobEpoch; else return 0; }
     * else { double tempTime = respTime - resTimeEpoch; double tempJob =
     * totalJob - queueLength - totalJobEpoch; resTimeEpoch = respTime;
     * totalJobEpoch = totalJob - queueLength; LOGGER.info(
     * "in get MeanResponse Last Epoch   " + tempTime / tempJob + "\t" +
     * chassisID); if (tempJob != 0) return tempTime / tempJob; else return 0; }
     * }
     */
}
