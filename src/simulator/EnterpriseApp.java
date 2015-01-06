package simulator;

import simulator.physical.BladeServer;
import simulator.physical.DataCenter;
import simulator.am.ApplicationAM;
import simulator.jobs.EnterpriseJob;
import simulator.schedulers.Scheduler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public final class EnterpriseApp {

    public int id = 0;
    //int usedNode=0;
    public int maxProc = 0, minProc = 0, maxExpectedResTime = 0;
    public ArrayList<BladeServer> ComputeNodeList;
    //ArrayList <Integer> ComputeNodeIndex;
    public ArrayList<EnterpriseJob> queueApp;
    public ArrayList<ResponseTime> responseList;
    //jobPlacement placement;
    public int timeTreshold = 0;
    public int SLAPercentage;
    public int SLAviolation = 0;
    public int NumofViolation = 0;
    BufferedReader bis = null;
    File logFile;
    public ApplicationAM AM;
    //EnterpriseSystem mySys; //Application knows in which Sys it is located. initialize in EnterpriseSystem
    public int MaxNumberOfRequest = 0; //# of Request can be handled by number of basic node which for 100% CPU utilization
    public int NumberofBasicNode = 0;
    GeneralSystem parent;

    public EnterpriseApp(String path, Node node, GeneralSystem parent) {
        this.parent = parent;
        ComputeNodeList = new ArrayList<BladeServer>();
        queueApp = new ArrayList<EnterpriseJob>();
        responseList = new ArrayList<ResponseTime>();
        //ComputeNodeIndex=new ArrayList<Integer>();
        readFromNode(node, path);
        configSLAallcomputingNode();
        //placement= new jobPlacement(ComputeNodeList) ;
        AM = new ApplicationAM((EnterpriseSystem) parent, this);
    }

    public double numberOfWaitingJobs() {
        double lenJob = 0;
        for (int i = 0; i < queueApp.size(); i++) {
            if (queueApp.get(i).arrivalTimeOfJob <= Simulator.getInstance().localTime) {
                lenJob = +queueApp.get(i).numberOfJob;
            }
        }

        return lenJob;
    }

    public void configSLAallcomputingNode() {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            ComputeNodeList.get(i).configSLAparameter(timeTreshold, SLAPercentage);
        }
    }

    public void addCompNodetoBundle(BladeServer b) {
        b.restart();
        ComputeNodeList.add(b);
    }

    public void removeCompNodeFromBundle(BladeServer b) {
        b.restart();
        b.ready = -2;
        ComputeNodeList.remove(b);
    }
    /*
    Return Values:
     * 1: read successfully
     * 0:put in waiting list
     * -1: end of file or error
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
            j.arrivalTimeOfJob = Integer.parseInt(numbers[0]);
            j.numberOfJob = Double.parseDouble(numbers[1]);
            queueApp.add(j);
            return 1;
            //System.out.println("Readed inputTime= " + inputTime + " Job Reqested Time=" + j.startTime+" Total job so far="+ total);
        } catch (IOException ex) {
            System.out.println("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
    }

    int readWebJob() {
        int retReadLogfile = readingLogFile();
        if (queueApp.size() > 0) {
            if (queueApp.get(0).arrivalTimeOfJob == Simulator.getInstance().localTime | queueApp.get(0).arrivalTimeOfJob < Simulator.getInstance().localTime) {
                return 1;
            } else {
                return 0;
            }
        }
        // ending condition means there is no job in the logfile
        //System.out.println(" One dispacher !!!  in the readWebJob     enterprise      "+retReadLogfile);
        return retReadLogfile;
    }
    //reset all working node ready flag and CPU utilization 

    void resetReadyFlagAndCPU() {
        int i;
        for (i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready != -1) { //if it is idle dont change it! it is responsibility of its AM to change it
                ComputeNodeList.get(i).currentCPU = 0;
                ComputeNodeList.get(i).ready = 1;
            }
        }
    }
    //False: logfile is finished and no remain job

    boolean runAcycle() {
        int readingResult = readWebJob();
        ////////RESET READY FLAGS for all nodes
        resetReadyFlagAndCPU();
        //need more thought
        if (readingResult == 0) //we have jobs but it is not the time to run them
        {
            return true;
        }
        if (readingResult == -2 & queueApp.isEmpty()) // no jobs are in the queue and in logfile
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
        EnterpriseJob jj = new EnterpriseJob();
        //jj=queueApp.get(0);

        jj = (EnterpriseJob) parent.schdler.nextJob(queueApp);
        while (capacityOfNode > 0) {
            capacityOfNode = capacityOfNode - jj.numberOfJob;
            if (capacityOfNode == 0) {
                addToresponseArray(jj.numberOfJob, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                //System.out.println((Main.localTime-wJob.arrivalTimeOfJob+1)*(wJob.numberOfJob) +"\t"+wJob.numberOfJob+"\t q len="+queueLength);
                beenRunJobs = beenRunJobs + jj.numberOfJob;
                queueApp.remove(jj);
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
                queueApp.remove(jj);
                while (queueApp.size() > 0) {

                    //jj=queueApp.get(0);
                    jj = (EnterpriseJob) parent.schdler.nextJob(queueApp);
                    double copyTedat = capacityOfNode;
                    capacityOfNode = capacityOfNode - jj.numberOfJob;
                    if (capacityOfNode == 0) {
                        addToresponseArray(jj.numberOfJob, (Simulator.getInstance().localTime - jj.arrivalTimeOfJob + 1));
                        //System.out.println(wJob.numberOfJob);
                        beenRunJobs = beenRunJobs + jj.numberOfJob;
                        queueApp.remove(0);
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
                        queueApp.remove(0);
                    }
                } //end while
                break;
            }//end if
        }
        if (capacityOfNode_COPY == beenRunJobs)// we're done all our capacity
        {
            for (int k = 0; k < ComputeNodeList.size(); k++) {
                if (ComputeNodeList.get(k).ready == 1) {
                    ComputeNodeList.get(k).currentCPU = 100;
                    ComputeNodeList.get(k).ready = 0;
                }
            }
            // usedNode=usedNode+ComputeNodeList.size();
        } else if (beenRunJobs < 0) {
            System.out.println("it is impossible!!!!  Enterprise BoN");
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
            //usedNode=usedNode+k;
        }
//       AM.monitor();
//       AM.analysis(SLAviolation);
//       AM.planning();
        if (queueApp.isEmpty() && readingResult == -2) {
            return false;
        } else {
            return true;
        }
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
        System.out.println("MIIIIPPPSSS    " + ComputeNodeList.get(i).Mips);
    }

    void addToresponseArray(double num, int time) {
        ResponseTime t = new ResponseTime();
        t.setNumberOfJob(num);
        t.setResponseTime(time);
        responseList.add(t);
    }

    void parseXmlConfig(String config) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final File file = new File(config);
            Document doc = docBuilder.parse(file);
            // normalize text representation
            doc.getDocumentElement().normalize();
            readFromNode(doc.getDocumentElement(), file.getParent());
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void readFromNode(Node node, String path) {
        ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("id")) {
                    id = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()); //Id of the application
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplicationWorkLoad")) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        bis = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
                    } catch (IOException e) {
                        System.out.println("Uh oh, got an IOException error!" + e.getMessage());
                    } finally {
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("MaxNumberOfRequest")) {
                    MaxNumberOfRequest = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("NumberofBasicNode")) {
                    NumberofBasicNode = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("timeTreshold")) {
                    timeTreshold = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()); //
                    maxExpectedResTime = timeTreshold;
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Percentage")) {
                    SLAPercentage = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()); //
                }                //We dont have server list now but may be in future we had
                /*if(childNodes.item(i).getNodeName().equalsIgnoreCase("ServerList"))
                {
                String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                String[] split = str.split(" ");
                for(int j=0;j<split.length;j++)
                {
                int serverIndex=Integer.parseInt(split[j]);
                int indexChassis=serverIndex/DC.chassisSet.get(0).servers.size();
                int indexServer=serverIndex%DC.chassisSet.get(0).servers.size();
                addCompNodetoBundle(DC.chassisSet.get(indexChassis).servers.get(indexServer));
                DC.chassisSet.get(indexChassis).servers.get(indexServer).Mips=1;
                DC.chassisSet.get(indexChassis).servers.get(indexServer).ready=1;
                ComputeNodeIndex.add(serverIndex);
                }
                }*/
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("minProcessor")) {
                    minProc = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("maxProcessor")) {
                    maxProc = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }

            }
        }
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

    void destroyApplication() throws IOException {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            ComputeNodeList.get(i).restart();
            ComputeNodeList.get(i).ready = -2; //ready to be assinged to other application
        }
        bis.close();
    }

    boolean isThereIdleNode() {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -1) {
                return true;
            }
        }
        return false;
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
    //Check the responseTime of each server for setting the frequency level for the next time slot
    /*  double  finalized ()
    {
    try {
    bis.close();
    } catch (IOException ex) {
    Logger.getLogger(application.class.getName()).log(Level.SEVERE, null, ex);
    }
    double meanResponsetime=0;
    double totalJob=0;
    for(int i=0;i<Main.responseList.size();i++) {
    meanResponsetime=meanResponsetime+ Main.responseList.get(i).responseTime*Main.responseList.get(i).numberOfJob;
    totalJob+=Main.responseList.get(i).numberOfJob;
    //System.out.println("respTime="+serverList.get(i).respTime+"\t TotalJob="+serverList.get(i).totalJob);
    }

    return meanResponsetime;///totalJob;
    }*/
}
