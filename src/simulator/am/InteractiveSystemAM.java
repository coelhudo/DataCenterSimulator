package simulator.am;

import simulator.InteractiveSystem;
import simulator.Simulator;

public class InteractiveSystemAM extends GeneralAM {

	InteractiveSystem IS;
	double[] percentCompPwr;
	private int[] allocationVector;
	double wlkIntens = 0;
	int[] accuSLA;
	double[] queueLengthUsr;
	int lastTime = 0;
	private Simulator.LocalTime localTime;

	public InteractiveSystemAM(InteractiveSystem is, Simulator.LocalTime localTime) {
		this.IS = is;
		this.localTime = localTime;
		setRecForCoop(new int[IS.getUserList().size()]);
	}

	@Override
	public void analysis(Object violation) {
		// averageWeight();
		iterativeAlg();

	}

	@Override
	public void planning() {
		///// Server Provisioning for each application Bundle///////////
		if (localTime.getCurrentLocalTime() % 1200 == 0) {
			// numberOfActiveServ=0;
			// kalmanIndex=Main.localTime/1200;
			// serverProvisioning();
			// kalmanIndex++;
			// int i=ES.applicationList.get(0).occupiedPercentage();
			// System.out.println("occupied\t"+i);
			// if(i>50)
			// ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()+1;
			// else
			// ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()-1;
		}

	}

	@Override
	public void execution() {
		IS.getResourceAllocation().resourceProvision(IS, getAllocationVector());
	}

	void workloadIntensity() {
		double avg = 0.0;
		for (int i = 0; i < IS.getUserList().size(); i++) {
			avg = avg + (double) IS.getUserList().get(i).getNumberofBasicNode()
					/ IS.getUserList().get(i).getMaxNumberOfRequest();
		}
		wlkIntens = (double) avg / IS.getUserList().size();
	}

	@Override
	public void monitor() {
		percentCompPwr = new double[IS.getUserList().size()];
		setAllocationVector(new int[IS.getUserList().size()]);
		accuSLA = new int[IS.getUserList().size()];
		queueLengthUsr = new double[IS.getUserList().size()];
		workloadIntensity();
		for (int i = 0; i < IS.getUserList().size(); i++) {
			// assume epoch system 2 time epoch application
			percentCompPwr[i] = IS.getUserList().get(i).getAM().percnt / ((localTime.getCurrentLocalTime() - lastTime)
					* 3 * IS.getUserList().get(i).getComputeNodeList().size());// (Main.epochSys*/*3*ES.applicationList.get(i).ComputeNodeList.size());
			IS.getUserList().get(i).getAM().percnt = 0;
			accuSLA[i] = IS.getUserList().get(i).getAM().accumulativeSLA / (localTime.getCurrentLocalTime() - lastTime);// Main.epochSys;
			IS.getUserList().get(i).getAM().accumulativeSLA = 0;
			// for fair allocate/release node needs to know how many jobs are
			// already in each application queue
			queueLengthUsr[i] = IS.getUserList().get(i).numberOfWaitingJobs();
		}
		calcSysUtility();
		lastTime = localTime.getCurrentLocalTime();
		SLAViolationGen = IS.getSLAviolation();
	}

	public void calcSysUtility() {
		int localUtil = 0;
		//int globalUtil;
		for (int i = 0; i < IS.getUserList().size(); i++) {
			localUtil += IS.getUserList().get(i).getAM().util;
		}
		localUtil = localUtil / IS.getUserList().size();

		// if(ES.applicationList.isEmpty())
		// { super.utility=-1;
		// return;
		// }
		// localUtil=localUtil/ES.applicationList.size();
		// int idlePercent=100*ES.numberofIdleNode/ES.numberofNode;
		// int qos=ES.SLAviolation;
		// globalUtil=idlePercent+localUtil;
		// super.utility=sigmoid(globalUtil-100);
	}

