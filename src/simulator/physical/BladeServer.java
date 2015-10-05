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

public class BladeServer extends DataCenterEntity {

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
    private BladeServerStatus status;
    private BladeServerStatus savedReady;
    private List<BatchJob> activeBatchJobs;
    private List<BatchJob> blockedBatchJobs;
    private List<EnterpriseJob> enterpriseJobs;
    private List<InteractiveJob> interactiveJobs;
    private int totalFinishedJob = 0;
    // Application Bundle
    private int timeTreshold = 0;
    private int slaPercentage;
    // WorkLoad Bundle
    private int maxExpectedRes = 0;
    private Environment environment;
    private final BladeServerStats stats = new BladeServerStats();
    
    public BladeServer(BladeServerPOD bladeServerPOD, Environment environment) {
        super(bladeServerPOD.getID());
        this.environment = environment;
        setRespTime(0);
        // if it is -1 means that it is not put in the proper position yet ID
        // should be set
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
        setStatusAsNotAssignedToAnySystem();
        setTotalFinishedJob(0);
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
        if (getMips() == 0 || !isRunning()) {
            return idleConsumption;
        }

        int j;
        for (j = 0; j < 3; j++) {
            if (frequencyLevel[j] == mips) {
                break;
            }
        }

        final double w = powerIdle[j];
        final double a = powerBusy[j] - w;

        return a * getCurrentCPU() / 100 + w;
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
    }
    // if it blongs to Enterprise system

    public void makeItIdle(EnterpriseJob jj) {
        // System.out.print("\tIdle\t\t\t\t\t@:"+Main.localTime);
        setStatusAsIdle();
        setMips(frequencyLevel[0]);
    }

    public void feedWork(InteractiveJob interactiveJob) {
        final int nums = interactiveJob.getNumberOfJob();
        setQueueLength(getQueueLength() + nums);
        getInteractiveList().add(new InteractiveJob(interactiveJob));
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
        final int nums = enterpriseJob.getNumberOfJob();
        setQueueLength(getQueueLength() + nums);
        getEnterpriseList().add(new EnterpriseJob(enterpriseJob));
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
        }

        // getCurrentFrequency already increased the freq level
        setMips(frequencyLevel[getCurrentFreqLevel() + 1]);
        environment.updateNumberOfMessagesFromDataCenterToSystem();

        if (getMips() == 0) {
            LOGGER.info("Mipss sefr shoodd!!!");
        }

