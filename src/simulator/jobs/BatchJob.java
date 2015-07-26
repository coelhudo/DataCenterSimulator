package simulator.jobs;

import simulator.physical.BladeServer;
import simulator.Simulator;

public class BatchJob extends Job {

	private double startTime;
	private double exitTime;
	private double deadline;
	private int isChangedThisTime = 0;
	private double reqTime;
	private double utilization;
	private double[] remain;
	private int numOfNode;
	private int[] listOfServer;

	public void setRemainParam(double exp, double ut, int node, int deadln) {
		if (ut < 1) {
			setUtilization(1);
		} else {
			setUtilization(ut / 100);
		}
		setNumOfNode(node);
		setListOfServer(new int[getNumOfNode()]);
		setRemain(new double[getNumOfNode()]);
		for (int i = 0; i < getNumOfNode(); i++) {
			getRemain()[i] = exp;
		}
		setReqTime(exp);
		setDeadline(deadln);
	}

	public BatchJob() {
		setStartTime(0);
		setExitTime(0);
		setReqTime(0);
		setExitTime(0);
		setNumOfNode(0);
		setDeadline(0);
	}

	public boolean allDone() {
		int i;
		for (i = 0; i < getNumOfNode(); i++) {
			if (getRemain()[i] > 0) {
				return false;
			}
		}
		return true;

	}

	public void jobFinished() {
		setExitTime(Simulator.getInstance().getLocalTime());
		double waitTime = (Simulator.getInstance().getLocalTime() + 1) - getStartTime();
																					//-  (int)(reqTime);
		if (waitTime < 0) {
			System.out.println("Alert: Error in BatchJob\t" + waitTime);
		}

		BladeServer server = Simulator.getInstance().getDatacenter().getServer(getListOfServer()[0]);
		server.setRespTime(waitTime + server.getRespTime());
		for (int i = 0; i < getNumOfNode(); i++) {
			server = Simulator.getInstance().getDatacenter().getServer(getListOfServer()[i]);
			server.getBlockedBatchList().remove(this);
		}

		return;
	}

	public int getThisNodeIndex(int serverIndex) {
		int ki;
		for (ki = 0; ki < getRemain().length; ki++) {
			if (getListOfServer()[ki] == serverIndex) {
				return ki;
			}
		}
		return -1;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getExitTime() {
		return exitTime;
	}

	public void setExitTime(double exitTime) {
		this.exitTime = exitTime;
	}

	public double getDeadline() {
		return deadline;
	}

	public void setDeadline(double deadline) {
		this.deadline = deadline;
	}

	public int getIsChangedThisTime() {
		return isChangedThisTime;
	}

	public void setIsChangedThisTime(int isChangedThisTime) {
		this.isChangedThisTime = isChangedThisTime;
	}

	public double getReqTime() {
		return reqTime;
	}

	public void setReqTime(double reqTime) {
		this.reqTime = reqTime;
	}

	public double getUtilization() {
		return utilization;
	}

	public void setUtilization(double utilization) {
		this.utilization = utilization;
	}

	public double[] getRemain() {
		return remain;
	}

	public void setRemain(double[] remain) {
		this.remain = remain;
	}

	public int getNumOfNode() {
		return numOfNode;
	}

	public void setNumOfNode(int numOfNode) {
		this.numOfNode = numOfNode;
	}

	public int[] getListOfServer() {
		return listOfServer;
	}

	public void setListOfServer(int[] listOfServer) {
		this.listOfServer = listOfServer;
	}
}