	void iterativeAlg() {
		for (int i = 0; i < IS.getUserList().size(); i++) {
			IS.getUserList().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
																							// Strategy
			double wkIntensApp;
			wkIntensApp = (double) IS.getUserList().get(i).getNumberofBasicNode()
					/ IS.getUserList().get(i).getMaxNumberOfRequest();
			// if cpmPwr > 50% & violation then allocate a server
			getAllocationVector()[i] = 0;
			if (percentCompPwr[i] > 0.5 && accuSLA[i] > 0) {

				// considering wl intensity of apps for node allocation
				// if app has more than average give it more node
				int bishtar = 0;
				if (wkIntensApp > wlkIntens) {
					bishtar = (int) Math.ceil(Math.abs((wkIntensApp - wlkIntens) / wlkIntens));
				} else {
					bishtar = 0;
				}
				getAllocationVector()[i] = 1 + bishtar;// +(int)Math.abs((Math.floor((wlkIntens-wkIntensApp)/wlkIntens)));
				// System.out.println("Switching Strategy in Application =" +i
				// +" to SLA ");
				IS.getUserList().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
																							// strategy
			}
			// if cpmPwr < 50% & violation is less then release a server
			if (percentCompPwr[i] <= 0.5 && accuSLA[i] == 0) {
				getAllocationVector()[i] = -1;
				// System.out.println("Releasing a Server");
			}
			// if cpmPwr < 50% & violation is ziyad then nothing no server
			// exchange
			if (percentCompPwr[i] < 0.5 && accuSLA[i] > 0) {
				getAllocationVector()[i] = 1;
				// System.out.println("Switching Strategy in Application =" +i
				// +" to SLA ");
				IS.getUserList().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA; // SLA
																								// strategy
			}
		}
		int requestedNd = 0;
		for (int i = 0; i < getAllocationVector().length; i++) {
			int valNode = IS.getUserList().get(i).getComputeNodeList().size() + getAllocationVector()[i];
			if (IS.getUserList().get(i).getMinProc() > valNode || IS.getUserList().get(i).getMaxProc() < valNode) {
				// if(ES.applicationList.get(i).minProc>
				// ES.applicationList.get(i).ComputeNodeList.size()+allocationVector[i])
				// System.out.println("error requested less than min in AM
				// system ");
				// if(ES.applicationList.get(i).maxProc<
				// ES.applicationList.get(i).ComputeNodeList.size()+allocationVector[i])
				// System.out.println("error requested more than maxxxx in AM
				// system ");
				getAllocationVector()[i] = 0;
			}
			requestedNd = requestedNd + getAllocationVector()[i];
		}
		// if(requestedNd>ES.numberofIdleNode)
		// System.out.println("IN AM system can not provide server reqested=
		// "+requestedNd);
	}
	// determining aloc/release vector and active strategy

	void averageWeight() {
		double[] cofficient = new double[IS.getUserList().size()];
		int[] sugestForAlo = new int[IS.getUserList().size()];
		double sumCoff = 0;
		// in each app calculate the expected Coefficient which is
		// multiplication SLA violation and queue Length
		for (int i = 0; i < IS.getUserList().size(); i++) {
			cofficient[i] = queueLengthUsr[i] * accuSLA[i] + accuSLA[i] + queueLengthUsr[i];
			sumCoff = sumCoff + cofficient[i];
		}
		int totalNode = IS.getComputeNodeList().size();
		for (int i = 0; i < IS.getUserList().size(); i++) {
			sugestForAlo[i] = (int) (cofficient[i] * totalNode / sumCoff);
			if (sugestForAlo[i] < IS.getUserList().get(i).getMinProc()) {
				sugestForAlo[i] = IS.getUserList().get(i).getMinProc();
			}
			if (sugestForAlo[i] > IS.getUserList().get(i).getMaxProc()) {
				sugestForAlo[i] = IS.getUserList().get(i).getMaxProc();
			}
			getAllocationVector()[i] = sugestForAlo[i] - IS.getUserList().get(i).getComputeNodeList().size();
		}
		for (int i = 0; i < IS.getUserList().size(); i++) {
			IS.getUserList().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
																							// Strategy
			if (accuSLA[i] > 0) {
				// System.out.println("Switching Strategy in Application =" +i
				// +" to SLA ");
				IS.getUserList().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
																							// strategy
			}
		}
	}
	// void serverProvisioning() {
	// int[] numberOfPredictedReq = {251, 246, 229, 229, 223, 225, 231, 241,
	// 265, 265, 271, 276, 273, 273, 268, 258, 255, 257, 242, 241, 233, 228,
	// 231, 261, 274, 302, 343, 375, 404, 405, 469, 562, 1188, 1806, 2150, 2499,
	// 2624, 2793, 2236, 1905, 1706, 1558, 1495, 1448, 1414, 1391, 1430, 1731,
	// 2027, 2170, 2187, 2224, 2363, 1317};
	// if (kalmanIndex >= numberOfPredictedReq.length) {
	// return;
	// }
	// ES.numberOfActiveServ = (int)
	// Math.floor(numberOfPredictedReq[kalmanIndex]*5*ES.applicationList.get(0).NumberofBasicNode/
	// ES.applicationList.get(0).MaxNumberOfRequest);
	// if (ES.numberOfActiveServ > ES.numberofNode) {
	// System.out.println("In ES : is gonna alocate this number of servers:
	// "+(ES.numberOfActiveServ-ES.numberofNode));
	// }
	// }

	public int[] getAllocationVector() {
		return allocationVector;
	}

	public void setAllocationVector(int[] allocationVector) {
		this.allocationVector = allocationVector;
	}
}
