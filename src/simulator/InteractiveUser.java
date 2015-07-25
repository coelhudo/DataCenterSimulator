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
    private int maxProc = 0;
	private int minProc = 0;
	private int maxExpectedResTime = 0;
    double duration;
    double remain;
    private int id = 0;
    String logFileName;
    File logFile = null;
    private ArrayList<BladeServer> ComputeNodeList;
    private ArrayList<Integer> ComputeNodeIndex;
    private ArrayList<InteractiveJob> queueWL;
    private ArrayList<ResponseTime> responseList;
    //jobPlacement placement;
    BufferedReader bis = null;
    //SLA
    private int SLAviolation = 0;
    private IteractiveUserAM AM;
    int usedNode = 0;
    private int MaxNumberOfRequest = 0; //# of Request can be handled by number of basic node which for 100% CPU utilization
    private int NumberofBasicNode = 0;
    GeneralSystem parent;

    public InteractiveUser(GeneralSystem parent) {
        setComputeNodeList(new ArrayList<BladeServer>());
        setComputeNodeIndex(new ArrayList<Integer>());
        setQueueWL(new ArrayList<InteractiveJob>());
        setResponseList(new ArrayList<ResponseTime>());
        logFileName = new String();
        // placement=new jobPlacement(ComputeNodeList);
        setAM(new IteractiveUserAM((InteractiveSystem) parent, this));
        this.parent = parent;
    }

    public void addCompNodetoBundle(BladeServer b) {
        b.restart();
        getComputeNodeList().add(b);
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
            j.setArrivalTimeOfJob(Integer.parseInt(numbers[0]));
            j.setNumberOfJob(Double.parseDouble(numbers[1]) * 50);
            getQueueWL().add(j);
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
        for (i = 0; i < getComputeNodeList().size(); i++) {
            getComputeNodeList().get(i).currentCPU = 0;
            getComputeNodeList().get(i).ready = 1;
        }
    }

    int readWebJob() {
        int retReadLogfile = readingLogFile();
        if (getQueueWL().size() > 0) {
            if (getQueueWL().get(0).getArrivalTimeOfJob() == Simulator.getInstance().localTime | getQueueWL().get(0).getArrivalTimeOfJob() < Simulator.getInstance().localTime) {
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
        setSLAviolation(0);
        int readingResult = readWebJob();
        ////////RESET READY FLAGS for all nodes
        resetReadyFlag();
        //need more thought
        if (readingResult == 0) //we have jobs but it is not the time to run them
        {
            return true;
        }
        if (readingResult == -2 & getQueueWL().isEmpty()) // no jobs are in the queue and in logfile
        {
            return false;
        }
        double CPUpercentage = 0;
        int numberofReadyNodes = 0;
        double beenRunJobs = 0;  //number of jobs have been run so far
        int i = 0;
        for (i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).ready == 1) {
                CPUpercentage = (100.0 - getComputeNodeList().get(i).currentCPU) * getComputeNodeList().get(i).Mips + CPUpercentage;
                numberofReadyNodes++;
            }
        }
        double capacityOfNode = (int) Math.ceil((getMaxNumberOfRequest() * CPUpercentage) / (getNumberofBasicNode() * 100.0));
        double capacityOfNode_COPY = capacityOfNode;
        InteractiveJob jj = new InteractiveJob();
        //jj=queueWL.get(0);
        jj = (InteractiveJob) parent.getScheduler().nextJob(getQueueWL());
        while (capacityOfNode > 0) {
            capacityOfNode = capacityOfNode - jj.getNumberOfJob();
            if (capacityOfNode == 0) {
                addToresponseArray(jj.getNumberOfJob(), (Simulator.getInstance().localTime - jj.getArrivalTimeOfJob() + 1));
                //System.out.println((Main.localTime-wJob.arrivalTimeOfJob+1)*(wJob.numberOfJob) +"\t"+wJob.numberOfJob+"\t q len="+queueLength);
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueWL().remove(jj);
                break;
            }
            if (capacityOfNode < 0) // there are more jobs than capacity
            {
                addToresponseArray(capacityOfNode + jj.getNumberOfJob(), (Simulator.getInstance().localTime - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + capacityOfNode + jj.getNumberOfJob();
                jj.setNumberOfJob(-1 * capacityOfNode);
                //System.out.println(1000.0*Mips);
                break;
            }
            if (capacityOfNode > 0) //still we have capacity to run the jobs
            {
                addToresponseArray(jj.getNumberOfJob(), (Simulator.getInstance().localTime - jj.getArrivalTimeOfJob() + 1));
                beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                getQueueWL().remove(jj);
                while (getQueueWL().size() > 0) {
                    //jj=queueWL.get(0);
                    jj = (InteractiveJob) parent.getScheduler().nextJob(getQueueWL());
                    double copyTedat = capacityOfNode;
                    capacityOfNode = capacityOfNode - jj.getNumberOfJob();
                    if (capacityOfNode == 0) {
                        addToresponseArray(jj.getNumberOfJob(), (Simulator.getInstance().localTime - jj.getArrivalTimeOfJob() + 1));
                        //System.out.println(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueWL().remove(0);
                        break;
                    }
                    if (capacityOfNode < 0) //there are more jobs than 1000.0*MIPS
                    {
                        addToresponseArray(copyTedat, (Simulator.getInstance().localTime - jj.getArrivalTimeOfJob() + 1));
                        jj.setNumberOfJob(-1 * capacityOfNode);
                        beenRunJobs = beenRunJobs + copyTedat;
                        //System.out.println(copyTedat);
                        break;
                    }
                    if (capacityOfNode > 0) {
                        addToresponseArray(jj.getNumberOfJob(), (Simulator.getInstance().localTime - jj.getArrivalTimeOfJob() + 1));
                        //System.out.println(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.getNumberOfJob();
                        getQueueWL().remove(0);
                    }
                } //end while
                break;
            }//end if
        }
        if (capacityOfNode_COPY == beenRunJobs)// we're done all our capacity
        {
            for (int k = 0; k < getComputeNodeList().size(); k++) {
                getComputeNodeList().get(k).currentCPU = 100;
                getComputeNodeList().get(k).ready = 0;
            }
            usedNode = usedNode + getComputeNodeList().size();
        } else if (beenRunJobs < 0) {
            System.out.println("it is impossible!!!!  webbased BoN");
        } else if (beenRunJobs > 0) {
            int k = 0;
            for (k = 0; k < numberofReadyNodes; k++) {
                int serID = parent.getResourceAllocation().nextServer(getComputeNodeList());
                if (serID == -2) {
                    System.out.println("enterPrise BoN : servID =-2\t " + k + "\t" + numberofReadyNodes);
                    break;
                }
                double CPUspace = (100 - getComputeNodeList().get(serID).currentCPU) * getComputeNodeList().get(serID).Mips;
                double reqSpace = (int) Math.ceil(CPUspace * getMaxNumberOfRequest() / (getNumberofBasicNode() * 100.0));
                getComputeNodeList().get(serID).currentCPU = 100;
                getComputeNodeList().get(serID).ready = 0;
                beenRunJobs = beenRunJobs - reqSpace;
                if (beenRunJobs == 0) {
                    k++;
                    break;
                }
                if (beenRunJobs < 0) {
                    getComputeNodeList().get(serID).currentCPU = (int) Math.ceil((reqSpace + beenRunJobs) * 100 / reqSpace);
                    getComputeNodeList().get(serID).ready = 1;
                    k++;
                    break;
                }
            }
            //System.out.println(k +"\t Running node= "+numberofReadyNodes);
            usedNode = usedNode + k;
        }
        getAM().monitor();
        getAM().analysis(getSLAviolation());
        //AM.planning();
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
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).ready != -1) //-1 : means this server is idle not so as to compute its idle power
            {
                if (getComputeNodeList().get(i).WebBasedList.isEmpty()) {
                    getComputeNodeList().get(i).ready = 1;
                    getComputeNodeList().get(i).currentCPU = 0;
                }// bahs
                else {
                    getComputeNodeList().get(i).ready = 0;
                    //System.out.println("queulength in SetReady FLag: "+ComputeNodeList.get(i).queueLength);
                }
            }
        }
    }

    ArrayList getindexSet() {
        return getComputeNodeIndex();
    }

    void destroyWLBundle() throws IOException {
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            getComputeNodeList().get(i).restart();
        }

        bis.close();
    }

    public int numberofRunningNode() {
        int cnt = 0;
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).ready > -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public int numberofIdleNode() {
        int cnt = 0;
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).ready == -1) {
                cnt++;
            }
        }
        return cnt;
    }

    public int myFirstIdleNode() {
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            if (getComputeNodeList().get(i).ready == -1) {
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
            if (getComputeNodeList().get(i).ready == -1) {
                getComputeNodeList().get(i).restart();
                getComputeNodeList().get(i).ready = 1;
                break;
            }
        }
        System.out.println("MIIIIPPPSSS    " + getComputeNodeList().get(i).Mips);
    }

    public double numberOfWaitingJobs() {
        double lenJob = 0;
        for (int i = 0; i < getQueueWL().size(); i++) {
            if (getQueueWL().get(i).getArrivalTimeOfJob() <= Simulator.getInstance().localTime) {
                lenJob = +getQueueWL().get(i).getNumberOfJob();
            }
        }

        return lenJob;
    }

    public double getAverageCPUutil() {
        int i = 0;
        double cpu = 0;
        for (i = 0; i < getComputeNodeList().size(); i++) {
            cpu = cpu + getComputeNodeList().get(i).currentCPU;
        }
        cpu = cpu / i;
        return cpu;
    }

    public double[] getAveragePwrParam() {
        double[] ret = new double[3];
        for (int i = 0; i < getComputeNodeList().size(); i++) {
            ret[0] = ret[0] + getComputeNodeList().get(i).getPwrParam()[0];
            ret[1] = ret[1] + getComputeNodeList().get(i).getPwrParam()[1];
            ret[2] = ret[2] + getComputeNodeList().get(i).getPwrParam()[2];
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

	public ArrayList<BladeServer> getComputeNodeList() {
		return ComputeNodeList;
	}

	public void setComputeNodeList(ArrayList<BladeServer> computeNodeList) {
		ComputeNodeList = computeNodeList;
	}

	public ArrayList<Integer> getComputeNodeIndex() {
		return ComputeNodeIndex;
	}

	public void setComputeNodeIndex(ArrayList<Integer> computeNodeIndex) {
		ComputeNodeIndex = computeNodeIndex;
	}

	public ArrayList<InteractiveJob> getQueueWL() {
		return queueWL;
	}

	public void setQueueWL(ArrayList<InteractiveJob> queueWL) {
		this.queueWL = queueWL;
	}

	public ArrayList<ResponseTime> getResponseList() {
		return responseList;
	}

	public void setResponseList(ArrayList<ResponseTime> responseList) {
		this.responseList = responseList;
	}

	public int getSLAviolation() {
		return SLAviolation;
	}

	public void setSLAviolation(int sLAviolation) {
		SLAviolation = sLAviolation;
	}

	public IteractiveUserAM getAM() {
		return AM;
	}

	public void setAM(IteractiveUserAM aM) {
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
