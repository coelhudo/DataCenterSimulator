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
import simulator.Simulator.Environment;

public abstract class ResourceAllocation {

    protected DataCenter dataCenter;
    private Environment environment;

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

    public ResourceAllocation(Simulator.Environment environment, DataCenter dataCenter) {
	this.environment = environment;
	this.dataCenter = dataCenter;
    }

    void resourceAlocViolation(EnterpriseSystem ES) {
    }

    void resourceAlocViolation(InteractiveSystem WS) {
    }

    abstract public void resourceAloc(EnterpriseSystem ES);

    abstract public void resourceAloc(InteractiveSystem WS);

    void resourceRelease(EnterpriseSystem ES, int predicdetNumber) {

	int currentInvolved = ES.getComputeNodeList().size() - ES.getNumberofIdleNode();
	int difference = currentInvolved - predicdetNumber;
	// System.out.println("in releaseing resource "+difference);
	for (int j = 0; j < difference; j++) {
	    int indexServer = ES.applicationList.get(0).getComputeNodeList().get(difference - j).getServerID();
	    int indexChassis = ES.applicationList.get(0).getComputeNodeList().get(difference - j).getChassisID();
	    ES.applicationList.get(0).removeCompNodeFromBundle(
		    dataCenter.getServer(indexChassis, findServerInChasis(indexChassis, indexServer)));
	    // ES.applicationList.get(0).ComputeNodeIndex.remove(difference-j);///////
	    // not exactly correct
	    ES.setNumberofIdleNode(ES.getNumberofIdleNode() + 1);
	}
    }

    public void resourceProvision(EnterpriseSystem ES, int[] alocVectr) {
	// release first
	for (int i = 0; i < alocVectr.length; i++) {
	    if (alocVectr[i] < 0) {
		for (int ii = 0; ii < (-1 * alocVectr[i]); ii++) {
		    int indexi = ES.applicationList.get(i).myFirstIdleNode();
		    if (indexi == -2) {
			System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
			return;
		    }
		    int indexServer = ES.applicationList.get(i).getComputeNodeList().get(indexi).getServerID();
		    int indexChassis = ES.applicationList.get(i).getComputeNodeList().get(indexi).getChassisID();
		    ES.applicationList.get(i).getComputeNodeList().remove(indexi);
		    final BladeServer server = dataCenter.getServer(indexChassis,
			    findServerInChasis(indexChassis, indexServer));
		    server.setReady(-2);
		    server.setSLAPercentage(0);
		    server.setTimeTreshold(0);
		    /////
		    if (ES.applicationList.get(i).numberofRunningNode() == 0) {
			ES.applicationList.get(i).activeOneNode();
		    }
		    System.out.println("Release:app: " + i + "\t#of comp Node="
			    + ES.applicationList.get(i).getComputeNodeList().size() + "\t system Rdy to aloc="
			    + ES.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
			    + "\tNumber of running = " + ES.applicationList.get(i).numberofRunningNode());
		    environment.updateNumberOfMessagesFromDataCenterToSystem();
		}
	    }
	}
	// Allocation part come in second
	for (int i = 0; i < alocVectr.length; i++) {
	    if (alocVectr[i] >= 1) {
		for (int ii = 0; ii < alocVectr[i]; ii++) {
		    int indexInComputeList = nextServerInSys(ES.getComputeNodeList());
		    if (indexInComputeList == -2) {
			// System.out.println("nashod alocate konim! for this
			// application ->"+i +"\tsize quueue 0->"+
			// ES.applicationList.get(0).queueApp.size()+"\t1->"+ES.applicationList.get(1).queueApp.size());
			ES.getAM().getRecForCoop()[i] = 1;
		    } else {
			int indexServer = ES.getComputeNodeList().get(indexInComputeList).getServerID();
			int indexChassis = ES.getComputeNodeList().get(indexInComputeList).getChassisID();
			final BladeServer server = dataCenter.getServer(indexChassis,
				findServerInChasis(indexChassis, indexServer));
			ES.applicationList.get(i).addCompNodetoBundle(server);
			// ES.applicationList.get(i).ComputeNodeIndex.add(indexChassis);
			// //need to think about that!
			// now the node is assinged to a application and is
			// ready!
			server.setReady(1);
			server.setSLAPercentage(ES.applicationList.get(i).getSLAPercentage());
			server.setTimeTreshold(ES.applicationList.get(i).getTimeTreshold());
			System.out.println("Alloc: to app:" + i + "\t#of comp Node="
				+ ES.applicationList.get(i).getComputeNodeList().size() + "\tsys Rdy to aloc="
				+ ES.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
				+ "\tsys Number of running = " + ES.applicationList.get(i).numberofRunningNode());
			environment.updateNumberOfMessagesFromDataCenterToSystem();
		    }
		}
	    }
	}
    }
    // For Server Provisioning
    // assumption: just doing for one Application Bundle need to work on
    // multiple AB

