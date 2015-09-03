package simulator.ra;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.Chassis;
import simulator.physical.DataCenter;
import simulator.system.ComputeSystem;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseSystem;
import simulator.system.InteractiveSystem;
import simulator.system.InteractiveUser;

public abstract class ResourceAllocation {

    private static final Logger LOGGER = Logger.getLogger(ResourceAllocation.class.getName());

    protected DataCenter dataCenter;
    private Environment environment;

    public abstract int[] nextServerSys(List<Integer> chassisList);

    public int nextServerInSys(List<BladeServer> bs) {
        return 0;
    }

    public abstract int nextServer(List<BladeServer> bladeList);
    
    public abstract int[] allocateSystemLevelServer(List<BladeServer> bs, int list[]);

    public ResourceAllocation(Environment environment, DataCenter dataCenter) {
        this.environment = environment;
        this.dataCenter = dataCenter;
    }

    void resourceAlocViolation(EnterpriseSystem enterpriseSystem) {
    }

    void resourceAlocViolation(InteractiveSystem interactiveSystem) {
    }

    abstract public void resourceAloc(EnterpriseSystem enterpriseSystem);

    abstract public void resourceAloc(InteractiveSystem interactiveSystem);

    void resourceRelease(EnterpriseSystem enterpriseSystem, int predicdetNumber) {

        int currentInvolved = enterpriseSystem.getComputeNodeList().size() - enterpriseSystem.getNumberofIdleNode();
        int difference = currentInvolved - predicdetNumber;
        // LOGGER.info("in releaseing resource "+difference);
        for (int j = 0; j < difference; j++) {
            int indexServer = enterpriseSystem.getApplications().get(0).getComputeNodeList().get(difference - j).getServerID();
            int indexChassis = enterpriseSystem.getApplications().get(0).getComputeNodeList().get(difference - j).getChassisID();
            enterpriseSystem.getApplications().get(0).removeCompNodeFromBundle(
                    dataCenter.getServer(indexChassis, findServerInChasis(indexChassis, indexServer)));
            // ES.getApplications().get(0).ComputeNodeIndex.remove(difference-j);///////
            // not exactly correct
            enterpriseSystem.setNumberofIdleNode(enterpriseSystem.getNumberofIdleNode() + 1);
        }
    }

