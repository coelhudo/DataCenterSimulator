//Problems to be solved:
//Ready in web based workload is not set correctly, check it out!
package simulator.physical;

import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import simulator.Simulator;
import simulator.ResponseTime;

public class BladeServer {

    public ArrayList<ResponseTime> responseList;
    public ArrayList<ResponseTime> responseListWeb;
    public int dependency = 0;
    public double[] frequencyLevel;
    public double[] powerBusy;
    public double[] powerIdle;
    public double Mips, idleConsumption;
    String bladeType;
    public double respTime = 0, resTimeEpoch = 0, currentCPU = 0, queueLength, totalJob = 0, totalJobEpoch = 0;
    public int ready, backUpReady;
    public ArrayList<BatchJob> activeBatchList, blockedBatchList;
    public ArrayList<EnterpriseJob> EnterprizList;
    public ArrayList<InteractiveJob> WebBasedList;
    public int chassisID, totalFinishedJob = 0;
    public int serverID, rackId;
    //SLA Parameters
    //Application Bundle
    public int timeTreshold = 0;
    public int SLAPercentage;
    //WorkLoad Bundle
    public int maxExpectedRes = 0;
    public boolean SLAviolation;
    ////////////////

    public BladeServer(int chasID) {
        respTime = 0;
        //if it is -1 means that it is not put in the proper position yet ID should be set
        chassisID = chasID;
        bladeType = new String();
        currentCPU = 0;
        activeBatchList = new ArrayList<BatchJob>();
        blockedBatchList = new ArrayList<BatchJob>();
        EnterprizList = new ArrayList<EnterpriseJob>();
        WebBasedList = new ArrayList<InteractiveJob>();
        responseList = new ArrayList<ResponseTime>();
        responseListWeb = new ArrayList<ResponseTime>();
        queueLength = 0;
        //-3 means it is not assigned to any system yet
        //-2: it is in a system but is not assigned to an application
        //-1 idle
        //0 or 1 is ready or not just the matter of CPU utilization over 100% or not
        ready = -3;
        totalFinishedJob = 0;
        SLAviolation = false;
        Mips = 1.4;
    }
    //Transaction system

    public void configSLAparameter(int time, int percentage) {
        timeTreshold = time;
        SLAPercentage = percentage;
    }
    //Interactive System

    public void configSLAparameter(int time) {
        maxExpectedRes = time;
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
        double pw = 0, w = 0, a = 0, cpu = 0, mips = 0;
        int j;
        cpu = currentCPU;
        // if(cpu!=0)
        // System.out.println(cpu +" \ttime ="+Main.localTime +" \t  chassi id="+chassisID );
        //if(servers.get(i).currentCPU==100) System.out.println(chassisID);
        mips = Mips;
        if (mips == 0) {
            pw = pw + idleConsumption;
            System.out.println("MIPS Zero!!!!");
        } else {
            for (j = 0; j < 3; j++) {
                if (frequencyLevel[j] == mips) {
                    break;
                }
            }
            w = powerIdle[j];
            a = powerBusy[j] - w;
            if (ready == -1 | ready == -2 | ready == -3) //if the server is in idle state
            {
                a = 0;
                w = idleConsumption;
                //System.out.println(Main.localTime);
            }
            pw = pw + a * cpu / 100 + w;

        }
        return pw;
    }

    public void restart() {
        respTime = 0;
        currentCPU = 0;
        activeBatchList = new ArrayList<BatchJob>();
        blockedBatchList = new ArrayList<BatchJob>();
        EnterprizList = new ArrayList<EnterpriseJob>();
        WebBasedList = new ArrayList<InteractiveJob>();
        queueLength = 0;
        /////check
        ready = -1;
        totalFinishedJob = 0;
        Mips = 1.4;
        resTimeEpoch = 0;
        totalJob = 0;
        totalJobEpoch = 0;
        SLAviolation = false;
    }
    //if it blongs to Enterprise system

    public void makeItIdle(EnterpriseJob jj) {
        //System.out.print("\tIdle\t\t\t\t\t@:"+Main.localTime);
        ready = -1;
        Mips = frequencyLevel[0];
    }

    public void feedWork(InteractiveJob j) {
        double nums = j.getNumberOfJob();
        int time = j.getArrivalTimeOfJob();
        queueLength = queueLength + nums;
        InteractiveJob wJob = new InteractiveJob();
        wJob.setArrivalTimeOfJob(time);
        wJob.setNumberOfJob(nums);
        WebBasedList.add(wJob);
        totalJob = totalJob + nums;
    }
    //feeding batch type Job to blade server

