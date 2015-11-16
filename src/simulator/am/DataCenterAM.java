package simulator.am;

import java.util.List;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.Simulator;
import simulator.system.ComputeSystem;
import simulator.system.Systems;

public class DataCenterAM extends GeneralAM {

    private static final Logger LOGGER = Logger.getLogger(DataCenterAM.class.getName());

    private int[] computeSystemsSLAViolations;
    private int blockTimer = 0;
    private boolean slowDownFromCooler = false;
    private List<ComputeSystem> computeSystems;

    public DataCenterAM(Environment environment, Systems systems) {
        super(environment);
        this.computeSystems = systems.getComputeSystems();
        computeSystemsSLAViolations = new int[computeSystems.size()];
    }

    @Override
    public void monitor() {
        if (getBlockTimer() > 0) {
            setBlockTimer(getBlockTimer() - 1);
        }
        for (int i = 0; i < computeSystems.size(); i++) {
            computeSystemsSLAViolations[i] = computeSystems.get(i).getAM().getSLAViolationGen();
        }
    }

    @Override
    public void analysis() {
        // if(strtg==Main.strategyEnum.Green)
        analysisGreen();
        // if(strtg==Main.strategyEnum.SLA)
        // analysisSLA();
    }

    private void analysisGreen() {
        /*
         * update all HPC system heartbeat : is already done in Monitor For all
         * systems inside the DC begin If (SLA is violated) Switch strategy to
         * SLA based If (SLA is not violated) Switch to green strategy end
         */
        for (int i = 0; i < computeSystemsSLAViolations.length; i++) {
            if (computeSystemsSLAViolations[i] > 0 && computeSystems.get(i).getAM().getStrategy() == Simulator.StrategyEnum.Green) {
                computeSystems.get(i).getAM().setStrategy(Simulator.StrategyEnum.SLA);
                LOGGER.info("AM in DC Switch HPC system: " + i + " to SLA  @  " + environment().getCurrentLocalTime());
            }
            if (computeSystemsSLAViolations[i] == 0 && computeSystems.get(i).getAM().getStrategy() == Simulator.StrategyEnum.SLA) {
                LOGGER.info(
                        "AM in DC Switch HPC system: " + i + "  to Green @  " + environment().getCurrentLocalTime());
                computeSystems.get(i).getAM().setStrategy(Simulator.StrategyEnum.Green);
            }
        }
        /*
         * if Slowdown from cooler begin block workload with lowest priority
         * start a timer: “block timer” end if available nodes in system
         * allocate one node to the SOS sender
         */
        if (getBlockTimer() == 0 && computeSystems.get(0).isBlocked()) {
            // time to unblock hpc system
            computeSystems.get(0).unblock();
            computeSystems.get(0).makeSystemaUnBlocked();
            LOGGER.info("unblocked a system@ time : \t" + environment().getCurrentLocalTime());
        }
        
        if (!isSlowDownFromCooler()) {
            return;
        }

        if (!computeSystems.get(0).isBlocked()) {
            computeSystems.get(0).block();
            setBlockTimer(120);
            LOGGER.info("A system is blocked and we have this # of systems:  " + computeSystems.size() + "@ time= \t"
                    + environment().getCurrentLocalTime());
            // Every system should work in Greeeen
            computeSystems.get(1).getAM().setStrategy(Simulator.StrategyEnum.Green);
        } else {
            LOGGER.info("AM in data center level : HPC system is already blocked nothing can do here @: "
                    + environment().getCurrentLocalTime());
        }
    }

    @Override
    public void planning() {
    }

    @Override
    public void execution() {
    }

    public void resetBlockTimer() {
        setBlockTimer(0);
    }

    private int getBlockTimer() {
        return blockTimer;
    }

    private void setBlockTimer(int blockTimer) {
        this.blockTimer = blockTimer;
    }

    private boolean isSlowDownFromCooler() {
        return slowDownFromCooler;
    }

    public void setSlowDownFromCooler(boolean slowDownFromCooler) {
        this.slowDownFromCooler = slowDownFromCooler;
    }
}
