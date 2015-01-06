package simulator;

import simulator.physical.BladeServer;
import simulator.am.InteractiveSystemAM;
import simulator.ra.MHR;
import simulator.schedulers.FifoScheduler;
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

public class InteractiveSystem extends GeneralSystem {

    public ArrayList<InteractiveUser> UserList, waitingQueueWL;
    File logFile;

    public InteractiveSystem(String config) {
        ComputeNodeList = new ArrayList<BladeServer>();
        ComputeNodeIndex = new ArrayList<Integer>();
        UserList = new ArrayList<InteractiveUser>();
        waitingQueueWL = new ArrayList<InteractiveUser>();
        rc = new MHR();
        schdler = new FifoScheduler();
        parseXmlConfig(config);
        rc.initialResourceAlocator(this);
        SLAviolation = 0;
        am = new InteractiveSystemAM(this);
    }

    public int numberofAvailableNodetoAlocate() {
        int n = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -2) {
                n++;
            }
        }
        return n;
    }

    public boolean checkForViolation() {
        for (int i = 0; i < UserList.size(); i++) {
            if (UserList.get(i).SLAviolation > 0) {
                return true;
            }
        }
        return false;
    }
    //Return False means everything is finished!

    boolean runAcycle() throws IOException {
        if (UserList.size() > 0 & checkForViolation())//& Main.localTime%Main.epochSys==0)
        {
//            AM.monitor();
//            AM.analysis(SLAviolation);
//            AM.planning();
//            AM.execution();
        }
        int readingResult = forwardingJob();
        int finishedBundle = 0;
        for (int i = 0; i < UserList.size(); i++) {
            //TODO: if each bundle needs some help should ask and here resourceallocation should run
            if (UserList.get(i).runAcycle() == false) //return false if bundle set jobs are done, we need to re-resourcealocation
            {
                finishedBundle++;
                numberofIdleNode = UserList.get(i).ComputeNodeList.size() + numberofIdleNode;
                UserList.get(i).destroyWLBundle();//restart its servers
                UserList.remove(i);
            }
        }
        violationCheckandSet();
        ///TODO : some decisiones needed based on SLAviolation
        if (finishedBundle > 0) {
            rc.resourceAloc(this);
        }
        if (UserList.isEmpty() && waitingQueueWL.isEmpty()) {
            sysIsDone = true;
            return true;
        } else {
            return false;
        }
    }
    //First time resource Allocation

    int forwardingJob() {
        int readingResult = readWL();
        int index;
        while (readingResult == 1) {
            index = rc.initialResourceAloc(this);
            if (index == -1) {
                return index;
            }
            readingResult = readWL();
        }
        return readingResult;
    }

    int readWL() {
        int retReadLogfile = readingLogFile();
        if (waitingQueueWL.size() > 0) {
            if (waitingQueueWL.get(0).arrivalTime == Simulator.getInstance().localTime | waitingQueueWL.get(0).arrivalTime < Simulator.getInstance().localTime) {
                return 1;
            } else {
                return 0;
            }
        }
        return retReadLogfile;
    }
    //In Logfile: arrival time and description of WL (m,k,dur,wl,sla)--> (t,WL(m,k,dur,wl,sla))

    int readingLogFile() {
        try {
            String line = bis.readLine();
            if (line == null) {
                return -2;
            }
            line = line.replace("\t", " ");
            String[] numbers = new String[6];
            numbers = line.trim().split(" ");
            if (numbers.length < 6) {
                return -2;
            }
            InteractiveUser test = new InteractiveUser(this);
            test.arrivalTime = Integer.parseInt(numbers[0]);
            test.minProc = Integer.parseInt(numbers[1]);
            test.maxProc = Integer.parseInt(numbers[2]);
            test.duration = Double.parseDouble(numbers[3]);
            test.remain = test.duration; //for now I've not used that!
            test.logFileName = numbers[4];
            test.maxExpectedResTime = Integer.parseInt(numbers[5]);
            test.MaxNumberOfRequest = Integer.parseInt(numbers[6]);
            test.NumberofBasicNode = Integer.parseInt(numbers[7]);
            waitingQueueWL.add(test);
            return 1;
            //System.out.println("Readed inputTime= " + inputTime + " Job Reqested Time=" + j.startTime+" Total job so far="+ total);
        } catch (IOException ex) {
            System.out.println("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
    }

    void violationCheckandSet() throws IOException {
        SLAviolation = 0;
        for (int i = 0; i < UserList.size(); i++) {
            SLAviolation = +UserList.get(i).SLAviolation;
        }
        if (SLAviolation > 0) {
            Simulator.getInstance().logInteractiveViolation(name, SLAviolation);

            accumolatedViolation++;
        }
    }
//   void addComputeNodeToSys(BladeServer b){
//         b.restart();
//         ComputeNodeList.add(b);
//    }

//    void parseXmlConfig(String config) {
//        try {
//            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//            Document doc = docBuilder.parse(new File(config));
//            // normalize text representation
//            doc.getDocumentElement().normalize();
//            readFromNode(doc.getDocumentElement());
//        } catch (ParserConfigurationException ex) {
//            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SAXException ex) {
//            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    @Override
    void readFromNode(Node node, String path) {
        ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    numberofNode = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                    numberofIdleNode = numberofNode;
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        rackId.add(Integer.parseInt(split[j]));
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg"));
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler"));
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("WorkLoad")) {
                    String fileName = path + "/" +  childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        bis = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
                    } catch (IOException e) {
                        System.out.println("Uh oh, got an IOException error!" + e.getMessage());
                    } finally {
                    }
                }
            }
        }
    }
}