    public void feedWork(BatchJob job) {
        activeBatchList.add(job);
        setReady();
        setDependency();
        totalJob++;
    }
    //feeding webbased type Job to blade server

    public void feedWork(EnterpriseJob j) {
        double nums = j.getNumberOfJob();
        int time = j.getArrivalTimeOfJob();
        queueLength = queueLength + nums;
        EnterpriseJob wJob = new EnterpriseJob();
        wJob.setArrivalTimeOfJob(time);
        wJob.setNumberOfJob(nums);
        EnterprizList.add(wJob);
        totalJob = nums + totalJob;
    }

    public int getCurrentFreqLevel() {
        for (int i = 0; i < frequencyLevel.length; i++) {
            if (Mips == frequencyLevel[i]) {
                return i; //statrs from 1 not zero!
            }
        }
        System.out.println("wrong frequency level !! ");
        return -1;
    }

    public int increaseFrequency() {
        //System.out.println("MIIIPPSSS "+Mips);
        if (getCurrentFreqLevel() == 2) {
            return 0;
        } else {
            Mips = frequencyLevel[getCurrentFreqLevel() + 1]; //getCurrentFrequency already increased the freq level
            Simulator.getInstance().mesg++;
        }
        if (Mips == 0) {
            System.out.println("Mipss sefr shoodd!!!");
        }
        return 1;
    }

    public int decreaseFrequency() {
        //System.out.println("Decreasing frequency");
        if (getCurrentFreqLevel() == 0) {//System.out.println("Minimum Frequency Level ~~~ ");
            return 0;
        } else {
            Mips = frequencyLevel[getCurrentFreqLevel() - 1];
            Simulator.getInstance().mesg++;
        }
        if (Mips == 0) {
            System.out.println("Mipss sefr shoodd!!!");
        }
        return 1;
    }
    //running batch type JOB