        return 1;
    }

    public int decreaseFrequency() {
        if (getCurrentFreqLevel() == 0) {
            return 0;
        }

        setMips(frequencyLevel[getCurrentFreqLevel() - 1]);
        environment.updateNumberOfMessagesFromDataCenterToSystem();

        if (getMips() == 0) {
            LOGGER.info("Mipss sefr shoodd!!!");
        }

        return 1;
    }

    public int run() {
        final int num = getActiveBatchList().size();
        int index = 0;
        if (num == 0) {
            setStatusAsRunningNormal();
            setCurrentCPU(0);
            return 0;
        }
        double share = getMips() / num;
        final double originalShare = share;
        double tempCpu = 0;
        boolean anyJobModified = true;
        while (index < num && anyJobModified) {

            double extraShare = 0;
            anyJobModified = false;
            for (int i = 0; i < getActiveBatchList().size(); i++) {
                BatchJob job = getActiveBatchList().get(i);
                if (job.getUtilization() > share || job.isModified()) {
                    continue;
                }

                extraShare = extraShare + share - job.getUtilization();
                index++;
                anyJobModified = true;
                job.setAsModified();
                tempCpu = job.getUtilization() + tempCpu;
                if (done(job, originalShare)) {
                    i--;
                }
            }

            share = adjustShare(share, extraShare);
        }

        for (int i = 0; i < getActiveBatchList().size(); i++) {
            BatchJob job = getActiveBatchList().get(i);
            if (job.isModified()) {
                continue;
            }

            final double utilization = job.getUtilization();
            final Double shareUtilizationRatio = share / utilization;
            if (shareUtilizationRatio.isInfinite()) {
                throw new ArithmeticException("Division by Zero");
            }

            if (shareUtilizationRatio > 1) {
                LOGGER.info("share more than one!\t" + originalShare + "\t" + share + "\t" + utilization + "\t"
                        + environment.getCurrentLocalTime());
            }
            job.setAsModified();
            if (done(job, shareUtilizationRatio)) {
                i--;
            }
            tempCpu = tempCpu + share;
        }

        for (BatchJob job : getActiveBatchList()) {
            job.setAsNotModified();
        }
        // Inja be nazaram /MIPS ham mikhad ke sad beshe fek konam MIPS ro dar
        // nazar nagereftam!
        setCurrentCPU(100.0 * tempCpu / getMips());
        setReady();
        return 1;
    }

    private double adjustShare(double share, double extraShare) {
        double countJobsNotModified = 0;
        for (BatchJob batchJob : getActiveBatchList()) {
            if (!batchJob.isModified()) {
                countJobsNotModified++;
            }
        }

        if (countJobsNotModified != 0) {
            return share + extraShare / countJobsNotModified;
        }

        return share;
    }

    public boolean done(BatchJob job, double share) {
        // return 1 means: a job has been finished
        if (share == 0) {
            LOGGER.info("In DONE share== zero,revise the code  need some work!");
            job.setExitTime(environment.getCurrentLocalTime());
            getActiveBatchList().remove(job);
            return true;
        }

        job.setRemainAt(getID(), job.getRemainAt(getID()) - share);
        if (job.getRemainAt(getID()) > 0) {
            return false;
        }

        getBlockedBatchList().add(job);
        job.setAsNotModified();
        getActiveBatchList().remove(job);

        if (!job.allDone()) {
            return false;
        }

        setRespTime(job.Finish(environment.getCurrentLocalTime()) + getResponseTime());
        setTotalFinishedJob(getTotalFinishedJob() + 1);

        return true;

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
        return status == BladeServerStatus.NOT_ASSIGNED_TO_ANY_SYSTEM;
    }

    public boolean isNotApplicationAssigned() {
        return status == BladeServerStatus.NOT_ASSIGNED_TO_ANY_APPLICATION;
    }

    public boolean isIdle() {
        return status == BladeServerStatus.IDLE;
    }

    public boolean isRunningNormal() {
        return status == BladeServerStatus.RUNNING_NORMAL;
    }

    public boolean isRunningBusy() {
        return status == BladeServerStatus.RUNNING_BUSY;
    }

    public boolean isRunning() {
        return status == BladeServerStatus.RUNNING_BUSY || status == BladeServerStatus.RUNNING_NORMAL;
    }

    private void setStatusAsNotAssignedToAnySystem() {
        this.status = BladeServerStatus.NOT_ASSIGNED_TO_ANY_SYSTEM;
    }

    public void setStatusAsNotAssignedToAnyApplication() {
        this.status = BladeServerStatus.NOT_ASSIGNED_TO_ANY_APPLICATION;
    }

    public void setStatusAsIdle() {
        this.status = BladeServerStatus.IDLE;
    }

    public void setStatusAsRunningNormal() {
        this.status = BladeServerStatus.RUNNING_NORMAL;
    }

    public void setStatusAsRunningBusy() {
        this.status = BladeServerStatus.RUNNING_BUSY;
    }

    public void restoreStatus() {
        this.status = savedReady;
    }

    public void saveStatus() {
        this.savedReady = this.status;
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

    public class BladeServerStats extends DataCenterEntityStats {
        public BladeServerStatus getStatus() {
            return status;
        }
    }

    @Override
    public DataCenterEntityStats getStats() {
        return stats;
    }
}
