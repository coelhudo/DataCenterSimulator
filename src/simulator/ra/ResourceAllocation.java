package simulator.ra;

import java.util.List;
import java.util.ArrayList;
import simulator.physical.BladeServer;
import simulator.ComputeSystem;
import simulator.physical.DataCenter;
import simulator.EnterpriseSystem;
import simulator.InteractiveSystem;
import simulator.InteractiveUser;
import simulator.Simulator;

public abstract class ResourceAllocation {

    protected DataCenter dc = Simulator.getInstance().getDatacenter();

    public int[] nextServerSys(List<Integer> chassisList) {
        return null;
    }

    public int nextServerInSys(List<BladeServer> bs) {
        return 0;
    }

    public int nextServer(List<BladeServer> bladeList) {
        return 0;
    }

    public int[] allocateSystemLevelServer(List<BladeServer> bs, int list[]) {
        return null;
    }

    public ResourceAllocation() {
    }

    void resourceAlocViolation(EnterpriseSystem ES) {
    }

    void resourceAlocViolation(InteractiveSystem WS) {
    }

    abstract public void resourceAloc(EnterpriseSystem ES);

    abstract public void resourceAloc(InteractiveSystem WS);

    void resourceRelease(EnterpriseSystem ES, int predicdetNumber) {

        int currentInvolved = ES.ComputeNodeList.size() - ES.numberofIdleNode;
        int difference = currentInvolved - predicdetNumber;
        //System.out.println("in releaseing resource    "+difference);
        for (int j = 0; j < difference; j++) {
            int indexServer = ES.applicationList.get(0).ComputeNodeList.get(difference - j).serverID;
            int indexChassis = ES.applicationList.get(0).ComputeNodeList.get(difference - j).chassisID;
            ES.applicationList.get(0).removeCompNodeFromBundle(dc.getServer(indexChassis, findServerInChasis(indexChassis, indexServer)));
//                ES.applicationList.get(0).ComputeNodeIndex.remove(difference-j);/////// not exactly correct
            ES.numberofIdleNode++;
        }
    }

