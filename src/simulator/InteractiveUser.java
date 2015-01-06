package simulator;

import simulator.physical.BladeServer;
import simulator.am.IteractiveUserAM;
import simulator.jobs.InteractiveJob;
import simulator.schedulers.Scheduler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InteractiveUser {

    int arrivalTime;
    public int maxProc = 0, minProc = 0, maxExpectedResTime = 0;
    double duration;
    double remain;
    public int id = 0;
    String logFileName;
    File logFile = null;
    public ArrayList<BladeServer> ComputeNodeList;
    public ArrayList<Integer> ComputeNodeIndex;
    public ArrayList<InteractiveJob> queueWL;
    public ArrayList<ResponseTime> responseList;
    //jobPlacement placement;
    BufferedReader bis = null;
    //SLA
    public int SLAviolation = 0;
    public IteractiveUserAM AM;
    int usedNode = 0;
    public int MaxNumberOfRequest = 0; //# of Request can be handled by number of basic node which for 100% CPU utilization
    public int NumberofBasicNode = 0;
    GeneralSystem parent;

    public InteractiveUser(GeneralSystem parent) {
        ComputeNodeList = new ArrayList<BladeServer>();
        ComputeNodeIndex = new ArrayList<Integer>();
        queueWL = new ArrayList<InteractiveJob>();
        responseList = new ArrayList<ResponseTime>();
        logFileName = new String();
        // placement=new jobPlacement(ComputeNodeList);
        AM = new IteractiveUserAM((InteractiveSystem) parent, this);
        this.parent = parent;
    }

    public void addCompNodetoBundle(BladeServer b) {
        b.restart();
        ComputeNodeList.add(b);
    }
    /*
    Return Values:
     * 1: read successfully
     * 0:put in waiting list
     * -1: end of file or error
     */

    int readingLogFile() {
        if (bis == null) {
            try {
                logFile = new File(logFileName);
                bis = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
            } catch (IOException e) {
                System.out.println("Uh oh, got an IOException error!" + e.getMessage());
            } finally {
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
            j.arrivalTimeOfJob = Integer.parseInt(numbers[0]);
            j.numberOfJob = Double.parseDouble(numbers[1]) * 50;
            queueWL.add(j);
            return 1;
            //System.out.println("Readed inputTime= " + inputTime + " Job Reqested Time=" + j.startTime+" Total job so far="+ total);
        } catch (IOException ex) {
            System.out.println("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
    }
    //reset all working node ready flag and CPU utilization 

    void resetReadyFlag() {
        int i;
        for (i = 0; i < ComputeNodeList.size(); i++) {
            ComputeNodeList.get(i).currentCPU = 0;
            ComputeNodeList.get(i).ready = 1;
        }
    }

    int readWebJob() {
        int retReadLogfile = readingLogFile();
        if (queueWL.size() > 0) {
            if (queueWL.get(0).arrivalTimeOfJob == Simulator.getInstance().localTime | queueWL.get(0).arrivalTimeOfJob < Simulator.getInstance().localTime) {
                return 1;
            } else {
                return 0;
            }
        }
        // ending condition means there is no job in the logfile
        //System.out.println(" One dispacher !!!  in the readWebJob    interactive       "+retReadLogfile);
        return retReadLogfile;
    }

    boolean runAcycle() {
        SLAviolation = 0;
        int readingResult = readWebJob();
        ////////RESET READY FLAGS for all nodes
        resetReadyFlag();
        //need more thought
        if (readingResult == 0) //we have jobs but it is not the time to run them
        {
            return true;
        }
        if (readingResult == -2 & queueWL.isEmpty()) // no jobs are in the queue and in logfile
        {
            return false;
        }
        double CPUpercentage = 0;
        int numberofReadyNodes = 0;
        double beenRunJobs = 0;  //number of jobs have been run so far
        int i = 0;
        for (i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == 1) {
                CPUpercentage = (100.0 - ComputeNodeList.get(i).currentCPU) * ComputeNodeList.get(i).Mips + CPUpercentage;
                numberofReadyNodes++;
            }
        }
        double capacityOfNode = (int) Math.ceil((MaxNumberOfRequest * CPUpercentage) / (NumberofBasicNode * 100.0));
        double capacityOfNode_COPY = capacityOfNode;
        InteractiveJob jj = new InteractiveJob();
        //jj=queueWL.get(0);
        jj = (InteractiveJob) parent.schdler.nextJob(queueWL);
        while (capacityOfNode > 0) {
            capacityOfNode = capacityOfNode - jj.numberOfJob;
            if (capacityOfNode == 0) {
                addToresponseArray(jj.numberOfJob, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                //System.out.println((Main.localTime-wJob.arrivalTimeOfJob+1)*(wJob.numberOfJob) +"\t"+wJob.numberOfJob+"\t q len="+queueLength);
                beenRunJobs = beenRunJobs + jj.numberOfJob;
                queueWL.remove(jj);
                break;
            }
            if (capacityOfNode < 0) // there are more jobs than capacity
            {
                addToresponseArray(capacityOfNode + jj.numberOfJob, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                beenRunJobs = beenRunJobs + capacityOfNode + jj.numberOfJob;
                jj.numberOfJob = -1 * capacityOfNode;
                //System.out.println(1000.0*Mips);
                break;
            }
            if (capacityOfNode > 0) //still we have capacity to run the jobs
            {
                addToresponseArray(jj.numberOfJob, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                beenRunJobs = beenRunJobs + jj.numberOfJob;
                queueWL.remove(jj);
                while (queueWL.size() > 0) {
                    //jj=queueWL.get(0);
                    jj = (InteractiveJob) parent.schdler.nextJob(queueWL);
                    double copyTedat = capacityOfNode;
                    capacityOfNode = capacityOfNode - jj.numberOfJob;
                    if (capacityOfNode == 0) {
                        addToresponseArray(jj.numberOfJob, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                        //System.out.println(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.numberOfJob;
                        queueWL.remove(0);
                        break;
                    }
                    if (capacityOfNode < 0) //there are more jobs than 1000.0*MIPS
                    {
                        addToresponseArray(copyTedat, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                        jj.numberOfJob = -1 * capacityOfNode;
                        beenRunJobs = beenRunJobs + copyTedat;
                        //System.out.println(copyTedat);
                        break;
                    }
                    if (capacityOfNode > 0) {
                        addToresponseArray(jj.numberOfJob, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                        //System.out.println(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.numberOfJob;
                        queueWL.remove(0);
                    }
                } //end while
                break;
            }//end if
        }
        if (capacityOfNode_COPY == beenRunJobs)// we're done all our capacity
        {
            for (int k = 0; k < ComputeNodeList.size(); k++) {
                ComputeNodeList.get(k).currentCPU = 100;
                ComputeNodeList.get(k).ready = 0;
            }
            usedNode = usedNode + ComputeNodeList.size();
        } else if (beenRunJobs < 0) {
            System.out.println("it is impossible!!!!  webbased BoN");
        } else if (beenRunJobs > 0) {
            int k = 0;
            for (k = 0; k < numberofReadyNodes; k++) {
                int serID = parent.rc.nextServer(ComputeNodeList);
                if (serID == -2) {
                    System.out.println("enterPrise BoN : servID =-2\t " + k + "\t" + numberofReadyNodes);
                    break;
                }
                double CPUspace = (100 - ComputeNodeList.get(serID).currentCPU) * ComputeNodeList.get(serID).Mips;
                double reqSpace = (int) Math.ceil(CPUspace * MaxNumberOfRequest / (NumberofBasicNode * 100.0));
                ComputeNodeList.get(serID).currentCPU = 100;
                ComputeNodeList.get(serID).ready = 0;
                beenRunJobs = beenRunJobs - reqSpace;
                if (beenRunJobs == 0) {
                    k++;
                    break;
                }
                if (beenRunJobs < 0) {
                    ComputeNodeList.get(serID).currentCPU = (int) Math.ceil((reqSpace + beenRunJobs) * 100 / reqSpace);
                    ComputeNodeList.get(serID).ready = 1;
                    k++;
                    break;
                }
            }
            //System.out.println(k +"\t Running node= "+numberofReadyNodes);
            usedNode = usedNode + k;
        }
        AM.monitor();
        AM.analysis(SLAviolation);
        //AM.planning();
        if (queueWL.isEmpty() && readingResult == -2) {
            return false;
        } else {
            return true;
        }
    }

    void addToresponseArray(double num, int time) {
        if (time > maxExpectedResTime) {
            SLAviolation++;
        }
        ResponseTime resTime = new ResponseTime();
        resTime.setNumberOfJob(num);
        resTime.setResponseTime(time);
        responseList.add(resTime);
    }

    void setReadyFlag() {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready != -1) //-1 : means this server is idle not so as to compute its idle power
            {
                if (ComputeNodeList.get(i).WebBasedList.isEmpty()) {
                    ComputeNodeList.get(i).ready = 1;
                    ComputeNodeList.get(i).currentCPU = 0;
                }// bahs
                else {
                    ComputeNodeList.get(i).ready = 0;
                    //System.out.println("queulength in SetReady FLag: "+ComputeNodeList.get(i).queueLength);
                }
            }
        }
    }

    ArrayList getindexSet() {
        return ComputeNodeIndex;
    }

    void destroyWLBundle() throws IOException {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            ComputeNodeList.get(i).restart();
        }

        bis.close();
    }

    public int numberofRunningNode() {
        int cnt = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready > -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public int numberofIdleNode() {
        int cnt = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public int myFirstIdleNode() {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -1) {
                return i;
            }
        }
        if (ComputeNodeList.size() > 1) {
            return 0;
        }
        return -2;
    }

    public void activeOneNode() {
        int i = 0;
        for (i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -1) {
                ComputeNodeList.get(i).restart();
                ComputeNodeList.get(i).ready = 1;
                break;
            }
        }
        System.out.println("MIIIIPPPSSS    " + ComputeNodeList.get(i).Mips);
    }

    public double numberOfWaitingJobs() {
        double lenJob = 0;
        for (int i = 0; i < queueWL.size(); i++) {
            if (queueWL.get(i).arrivalTimeOfJob <= Simulator.getInstance().localTime) {
                lenJob = +queueWL.get(i).numberOfJob;
            }
        }

        return lenJob;
    }

    public double getAverageCPUutil() {
        int i = 0;
        double cpu = 0;
        for (i = 0; i < ComputeNodeList.size(); i++) {
            cpu = cpu + ComputeNodeList.get(i).currentCPU;
        }
        cpu = cpu / i;
        return cpu;
    }

    public double[] getAveragePwrParam() {
        double[] ret = new double[3];
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            ret[0] = ret[0] + ComputeNodeList.get(i).getPwrParam()[0];
            ret[1] = ret[1] + ComputeNodeList.get(i).getPwrParam()[1];
            ret[2] = ret[2] + ComputeNodeList.get(i).getPwrParam()[2];
        }
        ret[0] = ret[0] / ComputeNodeList.size();
        ret[1] = ret[1] / ComputeNodeList.size();
        ret[2] = ret[2] / ComputeNodeList.size();
        return ret;
    }
}