    public int run(BatchJob j) {
        double tempCpu = 0;
        double num = activeBatchList.size(), index = 0, index_1 = 0, rmpart = 0;
        int i = 0;
        double share = 0, share_t = 0, extraShare = 0;
        if (num == 0) {
            ready = 1;
            setDependency();
            currentCPU = 0;
            return 0;
        }
        share = Mips / num;//second freqcuency level!
        // System.out.println("Share   "+share);
        share_t = share;
        int ret_done = 0;
        while (index < num) {   //index<activeBatchList.size()
            index_1 = index;
            for (i = 0; i < activeBatchList.size(); i++) {
                if (activeBatchList.get(i).getUtilization() <= share & activeBatchList.get(i).getIsChangedThisTime() == 0) {
                    extraShare = extraShare + share - activeBatchList.get(i).getUtilization();
                    index++;
                    activeBatchList.get(i).setIsChangedThisTime(1);
                    tempCpu = activeBatchList.get(i).getUtilization() + tempCpu;
                    ret_done = done(i, share_t);
                    i = i - ret_done;
                    //i=i-done(i,activeBatchList.get(i).utilization);
                }
            }
            for (i = 0; i < activeBatchList.size(); i++) {
                if (activeBatchList.get(i).getIsChangedThisTime() == 0) {
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
        for (i = 0; i < activeBatchList.size(); i++) {
            if (activeBatchList.get(i).getIsChangedThisTime() == 0) {
                //ret_done=done(i,share/activeBatchList.get(i).utilization);
                if ((share / activeBatchList.get(i).getUtilization()) > 1) {
                    System.out.println("share more than one!\t" + share_t + "\t" + share + "\t" + activeBatchList.get(i).getUtilization() + "\t" + Simulator.getInstance().localTime);
                }
                activeBatchList.get(i).setIsChangedThisTime(1);
                ret_done = done(i, share / activeBatchList.get(i).getUtilization());
                tempCpu = tempCpu + share;
                i = i - ret_done; //if a job has been removed (finished) in DONE function
            }
        }
        for (i = 0; i < activeBatchList.size(); i++) {
            activeBatchList.get(i).setIsChangedThisTime(0);
        }
        //Inja be nazaram /MIPS ham mikhad ke sad beshe fek konam MIPS ro dar nazar nagereftam!
        currentCPU = 100.0 * tempCpu / Mips;
        //System.out.println("CPU=     " + currentCPU +"num=     "+num);
        setReady();
        setDependency();
        return 1;
    }

    public int done(int tmp, double share) {
        //return 1 means: a job has been finished
        BatchJob job = activeBatchList.get(tmp);
        int ki;
        //int serverIndex=chassisID*DataCenter.theDataCenter.chassisSet.get(0).servers.size()+serverID; //getting this server ID
        int serverIndex = serverID;
        ki = job.getThisNodeIndex(serverIndex);
        if (share == 0) {
            System.out.println("In DONE share== zero00000000000000000000000000000000000000oo,revise the code  need some work!");
            job.setExitTime(Simulator.getInstance().localTime);
            activeBatchList.remove(tmp--);
            //totalFinishedJob++;
            return 1;
        }
        if (ki == -1) {
            System.out.println("Blade server is wrong in BladeServer!!!");
        }
        //setRemainAllNodes(tmp, share);
        job.getRemain()[ki] = job.getRemain()[ki] - share;
        if (job.getRemain()[ki] <= 0) {
            blockedBatchList.add(job);
            activeBatchList.get(tmp).setIsChangedThisTime(0);
            activeBatchList.remove(job);//still exsits in other nodes
            if (job.allDone()) {

                job.jobFinished();

                setDependency();
                totalFinishedJob++;
                return 1;
            }
        }
        return 0;
    }

    void setDependency() {
        if (!blockedBatchList.isEmpty()) {
            dependency = 1;
            return;
        }
        dependency = 0;
    }

    public void setReady() {
        double tmp = 0, treshold = 1;
        int num = 0, i;
        num = activeBatchList.size();
        for (i = 0; i < num; i++) {
            tmp = tmp + activeBatchList.get(i).getUtilization();
        }
        if (tmp >= treshold) {
            ready = 0;
        } else {
            ready = 1;
        }
    }

    void readFromNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
//                if(childNodes.item(i).getNodeName().equalsIgnoreCase("ID"))
//                {
//                    serverID = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
//                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("BladeType")) {
                    bladeType = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("MIPS")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    frequencyLevel = new double[split.length];
                    for (int j = 0; j < split.length; j++) {
                        frequencyLevel[j] = Double.parseDouble(split[j]);
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("FullyLoaded")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    powerBusy = new double[split.length];
                    for (int j = 0; j < split.length; j++) {
                        powerBusy[j] = Double.parseDouble(split[j]);
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Idle")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(" ");
                    powerIdle = new double[split.length];
                    for (int j = 0; j < split.length; j++) {
                        powerIdle[j] = Double.parseDouble(split[j]);
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Standby")) {
                    idleConsumption = Double.parseDouble(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());

                }
            }
        }
    }
//   void addToresponseArray(double num,int time)
//      {
//                        responseTime t= new responseTime();
//                        t.numberOfJob=num;
//                        t.responseTime=time;
//                        responseList.add(t);
//      }
//   void addToresponseArrayWeb(double num,int time)
//      {
//                        if(time>maxExpectedRes)
//                                SLAviolation=true;
//                        responseTime t= new responseTime();
//                        t.numberOfJob=num;
//                        t.responseTime=time;
//                        responseList.add(t);
//      }
//        int whichServer(int i)
//        {
//            return i%DataCenter.theDataCenter.chassisSet.get(0).servers.size();
//        }
//        int whichChasiss (int i)
//        {
//            return i/DataCenter.theDataCenter.chassisSet.get(0).servers.size();
//        }
}

/* double getMeanResTimeLastEpoch()
{

if(resTimeEpoch==0) //the first time in
{
resTimeEpoch=respTime;
totalJobEpoch=totalJob-queueLength;
//System.out.println("First   Last Epoch   "+respTime+ totalJobEpoch+"\t"+chassisID);
if(totalJobEpoch>0)
return respTime/totalJobEpoch;
else
return 0;
}
else {
double tempTime=respTime-resTimeEpoch;
double tempJob=totalJob-queueLength-totalJobEpoch;
resTimeEpoch=respTime;
totalJobEpoch=totalJob-queueLength;
//System.out.println("in get MeanResponse Last Epoch   "+ tempTime/tempJob+"\t"+chassisID);
if(tempJob!=0)
return tempTime/tempJob;
else
return 0;
}
}*/