    public void resourceProvision(EnterpriseSystem ES, int[] alocVectr) {
        //release first
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] < 0) {
                for (int ii = 0; ii < (-1 * alocVectr[i]); ii++) {
                    int indexi = ES.applicationList.get(i).myFirstIdleNode();
                    if (indexi == -2) {
                        System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
                        return;
                    }
                    int indexServer = ES.applicationList.get(i).ComputeNodeList.get(indexi).serverID;
                    int indexChassis = ES.applicationList.get(i).ComputeNodeList.get(indexi).chassisID;
                    ES.applicationList.get(i).ComputeNodeList.remove(indexi);
                    final BladeServer server = dc.getServer(indexChassis, findServerInChasis(indexChassis, indexServer));
                    server.ready = -2;
                    server.SLAPercentage = 0;
                    server.timeTreshold = 0;
                    /////
                    if (ES.applicationList.get(i).numberofRunningNode() == 0) {
                        ES.applicationList.get(i).activeOneNode();
                    }
                    System.out.println("Release:app: " + i + "\t#of comp Node="
                            + ES.applicationList.get(i).ComputeNodeList.size() + "\t system Rdy to aloc="
                            + ES.numberofAvailableNodetoAlocate() + "\t@:" + Simulator.getInstance().localTime + "\tNumber of running = " + ES.applicationList.get(i).numberofRunningNode());
                    Simulator.getInstance().mesg++;
                }
            }
        }
        //Allocation part come in second 
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] >= 1) {
                for (int ii = 0; ii < alocVectr[i]; ii++) {
                    int indexInComputeList = nextServerInSys(ES.ComputeNodeList);
                    if (indexInComputeList == -2) {
                        //System.out.println("nashod alocate konim! for  this application ->"+i +"\tsize quueue 0->"+
                        //       ES.applicationList.get(0).queueApp.size()+"\t1->"+ES.applicationList.get(1).queueApp.size());
                        ES.am.getRecForCoop()[i] = 1;
                    } else {
                        int indexServer = ES.ComputeNodeList.get(indexInComputeList).serverID;
                        int indexChassis = ES.ComputeNodeList.get(indexInComputeList).chassisID;
                        final BladeServer server = dc.getServer(indexChassis, findServerInChasis(indexChassis, indexServer));
                        ES.applicationList.get(i).addCompNodetoBundle(server);
                        //                            ES.applicationList.get(i).ComputeNodeIndex.add(indexChassis); //need to think about that!
                        //now the node is assinged to a application and is ready!
                        server.ready = 1;
                        server.SLAPercentage = ES.applicationList.get(i).SLAPercentage;
                        server.timeTreshold = ES.applicationList.get(i).timeTreshold;
                        System.out.println("Alloc: to app:" + i + "\t#of comp Node=" + ES.applicationList.get(i).ComputeNodeList.size() + "\tsys Rdy to aloc=" + ES.numberofAvailableNodetoAlocate()
                                + "\t@:" + Simulator.getInstance().localTime + "\tsys Number of running = " + ES.applicationList.get(i).numberofRunningNode());
                        Simulator.getInstance().mesg++;
                    }
                }
            }
        }
    }
    //For Server Provisioning
    //assumption: just doing for one Application Bundle need to work on multiple AB

    void resourceProvision(EnterpriseSystem ES, int predicdetNumber) {
        int currentInvolved = ES.ComputeNodeList.size() - ES.numberofIdleNode;
        //System.out.println("resourceProvision : request for=" +   "\t" + predicdetNumber +"\t now has=\t"+currentInvolved+ "\t  localTime= "+Main.localTime);
        if (currentInvolved == predicdetNumber | predicdetNumber <= 0) {
            return;
        }
        if (currentInvolved > predicdetNumber && ES.SLAviolation == 0) //got to release some nodes
        {
            resourceRelease(ES, predicdetNumber);
        } //we already have more server involved and dont change the state
        else //need to provide more server
        {
            int difference = predicdetNumber - currentInvolved;
            for (int i = 0; i < difference; i++) {
                for (int j = 0; j < ES.ComputeNodeList.size(); j++) {
                    if (ES.ComputeNodeList.get(j).ready == -2 // is in System but not assigned to application
                            | ES.ComputeNodeList.get(j).ready == -1) //is idle
                    {
                        int indexServer = ES.ComputeNodeList.get(j).serverID;
                        int indexChassis = ES.ComputeNodeList.get(j).chassisID;
                        BladeServer server = dc.getServer(indexChassis, findServerInChasis(indexChassis, indexServer));
                        ES.applicationList.get(0).addCompNodetoBundle(server);
//                            ES.applicationList.get(0).ComputeNodeIndex.add(indexChassis); //need to think about that!
                        //now the node is assinged to a application and is ready!

                        server.ready = 1;
                        server.SLAPercentage = ES.applicationList.get(0).SLAPercentage;
                        server.timeTreshold = ES.applicationList.get(0).timeTreshold;
                        ES.numberofIdleNode--;
                        // here means we increased number of running nodes, needs to inform underneath AM
                        Simulator.getInstance().communicationAM = 1;
                        break; //found one free server go for another one if needed
                    }
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////COMPUTING////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    public void initialResourceAloc(ComputeSystem CS) {
        //Best fit resource allocation
        int[] serverIndex = new int[2];
        ArrayList<Integer> myChassisList = new ArrayList<Integer>();
        myChassisList = creatChassisArray(CS.rackId);// creats a list of servers ID that will be used for resource allocation
        for (int i = 0; i < CS.numberofNode; i++) {
            serverIndex = nextServerSys(myChassisList);
            if (serverIndex == null) {
                System.out.println("-2 index in which server  initialResourceAloc(ComputeSystem CS)  iiiii" + i);
                return;
            }
            //System.out.println(serverIndex);
            int indexChassis = serverIndex[0];
            int indexServer = serverIndex[1];
            final BladeServer server = dc.getServer(indexChassis, indexServer);
            CS.addComputeNodeToSys(server);
            //this node is in this CS nodelist but it is not assigned to any job yet!
            //in Allocation module ready flag will be changed to 1
            server.ready = 1;
            CS.ComputeNodeIndex.add(serverIndex[1]);
            System.out.println("HPC System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
        }
    }

    void allocateAserver(ComputeSystem CS) {
        int[] serverIndex = new int[2];
        ArrayList<Integer> myChassisList = new ArrayList<Integer>();
        serverIndex = nextServerSys(myChassisList);
        if (serverIndex == null) {
            System.out.println("-2 index in which server  initialResourceAloc(ComputeSystem CS)  iiiii");
            return;
        }
        //System.out.println(serverIndex);
        int indexChassis = serverIndex[0];
        int indexServer = serverIndex[1];
        final BladeServer server = dc.getServer(indexChassis, indexServer);
        CS.addComputeNodeToSys(server);
        //this node is in this CS nodelist but it is not assigned to any job yet!
        //in Allocation module ready flag will be changed to 1
        server.ready = 1;
        CS.ComputeNodeIndex.add(serverIndex[1]);
        System.out.println("HPC System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    //First time resource Allocation for system and bundle together

    public void initialResourceAlocator(EnterpriseSystem ES) {
        int[] serverIndex = new int[2];
        ArrayList<Integer> myChassisList = new ArrayList<Integer>();
        myChassisList = creatChassisArray(ES.rackId);// creats a list of servers ID that will be used for resource allocation
        for (int i = 0; i < ES.numberofNode; i++) {
            serverIndex = nextServerSys(myChassisList);
            if (serverIndex[0] == -2) {
                System.out.println("-2 index in which server initialResourceAloc(EnterpriseSystem ES)");
                return;
            }
            int indexChassis = serverIndex[0];
            int indexServer = serverIndex[1];
            final BladeServer server = dc.getServer(indexChassis, indexServer);
            ES.addComputeNodeToSys(server);
            //this node is in this ES nodelist but it is not assigned to any application yet!
            //in Allocation module ready flag will be changed to 1
            server.ready = -2;
            ES.ComputeNodeIndex.add(serverIndex[1]);
            System.out.println("Enterprise System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
        }
        //Minimum allocation give every bundle minimum of its requierments
        //Assume we have enough for min of all bundles!
        int neededProc = 0;
        int indexInComputeList = 0;// index each assigned node
        for (int i = 0; i < ES.applicationList.size(); i++) {
            neededProc = ES.applicationList.get(i).minProc;
            for (int index = 0; index < neededProc; index++) {
                int indexServer = ES.ComputeNodeList.get(indexInComputeList).serverID;
                int indexChassis = ES.ComputeNodeList.get(indexInComputeList++).chassisID;
                final BladeServer server = dc.getServer(indexChassis, findServerInChasis(indexChassis, indexServer));
                ES.applicationList.get(i).addCompNodetoBundle(server);
//                        ES.applicationList.get(i).ComputeNodeIndex.add(indexChassis); //need to think about that!
                //now the node is assinged to a application and is ready!
                server.ready = 1;
                server.SLAPercentage = ES.applicationList.get(i).SLAPercentage;
                server.timeTreshold = ES.applicationList.get(i).timeTreshold;
                //System.out.println("Allocating compute node to the Enterprise BoN : Chassis#\t"+ indexChassis );
            }
        }
        ES.numberofIdleNode = ES.ComputeNodeList.size() - indexInComputeList;
        System.out.println("Number of remained IdleNode in sys\t" + ES.numberofIdleNode);
        if (ES.numberofIdleNode < 0) {
            System.out.println("numberofIdleNode is negative!!!");
        }
    }
    //Tries to allocate number of requested compute node to the whole WebBased system
    //searching from cool affect place for the number of requested srever 

    int findServerInChasis(int chassis, int servID) {
        for (int i = 0; i < dc.chassisSet.get(chassis).servers.size(); i++) {
            if (dc.chassisSet.get(chassis).servers.get(i).serverID == servID) {
                return i;
            }
        }
        return -2;
    }

    public void initialResourceAlocator(InteractiveSystem WS) {
        ///Initial alocation of compute node 
        int[] serverIndex = new int[2];
        ArrayList<Integer> myChassisList = new ArrayList<Integer>();
        myChassisList = creatChassisArray(WS.rackId);// creats a list of servers ID that will be used for resource allocation
        for (int i = 0; i < WS.numberofNode; i++) {
            serverIndex = nextServerSys(myChassisList);
            if (serverIndex[0] == -2) {
                System.out.println("-2 index in which server in initialResourceAloc_sys(WebBasedSystem");
                return;
            }
            System.out.println("Interactive system: ChassisID= " + serverIndex[0] + " & Server= " + serverIndex[1]);
            int indexChassis = serverIndex[0];
            int indexServer = serverIndex[1];
            final BladeServer server = dc.getServer(indexChassis, indexServer);
            WS.addComputeNodeToSys(server);
            //this node is in this WS nodelist but it is not assigned to any workload yet!
            //in Allocation module ready flag will be changed to 1
            server.ready = -2;
            WS.ComputeNodeIndex.add(serverIndex[1]);
        }
    }
    //////////////////////

    public int initialResourceAloc(InteractiveSystem WS) {
        int i = 0, j;
        InteractiveUser test = new InteractiveUser(WS);
        test = WS.waitingQueueWL.get(0);
        if (test.minProc > WS.numberofIdleNode) {
            System.out.println("initialResource ALoc: not enough resource for WLBundle");
            return -1;
        }
        j = test.minProc;
        while (j-- > 0) {
            for (i = 0; i < WS.ComputeNodeList.size(); i++) {
                if (WS.ComputeNodeList.get(i).ready == -2) //this node is not assigned yet!
                {
                    break;
                }
            }
            if (i == WS.ComputeNodeList.size()) // just in case! this condition has been checked before ,no node is ready in this system
            {
                return -1;
            }
            int serverId = WS.ComputeNodeList.get(i).serverID;
            int indexChassis = WS.ComputeNodeList.get(i).chassisID;
            serverId = findServerInChasis(indexChassis, serverId);
            final BladeServer server = dc.getServer(indexChassis, serverId);
            test.addCompNodetoBundle(server);
            test.ComputeNodeIndex.add(serverId);
            server.ready = 1;
            server.configSLAparameter(test.maxExpectedResTime);
            WS.numberofIdleNode--;
            System.out.println("Allocating compute node to Inter. User: Chassis#" + indexChassis
                    + "\tNumber of remained IdleNode in sys\t" + WS.numberofIdleNode + "@ time: "
                    + Simulator.getInstance().localTime);
        }
        WS.UserList.add(test);
        WS.waitingQueueWL.remove(test);
        return 0;
    }

    ;

    int[] creatServerArray(int[] myRackID) {
        int[] myServerId = null;
        int index = 0;
        for (int i = 0; i < myRackID.length; i++) {
            int j = 0;
            for (; j < dc.chassisSet.size(); j++) {
                if (dc.chassisSet.get(j).rackId == myRackID[i]) {
                    for (int k = 0; k < dc.chassisSet.get(j).servers.size(); k++) {
                        myServerId[index++] = dc.chassisSet.get(j).servers.get(k).serverID;
                    }
                }
            }

        }
        return myServerId;
    }

    ArrayList<Integer> creatChassisArray(ArrayList<Integer> myRackID) {
        ArrayList<Integer> myChassisId = new ArrayList<Integer>();
        for (int i = 0; i < myRackID.size(); i++) {
            int j = 0;
            for (; j < dc.chassisSet.size(); j++) {
                if (dc.chassisSet.get(j).rackId == myRackID.get(i)) {
                    myChassisId.add(dc.chassisSet.get(j).chassisID);
                }
            }
        }
        return myChassisId;
    }
    ////////////////////////////////INTERACTIVE//////////////////////////////

    public void resourceProvision(InteractiveSystem IS, int[] alocVectr) {
        //release first
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] < 0) {
                for (int ii = 0; ii < (-1 * alocVectr[i]); ii++) {
                    int indexi = IS.UserList.get(i).myFirstIdleNode();
                    if (indexi == -2) {
                        System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
                        return;
                    }
                    int indexServer = IS.UserList.get(i).ComputeNodeList.get(indexi).serverID;
                    int indexChassis = IS.UserList.get(i).ComputeNodeList.get(indexi).chassisID;
                    IS.UserList.get(i).ComputeNodeList.remove(indexi);
                    final BladeServer server = dc.getServer(indexChassis, findServerInChasis(indexChassis, indexServer));
                    server.ready = -2;
                    server.SLAPercentage = 0;
                    server.timeTreshold = 0;
                    /////
                    if (IS.UserList.get(i).numberofRunningNode() == 0) {
                        IS.UserList.get(i).activeOneNode();
                    }
                    System.out.println("Release:User: " + i + "\t#of comp Node=" + IS.UserList.get(i).ComputeNodeList.size()
                            + "\t system Rdy to aloc=" + IS.numberofAvailableNodetoAlocate() + "\t@:" + Simulator.getInstance().localTime
                            + "\tNumber of running = " + IS.UserList.get(i).numberofRunningNode());
                }
            }
        }
        //Allocation part come in second 
        for (int i = 0; i < alocVectr.length; i++) {
            if (alocVectr[i] >= 1) {
                for (int ii = 0; ii < alocVectr[i]; ii++) {
                    int indexInComputeList = nextServerInSys(IS.ComputeNodeList);
                    if (indexInComputeList == -2) {
                        System.out.println("nashod alocate konim! for  this User ->" + i + "\tsize quueue 0->"
                                + IS.UserList.get(0).queueWL.size() + "\t1->" + IS.UserList.get(1).queueWL.size() + "\t2->"
                                + IS.UserList.get(2).queueWL.size());
                        IS.am.getRecForCoop()[i] = 1;
                    } else {
                        int indexServer = IS.ComputeNodeList.get(indexInComputeList).serverID;
                        int indexChassis = IS.ComputeNodeList.get(indexInComputeList).chassisID;
                        final BladeServer server = dc.getServer(indexChassis, findServerInChasis(indexChassis, indexServer));
                        IS.UserList.get(i).addCompNodetoBundle(server);
                        //                            ES.applicationList.get(i).ComputeNodeIndex.add(indexChassis); //need to think about that!
                        //now the node is assinged to a application and is ready!
                        server.ready = 1;
                        server.timeTreshold = IS.UserList.get(i).maxExpectedResTime;
                        System.out.println("Alloc: to User:" + i + "\t#of comp Node=" + IS.UserList.get(i).ComputeNodeList.size() + "\tsys Rdy to aloc=" + IS.numberofAvailableNodetoAlocate()
                                + "\t@:" + Simulator.getInstance().localTime + "\tsys Number of running = " + IS.UserList.get(i).numberofRunningNode());
                    }
                }
            }
        }
    }
}
