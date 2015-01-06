package simulator;

import simulator.physical.BladeServer;
import simulator.am.EnterpriseSystemAM;
import simulator.ra.MHR;
import simulator.schedulers.FifoScheduler;
import java.io.IOException;
import java.util.ArrayList;
import org.w3c.dom.*;

public class EnterpriseSystem extends GeneralSystem {

    public ArrayList<EnterpriseApp> applicationList;

    public EnterpriseSystem(String config) {
        ComputeNodeList = new ArrayList<BladeServer>();
        ComputeNodeIndex = new ArrayList<Integer>();
        applicationList = new ArrayList<EnterpriseApp>();
        rc = new MHR();
        parseXmlConfig(config);
        SLAviolation = 0;
        schdler = new FifoScheduler();
        rc.initialResourceAlocator(this);
        am = new EnterpriseSystemAM(this);
    }

    public boolean checkForViolation() {
        for (int i = 0; i < applicationList.size(); i++) {
            if (applicationList.get(i).SLAviolation > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isThereFreeNodeforApp() {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -2) {
                return true;
            }
        }
        return false;
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

    boolean runAcycle() throws IOException {
//        if(applicationList.size()>0 & checkForViolation())//& Main.localTime%Main.epochSys==0)
//        {
//            AM.monitor();
//            AM.analysis(SLAviolation);
//            AM.planning();
//            AM.execution();
//            Main.mesg++;
//            
//        }
        int finishedBundle = 0;
        for (int i = 0; i < applicationList.size(); i++) {
            //TODO: if each bundle needs some help should ask and here resourceallocation should run
            if (applicationList.get(i).runAcycle() == false) //return false if bundle set jobs are done, we need to re-resourcealocation
            {
                numberofIdleNode = applicationList.get(i).ComputeNodeList.size() + numberofIdleNode;
                System.out.println("Number of violation in " + applicationList.get(i).id + "th application=  " + applicationList.get(i).NumofViolation);
                //System.out.println("application "+i +"is destroyed and there are: "+(applicationList.size()-1)+"   left");
                applicationList.get(i).destroyApplication();
                applicationList.remove(i);
                finishedBundle++;
            }
        }
        if (finishedBundle > 0) {
            rc.resourceAloc(this); //Nothing for now!
        }
        if (applicationList.isEmpty()) {
            sysIsDone = true;     // all done!
            return true;
        } else {
            return false;
        }
    }

    @Override
    void readFromNode(Node node, String path) {
        ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    numberofNode = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
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
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplication")) {
                    applicationList.add(new EnterpriseApp(path, childNodes.item(i), this));
                    applicationList.get(applicationList.size() - 1).parent = this;
                }
            }
        }
    }
}