    void resourceProvision(EnterpriseSystem ES, int predicdetNumber) {
	int currentInvolved = ES.getComputeNodeList().size() - ES.getNumberofIdleNode();
	// System.out.println("resourceProvision : request for=" + "\t" +
	// predicdetNumber +"\t now has=\t"+currentInvolved+ "\t localTime=
	// "+Main.localTime);
	if (currentInvolved == predicdetNumber | predicdetNumber <= 0) {
	    return;
	}
	if (currentInvolved > predicdetNumber && ES.getSLAviolation() == 0) // got
									    // to
									    // release
									    // some
									    // nodes
	{
	    resourceRelease(ES, predicdetNumber);
	} // we already have more server involved and dont change the state
	else // need to provide more server
	{
	    int difference = predicdetNumber - currentInvolved;
	    for (int i = 0; i < difference; i++) {
		for (int j = 0; j < ES.getComputeNodeList().size(); j++) {
		    if (ES.getComputeNodeList().get(j).getReady() == -2 // is in
									// System
									// but
									// not
									// assigned
									// to
									// application
			    | ES.getComputeNodeList().get(j).getReady() == -1) // is
									       // idle
		    {
			int indexServer = ES.getComputeNodeList().get(j).getServerID();
			int indexChassis = ES.getComputeNodeList().get(j).getChassisID();
			BladeServer server = dataCenter.getServer(indexChassis,
				findServerInChasis(indexChassis, indexServer));
			ES.applicationList.get(0).addCompNodetoBundle(server);
			// ES.applicationList.get(0).ComputeNodeIndex.add(indexChassis);
			// //need to think about that!
			// now the node is assinged to a application and is
			// ready!

			server.setReady(1);
			server.setSLAPercentage(ES.applicationList.get(0).getSLAPercentage());
			server.setTimeTreshold(ES.applicationList.get(0).getTimeTreshold());
			ES.setNumberofIdleNode(ES.getNumberofIdleNode() - 1);
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

    public void initialResourceAloc(ComputeSystem CS) {
	// Best fit resource allocation
	int[] serverIndex = new int[2];
	List<Integer> myChassisList = createChassisArray(CS.getRackId());// creats
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
	for (int i = 0; i < CS.getNumberofNode(); i++) {
	    serverIndex = nextServerSys(myChassisList);
	    if (serverIndex == null) {
		System.out.println("-2 index in which server  initialResourceAloc(ComputeSystem CS)  iiiii" + i);
		return;
	    }
	    // System.out.println(serverIndex);
	    int indexChassis = serverIndex[0];
	    int indexServer = serverIndex[1];
	    final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
	    CS.addComputeNodeToSys(server);
	    // this node is in this CS nodelist but it is not assigned to any
	    // job yet!
	    // in Allocation module ready flag will be changed to 1
	    server.setReady(1);
	    CS.getComputeNodeIndex().add(serverIndex[1]);
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
	// System.out.println(serverIndex);
	int indexChassis = serverIndex[0];
	int indexServer = serverIndex[1];
	final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
	CS.addComputeNodeToSys(server);
	// this node is in this CS nodelist but it is not assigned to any job
	// yet!
	// in Allocation module ready flag will be changed to 1
	server.setReady(1);
	CS.getComputeNodeIndex().add(serverIndex[1]);
	System.out.println("HPC System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    // First time resource Allocation for system and bundle together

    public void initialResourceAlocator(EnterpriseSystem ES) {
	int[] serverIndex = new int[2];
	List<Integer> myChassisList = createChassisArray(ES.getRackId());// creats
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
	for (int i = 0; i < ES.getNumberofNode(); i++) {
	    serverIndex = nextServerSys(myChassisList);
	    if (serverIndex[0] == -2) {
		System.out.println("-2 index in which server initialResourceAloc(EnterpriseSystem ES)");
		return;
	    }
	    int indexChassis = serverIndex[0];
	    int indexServer = serverIndex[1];
	    final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
	    ES.addComputeNodeToSys(server);
	    // this node is in this ES nodelist but it is not assigned to any
	    // application yet!
	    // in Allocation module ready flag will be changed to 1
	    server.setReady(-2);
	    ES.getComputeNodeIndex().add(serverIndex[1]);
	    System.out.println("Enterprise System: ChassisID=" + indexChassis + "  & Server id = " + indexServer);
	}
	// Minimum allocation give every bundle minimum of its requierments
	// Assume we have enough for min of all bundles!
	int neededProc = 0;
	int indexInComputeList = 0;// index each assigned node
	for (int i = 0; i < ES.applicationList.size(); i++) {
	    neededProc = ES.applicationList.get(i).getMinProc();
	    for (int index = 0; index < neededProc; index++) {
		int indexServer = ES.getComputeNodeList().get(indexInComputeList).getServerID();
		int indexChassis = ES.getComputeNodeList().get(indexInComputeList++).getChassisID();
		final BladeServer server = dataCenter.getServer(indexChassis,
			findServerInChasis(indexChassis, indexServer));
		ES.applicationList.get(i).addCompNodetoBundle(server);
		// ES.applicationList.get(i).ComputeNodeIndex.add(indexChassis);
		// //need to think about that!
		// now the node is assinged to a application and is ready!
		server.setReady(1);
		server.setSLAPercentage(ES.applicationList.get(i).getSLAPercentage());
		server.setTimeTreshold(ES.applicationList.get(i).getTimeTreshold());
		// System.out.println("Allocating compute node to the Enterprise
		// BoN : Chassis#\t"+ indexChassis );
	    }
	}
	ES.setNumberofIdleNode(ES.getComputeNodeList().size() - indexInComputeList);
	System.out.println("Number of remained IdleNode in sys\t" + ES.getNumberofIdleNode());
	if (ES.getNumberofIdleNode() < 0) {
	    System.out.println("numberofIdleNode is negative!!!");
	}
    }
    // Tries to allocate number of requested compute node to the whole WebBased
    // system
    // searching from cool affect place for the number of requested srever

    int findServerInChasis(int chassis, int servID) {
	for (int i = 0; i < dataCenter.chassisSet.get(chassis).servers.size(); i++) {
	    if (dataCenter.chassisSet.get(chassis).servers.get(i).getServerID() == servID) {
		return i;
	    }
	}
	return -2;
    }

    public void initialResourceAlocator(InteractiveSystem WS) {
	/// Initial alocation of compute node
	int[] serverIndex = new int[2];
	List<Integer> myChassisList = createChassisArray(WS.getRackId());// creats
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
	for (int i = 0; i < WS.getNumberofNode(); i++) {
	    serverIndex = nextServerSys(myChassisList);
	    if (serverIndex[0] == -2) {
		System.out.println("-2 index in which server in initialResourceAloc_sys(WebBasedSystem");
		return;
	    }
	    System.out.println("Interactive system: ChassisID= " + serverIndex[0] + " & Server= " + serverIndex[1]);
	    int indexChassis = serverIndex[0];
	    int indexServer = serverIndex[1];
	    final BladeServer server = dataCenter.getServer(indexChassis, indexServer);
	    WS.addComputeNodeToSys(server);
	    // this node is in this WS nodelist but it is not assigned to any
	    // workload yet!
	    // in Allocation module ready flag will be changed to 1
	    server.setReady(-2);
	    WS.getComputeNodeIndex().add(serverIndex[1]);
	}
    }
    //////////////////////

    public int initialResourceAloc(InteractiveSystem WS) {
	int i = 0, j;
	InteractiveUser test = new InteractiveUser(WS, environment);
	test = WS.getWaitingQueueWL().get(0);
	if (test.getMinProc() > WS.getNumberofIdleNode()) {
	    System.out.println("initialResource ALoc: not enough resource for WLBundle");
	    return -1;
	}
	j = test.getMinProc();
	while (j-- > 0) {
	    for (i = 0; i < WS.getComputeNodeList().size(); i++) {
		if (WS.getComputeNodeList().get(i).getReady() == -2) // this
								     // node
								     // is
								     // not
								     // assigned
								     // yet!
		{
		    break;
		}
	    }
	    if (i == WS.getComputeNodeList().size()) // just in case! this
						     // condition has been
						     // checked before ,no
						     // node is ready in this
						     // system
	    {
		return -1;
	    }
	    int serverId = WS.getComputeNodeList().get(i).getServerID();
	    int indexChassis = WS.getComputeNodeList().get(i).getChassisID();
	    serverId = findServerInChasis(indexChassis, serverId);
	    final BladeServer server = dataCenter.getServer(indexChassis, serverId);
	    test.addCompNodetoBundle(server);
	    test.getComputeNodeIndex().add(serverId);
	    server.setReady(1);
	    server.configSLAparameter(test.getMaxExpectedResTime());
	    WS.setNumberofIdleNode(WS.getNumberofIdleNode() - 1);
	    System.out.println("Allocating compute node to Inter. User: Chassis#" + indexChassis
		    + "\tNumber of remained IdleNode in sys\t" + WS.getNumberofIdleNode() + "@ time: "
		    + environment.getCurrentLocalTime());
	}
	WS.getUserList().add(test);
	WS.getWaitingQueueWL().remove(test);
	return 0;
    }

    List<Integer> createServerArray(int[] myRackID) {
	List<Integer> myServerId = new ArrayList<Integer>();
	for (int i = 0; i < myRackID.length; i++) {
	    for (int j = 0; j < dataCenter.chassisSet.size(); j++) {
		if (dataCenter.chassisSet.get(j).rackId == myRackID[i]) {
		    for (int k = 0; k < dataCenter.chassisSet.get(j).servers.size(); k++) {
			myServerId.add(dataCenter.chassisSet.get(j).servers.get(k).getServerID());
		    }
		}
	    }

	}

	return myServerId;
    }

    List<Integer> createChassisArray(List<Integer> myRackID) {
	List<Integer> myChassisId = new ArrayList<Integer>();
	for (int i = 0; i < myRackID.size(); i++) {
	    int j = 0;
	    for (; j < dataCenter.chassisSet.size(); j++) {
		if (dataCenter.chassisSet.get(j).rackId == myRackID.get(i)) {
		    myChassisId.add(dataCenter.chassisSet.get(j).chassisID);
		}
	    }
	}
	return myChassisId;
    }
    //////////////////////////////// INTERACTIVE//////////////////////////////

    public void resourceProvision(InteractiveSystem IS, int[] alocVectr) {
	// release first
	for (int i = 0; i < alocVectr.length; i++) {
	    if (alocVectr[i] < 0) {
		for (int ii = 0; ii < (-1 * alocVectr[i]); ii++) {
		    int indexi = IS.getUserList().get(i).myFirstIdleNode();
		    if (indexi == -2) {
			System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
			return;
		    }
		    int indexServer = IS.getUserList().get(i).getComputeNodeList().get(indexi).getServerID();
		    int indexChassis = IS.getUserList().get(i).getComputeNodeList().get(indexi).getChassisID();
		    IS.getUserList().get(i).getComputeNodeList().remove(indexi);
		    final BladeServer server = dataCenter.getServer(indexChassis,
			    findServerInChasis(indexChassis, indexServer));
		    server.setReady(-2);
		    server.setSLAPercentage(0);
		    server.setTimeTreshold(0);
		    /////
		    if (IS.getUserList().get(i).numberofRunningNode() == 0) {
			IS.getUserList().get(i).activeOneNode();
		    }
		    System.out.println("Release:User: " + i + "\t#of comp Node="
			    + IS.getUserList().get(i).getComputeNodeList().size() + "\t system Rdy to aloc="
			    + IS.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
			    + "\tNumber of running = " + IS.getUserList().get(i).numberofRunningNode());
		}
	    }
	}
	// Allocation part come in second
	for (int i = 0; i < alocVectr.length; i++) {
	    if (alocVectr[i] >= 1) {
		for (int ii = 0; ii < alocVectr[i]; ii++) {
		    int indexInComputeList = nextServerInSys(IS.getComputeNodeList());
		    if (indexInComputeList == -2) {
			System.out.println("nashod alocate konim! for  this User ->" + i + "\tsize quueue 0->"
				+ IS.getUserList().get(0).getQueueWL().size() + "\t1->"
				+ IS.getUserList().get(1).getQueueWL().size() + "\t2->"
				+ IS.getUserList().get(2).getQueueWL().size());
			IS.getAM().getRecForCoop()[i] = 1;
		    } else {
			int indexServer = IS.getComputeNodeList().get(indexInComputeList).getServerID();
			int indexChassis = IS.getComputeNodeList().get(indexInComputeList).getChassisID();
			final BladeServer server = dataCenter.getServer(indexChassis,
				findServerInChasis(indexChassis, indexServer));
			IS.getUserList().get(i).addCompNodetoBundle(server);
			// ES.applicationList.get(i).ComputeNodeIndex.add(indexChassis);
			// //need to think about that!
			// now the node is assinged to a application and is
			// ready!
			server.setReady(1);
			server.setTimeTreshold(IS.getUserList().get(i).getMaxExpectedResTime());
			System.out.println("Alloc: to User:" + i + "\t#of comp Node="
				+ IS.getUserList().get(i).getComputeNodeList().size() + "\tsys Rdy to aloc="
				+ IS.numberofAvailableNodetoAlocate() + "\t@:" + environment.getCurrentLocalTime()
				+ "\tsys Number of running = " + IS.getUserList().get(i).numberofRunningNode());
		    }
		}
	    }
	}
    }
}
