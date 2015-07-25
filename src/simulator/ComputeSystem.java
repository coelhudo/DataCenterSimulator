package simulator;

import simulator.schedulers.LeastRemainFirst;
import simulator.physical.BladeServer;
import simulator.am.ComputeSystemAM;
import simulator.ra.MHR;
import simulator.jobs.BatchJob;
import simulator.schedulers.Scheduler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;

public class ComputeSystem extends GeneralSystem {

    Violation SLAViolationType; //different type of violation: ComputeNodeShortage, DEADLINEPASSED
    ArrayList<BatchJob> waitingList;
    int totalJob = 0;//, totalFinishedJob=0;
    int minNode, maxNode;
    double inputTime;
    public boolean blocked = false;
    File f;
    int predictNumberofNode;
    int priority;

    ////////////////////////////////////
    public ComputeSystem(String config) {
        ComputeNodeList = new ArrayList<BladeServer>();
        waitingList = new ArrayList<BatchJob>();
        ComputeNodeIndex = new ArrayList<Integer>();
        parseXmlConfig(config);
        schdler = new LeastRemainFirst();
        //placement=new jobPlacement(ComputeNodeList);
        rc = new MHR();
        totalJob = 0;
        rc.initialResourceAloc(this);
        am = new ComputeSystemAM(this);
    }

