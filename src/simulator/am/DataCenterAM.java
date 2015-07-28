package simulator.am;

import simulator.Simulator.StrategyEnum;
import simulator.Simulator;

public class DataCenterAM extends GeneralAM {

    int[] SoSCS;
    int[] SoSIS;
    int[] SoSES;
    int[] SLAVioES;
    int[] SLAVioIS;
    int[] SLAVioCS;
    private int blockTimer = 0;
    private boolean SlowDownFromCooler = false;

    @Override
    public void monitor() {
        if (getBlockTimer() > 0) {
            setBlockTimer(getBlockTimer() - 1);
        }
        SoSCS = new int[Simulator.getInstance().computeSystems.size()];
        SoSES = new int[Simulator.getInstance().enterpriseSystems.size()];
        SoSIS = new int[Simulator.getInstance().interactiveSystems.size()];
        SLAVioCS = new int[Simulator.getInstance().computeSystems.size()];
        SLAVioES = new int[Simulator.getInstance().enterpriseSystems.size()];
        SLAVioIS = new int[Simulator.getInstance().interactiveSystems.size()];
        for (int i = 0; i < Simulator.getInstance().computeSystems.size(); i++) {
            SLAVioCS[i] = Simulator.getInstance().computeSystems.get(i).getAM().SLAViolationGen;
        }
    }

    @Override
    public void analysis(Object vilation) {
        //if(strtg==Main.strategyEnum.Green)
        analysisGreen();
        // if(strtg==Main.strategyEnum.SLA)
        //   analysisSLA();
    }

    public void analysisGreen() {
        /* update all HPC system heartbeat    : is already done in Monitor     
        For all systems  inside the DC
        begin
        If (SLA is violated)
        Switch strategy to SLA based
        If (SLA is not violated)
        Switch to green strategy
        end 
         */
        for (int i = 0; i < SLAVioCS.length; i++) {
            if (SLAVioCS[i] > 0 && Simulator.getInstance().computeSystems.get(i).getAM().strategy == Simulator.StrategyEnum.Green) {
				Simulator.getInstance().computeSystems.get(i).getAM().strategy = Simulator.StrategyEnum.SLA;
                System.out.println("AM in DC Switch HPC system: " + i + " to SLA  @  " + Simulator.getInstance().getEnvironment().getCurrentLocalTime());
            }
            if (SLAVioCS[i] == 0 && Simulator.getInstance().computeSystems.get(i).getAM().strategy == Simulator.StrategyEnum.SLA) {
                System.out.println("AM in DC Switch HPC system: " + i + "  to Green @  " + Simulator.getInstance().getEnvironment().getCurrentLocalTime());
                Simulator.getInstance().computeSystems.get(i).getAM().strategy = Simulator.StrategyEnum.Green;
            }
        }
        /* if Slowdown from cooler        begin 
        block workload with lowest priority
        start a timer: “block timer”
        end
        if available nodes in system allocate one node to the SOS sender
         */
        if (getBlockTimer() == 0 && Simulator.getInstance().computeSystems.get(0).blocked) //time to unblock hpc system
        {
            Simulator.getInstance().computeSystems.get(0).blocked = false;
            Simulator.getInstance().computeSystems.get(0).makeSystemaUnBlocked();
            System.out.println("unblocked a system@ time : \t" + Simulator.getInstance().getEnvironment().getCurrentLocalTime());
        }
        if (isSlowDownFromCooler()) {
            if (!Simulator.getInstance().computeSystems.get(0).blocked) {
                Simulator.getInstance().computeSystems.get(0).blocked = true;
                setBlockTimer(120);
                System.out.println("A system is blocked and we have this # of systems:  " + Simulator.getInstance().computeSystems.size() + "@ time= \t" + Simulator.getInstance().getEnvironment().getCurrentLocalTime());
                //Every system should work in Greeeen
                Simulator.getInstance().computeSystems.get(1).getAM().strategy = Simulator.StrategyEnum.Green;
            } else {
                System.out.println("AM in data center level : HPC system is already blocked nothing can do here @: " + Simulator.getInstance().getEnvironment().getCurrentLocalTime());
            }
        }
    }

    public void analysisSLA() {
    }

    @Override
    public void planning() {
    }

    @Override
    public void execution() {
    }

    public void setStrategy(StrategyEnum strategy) {
        this.strategy = strategy;
    }

    public void resetBlockTimer() {
        setBlockTimer(0);
    }

	public int getBlockTimer() {
		return blockTimer;
	}

	public void setBlockTimer(int blockTimer) {
		this.blockTimer = blockTimer;
	}

	public boolean isSlowDownFromCooler() {
		return SlowDownFromCooler;
	}

	public void setSlowDownFromCooler(boolean slowDownFromCooler) {
		SlowDownFromCooler = slowDownFromCooler;
	}
}
