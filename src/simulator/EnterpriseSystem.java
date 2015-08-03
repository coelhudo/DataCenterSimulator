package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulator.am.EnterpriseSystemAM;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.schedulers.FifoScheduler;

public class EnterpriseSystem extends GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystem.class.getName());
    
    private List<EnterpriseApp> applicationList;
    private Environment environment;
    private File logFile;

    private EnterpriseSystem(String config, Environment environment, DataCenter dataCenter) {
        this.environment = environment;
        setComputeNodeList(new ArrayList<BladeServer>());
        setComputeNodeIndex(new ArrayList<Integer>());
        applicationList = new ArrayList<EnterpriseApp>();
        setResourceAllocation(new MHR(environment, dataCenter));
        parseXmlConfig(config);
        setSLAviolation(0);
        setScheduler(new FifoScheduler());
    }

    public List<EnterpriseApp> getApplications() {
        return applicationList;
    }
    
    public boolean checkForViolation() {
        for (EnterpriseApp enterpriseApplication : applicationList) {
            if (enterpriseApplication.getSLAviolation() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isThereFreeNodeforApp() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -2) {
                return true;
            }
        }
        return false;
    }

    public int numberofAvailableNodetoAlocate() {
        int n = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -2) {
                n++;
            }
        }
        return n;
    }

    boolean runAcycle() throws IOException {
        // if(applicationList.size()>0 & checkForViolation())//&
        // Main.localTime%Main.epochSys==0)
        // {
        // AM.monitor();
        // AM.analysis(SLAviolation);
        // AM.planning();
        // AM.execution();
        // Main.mesg++;
        //
        // }
        int finishedBundle = 0;
        for (int i = 0; i < applicationList.size(); i++) {
            // TODO: if each bundle needs some help should ask and here
            // resourceallocation should run
            if (applicationList.get(i).runAcycle() == false) // return false if
            // bundle set
            // jobs are
            // done, we need
            // to
            // re-resourcealocation
            {
                setNumberofIdleNode(applicationList.get(i).getComputeNodeList().size() + getNumberofIdleNode());
                LOGGER.info("Number of violation in " + applicationList.get(i).getID() + "th application=  "
                        + applicationList.get(i).getNumofViolation());
                // LOGGER.info("application "+i +"is destroyed and there
                // are: "+(applicationList.size()-1)+" left");
                applicationList.get(i).destroyApplication();
                applicationList.remove(i);
                finishedBundle++;
            }
        }
        if (finishedBundle > 0) {
            getResourceAllocation().resourceAloc(this); // Nothing for now!
        }
        if (applicationList.isEmpty()) {
            markAsDone(); // all done!
            return true;
        } else {
            return false;
        }
    }

    @Override
    void readFromNode(Node node, String path) {
        getComputeNodeList().clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    setNumberofNode(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        getRackId().add(Integer.parseInt(split[j]));
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg"))
                    ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler"))
                    ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplication")) {
                    EnterpriseApplicationPOD enterpriseApplicationPOD = getEnterpriseApplicationPOD(childNodes.item(i), path);
                    applicationList.add(new EnterpriseApp(enterpriseApplicationPOD, this, environment));
                    applicationList.get(applicationList.size() - 1).parent = this;
                }
            }
        }
    }
    
    EnterpriseApplicationPOD getEnterpriseApplicationPOD(Node node, String path) {
        getComputeNodeList().clear();
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("id")) {
                    enterpriseApplicationPOD.setID(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); // Id
                    // of
                    // the
                    // application
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplicationWorkLoad")) {
                    String fileName = path + "/" + childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        logFile = new File(fileName);
                        enterpriseApplicationPOD.setBIS(new BufferedReader(new InputStreamReader(new FileInputStream(logFile))));
                    } catch (IOException e) {
                        LOGGER.info("Uh oh, got an IOException error!" + e.getMessage());
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("MaxNumberOfRequest")) {
                    enterpriseApplicationPOD.setMaxNumberOfRequest(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("NumberofBasicNode")) {
                    enterpriseApplicationPOD.setNumberofBasicNode(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("timeTreshold")) {
                    enterpriseApplicationPOD.setTimeTreshold(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                    enterpriseApplicationPOD.setMaxExpectedResTime(enterpriseApplicationPOD.getTimeTreshold());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Percentage")) {
                    enterpriseApplicationPOD.setSLAPercentage(
                            Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim())); //
                } // We dont have server list now but may be in future we had
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("minProcessor")) {
                    enterpriseApplicationPOD.setMinProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("maxProcessor")) {
                    enterpriseApplicationPOD.setMaxProc(Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim()));
                }

            }
        }
        
        return enterpriseApplicationPOD;
    }


    public static EnterpriseSystem Create(String config, Environment environment, DataCenter dataCenter, SLAViolationLogger slaViolationLogger) {
        EnterpriseSystem enterpriseSytem = new EnterpriseSystem(config, environment, dataCenter);
        enterpriseSytem.getResourceAllocation().initialResourceAlocator(enterpriseSytem);
        enterpriseSytem.setAM(new EnterpriseSystemAM(enterpriseSytem, environment, slaViolationLogger)); //FIXME: why here the violation is logged by the AM class but not the system class?

        return enterpriseSytem;
    }
}
