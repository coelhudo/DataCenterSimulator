package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulator.am.ApplicationAM;
import simulator.jobs.EnterpriseJob;
import simulator.physical.BladeServer;
import simulator.schedulers.Scheduler;

public final class EnterpriseApp {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseApp.class.getName());
    
    private int id = 0;
    // int usedNode=0;
    private int maxProc = 0;
    private int minProc = 0;
    private int maxExpectedResTime = 0;
    private List<BladeServer> ComputeNodeList;
    // ArrayList <Integer> ComputeNodeIndex;
    private List<EnterpriseJob> queueApp;
    private List<ResponseTime> responseList;
    // jobPlacement placement;
    private int timeTreshold = 0;
    private int SLAPercentage;
    private int SLAviolation = 0;
    private int NumofViolation = 0;
    private BufferedReader bis = null;
    private File logFile;
    private ApplicationAM AM;
    // EnterpriseSystem mySys; //Application knows in which Sys it is located.
    // initialize in EnterpriseSystem
    private int MaxNumberOfRequest = 0; // # of Request can be handled by number
    // of basic node which for 100% CPU
    // utilization
    private int NumberofBasicNode = 0;
    GeneralSystem parent;
    private Environment environment;

    public EnterpriseApp(String path, Node node, GeneralSystem parent, Environment environment) {
        this.parent = parent;
        this.environment = environment;
        setComputeNodeList(new ArrayList<BladeServer>());
        setQueueApp(new ArrayList<EnterpriseJob>());
        setResponseList(new ArrayList<ResponseTime>());
        // ComputeNodeIndex=new ArrayList<Integer>();
        readFromNode(node, path);
        configSLAallcomputingNode();
        // placement= new jobPlacement(ComputeNodeList) ;
        setAM(new ApplicationAM((EnterpriseSystem) parent, this, environment));
    }

    public double numberOfWaitingJobs() {
        double lenJob = 0;
        for (EnterpriseJob job : getQueueApp()) {
            if (job.getArrivalTimeOfJob() <= environment.getCurrentLocalTime()) {
                lenJob = +job.getNumberOfJob(); //FIXME: += instead of = + 
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
        bladeServer.setReady(-2);
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
            j.setNumberOfJob(Double.parseDouble(numbers[1]));
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
            if (getQueueApp().get(0).getArrivalTimeOfJob() == environment.getCurrentLocalTime()
                    | getQueueApp().get(0).getArrivalTimeOfJob() < environment.getCurrentLocalTime()) {
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
            if (bladeServer.getReady() != -1) { // if it is idle
                // dont
                // change it! it
                // is
                // responsibility
                // of
                // its AM to
                // change
                // it
                bladeServer.setCurrentCPU(0);
                bladeServer.setReady(1);
            }
        }
    }
    // False: logfile is finished and no remain job

    boolean runAcycle() {
        int readingResult = readWebJob();
        //////// RESET READY FLAGS for all nodes
        resetReadyFlagAndCPU();
        // need more thought
        if (readingResult == 0) // we have jobs but it is not the time to run
        // them
        {
            return true;
        }
        if (readingResult == -2 & getQueueApp().isEmpty()) // no jobs are in the
        // queue and in
        // logfile
        {
            return false;
        }
        double CPUpercentage = 0;
        int numberofReadyNodes = 0;
        double beenRunJobs = 0; // number of jobs have been run so far
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == 1) {
                CPUpercentage = (100.0 - bladeServer.getCurrentCPU()) * bladeServer.getMips() + CPUpercentage;
                numberofReadyNodes++;
            }
        }
        double capacityOfNode = (int) Math
                .ceil((getMaxNumberOfRequest() * CPUpercentage) / (getNumberofBasicNode() * 100.0));
        double capacityOfNode_COPY = capacityOfNode;
        EnterpriseJob jj = new EnterpriseJob();
        // jj=queueApp.get(0);

        jj = (EnterpriseJob) parent.getScheduler().nextJob(getQueueApp());
        while (capacityOfNode > 0) {
            capacityOfNode = capacityOfNode - jj.getNumberOfJob();
            if (capacityOfNode == 0) {
                addToresponseArray(jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                // LOGGER.info((Main.localTime-wJob.arrivalTimeOfJob+1)*(wJob.numberOfJob)
                // +"\t"+wJob.numberOfJob+"\t q len="+queueLength);
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueApp().remove(jj);
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
            if (capacityOfNode > 0) // still we have capacity to run the jobs
            {
                addToresponseArray(jj.getNumberOfJob(),
                        (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueApp().remove(jj);
                while (!getQueueApp().isEmpty()) {

                    // jj=queueApp.get(0);
                    jj = (EnterpriseJob) parent.getScheduler().nextJob(getQueueApp());
                    double copyTedat = capacityOfNode;
                    capacityOfNode = capacityOfNode - jj.getNumberOfJob();
                    if (capacityOfNode == 0) {
                        addToresponseArray(jj.getNumberOfJob(),
                                (environment.getCurrentLocalTime() - jj.getArrivalTimeOfJob() + 1));
                        // LOGGER.info(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueApp().remove(0);
                        break;
                    }
                    if (capacityOfNode < 0) // there are more jobs than
                    // 1000.0*MIPS
                    {
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
                        getQueueApp().remove(0);
                    }
                } // end while
                break;
            } // end if
        }
        if (capacityOfNode_COPY == beenRunJobs) // we're done all our capacity
        {
            for (BladeServer bladeServer : getComputeNodeList()) {
                if (bladeServer.getReady() == 1) {
                    bladeServer.setCurrentCPU(100);
                    bladeServer.setReady(0);
                }
            }
            // usedNode=usedNode+ComputeNodeList.size();
        } else if (beenRunJobs < 0) {
            LOGGER.info("it is impossible!!!!  Enterprise BoN");
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
                double reqSpace = (int) Math
                        .ceil(CPUspace * getMaxNumberOfRequest() / (getNumberofBasicNode() * 100.0));
                getComputeNodeList().get(serID).setCurrentCPU(100);
                getComputeNodeList().get(serID).setReady(0);
                beenRunJobs = beenRunJobs - reqSpace;
                if (beenRunJobs == 0) {
                    k++;
                    break;
                }
                if (beenRunJobs < 0) {
                    getComputeNodeList().get(serID)
                            .setCurrentCPU((int) Math.ceil((reqSpace + beenRunJobs) * 100 / reqSpace));
                    getComputeNodeList().get(serID).setReady(1);
                    k++;
                    break;
                }
            }
            // LOGGER.info(k +"\t Running node= "+numberofReadyNodes);
            // usedNode=usedNode+k;
        }
        // AM.monitor();
        // AM.analysis(SLAviolation);
        // AM.planning();
        return !(getQueueApp().isEmpty() && readingResult == -2);
    }

    public int numberofRunningNode() {
        int cnt = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() > -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public int numberofIdleNode() {
        int cnt = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public void activeOneNode() {
        int i = 0;
        for (i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).getReady() == -1) {
                getComputeNodeList().get(i).restart();
                getComputeNodeList().get(i).setReady(1);
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

    void readFromNode(Node node, String path) {
        getComputeNodeList().clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("id")) {
                    setID(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); // Id
                    // of
                    // the
                    // application
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplicationWorkLoad")) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        bis = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
                    } catch (IOException e) {
                        LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("MaxNumberOfRequest")) {
                    setMaxNumberOfRequest(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("NumberofBasicNode")) {
                    setNumberofBasicNode(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("timeTreshold")) {
                    setTimeTreshold(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                    setMaxExpectedResTime(getTimeTreshold());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Percentage")) {
                    setSLAPercentage(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                } // We dont have server list now but may be in future we had
                /*
                 * if(childNodes.item(i).getNodeName().equalsIgnoreCase(
                 * "ServerList")) { String str =
                 * childNodes.item(i).getChildNodes().item(0).getNodeValue().
                 * trim(); String[] split = str.split(" "); for(int
                 * j=0;j<split.length;j++) { int
                 * serverIndex=Integer.parseInt(split[j]); int
                 * indexChassis=serverIndex/DC.chassisSet.get(0).servers.size();
                 * int
                 * indexServer=serverIndex%DC.chassisSet.get(0).servers.size();
                 * addCompNodetoBundle(DC.chassisSet.get(indexChassis).servers.
                 * get(indexServer));
                 * DC.chassisSet.get(indexChassis).servers.get(indexServer).Mips
                 * =1; DC.chassisSet.get(indexChassis).servers.get(indexServer).
                 * ready=1; ComputeNodeIndex.add(serverIndex); } }
                 */
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("minProcessor")) {
                    setMinProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("maxProcessor")) {
                    setMaxProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }

            }
        }
    }

    public double getAverageCPUutil() {
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

    void destroyApplication() throws IOException {
        for (BladeServer bladeServer : getComputeNodeList()) {
            bladeServer.restart();
            bladeServer.setReady(-2); // ready to be assinged to
            // other application
        }
        bis.close();
    }

    boolean isThereIdleNode() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -1) {
                return true;
            }
        }
        return false;
    }

    //FIXME: why get index instead of the instance?
    public int myFirstIdleNode() {
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).getReady() == -1) {
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
     * //LOGGER.info("respTime="+serverList.get(i).respTime+
     * "\t TotalJob="+serverList.get(i).totalJob); }
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
        return ComputeNodeList;
    }

    public void setComputeNodeList(ArrayList<BladeServer> computeNodeList) {
        ComputeNodeList = computeNodeList;
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
        return SLAPercentage;
    }

    public void setSLAPercentage(int sLAPercentage) {
        SLAPercentage = sLAPercentage;
    }

    public int getNumofViolation() {
        return NumofViolation;
    }

    public void setNumofViolation(int numofViolation) {
        NumofViolation = numofViolation;
    }

    public int getSLAviolation() {
        return SLAviolation;
    }

    public void setSLAviolation(int sLAviolation) {
        SLAviolation = sLAviolation;
    }

    public ApplicationAM getAM() {
        return AM;
    }

    public void setAM(ApplicationAM aM) {
        AM = aM;
    }

    public int getMaxNumberOfRequest() {
        return MaxNumberOfRequest;
    }

    public void setMaxNumberOfRequest(int maxNumberOfRequest) {
        MaxNumberOfRequest = maxNumberOfRequest;
    }

    public int getNumberofBasicNode() {
        return NumberofBasicNode;
    }

    public void setNumberofBasicNode(int numberofBasicNode) {
        NumberofBasicNode = numberofBasicNode;
    }
}
