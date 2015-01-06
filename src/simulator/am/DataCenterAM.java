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
    public int blockTimer = 0;
    public boolean SlowDownFromCooler = false;

    @Override
    public void monitor() {
        if (blockTimer > 0) {
            blockTimer--;
        }
        SoSCS = new int[Simulator.getInstance().CS.size()];
        SoSES = new int[Simulator.getInstance().ES.size()];
        SoSIS = new int[Simulator.getInstance().IS.size()];
        SLAVioCS = new int[Simulator.getInstance().CS.size()];
        SLAVioES = new int[Simulator.getInstance().ES.size()];
        SLAVioIS = new int[Simulator.getInstance().IS.size()];
        for (int i = 0; i < Simulator.getInstance().CS.size(); i++) {
            SLAVioCS[i] = Simulator.getInstance().CS.get(i).am.SLAViolationGen;
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
            if (SLAVioCS[i] > 0 && Simulator.getInstance().CS.get(i).am.strategy == Simulator.StrategyEnum.Green) {
                Simulator.getInstance().CS.get(i).am.strategy = Simulator.StrategyEnum.SLA;
                System.out.println("AM in DC Switch HPC system: " + i + " to SLA  @  " + Simulator.getInstance().localTime);
            }
            if (SLAVioCS[i] == 0 && Simulator.getInstance().CS.get(i).am.strategy == Simulator.StrategyEnum.SLA) {
                System.out.println("AM in DC Switch HPC system: " + i + "  to Green @  " + Simulator.getInstance().localTime);
                Simulator.getInstance().CS.get(i).am.strategy = Simulator.StrategyEnum.Green;
            }
        }
        /* if Slowdown from cooler        begin 
        block workload with lowest priority
        start a timer: “block timer”
        end
        if available nodes in system allocate one node to the SOS sender
         */
        if (blockTimer == 0 && Simulator.getInstance().CS.get(0).blocked) //time to unblock hpc system
        {
            Simulator.getInstance().CS.get(0).blocked = false;
            Simulator.getInstance().CS.get(0).makeSystemaUnBlocked();
            System.out.println("unblocked a system@ time : \t" + Simulator.getInstance().localTime);
        }
        if (SlowDownFromCooler) {
            if (!Simulator.getInstance().CS.get(0).blocked) {
                Simulator.getInstance().CS.get(0).blocked = true;
                blockTimer = 120;
                System.out.println("A system is blocked and we have this # of systems:  " + Simulator.getInstance().CS.size() + "@ time= \t" + Simulator.getInstance().localTime);
                //Every system should work in Greeeen
                Simulator.getInstance().CS.get(1).am.strategy = Simulator.StrategyEnum.Green;
            } else {
                System.out.println("AM in data center level : HPC system is already blocked nothing can do here @: " + Simulator.getInstance().localTime);
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
        blockTimer = 0;
    }
}