    public void resourceProvision(EnterpriseSystem enterpriseSystem, int[] alocVectr) {
        // release first
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] < 0) {
                for (int ii = 0; ii < (-1 * alocVectr[i]); ii++) {
                    int indexi = enterpriseSystem.getApplications().get(i).myFirstIdleNode();
                    if (indexi == -2) {
                        LOGGER.info("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
                        return;
                    }
                    int indexServer = enterpriseSystem.getApplications().get(i).getComputeNodeList().get(indexi).getServerID();
                    int indexChassis = enterpriseSystem.getApplications().get(i).getComputeNodeList().get(indexi).getChassisID();
                    enterpriseSystem.getApplications().get(i).getComputeNodeList().remove(indexi);
                    final BladeServer server = dataCenter.getServer(indexChassis,
                            findServerInChasis(indexChassis, indexServer));
                    server.setStatusAsNotAssignedToAnyApplication();
                    server.setSLAPercentage(0);
                    server.setTimeTreshold(0);
                    /////
                    if (enterpriseSystem.getApplications().get(i).numberofRunningNode() == 0) {
                        enterpriseSystem.getApplications().get(i).activeOneNode();
                    }
                    LOGGER.info("Release:app: " + i + "\t#of comp Node="
                            + enterpriseSystem.getApplications().get(i).getComputeNodeList().size() + "\t system Rdy to aloc="
                            + enterpriseSystem.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
                            + "\tNumber of running = " + enterpriseSystem.getApplications().get(i).numberofRunningNode());
                    environment.updateNumberOfMessagesFromDataCenterToSystem();
                }
            }
        }
        // Allocation part come in second
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] >= 1) {
                for (int ii = 0; ii < alocVectr[i]; ii++) {
                    int indexInComputeList = nextServerInSys(enterpriseSystem.getComputeNodeList());
                    if (indexInComputeList == -2) {
                        // LOGGER.info("nashod alocate konim! for this
                        // application ->"+i +"\tsize quueue 0->"+
                        // ES.getApplications().get(0).queueApp.size()+"\t1->"+ES.getApplications().get(1).queueApp.size());
                        enterpriseSystem.getAM().setRecForCoopAt(i, 1);
                    } else {
                        int indexServer = enterpriseSystem.getComputeNodeList().get(indexInComputeList).getServerID();
                        int indexChassis = enterpriseSystem.getComputeNodeList().get(indexInComputeList).getChassisID();
                        final BladeServer server = dataCenter.getServer(indexChassis,
                                findServerInChasis(indexChassis, indexServer));
                        enterpriseSystem.getApplications().get(i).addCompNodetoBundle(server);
                        // ES.getApplications().get(i).ComputeNodeIndex.add(indexChassis);
                        // //need to think about that!
                        // now the node is assinged to a application and is
                        // ready!
                        server.setStatusAsRunningNormal();
                        server.setSLAPercentage(enterpriseSystem.getApplications().get(i).getSLAPercentage());
                        server.setTimeTreshold(enterpriseSystem.getApplications().get(i).getTimeTreshold());
                        LOGGER.info("Alloc: to app:" + i + "\t#of comp Node="
                                + enterpriseSystem.getApplications().get(i).getComputeNodeList().size() + "\tsys Rdy to aloc="
                                + enterpriseSystem.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
                                + "\tsys Number of running = " + enterpriseSystem.getApplications().get(i).numberofRunningNode());
                        environment.updateNumberOfMessagesFromDataCenterToSystem();
                    }
                }
            }
        }
    }
    // For Server Provisioning
    // assumption: just doing for one Application Bundle need to work on
    // multiple AB

    void resourceProvision(EnterpriseSystem enterpriseSystem, int predicdetNumber) {
        int currentInvolved = enterpriseSystem.getComputeNodeList().size() - enterpriseSystem.getNumberofIdleNode();
        // LOGGER.info("resourceProvision : request for=" + "\t" +
        // predicdetNumber +"\t now has=\t"+currentInvolved+ "\t localTime=
        // "+Main.localTime);
        if (currentInvolved == predicdetNumber | predicdetNumber <= 0) {
            return;
        }
        if (currentInvolved > predicdetNumber && enterpriseSystem.getSLAviolation() == 0) // got
        // to
        // release
        // some
        // nodes
        {
            resourceRelease(enterpriseSystem, predicdetNumber);
        } // we already have more server involved and dont change the state
        else // need to provide more server
        {
            int difference = predicdetNumber - currentInvolved;
            for (int i = 0; i < difference; i++) {
                for (int j = 0; j < enterpriseSystem.getComputeNodeList().size(); j++) {
                    if (enterpriseSystem.getComputeNodeList().get(j).isNotApplicationAssigned()
                            || enterpriseSystem.getComputeNodeList().get(j).isIdle()) {
                        int indexServer = enterpriseSystem.getComputeNodeList().get(j).getServerID();
                        int indexChassis = enterpriseSystem.getComputeNodeList().get(j).getChassisID();
                        BladeServer server = dataCenter.getServer(indexChassis,
                                findServerInChasis(indexChassis, indexServer));
                        enterpriseSystem.getApplications().get(0).addCompNodetoBundle(server);
                        // ES.getApplications().get(0).ComputeNodeIndex.add(indexChassis);
                        // //need to think about that!
                        // now the node is assinged to a application and is
                        // ready!

                        server.setStatusAsRunningNormal();
                        server.setSLAPercentage(enterpriseSystem.getApplications().get(0).getSLAPercentage());
                        server.setTimeTreshold(enterpriseSystem.getApplications().get(0).getTimeTreshold());
                        enterpriseSystem.setNumberofIdleNode(enterpriseSystem.getNumberofIdleNode() - 1);
                        // here means we increased number of running nodes,
                        // needs to inform underneath AM
                        // Simulator.getInstance().communicationAM = 1;
                        break; // found one free server go for another one if
                               // needed
                    }
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// COMPUTING////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    public void initialResourceAloc(ComputeSystem computeSystem) {
        // Best fit resource allocation
        int[] serverIndex = new int[2];
        List<Integer> myChassisList = createChassisArray(computeSystem.getRackIDs());// creats
        // a
        // list
        // of
        // servers
        // ID
        // that
        // will
        // be
        // used
        // for
        // resource
        // allocation
        for (int i = 0; i < computeSystem.getNumberOfNode(); i++) {
            serverIndex = nextServerSys(myChassisList);
            if (serverIndex == null) {
                LOGGER.info("-2 index in which server  initialResourceAloc(ComputeSystem CS)  iiiii" + i);
                return;
            }
            // LOGGER.info(serverIndex);
            int indexChassis = serverIndex[0];
            int indexServer = serverIndex[1];
            final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
            computeSystem.addComputeNodeToSys(server);
            // this node is in this CS nodelist but it is not assigned to any
            // job yet!
            // in Allocation module ready flag will be changed to 1
            server.setStatusAsRunningNormal();
            computeSystem.appendBladeServerIndexIntoComputeNodeIndex(serverIndex[1]);
            LOGGER.info("HPC System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
        }
    }

    void allocateAserver(ComputeSystem computeSystem) {
        int[] serverIndex = new int[2];
        List<Integer> myChassisList = new ArrayList<Integer>();
        serverIndex = nextServerSys(myChassisList);
        if (serverIndex == null) {
            LOGGER.info("-2 index in which server  initialResourceAloc(ComputeSystem CS)  iiiii");
            return;
        }
        // LOGGER.info(serverIndex);
        int indexChassis = serverIndex[0];
        int indexServer = serverIndex[1];
        final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
        computeSystem.addComputeNodeToSys(server);
        // this node is in this CS nodelist but it is not assigned to any job
        // yet!
        // in Allocation module ready flag will be changed to 1
        server.setStatusAsRunningNormal();
        computeSystem.appendBladeServerIndexIntoComputeNodeIndex(serverIndex[1]);
        LOGGER.info("HPC System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    // First time resource Allocation for system and bundle together

    public void initialResourceAlocator(EnterpriseSystem enterpriseSystem) {
        int[] serverIndex = new int[2];
        List<Integer> myChassisList = createChassisArray(enterpriseSystem.getRackIDs());// creats
        // a
        // list
        // of
        // servers
        // ID
        // that
        // will
        // be
        // used
        // for
        // resource
        // allocation
        for (int i = 0; i < enterpriseSystem.getNumberOfNode(); i++) {
            serverIndex = nextServerSys(myChassisList);
            if (serverIndex[0] == -2) {
                LOGGER.info("-2 index in which server initialResourceAloc(EnterpriseSystem ES)");
                return;
            }
            int indexChassis = serverIndex[0];
            int indexServer = serverIndex[1];
            final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
            enterpriseSystem.addComputeNodeToSys(server);
            // this node is in this ES nodelist but it is not assigned to any
            // application yet!
            // in Allocation module ready flag will be changed to 1
            server.setStatusAsNotAssignedToAnyApplication();
            enterpriseSystem.appendBladeServerIndexIntoComputeNodeIndex(serverIndex[1]);
            LOGGER.info("Enterprise System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
        }
        // Minimum allocation give every bundle minimum of its requierments
        // Assume we have enough for min of all bundles!
        int neededProc = 0;
        int indexInComputeList = 0;// index each assigned node
        for (EnterpriseApp enterpriseApplication : enterpriseSystem.getApplications()) {
            neededProc = enterpriseApplication.getMinProc();
            for (int index = 0; index < neededProc; index++) {
                int indexServer = enterpriseSystem.getComputeNodeList().get(indexInComputeList).getServerID();
                int indexChassis = enterpriseSystem.getComputeNodeList().get(indexInComputeList++).getChassisID();
                final BladeServer server = dataCenter.getServer(indexChassis,
                        findServerInChasis(indexChassis, indexServer));
                enterpriseApplication.addCompNodetoBundle(server);
                // ES.getApplications().get(i).ComputeNodeIndex.add(indexChassis);
                // //need to think about that!
                // now the node is assinged to a application and is ready!
                server.setStatusAsRunningNormal();
                server.setSLAPercentage(enterpriseApplication.getSLAPercentage());
                server.setTimeTreshold(enterpriseApplication.getTimeTreshold());
                // LOGGER.info("Allocating compute node to the Enterprise
                // BoN : Chassis#\t"+ indexChassis );
            }
        }
        enterpriseSystem.setNumberofIdleNode(enterpriseSystem.getComputeNodeList().size() - indexInComputeList);
        LOGGER.info("Number of remained IdleNode in sys\t" + enterpriseSystem.getNumberofIdleNode());
        if (enterpriseSystem.getNumberofIdleNode() < 0) {
            LOGGER.info("numberofIdleNode is negative!!!");
        }
    }
    // Tries to allocate number of requested compute node to the whole WebBased
    // system
    // searching from cool affect place for the number of requested srever

    int findServerInChasis(int chassis, int servID) {
        for (int i = 0; i < dataCenter.getChassisSet().get(chassis).getServers().size(); i++) {
            if (dataCenter.getChassisSet().get(chassis).getServers().get(i).getServerID() == servID) {
                return i;
            }
        }
        return -2;
    }

    public void initialResourceAlocator(InteractiveSystem interactiveSystem) {
        /// Initial alocation of compute node
        int[] serverIndex = new int[2];
        List<Integer> myChassisList = createChassisArray(interactiveSystem.getRackIDs());// creats
        // a
        // list
        // of
        // servers
        // ID
        // that
        // will
        // be
        // used
        // for
        // resource
        // allocation
        for (int i = 0; i < interactiveSystem.getNumberOfNode(); i++) {
            serverIndex = nextServerSys(myChassisList);
            if (serverIndex[0] == -2) {
                LOGGER.info("-2 index in which server in initialResourceAloc_sys(WebBasedSystem");
                return;
            }
            LOGGER.info("Interactive system: ChassisID= " + serverIndex[0] + " & Server= " + serverIndex[1]);
            int indexChassis = serverIndex[0];
            int indexServer = serverIndex[1];
            final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
            interactiveSystem.addComputeNodeToSys(server);
            // this node is in this WS nodelist but it is not assigned to any
            // workload yet!
            // in Allocation module ready flag will be changed to 1
            server.setStatusAsNotAssignedToAnyApplication();
            interactiveSystem.appendBladeServerIndexIntoComputeNodeIndex(serverIndex[1]);
        }
    }

    public int initialResourceAloc(InteractiveSystem interactiveSystem) {
        int i = 0, j;
        InteractiveUser test = new InteractiveUser(interactiveSystem, environment);
        test = interactiveSystem.getWaitingQueueWL().get(0);
        if (test.getMinProc() > interactiveSystem.getNumberofIdleNode()) {
            LOGGER.info("initialResource ALoc: not enough resource for WLBundle");
            return -1;
        }
        j = test.getMinProc();
        while (j-- > 0) {
            for (i = 0; i < interactiveSystem.getComputeNodeList().size(); i++) {
                if (interactiveSystem.getComputeNodeList().get(i).isNotApplicationAssigned()) {
                    break;
                }
            }
            if (i == interactiveSystem.getComputeNodeList().size()) // just in case! this
            // condition has been
            // checked before ,no
            // node is ready in this
            // system
            {
                return -1;
            }
            int serverId = interactiveSystem.getComputeNodeList().get(i).getServerID();
            int indexChassis = interactiveSystem.getComputeNodeList().get(i).getChassisID();
            serverId = findServerInChasis(indexChassis, serverId);
            final BladeServer server = dataCenter.getServer(indexChassis, serverId);
            test.addCompNodetoBundle(server);
            test.getComputeNodeIndex().add(serverId);
            server.setStatusAsRunningNormal();
            server.configSLAparameter(test.getMaxExpectedResTime());
            interactiveSystem.setNumberofIdleNode(interactiveSystem.getNumberofIdleNode() - 1);
            LOGGER.info("Allocating compute node to Inter. User: Chassis#" + indexChassis
                    + "\tNumber of remained IdleNode in sys\t" + interactiveSystem.getNumberofIdleNode() + "@ time: "
                    + environment.getCurrentLocalTime());
        }
        interactiveSystem.getUserList().add(test);
        interactiveSystem.getWaitingQueueWL().remove(test);
        return 0;
    }

    List<Integer> createServerArray(int[] myRackID) {
        List<Integer> myServerId = new ArrayList<Integer>();
        for (int i = 0; i < myRackID.length; i++) {
            for (Chassis chassis : dataCenter.getChassisSet()) {
                if (chassis.getRackID() == myRackID[i]) {
                    for (BladeServer bladeServer : chassis.getServers()) {
                        myServerId.add(bladeServer.getServerID());
                    }
                }
            }
        }

        return myServerId;
    }

    List<Integer> createChassisArray(List<Integer> myRackID) {
        List<Integer> myChassisId = new ArrayList<Integer>();
        for (Integer rackID : myRackID) {
            for (Chassis chassis : dataCenter.getChassisSet()) {
                if (chassis.getRackID() == rackID) {
                    myChassisId.add(chassis.getChassisID());
                }
            }
        }
        return myChassisId;
    }
    //////////////////////////////// INTERACTIVE//////////////////////////////

    public void resourceProvision(InteractiveSystem interactiveSystem, int[] alocVectr) {
        // release first
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] < 0) {
                for (int ii = 0; ii < (-1 * alocVectr[i]); ii++) {
                    int indexi = interactiveSystem.getUserList().get(i).myFirstIdleNode();
                    if (indexi == -2) {
                        LOGGER.info("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
                        return;
                    }
                    int indexServer = interactiveSystem.getUserList().get(i).getComputeNodeList().get(indexi).getServerID();
                    int indexChassis = interactiveSystem.getUserList().get(i).getComputeNodeList().get(indexi).getChassisID();
                    interactiveSystem.getUserList().get(i).getComputeNodeList().remove(indexi);
                    final BladeServer server = dataCenter.getServer(indexChassis,
                            findServerInChasis(indexChassis, indexServer));
                    server.setStatusAsNotAssignedToAnyApplication();
                    server.setSLAPercentage(0);
                    server.setTimeTreshold(0);
                    /////
                    if (interactiveSystem.getUserList().get(i).numberofRunningNode() == 0) {
                        interactiveSystem.getUserList().get(i).activeOneNode();
                    }
                    LOGGER.info("Release:User: " + i + "\t#of comp Node="
                            + interactiveSystem.getUserList().get(i).getComputeNodeList().size() + "\t system Rdy to aloc="
                            + interactiveSystem.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
                            + "\tNumber of running = " + interactiveSystem.getUserList().get(i).numberofRunningNode());
                }
            }
        }
        // Allocation part come in second
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] >= 1) {
                for (int ii = 0; ii < alocVectr[i]; ii++) {
                    int indexInComputeList = nextServerInSys(interactiveSystem.getComputeNodeList());
                    if (indexInComputeList == -2) {
                        LOGGER.info("nashod alocate konim! for  this User ->" + i + "\tsize quueue 0->"
                                + interactiveSystem.getUserList().get(0).getQueueWL().size() + "\t1->"
                                + interactiveSystem.getUserList().get(1).getQueueWL().size() + "\t2->"
                                + interactiveSystem.getUserList().get(2).getQueueWL().size());
                        interactiveSystem.getAM().setRecForCoopAt(i, 1);
                    } else {
                        int indexServer = interactiveSystem.getComputeNodeList().get(indexInComputeList).getServerID();
                        int indexChassis = interactiveSystem.getComputeNodeList().get(indexInComputeList).getChassisID();
                        final BladeServer server = dataCenter.getServer(indexChassis,
                                findServerInChasis(indexChassis, indexServer));
                        interactiveSystem.getUserList().get(i).addCompNodetoBundle(server);
                        // ES.getApplications().get(i).ComputeNodeIndex.add(indexChassis);
                        // //need to think about that!
                        // now the node is assinged to a application and is
                        // ready!
                        server.setStatusAsRunningNormal();
                        server.setTimeTreshold(interactiveSystem.getUserList().get(i).getMaxExpectedResTime());
                        LOGGER.info("Alloc: to User:" + i + "\t#of comp Node="
                                + interactiveSystem.getUserList().get(i).getComputeNodeList().size() + "\tsys Rdy to aloc="
                                + interactiveSystem.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
                                + "\tsys Number of running = " + interactiveSystem.getUserList().get(i).numberofRunningNode());
                    }
                }
            }
        }
    }
}