    boolean runAcycle() {
        SLAviolation = 0;
        int numberOfFinishedJob = 0;
        // if(Main.localTime%1200==0 |Main.localTime%1200==2 )
        //         ASP();
        BatchJob j = new BatchJob();
        //reads all jobs with arrival time less than Localtime
        while (readJob(j)) {
            if (inputTime > Simulator.getInstance().localTime) {
                break;
            }
            j = new BatchJob();
        }
        if (!blocked) {
            //feeds jobs from waiting list to servers as much as possible
            getFromWaitinglist();
            for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
                ComputeNodeList.get(temp).run(new BatchJob());
            }
            for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
                numberOfFinishedJob = ComputeNodeList.get(temp).totalFinishedJob + numberOfFinishedJob;
            }
            // System.out.println("total "+totalJob+ "\t finished Job= "+numberOfFinishedJob+"\t LocalTime="+Main.localTime);
        }
        //if is blocked and was not belocked before make it blocked
        if (blocked && !allNodesAreBlocked()) {
            makeSystemaBlocked();
        }

        if (!blocked) {
            am.monitor();
            am.analysis(0);
        }
        //System.out.println(Main.localTime +"\t"+totalJob+ "\t"+numberOfFinishedJob);
        if (numberOfFinishedJob == totalJob) {
            sysIsDone = true;
            return true;
        } else {
            return false;
        }
    }
    ///returns true if all nodes are blocked

    boolean allNodesAreBlocked() {
        for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
            if (ComputeNodeList.get(temp).ready != -1) {
                return false;
            }
        }
        return true;
    }

    void makeSystemaBlocked() {
        for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
            ComputeNodeList.get(temp).backUpReady = ComputeNodeList.get(temp).ready;
            ComputeNodeList.get(temp).ready = -1;

        }
    }

    public void makeSystemaUnBlocked() {
        for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
            ComputeNodeList.get(temp).ready = ComputeNodeList.get(temp).backUpReady;
        }
    }

    int getFromWaitinglist() {
        setSLAviolation(Violation.NOTHING);
        if (waitingList.isEmpty()) {
            return 0;
        }
        BatchJob job = (BatchJob) (schdler.nextJob(waitingList));
        while (job.getStartTime() <= Simulator.getInstance().localTime) {
            int[] indexes = new int[job.getNumOfNode()]; //number of node the last job wants
            int[] listServer = new int[job.getNumOfNode()];
            if (rc.allocateSystemLevelServer(ComputeNodeList, indexes)[0] == -2) {
                setSLAviolation(Violation.COMPUTE_NODE_SHORTAGE);
                //  System.out.println("COMPUTE NODE SHORTAGE in getFromWaitingList");
                return 0; //can not find the bunch of requested node  for the job
            }
            listServer = makeListofServer(indexes);
            for (int i = 0; i < indexes.length; i++) {

                job.setListOfServer(listServer);
                ComputeNodeList.get(indexes[i]).feedWork(job);// feed also takes care of setting ready :)
                if (indexes.length > 1) {
                    ComputeNodeList.get(indexes[i]).dependency = 1; //means: this server has a process which is dependent on others
                } else {
                    ComputeNodeList.get(indexes[i]).dependency = 0;
                }
            }
            //Check if dealine is missed
            if (Simulator.getInstance().localTime - job.getStartTime() > job.getDeadline()) {
                setSLAviolation(Violation.DEADLINEPASSED);
                // System.out.println("DEADLINE PASSED in getFromWaitingList");
            }
            ////////////////////////////
            waitingList.remove(job);
            if (waitingList.isEmpty()) {
                return 0;
            }
            job = (BatchJob) (schdler.nextJob(waitingList));
        }
        return 0; //it is not important
    }

    int[] makeListofServer(int[] list) {
        int[] retList = new int[list.length];
        //int NumOfSerInChas=DataCenter.theDataCenter.chassisSet.get(0).servers.size();
        //map the index in CS compute node list to physical index(chassID , ServerID)
        for (int i = 0; i < list.length; i++) {
            retList[i] = ComputeNodeList.get(list[i]).serverID;//chassisID*NumOfSerInChas+ComputeNodeList.get(list[i]).serverID;
        }
        return retList;
    }

    void setSLAviolation(Violation flag) {
        SLAViolationType = Violation.NOTHING;
        if (flag == Violation.COMPUTE_NODE_SHORTAGE)//means there is not enough compute nodes for job, this function is called from resourceIsAvailable
        {
            SLAViolationType = Violation.COMPUTE_NODE_SHORTAGE;
            SLAviolation++;
        }
        if (flag == Violation.DEADLINEPASSED) {
            SLAViolationType = Violation.DEADLINEPASSED;
            SLAviolation++;
        }
        if (SLAViolationType != Violation.NOTHING) {
            Simulator.getInstance().logHpcViolation(name, SLAViolationType);
            accumolatedViolation++;
        }
    }

    boolean readJob(BatchJob j) {
        try {
            String line = bis.readLine();
            if (line == null) {
                return false;
            }
            line = line.replace("\t", " ");
            String[] numbers = line.split(" ");
            if (numbers.length < 5) {
                return false;
            }
            // Input log format: (time, requiertime, CPU utilization, number of core, dealine for getting to a server buffer)
            inputTime = Double.parseDouble(numbers[0]);
            j.setRemainParam(Double.parseDouble(numbers[1]), Double.parseDouble(numbers[2]), Integer.parseInt(numbers[3]), Integer.parseInt(numbers[4]));
            j.setStartTime(inputTime);
            boolean add = waitingList.add(j);
            //number of jobs which are copied on # of requested nodes
            totalJob = totalJob + 1 /*+Integer.parseInt(numbers[3])*/;
            //System.out.println("Readed inputTime= " + inputTime + " Job Reqested Time=" + j.startTime+" Total job so far="+ total);
            return add;
        } catch (IOException ex) {
            System.out.println("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    void readFromNode(Node node, String path) {
        //if (ComputeNodeList.size()>0) ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg"));
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler")); //TODO
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Workload")) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        f = new File(fileName);
                        bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                    } catch (IOException e) {
                        System.out.println("Uh oh, got an IOException error!" + e.getMessage());
                    } finally {
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    numberofNode = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Priority")) {
                    priority = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        rackId.add(Integer.parseInt(split[j]));
                    }
                }
            }
        }
    }

    ArrayList getindexSet() {
        return ComputeNodeIndex;
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

    public void activeOneNode() {
        int i = 0;
        for (i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -1) {
                ComputeNodeList.get(i).restart();
                ComputeNodeList.get(i).ready = 1;
                break;
            }
        }
        System.out.println("activeone node in compuet system MIIIIPPPSSS    " + ComputeNodeList.get(i).Mips);
    }

    double finalized() {
        try {
            bis.close();
        } catch (IOException ex) {
            Logger.getLogger(EnterpriseApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        double totalResponsetime = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            totalResponsetime = totalResponsetime + ComputeNodeList.get(i).respTime;

        }
        return totalResponsetime;
    }
}
